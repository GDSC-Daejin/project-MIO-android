package com.example.mio.NoticeBoard

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.model.Marker
import com.example.mio.*
import com.example.mio.Adapter.PlaceAdapter
import com.example.mio.Model.*
import com.example.mio.Navigation.SearchFragment
import com.example.mio.TabAccount.AccountSearchLocationActivity
import com.example.mio.TabCategory.CarpoolTabFragment
import com.example.mio.TabCategory.TaxiTabFragment
import com.example.mio.databinding.ActivityNoticeBoardEditBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.label.*
import com.kakao.vectormap.mapwidget.InfoWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import okhttp3.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*


class NoticeBoardEditActivity : AppCompatActivity() {
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
    //최근검색어
    private val recentSearchItems = arrayListOf<PlaceData>()
    private val recentSearchAdapter = PlaceAdapter(recentSearchItems)
    private var keyword = ""
    private lateinit var geocoder: Geocoder
    //private var mapView: MapView? = null
    //var mapViewContainer: RelativeLayout? = null
    //private var map: MapPOIItem? = null
    private val PREFS_NAME = "recent_search"
    private val KEY_RECENT_SEARCH = "search_items"

    private var map : com.kakao.vectormap.MapView? = null
    private var kakaoMapValue : KakaoMap? = null
    private var centerLabel: Label? = null
    private var startPosition: LatLng? = null
    private var labelLayerObject: LabelLayer? = null
    private var labelLatLng : LatLng? = null
    //콜백 리스너
    //private var variableChangeListener: VariableChangeListener? = null
    //모든 데이터 값
    private var isComplete = false
    //현재 페이지
    private var currentPage = 1
    private var countPage = 0 // 2번째 페이지 때문에 추가

    //add type
    private var type : String? = null

    /*첫 번째 vf*/
    //선택한 날짜
    private var selectTargetDate : String? = ""
    private var selectFormattedDate = ""
    private var isCategory = false //true : 카풀, false : 택시
    private var selectCategory = ""
    private var selectCategoryId = -1

    //설정한 제목
    private var editTitle = ""
    //선택한 시간
    private var selectTime :String?= ""
    private var selectFormattedTime = ""
    private var hour1 = 0
    private var minute1 = 0

    //선택한 탑승인원
    private var participateNumberOfPeople = 0


    /*두 번째 vf*/
    private var latitude : Double? = null
    private var longitude : Double? = null
    private var location = ""
    private var region3Depth = ""
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
        isParticipants = false,
        isTime = false,
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
    private var markers: MutableList<Marker> = mutableListOf()
    //뒤로가기
    // private lateinit var loadingDialog : LoadingProgressDialog
    private var backPressedTime = 0L
    //private var eventListener : SearchFragment.MarkerEventListener? = null   // 마커 클릭 이벤트 리스너

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityNoticeBoardEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        //뷰의 이벤트 리스너
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        //기기의 뒤로가기 콜백
        this.onBackPressedDispatcher.addCallback(this, callback)
        initMapView()
        /*mapView = MapView(this)


        val mapViewContainer = mBinding.mapView
        mapViewContainer.addView(mapView)*/

        //initMapView()

        //mBinding.mapView.addView(mapView)
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
            eTemp = intent.getSerializableExtra("editPostData") as PostData?
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
            this@NoticeBoardEditActivity.finish()
        }


        myViewModel.allCheck.observe(this) {
            if (it.isFirstVF.isTitle && it.isFirstVF.isTime && it.isFirstVF.isCalendar) {
                it.isFirstVF.isFirst = true
                isFirst = true
                /*Log.d("Allcheck", it.isFirstVF.isTitle.toString())
                Log.d("Allcheck", it.isFirstVF.isTime.toString())
                Log.d("Allcheck", it.isFirstVF.isCalendar.toString())
                Log.d("Allcheck", it.isFirstVF.isParticipants.toString())*/

                //println("ff")
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
                isFirst = true
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
                    Log.d("edit currentpage", currentPage.toString())
                    myViewModel.postCheckPage(currentPage)
                    // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                    val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                    if (inputMethodManager.isActive) {
                        // 가상 키보드가 올라가 있다면 내립니다.
                        inputMethodManager.hideSoftInputFromWindow(mBinding.editNext.windowToken, 0)
                    }
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

    private fun initMapView() {
        // 맵뷰 초기화 및 컨테이너 레이아웃에 추가
        map = mBinding.mapView
    }

    private fun firstVF() {
        if (type == "EDIT") {
            mBinding.editTitle.setText(eTemp!!.postTitle)
            editTitle = eTemp!!.postTitle
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
                isAllCheck.isFirstVF.isParticipants = true
                myViewModel.postCheckValue(isAllCheck)

            }
            override fun afterTextChanged(editable: Editable) {
                editTitle = editable.toString()
                println(editable.toString())
                /*if (editable.isEmpty()) {
                    Toast.makeText("")
                }*/
                //깜빡임 제거
                /*mBinding.editTitle.clearFocus()
                mBinding.editTitle.movementMethod = null*/
                mBinding.editTitle.isCursorVisible = false
                isAllCheck.isFirstVF.isTitle = editable.isNotEmpty()
            }
        })

        mBinding.editTitle.setOnClickListener {
            mBinding.editTitle.isCursorVisible = true
        }

        if (type == "EDIT") {
            mBinding.editSelectDateTv.text = eTemp!!.postTargetDate
            selectFormattedDate = eTemp!!.postTargetDate
        }
        mBinding.editCalendar.setOnClickListener {
            // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
            val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 가상 키보드가 올라가 있는지 여부를 확인합니다.
            if (inputMethodManager.isActive) {
                // 가상 키보드가 올라가 있다면 내립니다.
                inputMethodManager.hideSoftInputFromWindow(mBinding.editCalendar.windowToken, 0)
            }

            val cal = Calendar.getInstance()
            val today = Calendar.getInstance()

            val data = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                selectTargetDate = "${year}년/${month + 1}월/${day}일"
                selectFormattedDate = LocalDate.parse(selectTargetDate, DateTimeFormatter.ofPattern("yyyy년/M월/d일")).format(DateTimeFormatter.ISO_DATE)
                mBinding.editSelectDateTv.text = "${year}년/${month + 1}월/${day}일"
                mBinding.editSelectDateTv.setTextColor(ContextCompat.getColor(this, R.color.mio_gray_11))
                isAllCheck.isFirstVF.isCalendar = true
                myViewModel.postCheckValue(isAllCheck)
                if (selectTargetDate != null) {
                    mBinding.editCalendar.setImageResource(R.drawable.filter_calendar_update_icon)
                }
            }

            val datePickerDialog = DatePickerDialog(this, R.style.MySpinnerDatePickerStyle, data,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

            // Get the DatePicker instance from the DatePickerDialog
            val datePicker = datePickerDialog.datePicker

            // Set minimum date to today
            datePicker.minDate = today.timeInMillis

            datePickerDialog.show()
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
                selectCategoryId = 2
                mBinding.editCategoryTaxiBtn.apply {
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                myViewModel.postCheckValue(isAllCheck)
            } else {
                mBinding.editParticipateTv.text = "0"
                participateNumberOfPeople = 0
                //myViewModel.postCheckValue(isAllCheck)
            }
        }

        mBinding.editPlus.setOnClickListener {
            participateNumberOfPeople += 1
            if (participateNumberOfPeople < 11) {
                mBinding.editParticipateTv.text = participateNumberOfPeople.toString()
                myViewModel.postCheckValue(isAllCheck)
            } else {
                mBinding.editParticipateTv.text = "0"
                participateNumberOfPeople = 0
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

                mBinding.placeName.text = listItems[position].name
                mBinding.placeRoad.text = listItems[position].road
                addToRecentSearch(listItems[position])
                location = listItems[position].road + "/" + listItems[position].name
                getAddress(location)
                isAllCheck.isSecondVF.isPlaceName = true
                isAllCheck.isSecondVF.isPlaceRode = true
                myViewModel.postCheckValue(isAllCheck)
                startMapLifeCycle()
            }
        })

        recentSearchAdapter.setItemClickListener(object : PlaceAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                val inputMethodManager = this@NoticeBoardEditActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                if (inputMethodManager.isActive) {
                    // 가상 키보드가 올라가 있다면 내립니다.
                    inputMethodManager.hideSoftInputFromWindow(mBinding.btnSearch.windowToken, 0)
                }
                mBinding.editViewflipper.showNext()
                changeStatusbar()
                countPage += 1
                val mapPoint = MapPoint.mapPointWithGeoCoord(
                    recentSearchItems[position].y, recentSearchItems[position].x
                )
                latitude = recentSearchItems[position].y
                longitude = recentSearchItems[position].x

                //여기서 name고정됨
                mBinding.placeName.text = recentSearchItems[position].name
                mBinding.placeRoad.text = recentSearchItems[position].road
                location = recentSearchItems[position].road + "/" + recentSearchItems[position].name
                getAddress(location)
                isAllCheck.isSecondVF.isPlaceName = true
                isAllCheck.isSecondVF.isPlaceRode = true
                myViewModel.postCheckValue(isAllCheck)
                startMapLifeCycle()
            }
        })

        mBinding.etSearchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                keyword = editable.toString()
                if (keyword.isNotEmpty()) {
                    mBinding.recentSearch.text = "관련 검색어"
                } else {
                    mBinding.recentSearch.text = "최근 검색어"
                    mBinding.rvRecentSearchList.visibility = View.VISIBLE
                }
            }
        })

        mBinding.btnSearch.setOnClickListener {
            // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
            val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 가상 키보드가 올라가 있는지 여부를 확인합니다.
            if (inputMethodManager.isActive) {
                // 가상 키보드가 올라가 있다면 내립니다.
                inputMethodManager.hideSoftInputFromWindow(mBinding.btnSearch.windowToken, 0)
            }
            searchKeyword(keyword)
            Log.d("edit btn click", keyword)
            mBinding.rvRecentSearchList.visibility = View.INVISIBLE
            map?.resume()

        }

        mBinding.etSearchField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                if (inputMethodManager.isActive) {
                    // 가상 키보드가 올라가 있다면 내립니다.
                    inputMethodManager.hideSoftInputFromWindow(mBinding.etSearchField.windowToken, 0)
                }
                // 검색 버튼을 눌렀을 때 수행할 작업을 여기에 작성
                searchKeyword(keyword)
                mBinding.rvRecentSearchList.visibility = View.INVISIBLE
                map?.resume()
                return@setOnEditorActionListener true
            }
            false
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
                mBinding.editDetailContent.isCursorVisible = false
               // mBinding.editDetailContent.movementMethod = null
                myViewModel.postCheckValue(isAllCheck)
            }
        })

        //등/하교
       if (type == "EDIT") {
           mBinding.allC.visibility = View.GONE
       } else {
           mBinding.allC.visibility = View.VISIBLE
           mBinding.editGtschoolBtn.setOnClickListener {
               // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
               val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
               // 가상 키보드가 올라가 있는지 여부를 확인합니다.
               if (inputMethodManager.isActive) {
                   // 가상 키보드가 올라가 있다면 내립니다.
                   inputMethodManager.hideSoftInputFromWindow(mBinding.editGtschoolBtn.windowToken, 0)
               }

               mBinding.editGtschoolBtn.apply {
                   setBackgroundResource(R.drawable.round_btn_update_layout)
                   setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
               }
               mBinding.editAschoolBtn.apply {
                   setBackgroundResource(R.drawable.edit_check_btn)
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
                   setBackgroundResource(R.drawable.edit_check_btn)
                   setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_11))
               }
               isAllCheck.isThirdVF.isGSchool = false
               isAllCheck.isThirdVF.isASchool = true
               myViewModel.postCheckValue(isAllCheck)
           }
       }

        //흡연
        mBinding.editSmokerBtn.setOnClickListener {
            mBinding.editSmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editNsmokerBtn.apply {
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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

                detailContent = editable.toString()
                isAllCheck.isFourthVF.isContent = editable.isNotEmpty()
                mBinding.editDetailContent.isCursorVisible = true
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
            //통신
            /*val SERVER_URL = BuildConfig.server_URL
            val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                //.client(clientBuilder)

            //Authorization jwt토큰 로그인
            val interceptor = Interceptor { chain ->

                var newRequest: Request
                if (token != null && token != "") { // 토큰이 없는 경우
                    // Authorization 헤더에 토큰 추가
                    newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .build()

                    val expireDate: Long = getExpireDate.toLong()
                    if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                        //refresh 들어갈 곳
                        *//*newRequest =
                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                        val intent = Intent(this@NoticeBoardEditActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        Toast.makeText(this@NoticeBoardEditActivity, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
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
            val api = retrofit2.create(MioInterface::class.java)*/

            if (type.equals("ADD")) {
                Log.e("editactivity", "add")
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
                    /*val myAreaData = saveSharedPreferenceGoogleLogin.getSharedArea(this@NoticeBoardEditActivity).toString()*/
                    temp = AddPostData(editTitle, detailContent, selectFormattedDate, selectFormattedTime, school, participateNumberOfPeople, 0, false, latitude, longitude, location, selectCost.toInt(), region3Depth)
                    Log.d("edit add Temp ", temp.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        /*"application/json; charset=UTF-8",*/
                        RetrofitServerConnect.create(this@NoticeBoardEditActivity).addPostData(temp!!, selectCategoryId).enqueue(object : Callback<AddPostResponse> {
                            override fun onResponse(
                                call: Call<AddPostResponse>,
                                response: Response<AddPostResponse>
                            ) {
                                if (response.isSuccessful) {
                                    Log.d("NoticeBoardEdit", "response succcc")
                                } else {
                                    println("NoticeBoardEditActivityTempTest fafafafaf")
                                    Log.d("NoticeBoardEditActivityTempTest aa", response.errorBody()?.string()!!)
                                    Log.d("NoticeBoardEditActivityTempTestaf meeage", call.request().toString())
                                    println(response.code())
                                }
                            }

                            override fun onFailure(call: Call<AddPostResponse>, t: Throwable) {
                                Log.d("error", t.toString())
                            }

                        })
                    }
                    if (selectCategoryId == 1) {
                        val intent = Intent(this, NoticeBoardReadActivity::class.java).apply {
                            putExtra("postData", temp)
                            putExtra("flag", 0)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("flag", 1234)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }


                } else {
                    Toast.makeText(this, "빈 칸이 존재합니다. 빈 칸을 채워주세요!", Toast.LENGTH_SHORT).show()
                }

            } else if (type.equals("EDIT")) {

                Log.e("editactivity", "edit")
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
                    /*val myAreaData = saveSharedPreferenceGoogleLogin.getSharedArea(this@NoticeBoardEditActivity).toString()*/
                    val temp2 = EditPostData(editTitle, detailContent, selectCategoryId, selectFormattedDate, selectFormattedTime, participateNumberOfPeople, latitude, longitude, location, selectCost.toInt(), region3Depth)
                    Log.e("edit temp", temp2.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        val postId = eTemp?.postID ?: return@launch // postID가 null이면 실행 종료
                        RetrofitServerConnect.create(this@NoticeBoardEditActivity).editPostData(postId, temp2).enqueue(object : Callback<AddPostResponse> {
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

                    val castTemp2 = PostData(
                        eTemp!!.accountID,
                        eTemp!!.postID,
                        editTitle,
                        detailContent,
                        eTemp!!.postCreateDate,
                        selectFormattedDate,
                        selectFormattedTime,
                        if (selectCategoryId == 1) {
                               "carpool"
                        } else {
                               "taxi"
                        },
                        location,
                        eTemp!!.postParticipation,
                        participateNumberOfPeople,
                        selectCost.toInt(),
                        eTemp!!.postVerifyGoReturn,
                        eTemp!!.user,
                        latitude!!,
                        longitude!!
                    )
                    val intent = Intent(this, NoticeBoardReadActivity::class.java).apply {
                        putExtra("postData", castTemp2)
                        putExtra("flag", 33)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(this, "빈 칸이 존재합니다. 빈 칸을 채워주세요!", Toast.LENGTH_SHORT).show()
                }


            }
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
                if (currentPage == 2 && countPage == 1) {
                    returnStatusbar()
                }
                mBinding.editViewflipper.showNext()
                isComplete = !isComplete
                myViewModel.postCheckComplete(false)
                currentPage += 1
                Log.d("edit currentpage", currentPage.toString())
                myViewModel.postCheckPage(currentPage)
            }
            isFirst = true
        }

        mBinding.editPre.setOnClickListener {
            if (currentPage <= 1) {
                //뒤로가기
                val intent = Intent(this@NoticeBoardEditActivity, MainActivity::class.java).apply {
                    putExtra("flag", 9)
                }
                setResult(RESULT_OK, intent)
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

            // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
            val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            // 가상 키보드가 올라가 있는지 여부를 확인합니다.
            if (inputMethodManager.isActive) {
                // 가상 키보드가 올라가 있다면 내립니다.
                inputMethodManager.hideSoftInputFromWindow(mBinding.editPre.windowToken, 0)
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

                    selectTime = if (hourOfDay >= 12) {
                        val pm = if (hourOfDay == 12) hourOfDay else hourOfDay - 12
                        "오후 " + pm + "시 " + minute + "분"
                    } else {
                        "오전 " + hourOfDay + "시 " + minute + "분"
                    }
                    //selectTime = "${hourOfDay} 시 ${minute} 분"

                    mBinding.editSelectTime.text = selectTime
                    mBinding.editSelectTime.setTextColor(ContextCompat.getColor(this ,R.color.mio_gray_11))
                    isAllCheck.isFirstVF.isTime = true
                }
                if (selectTime != null) {
                    mBinding.editTime.setImageResource(R.drawable.filter_time_update_icon)
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

//    interface VariableChangeListener {
//        fun onVariableChanged(isFirstVF: FirstVF, isThirdVF: ThirdVF)
//    }
//    fun setVariableChangeListener(variableChangeListener: VariableChangeListener) {
//        this.variableChangeListener = variableChangeListener
//    }

    /*private fun signalChanged() {
        variableChangeListener?.onVariableChanged(isF)
    }*/

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
        val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 가상 키보드가 올라가 있는지 여부를 확인합니다.
        if (inputMethodManager.isActive) {
            // 가상 키보드가 올라가 있다면 내립니다.
            inputMethodManager.hideSoftInputFromWindow(mBinding.editCalendar.windowToken, 0)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)
        val call = api.getSearchKeyword(API_KEY, keyword)
        Log.e("edit search", keyword.toString())

        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(call: Call<ResultSearchKeyword>, response: Response<ResultSearchKeyword>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.e("edit search", result.toString())
                    if (response.code() == 200) {
                        val documents = result?.documents
                        if (documents?.isNotEmpty()==true) {
                            Log.e("edit search", response.message().toString())
                            Log.e("edit search", response.code().toString())
                            Log.e("edit search", documents.toString())
                            addItemsAndMarkers(result)
                        } else {
                            Log.e("Search Error", response.code().toString())
                            Log.e("Search Error", response.body().toString())
                            Log.e("Search Error", response.errorBody()?.string()!!)


                        }
                    } else {
                        Log.e("Search Error", response.code().toString())
                        Log.e("Search Error", response.message().toString())
                        Log.e("Search Error", response.body().toString())
                        Log.e("Search Error", response.errorBody()?.string()!!)
                    }
                    /*if (result != null) {

                    } else {
                        Log.e("edit search", "Response body is null")
                    }*/
                } else {
                    Log.e("EDIT Search", response.code().toString())
                    Log.e("EDIT Search", response.errorBody()?.string()!!)
                    Log.e("EDIT Search", response.errorBody()?.string() ?: "Unknown error")
                    Log.e("EDIT Search", call.request().toString())
                    Log.e("EDIT Search", response.message().toString())
                }
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                Log.w("LocalSearch", "통신 실패: ${t.message}")
            }
        })
    }


    private fun getAddress(location2 : String?) {
        val location = location2?.split("/")?.first()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)
        val call2 = api.getAddressSearch(API_KEY, location2?.replace("/", "").toString())
        call2.enqueue(object: Callback<ResultSearchAddress> {
            override fun onResponse(call: Call<ResultSearchAddress>, response: Response<ResultSearchAddress>) {
                if (response.isSuccessful) {
                    val responseData = response.body()?.documents
                    Log.e("search Result", location2.toString())
                    Log.e("search Result", response.body()?.documents.toString())
                    if (responseData?.isEmpty() == true) {
                        Toast.makeText(this@NoticeBoardEditActivity, "잘못된 지역입니다 다시 검색해주세요", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        if (responseData != null) {
                            response.body()?.documents.let {
                                if (it != null) {
                                    region3Depth = it.take(1).first().address?.region_3depth_name.toString()
                                }
                            }
                            Log.e("region3Depth", region3Depth)
                            /*adapter.updateData(tempList, keyword)
                            binding.textView4.visibility = View.GONE
                            binding.textView5.visibility = View.GONE
                            binding.rvSearchList.visibility = View.VISIBLE*/
                        }
                    }
                } else {
                    Log.e("search Result", response.code().toString())
                    Log.e("search Result", response.errorBody()?.string()!!)
                    Log.e("search Result", response.errorBody()?.string()!!)
                    Log.e("search Result", call.request().toString())
                    Log.e("search Result", response.message().toString())
                }
            }

            override fun onFailure(call: Call<ResultSearchAddress>, t: Throwable) {
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
            map?.removeAllViewsInLayout()
            for (document in searchResult!!.documents) {
                val item = PlaceData(document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.x.toDouble(),
                    document.y.toDouble())
                listItems.add(item)
                /*val index = listItems.indexOf(item)

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
                mapView?.addPOIItem(point)*/


                //mapView?.setPOIItemEventListener(eventListener)
                /*for (i in response.body()!!.filter { it.postId != postId }) {
                    //latLngList.add(LatLng.from(i.latitude, i.longitude))
                    // 스타일 지정. LabelStyle.from()안에 원하는 이미지 넣기
                    val style = kakaoMapValue?.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.map_poi_srn)))
                    // 라벨 옵션 지정. 위경도와 스타일 넣기
                    val options = LabelOptions.from(LatLng.from(i.latitude, i.longitude)).setStyles(style)
                    // 레이어 가져오기
                    val layer = kakaoMapValue?.labelManager?.layer
                    // 레이어에 라벨 추가
                    layer?.addLabel(options)
                }*/
                //latLngList.add(LatLng.from(i.latitude, i.longitude))
                // 스타일 지정. LabelStyle.from()안에 원하는 이미지 넣기
                //val style = kakaoMapValue?.labelManager?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.map_poi_srn)))
                // 라벨 옵션 지정. 위경도와 스타일 넣기
                //val options = LabelOptions.from(LatLng.from(i.latitude, i.longitude)).setStyles(style)
                // 레이어 가져오기
                //val layer = kakaoMapValue?.labelManager?.layer
                // 레이어에 라벨 추가
                //layer?.addLabel(options)
            }
            placeAdapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 좌표를 주소로 변환하여 가져오는 함수
    /*private fun getAddressFromCoordinates(longitude : Double, latitude : Double) {
val service = retrofit.create(ReverseGeocodingAPI::class.java)
    val apiKey = "YOUR_API_KEY_HERE" // Kakao API 키 입력
    val call = service.getReverseGeocode(apiKey, longitude, latitude)

    call.enqueue(object : Callback<ResultReverseGeocode> {
        override fun onResponse(
            call: Call<ResultReverseGeocode>,
            response: Response<ResultReverseGeocode>
        ) {
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null && result.documents.isNotEmpty()) {
                    val address = result.documents[0].address
                    // 주소 정보를 사용하여 원하는 작업 수행
                    Log.d("ReverseGeocode", "주소: $address")
                } else {
                    Log.d("ReverseGeocode", "주소를 찾을 수 없습니다.")
                }
            } else {
                Log.e("ReverseGeocode", "API 요청 실패: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<ResultReverseGeocode>, t: Throwable) {
            Log.e("ReverseGeocode", "API 요청 실패: ${t.message}")
        }
    })

    }*/

    private fun getAddressFromCoordinates(latitude: Double, longitude: Double, apiKey: String, callback: (String?) -> Unit) {
        val url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=$longitude&y=$latitude"
        val httpClient = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", apiKey)
            .build()

        httpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                val address = parseAddressFromResponse(responseBody)
                Log.d("getAddressFromCoordinates", address.toString())
                callback(address)
            }
        })
    }
    private fun parseAddressFromResponse(responseBody: String?): String? {
        if (responseBody.isNullOrBlank()) return null

        val json = JSONObject(responseBody)
        val documents = json.getJSONArray("documents")

        if (documents.length() > 0) {
            val firstDocument = documents.getJSONObject(0)
            //Log.d("Document", documents.toString())
            val address = firstDocument.getJSONObject("address")
            val addressName = address.getString("address_name")
            // road_address 필드가 있는지 확인하고 처리합니다.
            val roadAddress = if (!firstDocument.isNull("road_address")) {
                firstDocument.getJSONObject("road_address")
            } else {
                JSONObject() // "road_address" 필드가 없을 경우 빈 JSONObject 생성
            }
            // roadAddress가 null이 아니라면 해당 필드를 가져옵니다.
            val buildName = if (!roadAddress.isNull("building_name")) {
                roadAddress?.getString("building_name")
            } else {
                null
            }


            return "$addressName $buildName"
        }

        return null
    }

    // 맵뷰 이벤트 리스너
    /*private val mapViewEventListener: MapViewEventListener = object : MapViewEventListenerAdapter() {
        override fun onMapViewSingleTapped(mapView: MapView?, point: LatLng?) {
            point?.let {
                getAddressFromCoordinates(it)
            }
        }
    }*/


    /*override fun onMapViewInitialized(p0: MapView?) {

    }

    override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {

    }

    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {

    }

    override fun onMapViewSingleTapped(mapView: MapView?, mapPoint: MapPoint?) {

    }*/
    // Kakao Map API를 호출하여 주변 빌딩 정보를 검색하는 함수
    /*private fun searchNearbyBuildings(query : String, latitude: Double, longitude: Double, callback: (List<PlaceDocument>?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(KakaoApiService::class.java)
        val apiKey = API_KEY // 본인의 Kakao API 키 입력
        val call = service.searchAddress(query, longitude, latitude, radius = 100, sort = "accuracy", apiKey = apiKey)

        call.enqueue(object : Callback<SearchResult> {
            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                if (response.isSuccessful) {
                    val result = response.body()?.documents?.toList()
                    //val buildingNames = result?.documents?.map { it.road_address.building_name!! }
                    callback(result)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SearchResult>, t: Throwable) {
                callback(null)
            }
        })
    }*/
    private fun startMapLifeCycle() {
        map?.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.e("noticeboardeditMapTest", "onMapDestroy")
            }

            override fun onMapPaused() {
                super.onMapPaused()
                Log.e("noticeboardeditMapTest", "onmappaused")
                map?.resume()
            }

            override fun onMapResumed() {
                super.onMapResumed()
                Log.e("noticeboardeditMapTest", "onmapresumed")
            }

            override fun onMapError(error: Exception?) {
                Log.e("noticeboardedit", "onMapError", error)
            }

        }, object : KakaoMapReadyCallback() {
            override fun getPosition(): LatLng {
                return super.getPosition()
            }

            override fun getZoomLevel(): Int {
                return 17
            }

            override fun onMapReady(kakaoMap: KakaoMap) {
                Log.e("noticeboardedit", "onMapReady")
                kakaoMapValue = kakaoMap
                labelLayerObject = kakaoMap.labelManager!!.layer
                val trackingManager = kakaoMap.trackingManager
                //labelLatLng = LatLng.from(latitude, longitude)
                if (latitude != null && longitude != null) {
                    startPosition = LatLng.from(latitude!!, longitude!!)
                    centerLabel = labelLayerObject!!.addLabel(
                        LabelOptions.from("centerLabel", startPosition)
                            .setStyles(LabelStyle.from(R.drawable.map_poi_black_icon).setAnchorPoint(0.5f, 0.5f))
                            .setRank(1)
                    )

                    trackingManager!!.startTracking(centerLabel)
                }

                kakaoMapValue!!.setOnMapClickListener { _, latLng, _, poi ->
                    //showInfoWindow(position, poi)


                    trackingManager?.stopTracking()
                    latitude = latLng.latitude
                    longitude = latLng.longitude

                    val geocoder = Geocoder(this@NoticeBoardEditActivity, Locale.KOREA)
                    var address: Address? = null
                    var roadAddress : String? = null


                    isAllCheck.isSecondVF.isPlaceName = true
                    isAllCheck.isSecondVF.isPlaceRode = true
                    myViewModel.postCheckValue(isAllCheck)

                    if (Build.VERSION.SDK_INT < 33) { // SDK 버전이 33보다 큰 경우에만 아래 함수를 씁니다.
                        val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)?.first()
                        Log.e("notice Test Log", "ongeocode <33")
                        address?.let {
                            if (addresses != null) {
                                val adminArea = addresses.adminArea ?: ""
                                val subAdminArea = addresses.subAdminArea ?: ""
                                val locality = addresses.locality ?: ""
                                val subLocality = addresses.subLocality ?: ""
                                val thoroughfare = addresses.thoroughfare ?: ""
                                val featureName = addresses.featureName ?: ""

                                val detailedAddress = "$adminArea $subAdminArea $locality $subLocality $thoroughfare $featureName".trim()
                                roadAddress = detailedAddress
                                mBinding.placeRoad.text = roadAddress
                                location = roadAddress + "/" + poi.name//listItems[position].road + "/" + listItems[position].name
                                getAddress(location)
                            }
                        }
                    } else {
                        val geocodeListener = @RequiresApi(33) object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                Log.e("notice Test Log", "ongeocode")
                                // 주소 리스트를 가지고 할 것을 적어주면 됩니다.
                                address = addresses.firstOrNull()
                                address?.let {
                                    val adminArea = it.adminArea ?: ""
                                    val subAdminArea = it.subAdminArea ?: ""
                                    val locality = it.locality ?: ""
                                    val subLocality = it.subLocality ?: ""
                                    val thoroughfare = it.thoroughfare ?: ""
                                    val featureName = it.featureName ?: ""

                                    val detailedAddress =
                                        "$adminArea $subAdminArea $locality $subLocality $thoroughfare $featureName".trim()


                                    if (addresses.isNotEmpty()) {
                                        address = addresses[0]
                                        val roadAddressCheck = address?.getAddressLine(0) ?: ""

                                        //대한민국 경기도 포천시 선단동 834-2
                                        Log.d("Address", "도로명 주소: $roadAddressCheck")
                                        Log.d("detail", "$detailedAddress")
                                        mBinding.placeRoad.text = roadAddressCheck
                                        location = roadAddressCheck + "/" + poi.name
                                        getAddress(location)
                                        // Kakao Map API를 호출하여 해당 주소 주변의 빌딩 정보 검색
                                        /*searchNearbyBuildings(detailedAddress,latitude!!, longitude!!) { buildingNames ->
                                            buildingNames?.let { placeDocument ->
                                                //PlaceDocument(road_address=RoadAddress(address_name=경기 포천시 송선로 285, building_name=))]
                                                //1번 근데 빌딩이름은 없고 도로명만 나올때가있음
                                                Log.d("BuildingNames", "빌딩 이름들: $buildingNames")
                                                // 여기에서 UI 업데이트 또는 다른 작업을 수행할 수 있습니다.
                                                if (placeDocument.isNotEmpty()) {

                                                    location = placeDocument.first().road_address.address_name + "/" + placeDocument.first().road_address.building_name
                                                    mBinding.placeRoad.text = placeDocument.first().road_address.address_name
                                                } else {
                                                    getAddressFromCoordinates(latitude!!, longitude!!, API_KEY) { addressCoordinates ->
                                                        addressCoordinates.let {//2번
                                                            //경기 포천시 선단동 834-2
                                                            if (addressCoordinates != null) {
                                                                location = addressCoordinates
                                                                //mBinding.placeName.text = ""
                                                                mBinding.placeRoad.text = addressCoordinates
                                                            } else {
                                                                //대한민국 경기도 포천시 선단동 834-2
                                                                location = address?.getAddressLine(0).toString()
                                                                //mBinding.placeName.text = ""
                                                                mBinding.placeRoad.text = address?.getAddressLine(0).toString()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }*/
                                    } else {
                                        Log.e("Address", "주소를 가져올 수 없습니다.")
                                        //Toast.makeText(this, "주소를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                                    }


                                }
                            }

                            override fun onError(errorMessage: String?) {
                                address = null
                                Toast.makeText(
                                    this@NoticeBoardEditActivity,
                                    "주소가 발견되지 않았습니다.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        geocoder.getFromLocation(latitude!!, longitude!!, 10, geocodeListener)
                    }
                    if (poi.name.isNotEmpty()) {
                        labelLayerObject?.removeAll()

                        labelLatLng = LatLng.from(latLng.latitude, latLng.longitude)
                        Log.e("labelLatLng1", "$labelLatLng")

                        // 레이어 가져오기
                        //val labelLayer = kakaoMap.labelManager?.layer

                        // 스타일 지정
                        val style = kakaoMap.labelManager?.addLabelStyles(
                            LabelStyles.from(
                                LabelStyle.from(R.drawable.map_poi_black_icon).apply {
                                    setAnchorPoint(0.5f, 0.5f)
                                    isApplyDpScale = true
                                }
                            )
                        )

                        // 라벨 옵션 지정
                        val options = LabelOptions.from(labelLatLng).setStyles(style)

                        // 라벨 추가
                        val label = labelLayerObject?.addLabel(options)
                        mBinding.placeName.text = poi.name
                        // 라벨로 트래킹 시작
                        if (label != null) {
                            trackingManager?.startTracking(label)
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed(java.lang.Runnable {
                                trackingManager?.stopTracking()
                            },1000)
                        } else {
                            Log.e("kakaoMapValue", "Label is null, tracking cannot be started.")
                        }
                    }
                }

                //KakaoMap kakaoMap, LabelLayer layer, Label label
                kakaoMapValue!!.setOnLabelClickListener { kakaoMap, layer, label ->
                    Log.e("kakao map value", "label?")
                    if (label != null) { //return 값이 true 이면, 이벤트가 OnLabelClickListener 에서 끝난다.

                        Log.e("kakao map value", "label")
                        trackingManager?.startTracking(label)
                        return@setOnLabelClickListener true
                    } else { //return 값이 false 이면, 이벤트가 OnPoiClickListener, OnMapClickListener 까지 전달된다.
                        Log.e("kakao map value", "label x")
                        return@setOnLabelClickListener false
                    }
                }
            }
        })
    }

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 뒤로가기 클릭 시 실행시킬 코드 입력
            val fragmentManager = supportFragmentManager

            if (System.currentTimeMillis() > backPressedTime + 2000) {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(this@NoticeBoardEditActivity, "뒤로 버튼을 한 번 더 누르시면 홈으로 이동합니다.", Toast.LENGTH_SHORT).show()
                return
            }

            if (fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()
            } else {
                // AActivity를 종료하고 HomeActivity로 이동
                val intent = Intent(this@NoticeBoardEditActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e("NoticeBoardEdit TEst Log", "start")
    }

    override fun onResume() {
        super.onResume()
        Log.e("NoticeBoardEdit TEst Log", "reumse")
        startMapLifeCycle()
        map?.resume()
    }

    override fun onPause() {
        super.onPause()
        Log.e("NoticeBoardEdit TEst Log", "pause")
        map?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("NoticeBoardEdit TEst Log", "destory")
        map?.finish()
        map = null
    }

    /*override fun onResume() {
        super.onResume()
        Log.e("NoticeBoardEdit", "resume")
        if (mapView == null) {
            initMapView()
        } else {
            mapView = null

            mBinding.mapView.removeAllViews()
            mBinding.mapView.removeView(mapView)
        }
    }*/

    /*override fun onStop() {
        super.onStop()
        Log.e("NoticeBoardEdit", "stop")
        if (mapView != null) {
            mapView = null
            mBinding.mapView.removeAllViewsInLayout()
            mBinding.mapView.removeAllViews()
        }
    }

    override fun finish() {
        // 종료할 때 맵뷰 제거 (맵뷰 2개 이상 동시에 불가)
        if (mapView != null) {
            mapView = null

            mBinding.mapView.removeAllViews()
            mBinding.mapView.removeView(mapView)
        }
        Log.e("NoticeBoardEdit", "finish")
        super.finish()
    }*/
}