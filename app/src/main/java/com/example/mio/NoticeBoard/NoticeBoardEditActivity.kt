package com.example.mio.NoticeBoard

import com.example.mio.Adapter.PlaceAdapter
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Model.PlaceData
import com.example.mio.Model.PostData
import com.example.mio.Model.ResultSearchKeyword
import com.example.mio.Model.SharedViewModel
import com.example.mio.Model.*
import com.example.mio.TabCategory.TaxiTabFragment
import com.example.mio.databinding.ActivityNoticeBoardEditBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap


class NoticeBoardEditActivity : AppCompatActivity(), MapView.MapViewEventListener {
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        private const val API_KEY = BuildConfig.map_api_key
    }

    private lateinit var mBinding : ActivityNoticeBoardEditBinding
    //클릭한 포스트(게시글)의 데이터 임시저장
    private var temp : AddPostData? = null
    //edit용 임시저장 데이터
    private var eTemp : PostData? = null

    private var pos = 0
    //받은 계정 정보
    private var userEmail = ""
    private var isCheckData = false
    private var categorySelect = ""

    private val listItems = arrayListOf<PlaceData>()
    private val placeAdapter = PlaceAdapter(listItems)
    private val recentSearchItems = arrayListOf<PlaceData>()
    private val recentSearchAdapter = PlaceAdapter(recentSearchItems)
    private var keyword = ""
    private lateinit var geocoder: Geocoder
    private lateinit var mapView: MapView
    private var marker: MapPOIItem? = null
    private val PREFS_NAME = "recent_search"
    private val KEY_RECENT_SEARCH = "search_items"

    private var sharedViewModel: SharedViewModel? = null

    private var selectCalendarDataNoticeBoard : HashMap<String, ArrayList<PostData>> = HashMap()

    //콜백 리스너
    private var variableChangeListener: VariableChangeListener? = null
    //모든 데이터 값
    private var isComplete = false
    //현재 페이지
    private var currentPage = 1
    private var countPage = 0 // 2번째 페이지 때문에 추가
    //retrofit2
    val call = RetrofitServerConnect.service

    //add type
    private var type : String? = null

    /*첫 번째 vf*/
    //선택한 날짜
    private var selectTargetDate = ""
    private var selectFormattedDate = ""
    private var isCategory = false //true : 카풀, false : 택시
    private var selectCategory = ""
    private var selectCategoryId = 0
    //설정한 제목
    private var editTitle = ""
    //선택한 시간
    private var selectTime = ""
    private var selectFormattedTime = ""
    private var hour1 = 0
    private var minute1 = 0

    //선택한 탑승인원
    private var participateNumberOfPeople = 1
    /*두 번째 vf*/
    private var latitude = 0.0
    private var longitude = 0.0
    private var location = ""
    /*세 번째 vf*/
    //선택한 가격
    private var selectCost = ""
    /*네 번째 vf*/
    private var detailContent = ""

    private var isFirst = false
    private var isSecond = false
    private var isThird = false


    //모든 값 체크
    private var isAllCheck : RequirementData = RequirementData(
        FirstVF(
            isTitle = false,
            isCalendar = false,
            isTime = false,
            isParticipants = false,
            isFirst = false
        ), SecondVF(
            isPlaceName = false,
            isPlaceRode = false,
            isSecond = false
        ), ThirdVF(
            isAmount = false,
            isGSchool = false,
            isASchool = false,
            isMGender = false,
            isWGender = false,
            isSmoke = false,
            isNSmoke = false,
            isThird = false
        ),
        FourthVF(
            isContent = false,
            isFourth = false
        )

    )
    private lateinit var myViewModel : SharedViewModel
    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityNoticeBoardEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        //뷰의 이벤트 리스너
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        mapView = MapView(this)
        mapView.setMapViewEventListener(this)
        mBinding.mapView.addView(mapView)
        geocoder = Geocoder(this)

        type = intent.getStringExtra("type")

        if (type.equals("ADD")) { //add
            bottomBtnEvent()
            //vf 생성
            firstVF()
            secondVF()
            thirdVF()
            fourthVF()
            fifthVF()
        } else if (type.equals("EDIT")){ //edit
            eTemp = intent.getSerializableExtra("editPostData") as PostData
            bottomBtnEvent()
            //vf 생성
            firstVF()
            secondVF()
            thirdVF()
            fourthVF()
            fifthVF()
        }



        //여기가 사용할것들
        ////////////////////////////
        /* mBinding.datePickerBtn.setOnClickListener {
             val cal = Calendar.getInstance()
             val data = DatePickerDialog.OnDateSetListener { view, year, month, day ->
                selectTargetDate = "${year}년/${month+1}월/${day}일"
             }
             DatePickerDialog(this, data, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
         }

         mBinding.categorySelectBtn.setOnClickListener {
             categorySelect = "taxi"
         }*/
        //여기가 사용할것들
        ///////////////////////////////////
        //카테고리 생각하여 데이터 변경하기
        /*mBinding.editAdd.setOnClickListener {
           val contentPost = mBinding.editPostContent.text.toString()
           val contentTitle = mBinding.editPostTitle.text.toString()

           if (type.equals("ADD")) {
               if (contentPost.isNotEmpty() && contentTitle.isNotEmpty() && selectTargetDate.isNotEmpty()) {
                   val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                   //현재 로그인된 유저 email 가져오기
                   userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                   //데이터 세팅 후 임시저장
                   temp = PostData(userEmail, pos, contentTitle, contentPost, selectTargetDate, categorySelect, "location", "targetTime" ,1, 4)
                   selectCalendarDataNoticeBoard[selectTargetDate] = arrayListOf()
                   selectCalendarDataNoticeBoard[selectTargetDate]!!.add(temp!!)
                   //pos는 현재 저장되지 않지만 나중에 짜피 백엔드에 데이터 넣을 거니 괜찮을듯
                   //나중에 api연결할때 여기 바꾸기
                   sharedViewModel!!.setCalendarLiveData("add", selectCalendarDataNoticeBoard)

                   val intent = Intent().apply {
                       putExtra("postData", temp)
                       putExtra("flag", 0)
                   }
                   setResult(RESULT_OK, intent)
                   finish()
                  *//*val intent = Intent(this, TaxiTabFragment::class.java).apply {
                        putExtra("postData", temp)
                        putExtra("flag", 0)
                    }*//*
                    pos += 1

                } else {
                    if (contentTitle.isEmpty()) {
                        Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT) .show()
                    } else if (contentPost.isEmpty()) {
                        Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT) .show()
                    } else if (selectTargetDate.isEmpty()) {
                        Toast.makeText(this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT) .show()
                    }
                }
            } else { //edit
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty()) {
                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    //현재 로그인된 유저 email 가져오기
                    userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                }
            }
        }*/

        mBinding.rvList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvList.adapter = placeAdapter

        mBinding.rvRecentSearchList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mBinding.rvRecentSearchList.adapter = recentSearchAdapter

        //뒤로가기
        mBinding.backArrow.setOnClickListener {
            val intent = Intent(this@NoticeBoardEditActivity, MainActivity::class.java).apply {
                putExtra("flag", 9)
            }
            setResult(RESULT_OK, intent)
            finish()
        }


        myViewModel.allCheck.observe(this) {
            if (it.isFirstVF.isTitle && it.isFirstVF.isParticipants && it.isFirstVF.isTime && it.isFirstVF.isCalendar) {
                it.isFirstVF.isFirst = true
                isFirst = true
                println("ff")
            }

            if (it.isSecondVF.isPlaceName && it.isSecondVF.isPlaceRode) {
                it.isSecondVF.isSecond = true
            }

            if ((it.isThirdVF.isSmoke || it.isThirdVF.isNSmoke)
                && (it.isThirdVF.isGSchool || it.isThirdVF.isASchool)
                && (it.isThirdVF.isSmoke || it.isThirdVF.isNSmoke)
                && (it.isThirdVF.isMGender || it.isThirdVF.isWGender)
                && it.isThirdVF.isAmount ) {
                it.isThirdVF.isThird = true
            }

            if (it.isFirstVF.isFirst) {
                myViewModel.postCheckComplete(complete = true)
                it.isFirstVF.isFirst = false
                println("checkbool")
            }

            if (it.isSecondVF.isSecond) {
                myViewModel.postCheckComplete(complete = true)
                it.isSecondVF.isSecond = false
            }

            if (it.isThirdVF.isThird) {
                myViewModel.postCheckComplete(complete = true)
                it.isThirdVF.isThird = false
            }
        }

        //버튼 활성화를 실시간 체크를 위함
        myViewModel.checkComplete.observe(this) {
            if (it) {
                CoroutineScope(Dispatchers.Main).launch {
                    mBinding.editNext.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_3))
                    }
                }
                mBinding.editNext.setOnClickListener {
                    if (currentPage == 2 && countPage == 1) {
                        returnStatusbar()
                    }
                    mBinding.editViewflipper.showNext()
                    isComplete = !isComplete
                    myViewModel.postCheckComplete(false)
                    currentPage += 1
                    println(currentPage)
                    myViewModel.postCheckPage(currentPage)
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    mBinding.editNext.apply {
                        setBackgroundResource(R.drawable.btn_default_background)
                        setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_8))
                    }
                }
                mBinding.editNext.setOnClickListener {

                }
            }
        }

        //현재 페이지 체크
        myViewModel.checkCurrentPage.observe(this) {

            when (it) {
                5 -> {
                    mBinding.editNext.visibility = View.GONE
                    mBinding.editPre.visibility = View.GONE
                    mBinding.editBottomSpace.visibility = View.GONE
                    mBinding.completeBtn.visibility = View.VISIBLE

                    mBinding.editViewflipper.visibility = View.GONE
                    mBinding.editFifthVf.visibility = View.VISIBLE
                    val fadeIn = ObjectAnimator.ofFloat(mBinding.editCompleteIcon, "alpha", 0f, 1f)
                    fadeIn.duration = 1500
                    fadeIn.start()
                }
                else -> {
                    mBinding.editNext.visibility = View.VISIBLE
                    mBinding.editPre.visibility = View.VISIBLE
                    mBinding.editBottomSpace.visibility = View.VISIBLE
                    mBinding.completeBtn.visibility = View.GONE
                }
            }
        }


    }

    private fun firstVF() {
        if (type == "EDIT") {
            mBinding.editTitle.setText(eTemp!!.postTitle)
        }
        mBinding.editTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                /*if (type == "EDIT") {
                    mBinding.editTitle.setText(eTemp!!.postTitle)
                    editTitle = eTemp!!.postTitle
                }*/
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                isAllCheck.isFirstVF.isTitle = true
                //myViewModel.postCheckValue(isAllCheck.isFirstVF.isTitle)
                myViewModel.postCheckValue(isAllCheck)

            }
            override fun afterTextChanged(editable: Editable) {
                editTitle = editable.toString()
                /*if (editable.isEmpty()) {
                    Toast.makeText("")
                }*/
                //깜빡임 제거
                mBinding.editTitle.clearFocus()
                mBinding.editTitle.movementMethod = null
                isAllCheck.isFirstVF.isTitle = editable.isNotEmpty()
            }
        })


        if (type == "EDIT") {
            mBinding.editSelectDateTv.text = eTemp!!.postTargetDate
            selectFormattedDate = eTemp!!.postTargetDate
        }
        mBinding.editCalendar.setOnClickListener {
            val cal = Calendar.getInstance()
            val data = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                selectTargetDate = "${year}년/${month+1}월/${day}일"
                selectFormattedDate = LocalDate.parse(selectTargetDate, DateTimeFormatter.ofPattern("yyyy년/M월/d일")).format(DateTimeFormatter.ISO_DATE)
                mBinding.editSelectDateTv.text = "${year}년/${month+1}월/${day}일"
                mBinding.editSelectDateTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_gray_11))
                //isCalendar = true
                isAllCheck.isFirstVF.isCalendar = true
                myViewModel.postCheckValue(isAllCheck)
            }
            DatePickerDialog(this, R.style.MySpinnerDatePickerStyle, data, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                Calendar.DAY_OF_MONTH)).show()
        }


        if (type == "EDIT") {
            mBinding.editSelectTime.text = eTemp!!.postTargetTime
            selectFormattedTime = eTemp!!.postTargetTime
        }
        mBinding.editTime.setOnClickListener {
            showHourPicker()
        }

        //카테고리 관리
        if (type == "EDIT") {
            selectCategory = eTemp!!.postCategory
            if (selectCategory == "carpool") {
                selectCategoryId = 1
                mBinding.editCategoryCarpoolBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_update_layout)
                    setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
                }
            } else {
                selectCategoryId = 0
                mBinding.editCategoryCarpoolBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_update_layout)
                    setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
                }
            }
        }

        mBinding.editCategoryCarpoolBtn.setOnClickListener {
            selectCategory = "carpool"
            selectCategoryId = 1
            mBinding.editCategoryCarpoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editCategoryTaxiBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
        }
        mBinding.editCategoryTaxiBtn.setOnClickListener {
            selectCategory = "taxi"
            selectCategoryId = 2
            mBinding.editCategoryTaxiBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editCategoryCarpoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
        }



        if (type == "EDIT") {
            mBinding.editParticipateTv.text = eTemp!!.postParticipationTotal.toString()
            participateNumberOfPeople = eTemp!!.postParticipation
        }
        mBinding.editMinus.setOnClickListener {
            participateNumberOfPeople -= 1
            if (participateNumberOfPeople > 0) {
                mBinding.editParticipateTv.text = participateNumberOfPeople.toString()
                isAllCheck.isFirstVF.isParticipants = true
                myViewModel.postCheckValue(isAllCheck)
            } else {
                mBinding.editParticipateTv.text = "1"
                participateNumberOfPeople = 1
                isAllCheck.isFirstVF.isParticipants = true
                myViewModel.postCheckValue(isAllCheck)
            }
        }

        mBinding.editPlus.setOnClickListener {
            participateNumberOfPeople += 1
            if (participateNumberOfPeople < 11) {
                mBinding.editParticipateTv.text = participateNumberOfPeople.toString()
                isAllCheck.isFirstVF.isParticipants = true
                myViewModel.postCheckValue(isAllCheck)
            } else {
                mBinding.editParticipateTv.text = "1"
                participateNumberOfPeople = 1
                isAllCheck.isFirstVF.isParticipants = true
                myViewModel.postCheckValue(isAllCheck)
            }

        }

        /*if (isTitle && isCalendar && isTime && isParticipants) {
            isFirst = true
            println("F"+isFirst)
        } else {
            println("?")
        }*/
    }

    private fun secondVF() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(KEY_RECENT_SEARCH, null)
        if (json != null) {
            val type = object : TypeToken<ArrayList<PlaceData>>() {}.type
            val items: ArrayList<PlaceData> = gson.fromJson(json, type)
            recentSearchItems.addAll(items)
        }
        recentSearchAdapter.notifyDataSetChanged()
        placeAdapter.setItemClickListener(object : PlaceAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                mBinding.editViewflipper.showNext()
                changeStatusbar()
                countPage += 1
                val mapPoint = MapPoint.mapPointWithGeoCoord(
                    listItems[position].y, listItems[position].x
                )
                latitude = listItems[position].y
                longitude = listItems[position].x

                mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
                mBinding.placeName.text = listItems[position].name
                mBinding.placeRoad.text = listItems[position].road
                addToRecentSearch(listItems[position])
                location = listItems[position].name

                isAllCheck.isSecondVF.isPlaceName = true
                isAllCheck.isSecondVF.isPlaceRode = true
                myViewModel.postCheckValue(isAllCheck)
            }
        })

        recentSearchAdapter.setItemClickListener(object : PlaceAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                mBinding.editViewflipper.showNext()
                changeStatusbar()
                countPage += 1
                val mapPoint = MapPoint.mapPointWithGeoCoord(
                    recentSearchItems[position].y, recentSearchItems[position].x
                )
                latitude = recentSearchItems[position].y
                longitude = recentSearchItems[position].x

                mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)

                if (marker != null) {
                    mapView.removePOIItem(marker)
                }
                marker = MapPOIItem().apply {
                    itemName = recentSearchItems[position].name
                    this.mapPoint = mapPoint
                    markerType = MapPOIItem.MarkerType.BluePin
                    selectedMarkerType = MapPOIItem.MarkerType.CustomImage
                    customSelectedImageResourceId = R.drawable.map_poi_icon
                    isCustomImageAutoscale = false
                    setCustomImageAnchor(0.5f, 1.0f)
                }
                mapView.addPOIItem(marker)

                mBinding.placeName.text = recentSearchItems[position].name
                mBinding.placeRoad.text = recentSearchItems[position].road
                location = recentSearchItems[position].name

                isAllCheck.isSecondVF.isPlaceName = true
                isAllCheck.isSecondVF.isPlaceRode = true
                myViewModel.postCheckValue(isAllCheck)
            }
        })

        mBinding.btnSearch.setOnClickListener {
            keyword = mBinding.etSearchField.text.toString()
            mBinding.recentSearch.setText("관련 검색어")
            searchKeyword(keyword)
            mBinding.rvRecentSearchList.visibility = View.INVISIBLE
        }
    }

    private fun thirdVF() {
        //가격
        /*if (mBinding.editSelectAmount.text.toString().isEmpty()) {
            isAllCheck.isThirdVF.isAmount = false
        } else {
            selectCost = mBinding.editSelectAmount.text.toString()
            isAllCheck.isThirdVF.isAmount = true
        }*/
        if (type == "EDIT") {
            mBinding.editSelectAmount.setText(eTemp!!.postCost.toString())
            selectCost = eTemp!!.postCost.toString()
        }
        mBinding.editSelectAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                /*if (type == "EDIT") {
                    mBinding.editSelectAmount.setText(eTemp!!.postCost)
                }*/
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //타이틀 체크
                isAllCheck.isThirdVF.isAmount = true
                myViewModel.postCheckValue(isAllCheck)
            }
            override fun afterTextChanged(editable: Editable) {
                //숫자만 입력가능하게
                try {
                    selectCost = editable.toString().trim()
                    val cost = selectCost.toInt()
                } catch (e : java.lang.NumberFormatException) {
                    Toast.makeText(this@NoticeBoardEditActivity, "숫자로만 입력해 주세요", Toast.LENGTH_SHORT).show()
                }

                isAllCheck.isThirdVF.isAmount = editable.isNotEmpty()
                mBinding.editDetailContent.clearFocus()
                // mBinding.editDetailContent.movementMethod = null
                myViewModel.postCheckValue(isAllCheck)
            }
        })

        //등/하교
        mBinding.editGtschoolBtn.setOnClickListener {
            mBinding.editGtschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editAschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
            isAllCheck.isThirdVF.isGSchool = true
            isAllCheck.isThirdVF.isASchool = false
            myViewModel.postCheckValue(isAllCheck)
        }
        mBinding.editAschoolBtn.setOnClickListener {
            mBinding.editAschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editGtschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
            isAllCheck.isThirdVF.isGSchool = false
            isAllCheck.isThirdVF.isASchool = true
            myViewModel.postCheckValue(isAllCheck)
        }

        //흡연
        mBinding.editSmokerBtn.setOnClickListener {
            mBinding.editSmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editNsmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
            isAllCheck.isThirdVF.isSmoke = true
            isAllCheck.isThirdVF.isNSmoke = false
            myViewModel.postCheckValue(isAllCheck)
        }
        mBinding.editNsmokerBtn.setOnClickListener {
            mBinding.editNsmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editSmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
            isAllCheck.isThirdVF.isSmoke = false
            isAllCheck.isThirdVF.isNSmoke = true
            myViewModel.postCheckValue(isAllCheck)
        }
        mBinding.editManBtn.setOnClickListener {
            mBinding.editManBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editWomanBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
            isAllCheck.isThirdVF.isMGender = true
            isAllCheck.isThirdVF.isWGender = false
            myViewModel.postCheckValue(isAllCheck)
        }
        mBinding.editWomanBtn.setOnClickListener {
            mBinding.editWomanBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editManBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
            }
            isAllCheck.isThirdVF.isMGender = false
            isAllCheck.isThirdVF.isWGender = true
            myViewModel.postCheckValue(isAllCheck)
        }
    }

    private fun fourthVF() {
        if (type == "EDIT") {
            mBinding.editDetailContent.setText(eTemp!!.postContent)
            detailContent = eTemp!!.postContent
        }
        mBinding.editDetailContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                /* if (type == "EDIT") {
                     mBinding.editDetailContent.setText(eTemp!!.postContent)
                 }*/
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                /*if (editable.isNotEmpty()) {
                    mBinding.editPre.visibility = View.GONE
                    mBinding.editNext.visibility = View.GONE
                    mBinding.editBottomSpace.visibility = View.GONE
                    mBinding.completeBtn.visibility = View.VISIBLE
                } else {
                    mBinding.editPre.visibility = View.VISIBLE
                    mBinding.editNext.visibility = View.VISIBLE
                    mBinding.editBottomSpace.visibility = View.VISIBLE
                    mBinding.completeBtn.visibility = View.GONE
                }*/
                /*mBinding.editSmokerBtn.apply {
                    setBackgroundResource(R.drawable.round_btn_update_layout)
                    setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
                }*/

                detailContent = editable.toString()
                isAllCheck.isFourthVF.isContent = editable.isNotEmpty()
                mBinding.editDetailContent.clearFocus()
                mBinding.editDetailContent.movementMethod = null
                myViewModel.postCheckValue(isAllCheck)
            }
        })
    }

    private fun fifthVF() {
        mBinding.completeBtn.setOnClickListener {
            //저장된 값
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
            val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
            /*var interceptor = HttpLoggingInterceptor()
        interceptor = interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()*/

            /*val retrofit = Retrofit.Builder().baseUrl("url 주소")
                .addConverterFactory(GsonConverterFactory.create())
                //.client(client) 이걸 통해 통신 오류 log찍기 가능
                .build()
            val service = retrofit.create(MioInterface::class.java)*/
            //통신로그

            /*val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val clientBuilder = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()*/
            //통신
            val SERVER_URL = BuildConfig.server_URL
            val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
            //.client(clientBuilder)

            //Authorization jwt토큰 로그인
            val interceptor = Interceptor { chain ->

                var newRequest: Request
                if (token != null && token != "") { // 토큰이 없는 경우
                    // Authorization 헤더에 토큰 추가
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                    val expireDate: Long = getExpireDate.toLong()
                    if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                        //refresh 들어갈 곳
                        newRequest =
                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                        return@Interceptor chain.proceed(newRequest)
                    }
                } else newRequest = chain.request()
                chain.proceed(newRequest)
            }
            val builder = OkHttpClient.Builder()
            builder.interceptors().add(interceptor)
            val client: OkHttpClient = builder.build()
            retrofit.client(client)
            val retrofit2: Retrofit = retrofit.build()
            val api = retrofit2.create(MioInterface::class.java)

            if (type!! == "ADD") {
                if (isFirst) {
                    var school = false
                    var smoke = false
                    var gender = false
                    if (isAllCheck.isThirdVF.isGSchool) {
                        school = true
                    }
                    if (isAllCheck.isThirdVF.isSmoke) {
                        smoke = true
                    }
                    if (isAllCheck.isThirdVF.isMGender) {
                        gender = true
                    }
                    /*val t : RequestBody = editTitle.toRequestBody("text/plain".toMediaTypeOrNull())
                    val c : RequestBody = detailContent.toRequestBody("text/plain".toMediaTypeOrNull())
                    val std : RequestBody = selectTargetDate.toRequestBody("text/plain".toMediaTypeOrNull())
                    val stt : RequestBody = selectTime.toRequestBody("text/plain".toMediaTypeOrNull())
                    val s : RequestBody = school.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val pnp : RequestBody = participateNumberOfPeople.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val lt : RequestBody = "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                    val lot : RequestBody = "0.0".toRequestBody("text/plain".toMediaTypeOrNull())
                    val location : RequestBody = "수락산역 3번 출구".toRequestBody("text/plain".toMediaTypeOrNull())
                    val vr : RequestBody = "false".toRequestBody("text/plain".toMediaTypeOrNull())
                    val cos : RequestBody = selectCost.toRequestBody("text/plain".toMediaTypeOrNull())
                    val vc : RequestBody = "0".toRequestBody("text/plain".toMediaTypeOrNull())

                    val requestMap: HashMap<String, RequestBody> = HashMap()
                    requestMap.put("title", t)
                    requestMap.put("content", c)
                    requestMap.put("targetDate", std)
                    requestMap.put("targetTime", stt)
                    requestMap.put("verifyGoReturn", s)
                    requestMap.put("numberOfPassengers", pnp)
                    requestMap.put("viewCount", vc)
                    requestMap.put("verifyFinish", vr)
                    requestMap.put("latitude", lt)
                    requestMap.put("longitude", lot)
                    requestMap.put("location", location)
                    requestMap.put("cost", cos)

                    val obj = JsonObject()
                    obj.addProperty("title", editTitle)
                    obj.addProperty("content", detailContent)
                    obj.addProperty("targetDate", selectTargetDate)
                    obj.addProperty("verifyGoReturn", school)
                    obj.addProperty("numberOfPassengers", participateNumberOfPeople)
                    obj.addProperty("viewCount", 0)
                    obj.addProperty("verifyFinish", false)
                    obj.addProperty("latitude", 0.0)
                    obj.addProperty("longitude", 0.0)
                    obj.addProperty("location", "수락산역 3번 출구")
                    obj.addProperty("cost", selectCost)*/


                    temp = AddPostData(editTitle, detailContent, selectFormattedDate, selectFormattedTime, school, participateNumberOfPeople, 0, false, latitude, longitude, location, selectCost.toInt())
                    println(temp)
                    CoroutineScope(Dispatchers.IO).launch {
                        /*"application/json; charset=UTF-8",*/
                        api.addPostData(temp!!, selectCategoryId).enqueue(object : Callback<AddPostResponse> {
                            override fun onResponse(
                                call: Call<AddPostResponse>,
                                response: Response<AddPostResponse>
                            ) {
                                if (response.isSuccessful) {
                                    println("succcc")
                                } else {
                                    println("faafa")
                                    Log.d("add", response.errorBody()?.string()!!)
                                    Log.d("message", call.request().toString())
                                    println(response.code())
                                }
                            }

                            override fun onFailure(call: Call<AddPostResponse>, t: Throwable) {
                                Log.d("error", t.toString())
                            }

                        })
                    }
                } else {
                    Toast.makeText(this, "빈 칸이 존재합니다. 빈 칸을 채워주세요!", Toast.LENGTH_SHORT).show()
                }

                val intent = Intent(this, TaxiTabFragment::class.java).apply {
                    putExtra("postData", temp)
                    putExtra("flag", 0)
                }
                setResult(RESULT_OK, intent)
                finish()

            } else if (type.equals("EDIT")) {
                if (isFirst) {
                    var school = false
                    var smoke = false
                    var gender = false
                    if (isAllCheck.isThirdVF.isGSchool) {
                        school = true
                    }
                    if (isAllCheck.isThirdVF.isSmoke) {
                        smoke = true
                    }
                    if (isAllCheck.isThirdVF.isMGender) {
                        gender = true
                    }

                    temp = AddPostData(editTitle, detailContent, selectFormattedDate, selectFormattedTime, school, participateNumberOfPeople, 0, false, 0.0, 0.0, "수락산역 3번 출구", selectCost.toInt())

                    CoroutineScope(Dispatchers.IO).launch {
                        /*"application/json; charset=UTF-8",*/
                        api.editPostData(temp!!, eTemp!!.postID).enqueue(object : Callback<AddPostResponse> {
                            override fun onResponse(
                                call: Call<AddPostResponse>,
                                response: Response<AddPostResponse>
                            ) {
                                if (response.isSuccessful) {
                                    println("succcckkkk")
                                } else {
                                    println("faafa")
                                    Log.d("edit", response.errorBody()?.string()!!)
                                    Log.d("message", call.request().toString())
                                    println(response.code())
                                }
                            }

                            override fun onFailure(call: Call<AddPostResponse>, t: Throwable) {
                                Log.d("error", t.toString())
                            }

                        })
                    }
                } else {
                    Toast.makeText(this, "빈 칸이 존재합니다. 빈 칸을 채워주세요!", Toast.LENGTH_SHORT).show()
                }

                val intent = Intent(this, TaxiTabFragment::class.java).apply {
                    putExtra("postData", temp)
                    putExtra("flag", 1)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
            /*
            * if (type.equals("ADD")) {
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty() && selectTargetDate.isNotEmpty()) {
                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    //현재 로그인된 유저 email 가져오기
                    userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                    //데이터 세팅 후 임시저장
                    temp = PostData(userEmail, pos, contentTitle, contentPost, selectTargetDate, categorySelect, "location", "targetTime" ,1, 4)
                    selectCalendarDataNoticeBoard[selectTargetDate] = arrayListOf()
                    selectCalendarDataNoticeBoard[selectTargetDate]!!.add(temp!!)
                    //pos는 현재 저장되지 않지만 나중에 짜피 백엔드에 데이터 넣을 거니 괜찮을듯
                    //나중에 api연결할때 여기 바꾸기
                    sharedViewModel!!.setCalendarLiveData("add", selectCalendarDataNoticeBoard)

                    val intent = Intent().apply {
                        putExtra("postData", temp)
                        putExtra("flag", 0)
                    }
                   *//*val intent = Intent(this, TaxiTabFragment::class.java).apply {
                        putExtra("postData", temp)
                        putExtra("flag", 0)
                    }*//*
                    pos += 1
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    if (contentTitle.isEmpty()) {
                        Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT) .show()
                    } else if (contentPost.isEmpty()) {
                        Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT) .show()
                    } else if (selectTargetDate.isEmpty()) {
                        Toast.makeText(this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT) .show()
                    }
                }
            } else { //edit
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty()) {
                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    //현재 로그인된 유저 email 가져오기
                    userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                }
            }*/
        }
    }
    class HeaderInterceptor constructor(private val token: String) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val token = "Bearer $token"
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            return chain.proceed(newRequest)
        }
    }

    private fun bottomBtnEvent() {
        if (isFirst || type == "EDIT") {
            CoroutineScope(Dispatchers.Main).launch {
                mBinding.editNext.apply {
                    setBackgroundResource(R.drawable.round_btn_update_layout)
                    setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
                }
            }
            mBinding.editNext.setOnClickListener {
                mBinding.editViewflipper.showNext()
            }
        }

        mBinding.editPre.setOnClickListener {
            if (currentPage <= 1) {
                //뒤로가기
                val intent = Intent(this@NoticeBoardEditActivity, MainActivity::class.java).apply {

                }
                setResult(8, intent)
                finish()
            } else if (currentPage == 2 && countPage == 1) {
                countPage -= 1
                myViewModel.postCheckComplete(false)
                mBinding.editViewflipper.showPrevious()
                returnStatusbar()
            } else if (currentPage == 3) {
                myViewModel.postCheckComplete(true)
                currentPage -= 1
                mBinding.editViewflipper.showPrevious()
                changeStatusbar()
            } else {
                currentPage -= 1
                myViewModel.postCheckComplete(true)
                mBinding.editViewflipper.showPrevious()
            }
        }



    }

    private fun showHourPicker() {
        val myCalender = Calendar.getInstance()
        val hour = myCalender[Calendar.HOUR_OF_DAY]
        val minute = myCalender[Calendar.MINUTE]
        val myTimeListener =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                if (view.isShown) {
                    myCalender[Calendar.HOUR_OF_DAY] = hourOfDay
                    myCalender[Calendar.MINUTE] = minute
                    val tempS = hourOfDay.toString() + "시 " + minute + "분"
                    selectFormattedTime = LocalTime.parse(tempS, DateTimeFormatter.ofPattern("H시 m분")).format(DateTimeFormatter.ofPattern("HH:mm"))

                    selectTime = if (hourOfDay > 12) {
                        val pm = hourOfDay - 12;
                        "오후 " + pm + "시 " + minute + "분"
                    } else {
                        hour1 = hourOfDay
                        minute1 = minute
                        "오전 " + hour + "시 " + minute + "분"
                    }
                    //selectTime = "${hourOfDay} 시 ${minute} 분"

                    mBinding.editSelectTime.text = selectTime
                    mBinding.editSelectTime.setTextColor(ContextCompat.getColor(this ,R.color.mio_gray_11))
                    isAllCheck.isFirstVF.isTime = true
                }
            }
        val timePickerDialog = TimePickerDialog(
            this,
            //여기서 테마 설정해서 커스텀하기
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            myTimeListener,
            hour,
            minute,
            true
        )
        timePickerDialog.setTitle("시간 선택 :")
        timePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        timePickerDialog.show()
    }

    interface VariableChangeListener {
        fun onVariableChanged(isFirstVF: FirstVF, isThirdVF: ThirdVF)
    }
    fun setVariableChangeListener(variableChangeListener: VariableChangeListener) {
        this.variableChangeListener = variableChangeListener
    }

    /*private fun signalChanged() {
        variableChangeListener?.onVariableChanged(isF)
    }*/


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

        }
        return super.onOptionsItemSelected(item)
    }

    private fun returnStatusbar() {
        mBinding.toolbar.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.parseColor("white")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
        val layoutParams = mBinding.editBottomLl.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 0
        mBinding.editBottomLl.layoutParams = layoutParams
    }

    private fun changeStatusbar() {
        mBinding.toolbar.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.argb(1, 0, 0, 0) /*Color.TRANSPARENT*/
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
        val layoutParams = mBinding.editBottomLl.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 180
        mBinding.editBottomLl.layoutParams = layoutParams
    }

    private fun searchKeyword(keyword: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)
        val call = api.getSearchKeyword(API_KEY, keyword)

        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(call: Call<ResultSearchKeyword>, response: Response<ResultSearchKeyword>) {
                addItemsAndMarkers(response.body())
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                Log.w("LocalSearch", "통신 실패: ${t.message}")
            }
        })
    }

    private fun addToRecentSearch(placeData: PlaceData) {
        val MAX_RECENT_SEARCH = 10

        val existingIndex = recentSearchItems.indexOfFirst { it.name == placeData.name && it.x == placeData.x && it.y == placeData.y }
        if (existingIndex != -1) {
            recentSearchItems.removeAt(existingIndex)
        } else if (recentSearchItems.size >= MAX_RECENT_SEARCH) {
            recentSearchItems.removeLast()
        }
        recentSearchItems.add(0, placeData)
        recentSearchAdapter.notifyDataSetChanged()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(recentSearchItems)
        editor.putString(KEY_RECENT_SEARCH, json)
        editor.apply()
    }

    fun addItemsAndMarkers(searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {

            listItems.clear()
            mapView.removeAllPOIItems()
            for (document in searchResult!!.documents) {
                val item = PlaceData(document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.x.toDouble(),
                    document.y.toDouble())
                listItems.add(item)

                val point = MapPOIItem()
                point.apply {
                    itemName = document.place_name
                    mapPoint = MapPoint.mapPointWithGeoCoord(document.y.toDouble(),
                        document.x.toDouble())
                    markerType = MapPOIItem.MarkerType.BluePin
                    selectedMarkerType = MapPOIItem.MarkerType.CustomImage
                    customSelectedImageResourceId = R.drawable.map_poi_icon
                    isCustomImageAutoscale = true
                    setCustomImageAnchor(0.5f, 1.0f)
                }
                mapView.addPOIItem(point)
            }
            placeAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }
    override fun onMapViewSingleTapped(mapView: MapView?, mapPoint: MapPoint?) {
        latitude = mapPoint?.mapPointGeoCoord?.latitude ?: return
        longitude = mapPoint.mapPointGeoCoord.longitude

        isAllCheck.isSecondVF.isPlaceName = true
        isAllCheck.isSecondVF.isPlaceRode = true
        myViewModel.postCheckValue(isAllCheck)


        if (marker != null) {
            mapView?.removePOIItem(marker)
            marker!!.markerType = MapPOIItem.MarkerType.BluePin
        }
        marker = MapPOIItem().apply {
            itemName = "선택 위치"
            this.mapPoint = mapPoint //MapPoint.mapPointWithGeoCoord(tlatitude, tlongitude)
            markerType = MapPOIItem.MarkerType.CustomImage
            customImageResourceId = R.drawable.map_poi_icon
            isCustomImageAutoscale = false
            isDraggable = true
            setCustomImageAnchor(0.5f, 1.0f)
        }
        mapView?.addPOIItem(marker)

        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses != null) {
/*            if (addresses.isNotEmpty()) {
                val address = addresses[0].getAddressLine(0)
                mBinding.placeRoad.text = "$address"
            }*/
            if (addresses.isNotEmpty()) {
                val address = addresses[0]

                val adminArea = address.adminArea ?: ""
                val subAdminArea = address.subAdminArea ?: ""
                val locality = address.locality ?: ""
                val subLocality = address.subLocality ?: ""
                val thoroughfare = address.thoroughfare ?: ""
                val featureName = address.featureName ?: ""

                val detailedAddress = "$adminArea $subAdminArea $locality $subLocality $thoroughfare $featureName".trim()

                mBinding.placeRoad.text = detailedAddress
            }
        }
    }

    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

    }



}