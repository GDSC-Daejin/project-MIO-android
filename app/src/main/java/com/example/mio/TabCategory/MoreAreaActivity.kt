package com.example.mio.TabCategory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Adapter.MoreTaxiTabAdapter
import com.example.mio.BottomSheetFragment.AnotherBottomSheetFragment
import com.example.mio.BottomSheetFragment.BottomSheetFragment
import com.example.mio.MainActivity
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.Model.PostData
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.Model.SharedViewModel
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.R
import com.example.mio.RetrofitServerConnect
import com.example.mio.databinding.ActivityMoreAreaBinding
import com.example.mio.databinding.ActivityMoreCarpoolBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MoreAreaActivity : AppCompatActivity() {
    private lateinit var mttBinding : ActivityMoreAreaBinding
    private lateinit var myViewModel : SharedViewModel
    private var myArea : String? = null
    private var mtAdapter : MoreTaxiTabAdapter? = null
    private var getBottomSheetData = ""
    private var getBottomData = ""
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    private var moreAreaData = ArrayList<PostData?>()
    //필터 리셋시 사용
    //private var moreTempCarpoolAllData =  ArrayList<PostData?>()
    private var dataPosition = 0
    private var date = ""
    //var tempFilterPostData : ArrayList<PostData?> = ArrayList()
    private var tempFilterPostData : ArrayList<PostData?> = ArrayList()

    //칩 생성
    private var chipList = kotlin.collections.ArrayList<Chip>()

    //로딩 즉 item의 끝이며 스크롤의 끝인지
    private var isLoading = false
    //데이터의 현재 페이지 수
    private var currentPage = 0
    //데이터의 전체 페이지 수
    private var totalPages = 0

    private var currentData :  ArrayList<PostData?> = ArrayList()

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

        mttBinding.filterResetLl.setOnClickListener {//필터리셋
            mttBinding.moreFilterTv.setTextColor(ContextCompat.getColor(this@MoreAreaActivity ,R.color.mio_gray_8))
            mttBinding.moreFilterBtn.setImageResource(R.drawable.filter_icon)
            mttBinding.moreNonfilterTv.visibility = View.GONE
            mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
            mttBinding.filterResetLl.visibility = View.GONE
            mttBinding.moreAddFilterBtnSg.removeAllViewsInLayout()
            chipList.clear()
            CoroutineScope(Dispatchers.IO).launch {
                setSelectData()
            }
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
                    val temp = moreAreaData[position]
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
                    moreAreaData.sortByDescending { it?.postCreateDate }
                    mtAdapter?.notifyDataSetChanged()
                }
                "마감 임박 순" -> {
                    mttBinding.moreSearchTv.text = "마감 임박 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    // 리스트를 날짜(date) 먼저, 시간(time) 다음으로 정렬
                    moreAreaData.sortedWith(compareBy<PostData?> { sdf.parse(it?.postTargetDate + " " + it?.postTargetTime) }
                        .thenBy { it?.postTargetTime })
                    mtAdapter?.notifyDataSetChanged()
                }
                "낮은 가격 순" -> {
                    mttBinding.moreSearchTv.text = "낮은 가격 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    moreAreaData.sortBy { it?.postCost }
                    mtAdapter?.notifyDataSetChanged()
                }
            }
        }

        myViewModel.checkFilter.observe(this) { it ->
            //"${selectTargetDate} ${selectTime} ${participateNumberOfPeople} ${isCheckSchool} ${isCheckGender} ${isCheckSmoke} $isReset"
            println("it$it")
            val temp = it.split(",")
            Log.e("temp Filter Test", temp.toString())
            tempFilterPostData.clear()
            when (temp[3]) {
                "등교" -> {
                    chipList.add(createNewChip(
                        text = "등교"
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
                        text = "하교"
                    ))
                }
                else -> {
                    // 다른 경우 처리
                }
            }

            when (temp[4]) {
                "남성" -> {
                    chipList.add(createNewChip(
                        text = "남성"
                    ))
                }
                "여성" -> {
                    chipList.add(createNewChip(
                        text = "여성"
                    ))
                }
                else -> {

                }
            }

            when (temp[5]) {
                "흡연O" -> {
                    chipList.add(createNewChip(
                        text = "흡연O"
                    ))
                }
                "흡연x" -> {
                    chipList.add(createNewChip(
                        text = "흡연X"
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
                    Log.e("currentCondition", currentCondition.toString())
                    when (i) {
                        0 -> { // 날짜
                            if (currentCondition.isNotEmpty()) {
                                noConditionDate = currentCondition
                                Log.d("condition0", noConditionDate)
                            } else {
                                Log.e("No condition0", "empty")
                            }
                        }
                        1 -> { // 시간
                            if (currentCondition.isNotEmpty()) {
                                noConditionTime = currentCondition
                                Log.d("condition1", noConditionTime)
                            } else {
                                Log.e("No condition1", "empty")
                            }
                        }
                        2 -> { // 인원수
                            if (currentCondition.isNotEmpty()) {
                                noConditionPeople = currentCondition.toInt()
                                Log.d("condition2", noConditionPeople.toString())
                            } else {
                                Log.e("No condition2", "empty")
                            }
                        }
                        3 -> { // 등하교
                            if (currentCondition.isNotEmpty()) {
                                if (currentCondition == "등교") {
                                    noConditionSchool = true
                                } else if (currentCondition == "하교") {
                                    noConditionSchool = false
                                }
                                Log.d("condition3", noConditionSchool.toString())
                            } else {
                                Log.e("No condition3", "empty")
                            }
                        }
                        4 -> { // 성별
                            if (currentCondition.isNotEmpty()) {
                                if (currentCondition == "여성") {
                                    noConditionGender = true
                                } else if (currentCondition == "남성") {
                                    noConditionGender = false
                                }
                                Log.d("condition4", noConditionGender.toString())
                            } else {
                                Log.e("No condition4", "empty")
                            }
                        }
                        5 -> { // 흡연여부
                            if (currentCondition.isNotEmpty()) {
                                if (temp[5] == "흡연O") {
                                    noConditionSmoke = true
                                } else if (temp[5] == "흡연x") {
                                    noConditionSmoke = false
                                }
                                Log.d("condition5", noConditionSmoke.toString())
                            } else {
                                Log.e("No condition5", "empty")
                            }
                        }
                        else -> {
                            // 추가 조건이 있는 경우 여기에 추가할 수 있음
                            Log.e("Unknown condition", "Index $i not handled")
                        }
                    }
                }
                Log.d("filterArea", "$noConditionDate $noConditionTime $noConditionPeople $noConditionSchool $noConditionGender $noConditionSmoke")
                var tempData: List<PostData?>? = null
                if (noConditionPeople > 0) {
                    // 인원수가 0보다 큰 경우, 모든 조건을 적용하여 필터링
                    tempData = moreAreaData.filter { item ->
                        item != null &&
                                (noConditionDate.isEmpty() || item.postTargetDate == noConditionDate) &&
                                (noConditionTime.isEmpty() || item.postTargetTime == noConditionTime) &&
                                (noConditionPeople == -1 || item.postParticipationTotal == noConditionPeople) &&
                                (noConditionSchool == null || item.postVerifyGoReturn == noConditionSchool) &&
                                (noConditionGender == null || item.user.gender == noConditionGender) &&
                                (noConditionSmoke == null || item.user.verifySmoker == noConditionSmoke)
                    }
                } else {
                    // 인원수가 0인 경우, 날짜로만 필터링
                    tempData = moreAreaData.filter { item ->
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
                Log.d("filterArea", tempData.toString())
                //moreCarpoolAllData.clear()
                Log.d("filterArea", tempFilterPostData.toString())
                mtAdapter!!.moreTaxiData = tempFilterPostData


                withContext(Dispatchers.Main) {
                    if (tempFilterPostData.isEmpty()) {
                        mttBinding.moreNonfilterTv.visibility = View.VISIBLE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.GONE
                    } else {
                        mttBinding.moreNonfilterTv.visibility = View.GONE
                        mttBinding.moreRefreshSwipeLayout.visibility = View.VISIBLE
                        // UI 조작
                        mtAdapter!!.notifyDataSetChanged()
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

    }
    private fun setSelectData() {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getLocationPostData(myArea!!).enqueue(object :
                Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()!!
                        //println(response.body()!!.content)
                        /*val start = SystemClock.elapsedRealtime()

                        // 함수 실행시간
                        val date = Date(start)
                        val mFormat = SimpleDateFormat("HH:mm:ss")
                        val time = mFormat.format(date)
                        println(start)
                        println(time)*/
                        /*val s : ArrayList<PostReadAllResponse> = ArrayList()
                        s.add(PostReadAllResponse())*/
                        moreAreaData.clear()
                        if (responseData.isNotEmpty()) {
                            totalPages = if (moreAreaData.size % 5 == 0) {
                                moreAreaData.size / 5
                            } else {
                                moreAreaData.size / 5 + 1
                            }
                            for (i in responseData.indices) {
                                //탑승자 null체크
                                var part = 0
                                var location = ""
                                var title = ""
                                var content = ""
                                var targetDate = ""
                                var targetTime = ""
                                var categoryName = ""
                                var cost = 0
                                var verifyGoReturn = false
                                if (response.isSuccessful) {
                                    part = try {
                                        /*response.body()!!.content[i].participants.isEmpty()
                                        response.body()!!.content[i].participants.size*/
                                        response.body()!![i].participantsCount
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        0
                                    }
                                    location = try {
                                        response.body()!![i].location.isEmpty()
                                        response.body()!![i].location
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        "수락산역 3번 출구"
                                    }
                                    title = try {
                                        response.body()!![i].title.isEmpty()
                                        response.body()!![i].title
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        "null"
                                    }
                                    content = try {
                                        response.body()!![i].content.isEmpty()
                                        response.body()!![i].content
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        "null"
                                    }
                                    targetDate = try {
                                        response.body()!![i].targetDate.isEmpty()
                                        response.body()!![i].targetDate
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        "null"
                                    }
                                    targetTime = try {
                                        response.body()!![i].targetTime.isEmpty()
                                        response.body()!![i].targetTime
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        "null"
                                    }
                                    categoryName = try {
                                        response.body()!![i].category?.categoryName?.isEmpty()
                                        response.body()!![i].category?.categoryName!!
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        "null"
                                    }
                                    cost = try {
                                        response.body()!![i].cost
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        0
                                    }
                                    verifyGoReturn = try {
                                        response.body()!![i].verifyGoReturn
                                    } catch (e : java.lang.NullPointerException) {
                                        Log.d("null", e.toString())
                                        false
                                    }
                                }

                                //println(response!!.body()!!.content[i].user.studentId)
                                moreAreaData.add(PostData(
                                    response.body()!![i].user?.studentId!!,
                                    response.body()!![i].postId,
                                    title,
                                    content,
                                    response.body()!![i].createDate,
                                    targetDate,
                                    targetTime,
                                    categoryName,
                                    location,
                                    //participantscount가 현재 참여하는 인원들
                                    part,
                                    //numberOfPassengers은 총 탑승자 수
                                    response.body()!![i].numberOfPassengers,
                                    cost,
                                    verifyGoReturn,
                                    response.body()!![i].user!!,
                                    response.body()!![i].latitude,
                                    response.body()!![i].longitude
                                ))
                            }

                            currentData.addAll(moreAreaData.take(5))
                            mtAdapter!!.moreTaxiData = currentData
                            mtAdapter!!.notifyDataSetChanged()
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
                    }
                }

                override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }
    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.notice_board_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }

    private fun initRecyclerView() {
        setSelectData()
        mtAdapter = MoreTaxiTabAdapter()
        mtAdapter!!.moreTaxiData = moreAreaData
        mttBinding.moreTaxiTabRv.adapter = mtAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        mttBinding.moreTaxiTabRv.setHasFixedSize(true)
        mttBinding.moreTaxiTabRv.layoutManager = manager
    }

    private fun initSwipeRefresh() {
        mttBinding.moreRefreshSwipeLayout.setOnRefreshListener {
            //새로고침 시 터치불가능하도록
            this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 화면 터치 못하게 하기
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                setSelectData()
                mtAdapter!!.moreTaxiData = moreAreaData
                //noticeBoardAdapter.recyclerView.startLayoutAnimation()
                mttBinding.moreRefreshSwipeLayout.isRefreshing = false
                mtAdapter!!.notifyDataSetChanged()
                this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
            //터치불가능 해제ss
            //activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun initScrollListener(){
        mttBinding.moreTaxiTabRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (currentPage < totalPages - 1) {
                    if(!isLoading){
                        if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == moreAreaData.size - 1){
                            Log.e("true", "True")
                            getMoreItem()
                            isLoading =  true
                        }
                    }
                } else {
                    isLoading = false
                }
            }
        })
    }

    private fun getMoreItem() {
        val runnable = kotlinx.coroutines.Runnable {
            moreAreaData.add(null)
            mtAdapter?.notifyItemInserted(moreAreaData.size - 1)
        }
        mttBinding.moreTaxiTabRv.post(runnable)
        //null을 감지 했으니
        //이 부분에 프로그래스바가 들어올거라 알림
        //mttBinding.moreTaxiTabRv.adapter!!.notifyItemInserted(moreCarpoolAllData.size-1)
        //성공/

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(java.lang.Runnable {
            //null추가한 거 삭제
            moreAreaData.removeAt(moreAreaData.size - 1)
            mtAdapter?.notifyItemRemoved(moreAreaData.size)
            //data.clear()

            //page수가 totalpages 보다 작거나 같다면 데이터 더 가져오기 가능
            if (currentPage < totalPages - 1) {
                currentPage += 1

                CoroutineScope(Dispatchers.IO).launch {
                    val start = currentPage * 5
                    val end = minOf(start + 5, moreAreaData.size)
                    val nextData = moreAreaData.subList(start, end)

                    withContext(Dispatchers.Main) {
                        mtAdapter!!.addMoreData(nextData)
                    }
                }
            }

            //println(moreCarpoolAllData)
            isLoading = false
        }, 2000)
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
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
                        CoroutineScope(Dispatchers.IO).launch {
                            setSelectData()
                        }
                    }

                }
            }
        }
    }
}