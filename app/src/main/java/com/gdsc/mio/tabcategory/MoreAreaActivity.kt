package com.gdsc.mio.tabcategory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.mio.*
import com.gdsc.mio.adapter.MoreTaxiTabAdapter
import com.gdsc.mio.bottomsheetfragment.AnotherBottomSheetFragment
import com.gdsc.mio.bottomsheetfragment.BottomSheetFragment
import com.gdsc.mio.model.*
import com.gdsc.mio.noticeboard.NoticeBoardReadActivity
import com.gdsc.mio.databinding.ActivityMoreAreaBinding
import com.gdsc.mio.viewmodel.MoreAreaViewModel
import com.gdsc.mio.viewmodel.SharedViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class MoreAreaActivity : AppCompatActivity() {
    private lateinit var mttBinding : ActivityMoreAreaBinding
    private lateinit var myViewModel : SharedViewModel
    private var myArea : String? = null
    private var mtAdapter : MoreTaxiTabAdapter? = null
    private var getBottomSheetData = ""
    private var getBottomData = ""
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    //private var moreAreaData = ArrayList<PostData?>()
    //필터 리셋시 사용
    //private var moreTempCarpoolAllData =  ArrayList<PostData?>()
    private var dataPosition = 0

    private var tempFilterPostData : ArrayList<PostData?> = ArrayList()

    //칩 생성
    private var chipList = kotlin.collections.ArrayList<Chip>()

    /*//로딩 즉 item의 끝이며 스크롤의 끝인지
    private var isLoading = false
    //데이터의 현재 페이지 수
    private var currentPage = 0
    //데이터의 전체 페이지 수
    private var totalPages = 0*/

    private val viewModel: MoreAreaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mttBinding = ActivityMoreAreaBinding.inflate(layoutInflater)
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        setContentView(mttBinding.root)

        myArea = intent.getStringExtra("area")
        mttBinding.moreAreaTv.text = myArea
        initSwipeRefresh()
        initRecyclerView()
        initScrollListener()

        lifecycleScope.launchWhenStarted {
            viewModel.moreAreaPostData.collect { updatedData ->
                updateUI(updatedData)
                mtAdapter?.updateDataList(updatedData)  // 데이터를 어댑터에 설정
            }
        }

        mttBinding.filterResetLl.setOnClickListener {//필터리셋
            mttBinding.moreFilterTv.setTextColor(ContextCompat.getColor(this@MoreAreaActivity ,R.color.mio_gray_8))
            mttBinding.moreFilterBtn.setImageResource(R.drawable.filter_icon)
            mttBinding.filterResetLl.visibility = View.GONE
            mttBinding.moreAddFilterBtnSg.removeAllViewsInLayout()
            getBottomData = ""
            chipList.clear()
            initSwipeRefresh()
            refreshData()
            //setSelectData()
        }
        //이건 날짜, 탑승 수, 담배, 성별, 학교 순서 등 필터
        mttBinding.moreFilterBtn.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        //"${selectTargetDate} ${selectTime} ${participateNumberOfPeople} ${isCheckSchool} ${isCheckGender} ${isCheckSmoke}"
                        if (value.split(",").count {it == " "} < 5) {
                            getBottomData = value
                            myViewModel.postCheckFilter(getBottomData)
                            mttBinding.moreFilterTv.setTextColor(ContextCompat.getColor(this@MoreAreaActivity ,R.color.mio_blue_4))
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
                    val temp = viewModel.moreAreaPostData.value[position]
                    dataPosition = position
                    val intent = Intent(this@MoreAreaActivity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp!!.user.profileImageUrl)
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        myViewModel.checkSearchFilter.observe(this) {
            when(it) {
                "최신 순" -> {
                    mttBinding.moreSearchTv.text = "최신 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    //moreCarpoolAllData.sortByDescending { mSort -> mSort?.postCreateDate }
                    //mtAdapter?.notifyDataSetChanged()
                    viewModel.sortCarpoolData("최신 순")
                }
                "마감 임박 순" -> {
                    mttBinding.moreSearchTv.text = "마감 임박 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    // 날짜 및 시간 형식 지정
                    /*val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    // 정렬 로직
                    val sortedTargets = moreCarpoolAllData.sortedWith { t1, t2 ->
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
                    moreCarpoolAllData.clear()
                    moreCarpoolAllData.addAll(sortedTargets)*/
                    viewModel.sortCarpoolData("마감 임박 순")
                }
                "낮은 가격 순" -> {
                    mttBinding.moreSearchTv.text = "낮은 가격 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    //moreCarpoolAllData.sortBy { mSort -> mSort?.postCost }
                    //mtAdapter?.notifyDataSetChanged()
                    viewModel.sortCarpoolData("낮은 가격 순")
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
            val chipGroup = mttBinding.moreAddFilterBtnSg
            tempFilterPostData.clear()
            when (temp[3]) {
                "등교" -> {
                    chipList.add(createNewChip(
                        text = "등교", chipGroup
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
                        text = "하교", chipGroup
                    ))
                }
                else -> {
                    // 다른 경우 처리
                }
            }

            when (temp[4]) {
                "남성" -> {
                    chipList.add(createNewChip(
                        text = "남성", chipGroup
                    ))
                }
                "여성" -> {
                    chipList.add(createNewChip(
                        text = "여성", chipGroup
                    ))
                }
                else -> {

                }
            }

            when (temp[5]) {
                "흡연O" -> {
                    chipList.add(createNewChip(
                        text = "흡연O", chipGroup
                    ))
                }
                "흡연x" -> {
                    chipList.add(createNewChip(
                        text = "흡연X", chipGroup
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
                            //Log.e("Unknown condition", "Index $i not handled")
                        }
                    }
                }
                val tempData: List<PostData?>?
                if (noConditionPeople > 0) {
                    // 인원수가 0보다 큰 경우, 모든 조건을 적용하여 필터링
                    tempData = viewModel.moreAreaPostData.value.filter { item ->
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
                    tempData = viewModel.moreAreaPostData.value.filter { item ->
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
                /*//dszcctempFilterPostData.addAll(tempData)
                Log.d("filterArea", tempData.toString())
                //moreCarpoolAllData.clear()
                Log.d("filterArea", tempFilterPostData.toString())
                mtAdapter!!.moreTaxiData = tempFilterPostData
                mtAdapter?.notifyDataSetChanged()*/

                viewModel.setAreaPostData(tempFilterPostData)


                /*withContext(Dispatchers.Main) {
                    if (tempFilterPostData.isEmpty()) {
                        mttBinding.moreNonfilterTv.visibility = View.VISIBLE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.GONE
                    } else {
                        mttBinding.moreNonfilterTv.visibility = View.GONE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
                        // UI 조작
                        mtAdapter?.notifyDataSetChanged()
                    }
                }*/
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
    }

    private fun updateUI(items : List<PostData?>) {
        // 게시글 유무에 따른 UI 처리
        if (items.isEmpty()) {
            mttBinding.moreNonfilterTv.text = "등록된 지역 게시글이 존재하지 않습니다"
            mttBinding.moreNonfilterTv.visibility = View.VISIBLE
            mttBinding.moreRefreshSwipeLayout.visibility = View.GONE
        } else {
            mttBinding.moreNonfilterTv.text = "검색된 게시글이 없습니다"
            mttBinding.moreNonfilterTv.visibility = View.GONE
            mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
        }
    }

    /*private fun setSelectData() {
        RetrofitServerConnect.create(this@MoreAreaActivity).getActivityLocation("createDate,desc", 0, 5).enqueue(object :
            Callback<PostReadAllResponse> {
            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    moreAreaData.clear()
                    if (responseData != null) {
                        totalPages = responseData.totalPages

                        for (i in responseData.content.filter { it.isDeleteYN == "N" && it.postType == "BEFORE_DEADLINE" }.indices) {
                            //println(response!!.body()!!.content[i].user.studentId)
                            moreAreaData.add(PostData(
                                responseData.content[i].user.studentId,
                                responseData.content[i].postId,
                                responseData.content[i].title,
                                responseData.content[i].content,
                                responseData.content[i].createDate,
                                responseData.content[i].targetDate,
                                responseData.content[i].targetTime,
                                responseData.content[i].category.categoryName,
                                responseData.content[i].location,
                                //participantscount가 현재 참여하는 인원들
                                responseData.content[i].participantsCount,
                                //numberOfPassengers은 총 탑승자 수
                                responseData.content[i].numberOfPassengers,
                                responseData.content[i].cost,
                                responseData.content[i].verifyGoReturn,
                                responseData.content[i].user,
                                responseData.content[i].latitude,
                                responseData.content[i].longitude
                                ))
                        }

                        //currentData.addAll(moreAreaData.take(5))
                        *//*mtAdapter!!.moreTaxiData = moreAreaData
                        mtAdapter!!.notifyDataSetChanged()*//*
                        mtAdapter?.updateDataList(moreAreaData)
                    }

                    if (getBottomData.isNotEmpty()) {
                        myViewModel.postCheckFilter(getBottomData)
                    } else if (getBottomSheetData.isNotEmpty()) {
                        myViewModel.postCheckSearchFilter(getBottomSheetData)
                    } else if (getBottomData.isNotEmpty() && getBottomSheetData.isNotEmpty()) {
                        myViewModel.postCheckFilter(getBottomData)
                        myViewModel.postCheckSearchFilter(getBottomSheetData)
                    } else {
                        // 새로 고침 완료 후 터치 활성화
                        mttBinding.moreRefreshSwipeLayout.isRefreshing = false
                        this@MoreAreaActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }

                    //tempFilterPostData = moreCarpoolAllData

                    //moreTempCarpoolAllData.addAll(moreCarpoolAllData)
                    if (moreAreaData.isEmpty()) {
                        mttBinding.moreNonfilterTv.text = "등록된 지역 게시글이 존재하지 않습니다"
                        mttBinding.moreNonfilterTv.visibility = View.VISIBLE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.GONE
                    } else {
                        mttBinding.moreNonfilterTv.text = "검색된 게시글이 없습니다"
                        mttBinding.moreNonfilterTv.visibility = View.GONE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
                    }

                } else {
                    Log.d("f", response.code().toString())
                    Toast.makeText(this@MoreAreaActivity, "지역 게시글을 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                Log.d("error", t.toString())
                Toast.makeText(this@MoreAreaActivity, "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }*/
    private fun createNewChip(text: String, parent:ViewGroup): Chip {
        val chip = layoutInflater.inflate(R.layout.notice_board_chip_layout, parent, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }

    private fun initRecyclerView() {
        //setSelectData()
        viewModel.getMoreCarpoolData(this@MoreAreaActivity/*, getBottomData, getBottomSheetData*/) {
            applyFiltersAndSorting()
        }
        mtAdapter = MoreTaxiTabAdapter()
        //mtAdapter!!.moreTaxiData = moreCarpoolAllData
        mttBinding.moreTaxiTabRv.adapter = mtAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        mttBinding.moreTaxiTabRv.setHasFixedSize(true)
        mttBinding.moreTaxiTabRv.layoutManager = manager
    }

    private fun initSwipeRefresh() {
        mttBinding.moreRefreshSwipeLayout.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // 데이터 새로 고침
            refreshData()

            /* // 새로 고침 완료 및 터치 가능하게 설정
             mttBinding.moreRefreshSwipeLayout.isRefreshing = false
             this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/

            // 스크롤 리스너 초기화
            //initScrollListener()
        }
    }

    private fun refreshData() {
        // 데이터 로딩 중으로 설정
        //viewModel.setLoading(true)
        viewModel.setLoading(false)

        // 페이지와 데이터 초기화
        viewModel.setCurrentPages(0) // 페이지를 초기화
        viewModel.setAreaPostData(emptyList()) // 기존 데이터 클리어

        // 새 데이터를 요청 (1페이지부터 다시 불러옴)
        viewModel.getMoreCarpoolData(this@MoreAreaActivity/*, getBottomData, getBottomSheetData*/) {
            applyFiltersAndSorting()
        }
        mttBinding.moreFilterTv.setTextColor(ContextCompat.getColor(this@MoreAreaActivity ,R.color.mio_gray_8))
        mttBinding.moreFilterBtn.setImageResource(R.drawable.filter_icon)
        mttBinding.filterResetLl.visibility = View.GONE
        mttBinding.moreAddFilterBtnSg.removeAllViewsInLayout()
        getBottomData = ""
        chipList.clear()

        // 로딩 완료 후 터치 가능하도록 설정
        mttBinding.moreRefreshSwipeLayout.isRefreshing = false
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        // 스크롤 리스너 재설정
        //initScrollListener()
    }


    private fun initScrollListener() {
        mttBinding.moreTaxiTabRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                /*val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val lastVisibleItemPosition = layoutManager?.findLastCompletelyVisibleItemPosition() ?: -1
                val itemTotalCount = recyclerView.adapter?.itemCount ?: 0*/

                /*// 스크롤이 끝에 도달했는지 확인하고 isLoading 상태 확인
                if (lastVisibleItemPosition >= itemTotalCount - 1 && !isLoading) {
                    if (currentPage < totalPages - 1) {
                        isLoading = true // Set isLoading to true to prevent multiple calls

                        // Add a placeholder for the loading item
                        val runnable = Runnable {
                            moreCarpoolAllData.add(null)
                            mtAdapter?.notifyItemInserted(moreCarpoolAllData.size - 1)
                        }
                        mttBinding.moreTaxiTabRv.post(runnable)

                        // Load more items
                        //getMoreItem()
                        viewModel.getMoreCarpoolData(this@MoreCarpoolTabActivity)
                    }
                }*/
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                layoutManager?.let {
                    val lastVisibleItemPosition = it.findLastCompletelyVisibleItemPosition()
                    val totalItemCount = recyclerView.adapter?.itemCount ?: 0

                    // 스크롤이 끝에 도달했는지 확인
                    if (lastVisibleItemPosition >= totalItemCount - 1 && !viewModel.isLoading.value) {
                        // 데이터 변경 작업을 post로 다음 프레임으로 지연 처리
                        recyclerView.post {
                            viewModel.getMoreCarpoolData(this@MoreAreaActivity/*, getBottomData, getBottomSheetData*/) { // 스크롤이 끝에 도달하면 더 많은 데이터를 요청
                                applyFiltersAndSorting()
                            }
                        }
                    }
                }
            }
        })
    }

    /*private fun getMoreItem() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // Remove the loading item placeholder
            val loadingPosition = moreAreaData.indexOf(null)
            if (loadingPosition != -1) {
                moreAreaData.removeAt(loadingPosition)
                mtAdapter?.notifyItemRemoved(loadingPosition)
            }

            // Fetch more data if necessary
            if (currentPage < totalPages - 1) {
                currentPage += 1
                RetrofitServerConnect.create(this@MoreAreaActivity).getActivityLocation("createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
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

                                moreAreaData.addAll(newItems)
                                if (getBottomData.isNotEmpty()) {
                                    myViewModel.postCheckFilter(getBottomData)
                                }

                                if (getBottomSheetData.isNotEmpty()) {
                                    myViewModel.postCheckSearchFilter(getBottomSheetData)
                                }
                            }
                        }
                        isLoading = false
                    }

                    override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                        isLoading = false
                    }
                })

            }
        }, 2000)
    }*/

    private fun applyFiltersAndSorting() {
        when {
            getBottomData.isNotEmpty() -> myViewModel.postCheckFilter(getBottomData)
            getBottomSheetData.isNotEmpty() -> myViewModel.postCheckSearchFilter(getBottomSheetData)
            getBottomData.isNotEmpty() && getBottomSheetData.isNotEmpty() -> {
                myViewModel.postCheckFilter(getBottomData)
                myViewModel.postCheckSearchFilter(getBottomSheetData)
            }
            else -> {
                // 조건이 없을 때 기본 정렬(필요한 경우)
                viewModel.sortCarpoolData("최신 순") // 기본 정렬을 적용
            }
        }
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            RESULT_OK -> {
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        CoroutineScope(Dispatchers.IO).launch {

                        }
                    }
                    //edit
                    1 -> {

                    }

                    //delte
                    2 -> {
                        /*CoroutineScope(Dispatchers.IO).launch {
                            //setSelectData()
                        }*/
                    }

                }
            }
        }
    }
}