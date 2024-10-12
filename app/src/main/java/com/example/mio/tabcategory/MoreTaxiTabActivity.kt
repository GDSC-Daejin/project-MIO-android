package com.example.mio.tabcategory

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.adapter.MoreTaxiTabAdapter
import com.example.mio.bottomsheetfragment.AnotherBottomSheetFragment
import com.example.mio.bottomsheetfragment.BottomSheetFragment
import com.example.mio.model.PostData
import com.example.mio.model.PostReadAllResponse
import com.example.mio.viewmodel.SharedViewModel
import com.example.mio.noticeboard.NoticeBoardReadActivity
import com.example.mio.databinding.ActivityMoreTaxiTabBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList


class MoreTaxiTabActivity : AppCompatActivity() {
    private lateinit var mttBinding : ActivityMoreTaxiTabBinding
    private lateinit var myViewModel : SharedViewModel
    private var mtAdapter : MoreTaxiTabAdapter? = null
    private var getBottomSheetData = ""
    private var getBottomData = ""
    private var manager : LinearLayoutManager = LinearLayoutManager(this)

    private var moreTaxiAllData = ArrayList<PostData?>()
    private var dataPosition = 0
    private var date = ""

    //칩 생성
    private var chipList = kotlin.collections.ArrayList<Chip>()

    private var tempFilterPostData : ArrayList<PostData?> = ArrayList()

    //로딩 즉 item의 끝이며 스크롤의 끝인지
    private var isLoading = false
    //데이터의 현재 페이지 수
    private var currentPage = 0
    //데이터의 전체 페이지 수
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mttBinding = ActivityMoreTaxiTabBinding.inflate(layoutInflater)
        //뷰의 이벤트 리스너
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        date = intent.getStringExtra("type").toString()

        if (date == "DATE") {
            date = intent.getStringExtra("date").toString()

            mttBinding.moreDate.text = getString(R.string.setDateTextMonth, date)
        }


        initSwipeRefresh()
        initRecyclerView()
        initScrollListener()
        //showBottomSheetAd(this)

        //이건 날짜, 탑승 수, 담배, 성별, 학교 순서 등 필터
        mttBinding.filterResetLl.setOnClickListener {//필터리셋
            mttBinding.moreFilterTv.setTextColor(ContextCompat.getColor(this@MoreTaxiTabActivity ,R.color.mio_gray_8))
            mttBinding.moreFilterBtn.setImageResource(R.drawable.filter_icon)
            mttBinding.filterResetLl.visibility = View.GONE
            mttBinding.moreAddFilterBtnSg.removeAllViewsInLayout()
            getBottomData = ""
            chipList.clear()
            initSwipeRefresh()
            setSelectData()
        }
        //이건 날짜, 탑승 수, 담배, 성별, 학교 순서 등 필터
        //필터 취소 기능 넣기 TODO
        mttBinding.moreFilterBtn.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        //"${selectTargetDate} ${selectTime} ${participateNumberOfPeople} ${isCheckSchool} ${isCheckGender} ${isCheckSmoke}"
                        if (value.split(",").count {it == " "} < 5) {
                            getBottomData = value
                            myViewModel.postCheckFilter(getBottomData)
                            mttBinding.moreFilterTv.setTextColor(ContextCompat.getColor(this@MoreTaxiTabActivity,R.color.mio_blue_4))
                            mttBinding.moreFilterBtn.setImageResource(R.drawable.filter_update_icon)
                            mttBinding.filterResetLl.visibility = View.VISIBLE
                        }
                    }
                })
            }
        }

        //최신순, 가까운 순 등..
        mttBinding.moreSearchFilterBtn.setOnClickListener {
            val bottomSheet = AnotherBottomSheetFragment()
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : AnotherBottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        getBottomSheetData = value
                        myViewModel.postCheckSearchFilter(getBottomSheetData)
                    }
                })
            }
        }

        //recyclerview item클릭 시
        mtAdapter!!.setItemClickListener(object : MoreTaxiTabAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = moreTaxiAllData[position]
                    dataPosition = position
                    val intent = Intent(this@MoreTaxiTabActivity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp!!.user.profileImageUrl)
                    }
                    requestActivity.launch(intent)
                }
            }
        })


       /* myViewModel.checkSmokeFilter.observe(this) {
            println("흡연")
            when(it.toString()) {
                "흡연o" -> {
                    println("?")
                }
                "흡연x" -> {
                    println("ㅌ")
                }
            }
        }
        myViewModel.checkSchoolFilter.observe(this) {
            println("학교")
            when(it.toString()) {
                "등교" -> {
                    println("등교")
                }
                "하교" -> {
                    println("하교")
                }
            }
        }
        myViewModel.checkGenderFilter.observe(this) {
            when(it.toString()) {
                "남성" -> {
                    println("남성")
                }
                "여성" -> {
                    println("d서엉")
                }
            }
        }*/

        myViewModel.checkSearchFilter.observe(this) {
            when(it) {
                "최신 순" -> {
                    mttBinding.moreSearchTv.text = "최신 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    moreTaxiAllData.sortByDescending { mSort -> mSort?.postCreateDate }
                    mtAdapter?.notifyDataSetChanged()
                }
                "마감 임박 순" -> {
                    mttBinding.moreSearchTv.text = "마감 임박 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    // 날짜 및 시간 형식 지정
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    // 정렬 로직
                    val sortedTargets = moreTaxiAllData.sortedWith { t1, t2 ->
                        // 날짜 비교
                        val dateComparison = LocalDate.parse(t1?.postTargetDate, dateFormatter)
                            .compareTo(LocalDate.parse(t2?.postTargetDate, dateFormatter))

                        // 날짜가 같으면 시간 비교
                        if (dateComparison == 0) {
                            LocalTime.parse(t1?.postTargetTime, timeFormatter)

                                .compareTo(LocalTime.parse(t2?.postTargetTime, timeFormatter))
                        } else {
                            dateComparison
                        }
                    }
                    moreTaxiAllData.clear()
                    moreTaxiAllData.addAll(sortedTargets)
                    mtAdapter?.notifyDataSetChanged()
                }
                "낮은 가격 순" -> {
                    mttBinding.moreSearchTv.text = "낮은 가격 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    moreTaxiAllData.sortBy { mSort -> mSort?.postCost }
                    mtAdapter?.notifyDataSetChanged()
                }
            }
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                mttBinding.moreRefreshSwipeLayout.isRefreshing = false
                this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
        }

        myViewModel.checkFilter.observe(this) {
            //"${selectTargetDate} ${selectTime} ${participateNumberOfPeople} ${isCheckSchool} ${isCheckGender} ${isCheckSmoke} $isReset"
            val temp = it.split(",")
            val cGroup = mttBinding.moreAddFilterBtnSg
            tempFilterPostData.clear()
            when (temp[3]) {
                "등교" -> {
                    chipList.add(createNewChip(
                        text = "등교", cGroup
                    ))
                    /*val params = LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(20, 15, 0, 15) // 왼쪽, 위, 오른쪽, 아래 순서입니다.

                    val btn = Button(this).apply {
                        layoutParams = params
                        *//*setOnClickListener {
                                println("ciclcc")
                            }*//*
                            setBackgroundResource(R.color.white)
                            setBackgroundResource(R.drawable.round_filter_btn)
                            setTextColor(
                                ContextCompat.getColor(
                                    this@MoreTaxiTabActivity,
                                    R.color.mio_blue_4
                                )
                            )
                            text = "흡연O"

                            width = resources.getDimensionPixelSize(R.dimen.button_height)
                            height = resources.getDimensionPixelSize(R.dimen.button_height)

                        }
                        mttBinding.moreAddFilterBtnLl.addView(btn)*/

                }
                "하교" -> {
                    chipList.add(createNewChip(
                        text = "하교", cGroup
                    ))
                }
                else -> {
                    // 다른 경우 처리
                }
            }

            when (temp[4]) {
                "남성" -> {
                    chipList.add(createNewChip(
                        text = "남성", cGroup
                    ))
                }
                "여성" -> {
                    chipList.add(createNewChip(
                        text = "여성", cGroup
                    ))
                }
                else -> {

                }
            }

            when (temp[5]) {
                "흡연O" -> {
                    chipList.add(createNewChip(
                        text = "흡연O", cGroup
                    ))
                }
                "흡연x" -> {
                    chipList.add(createNewChip(
                        text = "흡연X", cGroup
                    ))
                }
                else -> {

                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                //"${selectTargetDate} ${selectTime} ${participateNumberOfPeople} ${isCheckSchool} ${isCheckGender} ${isCheckSmoke}"
                var noConditionDate = ""
                var noConditionTime = ""
                var noConditionPeople = -1 //0이 아니면 true
                var noConditionSchool : Boolean?= null
                var noConditionGender: Boolean? = null
                var noConditionSmoke: Boolean? = null
                tempFilterPostData.clear()
                // temp 배열을 기준으로 필터링 조건 설정
                for (i in temp.indices) {
                    val currentCondition = temp[i]
                    when (i) {
                        0 -> { // 날짜
                            if (currentCondition.isNotEmpty()) {
                                noConditionDate = currentCondition
                            } else {
                                Log.e("No condition0", "empty")
                            }
                        }
                        1 -> { // 시간
                            if (currentCondition.isNotEmpty()) {
                                noConditionTime = currentCondition
                            }
                        }
                        2 -> { // 인원수
                            if (currentCondition.isNotEmpty()) {
                                noConditionPeople = currentCondition.toInt()
                            }
                        }
                        3 -> { // 등하교
                            if (currentCondition.isNotEmpty()) {
                                if (currentCondition == "등교") {
                                    noConditionSchool = true
                                } else if (currentCondition == "하교") {
                                    noConditionSchool = false
                                }
                            }
                        }
                        4 -> { // 성별
                            if (currentCondition.isNotEmpty()) {
                                if (currentCondition == "여성") {
                                    noConditionGender = true
                                } else if (currentCondition == "남성") {
                                    noConditionGender = false
                                }
                            }
                        }
                        5 -> { // 흡연여부
                            if (currentCondition.isNotEmpty()) {
                                if (temp[5] == "흡연O") {
                                    noConditionSmoke = true
                                } else if (temp[5] == "흡연x") {
                                    noConditionSmoke = false
                                }
                            }
                        }
                        else -> {
                            // 추가 조건이 있는 경우 여기에 추가할 수 있음
                            Log.e("Unknown condition", "Index $i not handled")
                        }
                    }
                }

                val tempData: List<PostData?>?
                if (noConditionPeople > 0) {
                    // 인원수가 0보다 큰 경우, 모든 조건을 적용하여 필터링
                    tempData = moreTaxiAllData.filter { item ->
                        item != null &&
                                (noConditionDate.isEmpty() || item.postTargetDate == noConditionDate) &&
                                (noConditionTime.isEmpty() || item.postTargetTime == noConditionTime) &&
                                /*(noConditionPeople == -1 || item.postParticipationTotal == noConditionPeople) &&*/
                                (noConditionSchool == null || item.postVerifyGoReturn == noConditionSchool) &&
                                (noConditionGender == null || item.user.gender == noConditionGender) &&
                                (noConditionSmoke == null || item.user.verifySmoker == noConditionSmoke)
                    }
                } else {
                    // 인원수가 0인 경우, 날짜로만 필터링
                    tempData = moreTaxiAllData.filter { item ->
                        item != null &&
                                (noConditionDate.isEmpty() || item.postTargetDate == noConditionDate) &&
                                (noConditionTime.isEmpty() || item.postTargetTime == noConditionTime) &&
                                (noConditionSchool == null || item.postVerifyGoReturn == noConditionSchool) &&
                                (noConditionGender == null || item.user.gender == noConditionGender) &&
                                (noConditionSmoke == null || item.user.verifySmoker == noConditionSmoke)
                    }
                }
                tempData.forEach { item ->
                    tempFilterPostData.add(item)
                }
                //dszcctempFilterPostData.addAll(tempData)
                Log.d("filter", tempData.toString())
                //moreCarpoolAllData.clear()
                Log.d("filter", tempFilterPostData.toString())
                mtAdapter!!.moreTaxiData = tempFilterPostData
                mtAdapter?.notifyDataSetChanged()


                withContext(Dispatchers.Main) {
                    if (tempFilterPostData.isEmpty()) {
                        mttBinding.moreNonfilterTv.visibility = View.VISIBLE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.GONE
                    } else {
                        mttBinding.moreNonfilterTv.visibility = View.GONE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
                        // UI 조작
                        mtAdapter?.notifyDataSetChanged()
                    }
                }
            }

            if (chipList.isNotEmpty()) {
                mttBinding.moreAddFilterBtnSg.removeAllViewsInLayout()
                for (i in chipList.indices) {
                    // 마지막 Chip 뷰의 인덱스를 계산
                    val lastChildIndex = mttBinding.moreAddFilterBtnSg.childCount - 1
                    // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                    // 현재 Chip을 바로 그 앞에 추가
                    if (lastChildIndex >= 0) {
                        mttBinding.moreAddFilterBtnSg.addView(chipList[i], lastChildIndex)
                    } else {
                        // ChipGroup에 자식이 없는 경우, 그냥 추가
                        mttBinding.moreAddFilterBtnSg.addView(chipList[i])
                    }
                }
                chipList.clear()
            }
        }

        mttBinding.backArrow.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        setContentView(mttBinding.root)
    }

    private fun initSwipeRefresh() {
        mttBinding.moreRefreshSwipeLayout.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // 데이터 새로 고침
            refreshData()

            // 스크롤 리스너 초기화
            initScrollListener()
        }
    }

    private fun refreshData() {
        isLoading = false
        currentPage = 0
        //moreCarpoolAllData.clear() // Clear existing data
        mtAdapter?.notifyDataSetChanged() // Notify adapter of data change

        // Fetch fresh data
        setSelectData()
    }

    private fun initScrollListener() {
        mttBinding.moreTaxiTabRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val lastVisibleItemPosition = layoutManager?.findLastCompletelyVisibleItemPosition() ?: -1
                val itemTotalCount = recyclerView.adapter?.itemCount ?: 0

                // 스크롤이 끝에 도달했는지 확인하고 isLoading 상태 확인
                if (lastVisibleItemPosition >= itemTotalCount - 1 && !isLoading) {
                    if (currentPage < totalPages - 1) {
                        isLoading = true // Set isLoading to true to prevent multiple calls

                        // Add a placeholder for the loading item
                        val runnable = kotlinx.coroutines.Runnable {
                            moreTaxiAllData.add(null)
                            mtAdapter?.notifyItemInserted(moreTaxiAllData.size - 1)
                        }
                        mttBinding.moreTaxiTabRv.post(runnable)

                        // Load more items
                        getMoreItem()
                    }
                }
            }
        })
    }

    private fun getMoreItem() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // Remove the loading item placeholder
            val loadingPosition = moreTaxiAllData.indexOf(null)
            if (loadingPosition != -1) {
                moreTaxiAllData.removeAt(loadingPosition)
                mtAdapter?.notifyItemRemoved(loadingPosition)
            }

            // Fetch more data if necessary
            if (currentPage < totalPages - 1) {
                currentPage += 1
                RetrofitServerConnect.create(this@MoreTaxiTabActivity).getCategoryPostData(1, "createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
                    override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            responseData?.let {
                                val newItems = it.content.filter { item ->
                                    item.isDeleteYN == "N" && item.postType == "BEFORE_DEADLINE"
                                }.map { item ->
                                    PostData(
                                        item.user.studentId,
                                        item.postId,
                                        item.title,
                                        item.content,
                                        item.createDate,
                                        item.targetDate,
                                        item.targetTime,
                                        item.category.categoryName,
                                        item.location,
                                        item.participantsCount,
                                        item.numberOfPassengers,
                                        item.cost,
                                        item.verifyGoReturn,
                                        item.user,
                                        item.latitude,
                                        item.longitude
                                    )
                                }

                                moreTaxiAllData.addAll(newItems)

                                if (getBottomData.isNotEmpty()) {
                                    myViewModel.postCheckFilter(getBottomData)
                                }

                                if (getBottomSheetData.isNotEmpty()) {
                                    myViewModel.postCheckSearchFilter(getBottomSheetData)
                                }
                            }
                        } else {
                            Log.d("Error", "Response code: ${response.code()}")
                        }
                        isLoading = false
                    }

                    override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                        Log.d("Error", "Failure: ${t.message}")
                        isLoading = false
                    }
                })
            }
        }, 2000)
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            RESULT_OK -> {
                //val post = it.data?.getSerializableExtra("postData") as PostData
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            /*taxiAllData.add(post)
                            calendarTaxiAllData.add(post) //데이터 전부 들어감

                            //들어간 데이터를 key로 분류하여 저장하도록함
                            selectCalendarData[post.postTargetDate] = arrayListOf()
                            selectCalendarData[post.postTargetDate]!!.add(post)

                            println(selectCalendarData)*/
                            //setData()
                        }
                        //livemodel을 통해 저장
                        //sharedViewModel!!.setCalendarLiveData("add", selectCalendarData)
                        //noticeBoardAdapter!!.notifyDataSetChanged()
                    }
                    //edit
                    1 -> {

                    }

                    2 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            initSwipeRefresh()
                            setSelectData()
                        }
                    }

                }
            }
        }
    }

    private fun setSelectData() {
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(this@MoreTaxiTabActivity).getCategoryPostData(2,"createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseData ->
                            moreTaxiAllData.clear()
                            totalPages = responseData.totalPages

                            // 필터링된 게시글만 처리
                            val filteredContent = responseData.content.filter {
                                it.isDeleteYN == "N" && it.postType == "BEFORE_DEADLINE"
                            }

                            // 필터링된 게시글 처리
                            for (post in filteredContent) {
                                val part = post.participantsCount
                                val location = post.location
                                val title = post.title
                                val content = post.content
                                val targetDate = post.targetDate
                                val targetTime = post.targetTime
                                val categoryName = post.category.categoryName
                                val cost = post.cost
                                val verifyGoReturn = post.verifyGoReturn

                                moreTaxiAllData.add(PostData(
                                    post.user.studentId,
                                    post.postId,
                                    title,
                                    content,
                                    post.createDate,
                                    targetDate,
                                    targetTime,
                                    categoryName,
                                    location,
                                    part,
                                    post.numberOfPassengers,
                                    cost,
                                    verifyGoReturn,
                                    post.user,
                                    post.latitude,
                                    post.longitude
                                ))
                            }

                            Log.e("moreTaxiAllData", "$moreTaxiAllData")
                            // 어댑터 데이터 갱신
                            mtAdapter?.let { adapter ->
                                adapter.moreTaxiData = moreTaxiAllData
                                adapter.notifyDataSetChanged()
                            }
                            //mtAdapter?.updateDataList(moreTaxiAllData)

                            // ViewModel 필터링 및 검색 필터 확인
                            when {
                                getBottomData.isNotEmpty() -> myViewModel.postCheckFilter(getBottomData)
                                getBottomSheetData.isNotEmpty() -> myViewModel.postCheckSearchFilter(getBottomSheetData)
                                getBottomData.isNotEmpty() && getBottomSheetData.isNotEmpty() -> {
                                    myViewModel.postCheckFilter(getBottomData)
                                    myViewModel.postCheckSearchFilter(getBottomSheetData)
                                }
                                else -> {
                                    // 새로 고침 완료 후 터치 활성화
                                    mttBinding.moreRefreshSwipeLayout.isRefreshing = false
                                    this@MoreTaxiTabActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                }
                            }

                            // 게시글 유무에 따른 UI 처리
                            if (moreTaxiAllData.isEmpty()) {
                                mttBinding.moreNonfilterTv.apply {
                                    text = "카풀 게시글이 존재하지 않습니다"
                                    visibility = View.VISIBLE
                                }
                                mttBinding.moreRefreshSwipeLayout.visibility = View.GONE
                            } else {
                                mttBinding.moreNonfilterTv.visibility = View.GONE
                                mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
                            }
                        } ?: run {
                            Log.e("setSelectData", "Response body is null")
                        }
                    } else {
                        Log.e("setSelectData", "Error code: ${response.code()}")
                        if (moreTaxiAllData.isEmpty()) {
                            mttBinding.moreNonfilterTv.apply {
                                text = "카풀 게시글이 존재하지 않습니다"
                                visibility = View.VISIBLE
                            }
                            mttBinding.moreRefreshSwipeLayout.visibility = View.GONE
                        } else {
                            mttBinding.moreNonfilterTv.visibility = View.GONE
                            mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Toast.makeText(this@MoreTaxiTabActivity, "연결에 실패하였습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun initRecyclerView() {
        setSelectData()
        mtAdapter = MoreTaxiTabAdapter()
        //mtAdapter!!.moreTaxiData = moreTaxiAllData
        mttBinding.moreTaxiTabRv.adapter = mtAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        mttBinding.moreTaxiTabRv.setHasFixedSize(true)
        mttBinding.moreTaxiTabRv.layoutManager = manager
    }

    private fun createNewChip(text: String, parent : ViewGroup): Chip {
        val chip = layoutInflater.inflate(R.layout.notice_board_chip_layout, parent, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }

}