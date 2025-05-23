package com.gdsc.mio.noticeboard

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
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
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.mio.*
import com.gdsc.mio.adapter.PlaceAdapter
import com.gdsc.mio.databinding.ActivityNoticeBoardEditBinding
import com.gdsc.mio.model.*
import com.gdsc.mio.viewmodel.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.label.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.*


class NoticeBoardEditActivity : AppCompatActivity() {
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = BuildConfig.map_api_key
    }

    private lateinit var mBinding : ActivityNoticeBoardEditBinding

    //클릭한 포스트(게시글)의 데이터 임시저장
    private var temp : AddPostData? = null
    //edit용 임시저장 데이터
    private var eTemp : PostData? = null

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
    private val prefsName = "recent_search"
    private val keyRecentSearch = "search_items"

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
    private var selectCategory = ""
    private var selectCategoryId = -1

    //설정한 제목
    private var editTitle = ""
    //선택한 시간
    private var selectTime :String?= ""
    private var selectFormattedTime = ""

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
    //뒤로가기
    // private lateinit var loadingDialog : LoadingProgressDialog
    private var backPressedTime = 0L

    //체크 변수들 true면 작성완료
    private var isSchool = false
    private var isSmoker = false
    private var isGender = false


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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                eTemp = intent.getParcelableExtra("editPostData")
                bottomBtnEvent()
                //vf 생성
                firstVF()
                secondVF()
                thirdVF()
                fourthVF()
                fifthVF()
            } else {
                eTemp = intent.getParcelableExtra("editPostData", PostData::class.java)
                bottomBtnEvent()
                //vf 생성
                firstVF()
                secondVF()
                thirdVF()
                fourthVF()
                fifthVF()
            }
        }

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
                        returnStatusBar()
                    }
                    mBinding.editViewflipper.showNext()
                    isComplete = !isComplete
                    myViewModel.postCheckComplete(false)
                    currentPage += 1
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
                    isFirst = true
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
        myViewModel.postCheckPage(currentPage)
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
                //isAllCheck.isFirstVF.isParticipants = true
                myViewModel.postCheckValue(isAllCheck)
            }

            override fun afterTextChanged(editable: Editable) {
                editTitle = editable.toString()
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
            showDatePicker()
        }

        mBinding.editDateLl.setOnClickListener {
            showDatePicker()
        }


        if (type == "EDIT") {
            mBinding.editSelectTime.text = eTemp!!.postTargetTime
            selectFormattedTime = eTemp!!.postTargetTime
        }
        mBinding.editTime.setOnClickListener {
            showCustomTimePickerDialog()
        }
        mBinding.editTimeLl.setOnClickListener {
            showCustomTimePickerDialog()
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

            isAllCheck.isFirstVF.isFirst = true
            myViewModel.postCheckValue(isAllCheck)
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
            isAllCheck.isFirstVF.isFirst = true
            myViewModel.postCheckValue(isAllCheck)
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
                mBinding.editParticipateTv.text = "0"
                participateNumberOfPeople = 0
                //myViewModel.postCheckValue(isAllCheck)
            }
        }

        mBinding.editPlus.setOnClickListener {
            participateNumberOfPeople += 1
            if (participateNumberOfPeople < 11) {
                mBinding.editParticipateTv.text = participateNumberOfPeople.toString()
                isAllCheck.isFirstVF.isParticipants = true
                myViewModel.postCheckValue(isAllCheck)
            } else {
                mBinding.editParticipateTv.text = "0"
                participateNumberOfPeople = 0
            }
        }

    }

    private fun parseHolidays(jsonString: String, year : String): Map<String, List<String>> {
        val holidaysMap = mutableMapOf<String, List<String>>()

        try {
            val jsonObject = JSONObject(jsonString)

            val yearParse = jsonObject.getJSONObject(year)

            for (key in yearParse.keys()) {
                val holidayNames = yearParse.getJSONArray(key)

                val holidayList = mutableListOf<String>()
                for (i in 0 until holidayNames.length()) {
                    holidayList.add(holidayNames.getString(i))
                }
                holidaysMap[key] = holidayList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return holidaysMap
    }

    private fun secondVF() {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(keyRecentSearch, null)
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
                latitude = listItems[position].y
                longitude = listItems[position].x

                mBinding.placeName.text = listItems[position].name
                mBinding.placeRoad.text = listItems[position].road
                addToRecentSearch(listItems[position])
                location = listItems[position].road + "/" + listItems[position].name
                getAddress(location)
                isAllCheck.isSecondVF.isPlaceName = true
                isAllCheck.isSecondVF.isPlaceRode = true
                isAllCheck.isSecondVF.isSecond = true
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
                latitude = recentSearchItems[position].y
                longitude = recentSearchItems[position].x

                //여기서 name고정됨
                mBinding.placeName.text = recentSearchItems[position].name
                mBinding.placeRoad.text = recentSearchItems[position].road
                location = recentSearchItems[position].road + "/" + recentSearchItems[position].name
                getAddress(location)
                isAllCheck.isSecondVF.isPlaceName = true
                isAllCheck.isSecondVF.isPlaceRode = true
                isAllCheck.isSecondVF.isSecond = true
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
                } catch (e : java.lang.NumberFormatException) {
                    Toast.makeText(this@NoticeBoardEditActivity, "숫자로만 입력해 주세요", Toast.LENGTH_SHORT).show()
                }

                isAllCheck.isThirdVF.isAmount = editable.isNotEmpty()
                isAllCheck.isThirdVF.isThird = true
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

        //성별
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
                isAllCheck.isFourthVF.isFourth = true
                mBinding.editDetailContent.isCursorVisible = true
                mBinding.editDetailContent.movementMethod = null
                myViewModel.postCheckValue(isAllCheck)
            }
        })
    }

    private fun fifthVF() {
        mBinding.completeBtn.setOnClickListener {
            if (type.equals("ADD")) {
                if (isFirst) {
                    var school = false

                    if (isAllCheck.isThirdVF.isGSchool) {
                        school = true
                    }

                    /*val myAreaData = saveSharedPreferenceGoogleLogin.getSharedArea(this@NoticeBoardEditActivity).toString()*/
                    temp = AddPostData(editTitle, detailContent, selectFormattedDate, selectFormattedTime, school, participateNumberOfPeople, 0, false, latitude, longitude, location, selectCost.toInt(), region3Depth)

                    RetrofitServerConnect.create(this@NoticeBoardEditActivity).addPostData(temp!!, selectCategoryId).enqueue(object : Callback<AddPostResponse> {
                        override fun onResponse(
                            call: Call<AddPostResponse>,
                            response: Response<AddPostResponse>
                        ) {
                            if (!response.isSuccessful) {
                                Toast.makeText(this@NoticeBoardEditActivity, "게시글 생성에 실패하였습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<AddPostResponse>, t: Throwable) {
                            Log.d("error", t.toString())
                            Toast.makeText(this@NoticeBoardEditActivity, "연결에 실패하였습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                        }

                    })
                    if (selectCategoryId == 1) {
                        val intent = Intent().apply {
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

                if (isFirst) {
                    /*val myAreaData = saveSharedPreferenceGoogleLogin.getSharedArea(this@NoticeBoardEditActivity).toString()*/
                    val temp2 = EditPostData(editTitle, detailContent, selectCategoryId, selectFormattedDate, selectFormattedTime, participateNumberOfPeople, latitude, longitude, location, selectCost.toInt(), region3Depth)

                    CoroutineScope(Dispatchers.IO).launch {
                        val postId = eTemp?.postID ?: return@launch // postID가 null이면 실행 종료
                        RetrofitServerConnect.create(this@NoticeBoardEditActivity).editPostData(postId, temp2).enqueue(object : Callback<AddPostResponse> {
                            override fun onResponse(
                                call: Call<AddPostResponse>,
                                response: Response<AddPostResponse>
                            ) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@NoticeBoardEditActivity, "게시글 수정을 완료하였습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@NoticeBoardEditActivity, "게시글 수정에 실패하였습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<AddPostResponse>, t: Throwable) {
                                Log.d("error", t.toString())
                                Toast.makeText(this@NoticeBoardEditActivity, "게시글 수정에 실패하였습니다. ${t.message}", Toast.LENGTH_SHORT).show()
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
                    returnStatusBar()
                }
                mBinding.editViewflipper.showNext()
                isComplete = !isComplete
                myViewModel.postCheckComplete(false)
                currentPage += 1
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
            } else if (currentPage == 2) {
                currentPage -= 1
                myViewModel.postCheckPage(currentPage)
                mBinding.editViewflipper.showPrevious()
                returnStatusBar()
            } else if (currentPage == 3) {
                myViewModel.postCheckPage(currentPage)
                currentPage -= 1
                mBinding.editViewflipper.showPrevious()
                changeStatusbar()
            } else {
                currentPage -= 1
                myViewModel.postCheckPage(currentPage)
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

    private fun showDatePicker() {
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

            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, day)

            // 주말 여부 확인
            val dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                Toast.makeText(this, "현행법상 주말은 선택할 수 없습니다. 다른 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@OnDateSetListener
            }


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // URL 요청 및 JSON 데이터 읽기
                    val url = URL("https://holidays.hyunbin.page/basic.json")
                    val inputStream = withContext(Dispatchers.IO) { url.openStream() }
                    val data = inputStream.bufferedReader().use { it.readText() }
                    val holidays = parseHolidays(data, year.toString()) // JSON 파싱 함수 호출

                    // 선택한 날짜와 비교
                    val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day) // 2024-04-10 형식
                    withContext(Dispatchers.Main) {
                        if (holidays.containsKey(formattedDate)) {
                            Toast.makeText(
                                this@NoticeBoardEditActivity,
                                "현행법상 공휴일은 선택할 수 없습니다. 다른 날짜를 선택해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@withContext
                        } else {
                            // 선택이 유효한 경우, 처리 로직 실행
                            selectTargetDate = "${year}년/${month + 1}월/${day}일"
                            selectFormattedDate = LocalDate.parse(
                                selectTargetDate,
                                DateTimeFormatter.ofPattern("yyyy년/M월/d일")
                            ).format(DateTimeFormatter.ISO_DATE)
                            mBinding.editSelectDateTv.text = getString(
                                R.string.setDateText3,
                                "$year",
                                "${month + 1}",
                                "$day"
                            )
                            mBinding.editSelectDateTv.setTextColor(
                                ContextCompat.getColor(this@NoticeBoardEditActivity, R.color.mio_gray_11)
                            )
                            isAllCheck.isFirstVF.isCalendar = true
                            myViewModel.postCheckValue(isAllCheck)

                            if (selectTargetDate != null) {
                                mBinding.editCalendar.setImageResource(R.drawable.filter_calendar_update_icon)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@NoticeBoardEditActivity, "공휴일 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val datePickerDialog = DatePickerDialog(this, R.style.MySpinnerDatePickerStyle, data,
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        val datePicker = datePickerDialog.datePicker

        datePicker.minDate = today.timeInMillis

        //최대날짜를 현재년도의 다음년도의 12월 31일까지
        val maxDate = Calendar.getInstance()
        maxDate.set(today.get(Calendar.YEAR) + 1, Calendar.DECEMBER, 31)
        datePicker.maxDate = maxDate.timeInMillis

        datePickerDialog.show()
    }

    /*private fun showHourPicker() {
        val myCalender = Calendar.getInstance()
        val hour = myCalender[Calendar.HOUR_OF_DAY]
        val minute = myCalender[Calendar.MINUTE]
        val myTimeListener =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, pickerMinute ->
                if (view.isShown) {
                    // 시간 검증
                    val isValidTime = (hourOfDay in 7..8 && pickerMinute >= 0) || (hourOfDay in 18..19 && pickerMinute >= 0)
                    if (!isValidTime) {
                        Toast.makeText(
                            this,
                            "현행법상 오전 7시부터 9시, 오후 6시부터 8시까지만 선택 가능합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        showHourPicker() // 다시 시간 선택창 띄움
                        return@OnTimeSetListener
                    }

                    val tempS = "${hourOfDay}시 ${pickerMinute}분"
                    selectFormattedTime = LocalTime.parse(tempS, DateTimeFormatter.ofPattern("H시 m분")).format(DateTimeFormatter.ofPattern("HH:mm"))

                    // 오전/오후 표시 처리
                    selectTime = when {
                        hourOfDay > 12 -> { // 오후 시간
                            val pm = hourOfDay - 12
                            "오후 $pm 시 $pickerMinute 분"
                        }
                        else -> { // 오전 시간
                            "오전 $hourOfDay 시 $pickerMinute 분"
                        }
                    }

                    // 선택된 시간 UI 업데이트
                    mBinding.editSelectTime.text = selectTime
                    mBinding.editSelectTime.setTextColor(ContextCompat.getColor(this, R.color.mio_gray_11))
                    isAllCheck.isFirstVF.isTime = true

                    // 아이콘 변경
                    if (selectTime != null) {
                        mBinding.editTime.setImageResource(R.drawable.filter_time_update_icon)
                    }
                }
            }
        val timePickerDialog = TimePickerDialog(
            this,
            myTimeListener,
            hour,
            minute,
            true
        ).apply {
            setTitle("Select Time")
            window!!.setBackgroundDrawableResource(R.drawable.dialog_round_background)
        }

        timePickerDialog.show()
    }*/
    private fun showCustomTimePickerDialog() {
        val myCalendar = Calendar.getInstance()
        var hour = myCalendar[Calendar.HOUR_OF_DAY]
        val minute = myCalendar[Calendar.MINUTE]
        val isAm = hour < 12 // 현재 시간이 오전인지 여부

        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null)

        val amPmPicker: NumberPicker = dialogView.findViewById(R.id.am_pm_picker)
        val hourPicker: NumberPicker = dialogView.findViewById(R.id.hourPicker)
        val minutePicker: NumberPicker = dialogView.findViewById(R.id.minutePicker)
        val btnOk: TextView = dialogView.findViewById(R.id.btn_ok) // 선택 완료 버튼

        // 오전/오후 선택 값 설정
        val values = arrayOf("오전", "오후")
        amPmPicker.minValue = 0
        amPmPicker.maxValue = values.size - 1
        amPmPicker.wrapSelectorWheel = false
        amPmPicker.displayedValues = values
        amPmPicker.value = if (isAm) 0 else 1 // 현재 시간 기준으로 자동 선택

        // 현재 시간이 오전이면 7~8시, 오후면 18~19시로 조정
        hour = when {
            isAm && hour !in 7..8 -> 7
            !isAm && hour !in 18..19 -> 18
            else -> hour
        }

        // 시간 NumberPicker 설정
        hourPicker.minValue = 7
        hourPicker.maxValue = 8
        if (!isAm) {
            hourPicker.minValue = 18
            hourPicker.maxValue = 19
        }
        hourPicker.value = hour
        hourPicker.wrapSelectorWheel = false

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.value = minute // 현재 분 설정
        minutePicker.wrapSelectorWheel = true

        hourPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        minutePicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        amPmPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        amPmPicker.setOnValueChangedListener { _, _, newVal ->
            if (newVal == 0) { // 오전 선택 시
                hourPicker.minValue = 7
                hourPicker.maxValue = 8
                if (hourPicker.value !in 7..8) {
                    hourPicker.value = 7
                }
            } else { // 오후 선택 시
                hourPicker.minValue = 18
                hourPicker.maxValue = 19
                if (hourPicker.value !in 18..19) {
                    hourPicker.value = 18
                }
            }
        }

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corner_dialog)

        btnOk.setOnClickListener {
            val selectedAmPm = amPmPicker.value // 0 = 오전, 1 = 오후
            val selectedHour = hourPicker.value
            val selectedMinute = minutePicker.value

            val tempS = "${selectedHour}시 ${selectedMinute}분"
            selectFormattedTime = LocalTime.parse(tempS, DateTimeFormatter.ofPattern("H시 m분"))
                .format(DateTimeFormatter.ofPattern("HH:mm"))

            // 오전/오후 표시 처리
            selectTime = if (selectedAmPm == 1) { // 오후
                val pmHour = if (selectedHour > 12) selectedHour - 12 else selectedHour
                "오후 $pmHour 시 $selectedMinute 분"
            } else { // 오전
                "오전 $selectedHour 시 $selectedMinute 분"
            }

            mBinding.editSelectTime.text = selectTime
            mBinding.editSelectTime.setTextColor(ContextCompat.getColor(this, R.color.mio_gray_11))
            isAllCheck.isFirstVF.isTime = true
            myViewModel.postCheckValue(isAllCheck)

            mBinding.editTime.setImageResource(R.drawable.filter_time_update_icon)

            dialog.dismiss()
        }

        dialog.show()
    }


    private fun returnStatusBar() {
        mBinding.toolbar.visibility = View.VISIBLE
        // 기존의 FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS를 추가합니다.
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // 상태 바 색상을 설정합니다.
        window.statusBarColor = Color.parseColor("white")

        // Android 11 (API 30) 이상에서는 setDecorFitsSystemWindows(true)를 설정합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
        } else {
            // API 21 이상에서는 상태 바의 배경을 설정하고 레이아웃을 조정합니다.
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            // 아래의 두 줄로 상태 바를 투명하게 만들고, 레이아웃을 조정합니다.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        // 레이아웃의 하단 마진을 조정합니다.
        val layoutParams = mBinding.editBottomLl.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 0
        mBinding.editBottomLl.layoutParams = layoutParams
    }

    private fun changeStatusbar() {
        mBinding.toolbar.visibility = View.GONE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // 상태 바 색상을 설정합니다 (반투명)
        window.statusBarColor = Color.TRANSPARENT // 또는 Color.argb(1, 0, 0, 0)로 반투명하게 설정

        // Android 11 (API 30) 이상에서는 setDecorFitsSystemWindows를 false로 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            // API 21 이하에서는 시스템 UI 플래그를 설정
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }

        // 하단 레이아웃 마진을 조정
        val layoutParams = mBinding.editBottomLl.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.bottomMargin = 180 // 원하는 마진 값으로 설정
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

        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(call: Call<ResultSearchKeyword>, response: Response<ResultSearchKeyword>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (response.code() == 200) {
                        val documents = result?.documents
                        if (documents?.isNotEmpty()==true) {
                            addItemsAndMarkers(result)
                        } else {
                            Toast.makeText(this@NoticeBoardEditActivity, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@NoticeBoardEditActivity, "검색에 실패하였습니다 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                    /*if (result != null) {

                    } else {
                        Log.e("edit search", "Response body is null")
                    }*/
                } else {
                    Toast.makeText(this@NoticeBoardEditActivity, "검색에 실패하였습니다 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                Toast.makeText(this@NoticeBoardEditActivity, "연결에 실패하였습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun getAddress(location2 : String?) {
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
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResultSearchAddress>, t: Throwable) {
                Toast.makeText(this@NoticeBoardEditActivity, "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addToRecentSearch(placeData: PlaceData) {
        val maxRecentSearch = 10

        val existingIndex = recentSearchItems.indexOfFirst { it.name == placeData.name && it.x == placeData.x && it.y == placeData.y }
        if (existingIndex != -1) {
            recentSearchItems.removeAt(existingIndex)
        } else if (recentSearchItems.size >= maxRecentSearch) {
            recentSearchItems.removeLast()
        }
        recentSearchItems.add(0, placeData)
        recentSearchAdapter.notifyDataSetChanged()

        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(recentSearchItems)
        editor.putString(keyRecentSearch, json)
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
            }

            override fun onMapPaused() {
                super.onMapPaused()
                map?.resume()
            }

            override fun onMapResumed() {
                super.onMapResumed()
            }

            override fun onMapError(error: Exception?) {
            }

        }, object : KakaoMapReadyCallback() {
            override fun getPosition(): LatLng {
                return super.getPosition()
            }

            override fun getZoomLevel(): Int {
                return 17
            }

            override fun onMapReady(kakaoMap: KakaoMap) {
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
                    var roadAddress : String?


                    isAllCheck.isSecondVF.isPlaceName = true
                    isAllCheck.isSecondVF.isPlaceRode = true
                    myViewModel.postCheckValue(isAllCheck)

                    if (Build.VERSION.SDK_INT < 33) { // SDK 버전이 33보다 낮은 경우에만 아래 함수를 씁니다.
                        val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)?.first()
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
                                // 주소 리스트를 가지고 할 것을 적어주면 됩니다.
                                address = addresses.firstOrNull()
                                address?.let {
                                    if (addresses.isNotEmpty()) {
                                        address = addresses[0]
                                        val roadAddressCheck = address?.getAddressLine(0) ?: ""

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
                                        Toast.makeText(this@NoticeBoardEditActivity, "주소를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
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
                            handler.postDelayed( {
                                trackingManager?.stopTracking()
                            },1000)
                        } else {
                            //Log.e("kakaoMapValue", "Label is null, tracking cannot be started.")
                            Toast.makeText(this@NoticeBoardEditActivity, "라벨이 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //KakaoMap kakaoMap, LabelLayer layer, Label label
                kakaoMapValue!!.setOnLabelClickListener { _, _, label ->
                    if (label != null) { //return 값이 true 이면, 이벤트가 OnLabelClickListener 에서 끝난다.
                        trackingManager?.startTracking(label)
                        return@setOnLabelClickListener true
                    } else { //return 값이 false 이면, 이벤트가 OnPoiClickListener, OnMapClickListener 까지 전달된다.
                        return@setOnLabelClickListener false
                    }
                }
            }
        })
    }

    private fun updateButtonStatus() {
        val conditions = Conditions(isSchool, isGender, isSmoker)

        val shouldEnableButton = conditions.shouldEnableButton()

        if (shouldEnableButton) {
            mBinding.editNext.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_1))
            }
            mBinding.editNext.setOnClickListener {
                mBinding.editViewflipper.showNext()

                isComplete = !isComplete
                myViewModel.postCheckComplete(false)
                currentPage += 1
                myViewModel.postCheckPage(currentPage)
            }
        }
        mBinding.editNext.isEnabled = shouldEnableButton
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


    override fun onResume() {
        super.onResume()
        startMapLifeCycle()
        map?.resume()
    }

    override fun onPause() {
        super.onPause()
        map?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map?.finish()
        map = null
    }
}