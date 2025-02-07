package com.gdsc.mio.tabcategory

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.gdsc.mio.*
import com.gdsc.mio.adapter.CalendarAdapter
import com.gdsc.mio.adapter.CurrentNoticeBoardAdapter
import com.gdsc.mio.adapter.NoticeBoardAdapter
import com.gdsc.mio.adapter.NoticeBoardMyAreaAdapter
import com.gdsc.mio.databinding.FragmentTaxiTabBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.*
import com.gdsc.mio.noticeboard.NoticeBoardEditActivity
import com.gdsc.mio.noticeboard.NoticeBoardReadActivity
import com.gdsc.mio.viewmodel.CurrentDataViewModel
import com.gdsc.mio.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TaxiTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaxiTabFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var taxiTabBinding: FragmentTaxiTabBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var areaManager : LinearLayoutManager = LinearLayoutManager(activity)
    private var horizonManager : LinearLayoutManager = LinearLayoutManager(activity)
    private var horizonManager2 : LinearLayoutManager = LinearLayoutManager(activity)
    private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private var currentNoticeBoardAdapter : CurrentNoticeBoardAdapter? = null
    private var noticeBoardMyAreaAdapter : NoticeBoardMyAreaAdapter? = null

    //나의 활동 지역
    private var myAreaItemData = ArrayList<Content?>()


    //캘린더
    private var calendarAdapter : CalendarAdapter? = null
    private var calendarItemData : MutableList<DateData?> = mutableListOf()

    //게시글 전체 데이터 및 adapter와 공유하는 데이터
    private var taxiAllData : ArrayList<PostData> = ArrayList()
    private var currentTaxiAllData = ArrayList<PostData>()
    private var taxiParticipantsData = ArrayList<ArrayList<ParticipationData>?>()
    //게시글 선택 시 위치를 잠시 저장하는 변수
    private var dataPosition = 0

    //게시글과 targetDate를 받아 viewmodel에저장
    private var sharedViewModel: SharedViewModel? = null
    private var calendarTempData = ArrayList<String>()
    private var selectCalendarTaxiData : ArrayList<PostData> = ArrayList()
    //edit에서 받은 값
    private var testselectCalendarData = HashMap<String, ArrayList<PostData>>()

    private var isFirst = true

    private lateinit var viewModel : CurrentDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        taxiTabBinding = FragmentTaxiTabBinding.inflate(inflater, container, false)

        initNoticeBoardRecyclerView()

        initCalendarRecyclerView()
        //initSwipeRefresh()

        initCurrentNoticeBoardRecyclerView()
        initMyAreaRecyclerView()



        //recyclerview item클릭 시
        noticeBoardAdapter!!.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = taxiAllData[position]
                    dataPosition = position
                    val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp.user.profileImageUrl)
                        putExtra("tabType", "택시")
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        currentNoticeBoardAdapter!!.setItemClickListener(object : CurrentNoticeBoardAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int, status : CurrentNoticeBoardAdapter.PostStatus?) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = currentTaxiAllData[position]
                    var intent : Intent? = null
                    dataPosition = position
                    when(status) {
                        CurrentNoticeBoardAdapter.PostStatus.Passenger -> {//내가 손님으로 카풀이 완료되었을 떄
                            intent = Intent(activity, CompleteActivity::class.java).apply {
                                putExtra("type", "PASSENGER")
                                putExtra("postDriver", temp.user)
                                putExtra("postData", currentTaxiAllData[position])
                                putExtra("category", "taxi")
                            }
                            //sendAlarmData("PASSENGER", position, currentTaxiAllData[position])
                            //patchVerifyFinish(temp.postID)
                        }

                        CurrentNoticeBoardAdapter.PostStatus.Driver -> { //내가 운전자로 카풀이 완료되었을 떄
                            intent = Intent(activity, CompleteActivity::class.java).apply {
                                putExtra("type", "DRIVER")
                                putExtra("postData", currentTaxiAllData[position])
                                putExtra("category", "taxi")
                            }
                            //sendAlarmData("DRIVER", position, currentTaxiAllData[position])
                            //patchVerifyFinish(temp.postID)
                        }

                        CurrentNoticeBoardAdapter.PostStatus.Neither -> {
                            intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                putExtra("type", "READ")
                                putExtra("postItem", temp)
                                putExtra("uri", temp.user.profileImageUrl)
                                putExtra("tabType", "택시")
                            }
                        }
                        else -> {

                        }
                    }
                    requestActivity.launch(intent)
                }
            }
        })


        calendarAdapter?.setItemClickListener(object : CalendarAdapter.ItemClickListener {
            //여기서 position = 0시작은 date가 되야함 itemId=1로 시작함
            override fun onClick(view: View, position: Int, itemId: String) {
                setData(itemId)
                /*if (taxiAllData.isNotEmpty()) {
                    try {
                        val selectDateData = taxiAllData.filter { it.postTargetDate == itemId }

                        if (selectDateData.isNotEmpty()) {
                            selectCalendarTaxiData.clear()

                            *//* for (i in selectDateData.indices) {
                                 calendarTaxiAllData.add(selectDateData[i])
                             }*//*
                            for (select in selectDateData) {
                                selectCalendarTaxiData.add(select)
                            }

                            //noticeBoardAdapter = NoticeBoardAdapter()
                            noticeBoardAdapter!!.postItemData = selectCalendarTaxiData
                            taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                            taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                            *//* noticeBoardAdapter?.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
                                 override fun onClick(view: View, position: Int, itemId: Int) {
                                     CoroutineScope(Dispatchers.IO).launch {
                                         val temp = taxiAllData[position]
                                         dataPosition = position
                                         val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                             putExtra("type", "READ")
                                             putExtra("postItem", temp)
                                             putExtra("uri", temp.user.profileImageUrl)
                                         }
                                         requestActivity.launch(intent)
                                     }
                                 }
                             })*//*


                        } else {
                            taxiTabBinding.noticeBoardRV.visibility = View.GONE
                            taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                        }

                    } catch (e: java.lang.IndexOutOfBoundsException) {
                        Log.e("current taxi", e.toString())
                    }
                } else {
                    Toast.makeText(requireContext(), "선택된 날의 게시글이 없거나 최신글이 아닙니다. 더보기를 통해 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
                *//* calendarAdapter!!.notifyItemChanged(selectedPostion)
                calendarAdapter!!.notifyItemChanged(oldSelectedPostion)*//*

                noticeBoardAdapter!!.notifyDataSetChanged()*/
            }
        })

        /*//월 클릭 시 월에 들어있는 모든 데이터
        taxiTabBinding.monthTv.setOnClickListener {
            for ((key, value) in testselectCalendarData) {
                println("전체 : ${key} : ${value}")
            }

            noticeBoardAdapter!!.postItemData = taxiAllData
            noticeBoardAdapter!!.notifyDataSetChanged()
        }*/


        taxiTabBinding.moreBtn.setOnClickListener {
            val intent = Intent(activity, MoreTaxiTabActivity::class.java).apply {
                //putExtra("type", "MoreADD")
                putExtra("type", "DATE")
                putExtra("date", LocalDate.now().monthValue.toString())
            }
            requestActivity.launch(intent)
        }

        taxiTabBinding.carpoolBannerIv.setOnClickListener {
            val intent = Intent(requireActivity(), NoticeBoardEditActivity::class.java).apply {
                putExtra("type","ADD")
            }
            requestActivity.launch(intent)
        }

        taxiTabBinding.moreAreaBtn.setOnClickListener {
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            val myAreaData = saveSharedPreferenceGoogleLogin.getSharedArea(requireActivity()).toString()
            val intent = Intent(requireActivity(), MoreAreaActivity::class.java).apply {
                putExtra("area", myAreaData)
            }
            requestActivity.launch(intent)
        }

        noticeBoardMyAreaAdapter!!.setItemClickListener(object : NoticeBoardMyAreaAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                val temp = myAreaItemData[position]
                if (temp != null) {
                    val tempPostData = PostData(
                        temp.user.studentId,
                        temp.postId,
                        temp.title,
                        temp.content,
                        temp.createDate,
                        temp.targetDate,
                        temp.targetTime,
                        temp.category.categoryName,
                        temp.location,
                        temp.participantsCount,
                        temp.numberOfPassengers,
                        temp.cost,
                        temp.verifyGoReturn,
                        temp.user,
                        temp.latitude,
                        temp.longitude
                    )
                    dataPosition = position
                    val intent = Intent(requireActivity(), NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", tempPostData)
                    }
                    requestActivity.launch(intent)
                }
            }
        })


        return taxiTabBinding.root
    }

    private fun initNoticeBoardRecyclerView() {
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        //저장된 거 가져옴
        val editObserver = androidx.lifecycle.Observer<HashMap<String, ArrayList<PostData>>> { textValue ->
            testselectCalendarData = textValue
        }
        sharedViewModel!!.getCalendarLiveData().observe(viewLifecycleOwner, editObserver)

        LoadingProgressDialogManager.show(requireContext())

        if (isFirst) {
            setData(LocalDate.now().toString())
        }


        noticeBoardAdapter = NoticeBoardAdapter()
        noticeBoardAdapter!!.postItemData = taxiAllData
        taxiTabBinding.noticeBoardRV.adapter = noticeBoardAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        taxiTabBinding.noticeBoardRV.setHasFixedSize(true)
        taxiTabBinding.noticeBoardRV.layoutManager = manager
        /*
        taxiTabBinding.noticeBoardRV.itemAnimator =  SlideInUpAnimator(OvershootInterpolator(1f))
        taxiTabBinding.noticeBoardRV.itemAnimator?.apply {
            addDuration = 1000
            removeDuration = 100
            moveDuration = 1000
            changeDuration = 100
        }*/
    }

    private fun initMyAreaRecyclerView() {
        setMyAreaData()

        noticeBoardMyAreaAdapter = NoticeBoardMyAreaAdapter()
        noticeBoardMyAreaAdapter!!.postAreaItemData = myAreaItemData
        taxiTabBinding.myAreaOfActivityRV.adapter = noticeBoardMyAreaAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        taxiTabBinding.myAreaOfActivityRV.setHasFixedSize(true)
        taxiTabBinding.myAreaOfActivityRV.layoutManager = areaManager

        /*taxiTabBinding.noticeBoardRV.itemAnimator =  SlideInUpAnimator(OvershootInterpolator(1f))
        taxiTabBinding.noticeBoardRV.itemAnimator?.apply {
            addDuration = 1000
            removeDuration = 100
            moveDuration = 1000
            changeDuration = 100
        }*/
    }

    private fun initCurrentNoticeBoardRecyclerView() {
        //로딩창 실행
        /*loadingDialog = LoadingProgressDialog(activity)
        loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()*/

        val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 리사이클러뷰가 스크롤되었을 때 뷰페이저의 스크롤을 막음
                val viewpager = requireActivity().findViewById<ViewPager2>(R.id.viewpager)
                viewpager.isUserInputEnabled = newState == RecyclerView.SCROLL_STATE_IDLE
            }
        }

        // RecyclerView 스크롤 이벤트 리스너 등록
        taxiTabBinding.currentRv.addOnScrollListener(recyclerViewScrollListener)

        //setCurrentTaxiData()
        currentNoticeBoardAdapter = CurrentNoticeBoardAdapter()
        //currentNoticeBoardAdapter!!.currentPostItemData = currentTaxiAllData
        taxiTabBinding.currentRv.adapter = currentNoticeBoardAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        taxiTabBinding.currentRv.setHasFixedSize(true)
        horizonManager2.orientation = LinearLayoutManager.HORIZONTAL
        taxiTabBinding.currentRv.layoutManager = horizonManager2
    }

    private fun initCalendarRecyclerView() {
        // 데이터를 설정 후 어댑터에 할당
        setCalendarData()

        calendarAdapter = CalendarAdapter()
        CalendarUtil.selectedDate = LocalDate.now()

        calendarAdapter!!.calendarItemData = calendarItemData

        // 리사이클러뷰 스크롤 리스너
        val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val viewpager = requireActivity().findViewById<ViewPager2>(R.id.viewpager)
                viewpager.isUserInputEnabled = newState == RecyclerView.SCROLL_STATE_IDLE
            }
        }
        taxiTabBinding.calendarRV.addOnScrollListener(recyclerViewScrollListener)
        taxiTabBinding.calendarRV.adapter = calendarAdapter
        taxiTabBinding.calendarRV.setHasFixedSize(true)

        horizonManager.orientation = LinearLayoutManager.HORIZONTAL
        taxiTabBinding.calendarRV.layoutManager = horizonManager

        // 어댑터 데이터 변경 후 "오늘" 항목을 자동으로 클릭
        if (calendarItemData.isNotEmpty()) {
            val todayPosition = calendarItemData.indexOfFirst { it?.day == "오늘" }
            if (todayPosition >= 0) {
                triggerFirstItemOfCalendarAdapter(todayPosition)
            }
        }
    }

    private fun setCalendarData() {

        //val cal = Calendar.getInstance()
        //cal.set(2023, 5, 1)
        /*cal.add(Calendar.YEAR, LocalDate.now().year)
        cal.add(Calendar.MONTH, LocalDate.now().monthValue)
        cal.add(Calendar.DAY_OF_MONTH, LocalDate.now().dayOfMonth)

         */

        //현재 달의 마지막 날짜
        val localDate = LocalDate.now()
        val yearMonth = YearMonth.from(localDate)
        val lastDayOfMonth = yearMonth.lengthOfMonth()

        //월 설정
        //taxiTabBinding.monthTv.text = LocalDate.now().month.toString()


        //val localDate = LocalDate.parse("${LocalDate.now().year}-${LocalDate.now().monthValue}-${LocalDate.now().dayOfMonth}")
        //현재 날짜
        //val currentDate = LocalDate.now()
        //현재 달의 마지막 날짜
        //val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        //val lastDayOfMonth = localDate.withDayOfMonth(localDate.lengthOfMonth())
        //현재 날짜에서 이번달의 마지막 날짜 까지 ex)오늘 11월20일이면 캘린더 리사이클러뷰에는 20일 부터 30일까지
        for (i in localDate.toString().substring(8..9).toInt()..lastDayOfMonth) {
            val date = LocalDate.of(LocalDate.now().year, LocalDate.now().month, i)
            val dayOfWeek: DayOfWeek = date.dayOfWeek
            val tempDayOfWeek = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            //println("dadad" + tempDayOfWeek.toString().substring(0, 3))
            /*println("날짜" + date)
            println("dayofweek" + dayOfWeek)*/
            //현재 월, 현재 요일, 날짜
            //println(DateData(LocalDate.now().year.toString(), LocalDate.now().month.toString(), dayOfWeek.toString().substring(0, 3), i.toString()))

            if (calendarItemData.isEmpty()) {
                calendarItemData.add(DateData(LocalDate.now().year.toString(), LocalDate.now().monthValue.toString(), "오늘", i.toString()))
            } else {
                calendarItemData.add(DateData(LocalDate.now().year.toString(), LocalDate.now().monthValue.toString(), tempDayOfWeek.toString().substring(0, 1), i.toString()))
            }
        }
        /*val currentDate = localDate.toString().substring(8..9).toInt()
        triggerFirstItemOfCalendarAdapter(currentDate)*/
    }


    private fun setData(targetDate : String) {
        val postsByDate = PostsByDateData(2, targetDate, "BEFORE_DEADLINE")

        RetrofitServerConnect.create(requireContext()).postTargetDatePageList("createDate,desc", 0, 5, postsByDate).enqueue(object : Callback<PostReadAllResponse> {
            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseData ->
                        taxiAllData.clear()

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
                            val targetDate2 = post.targetDate
                            val targetTime = post.targetTime
                            val categoryName = post.category.categoryName
                            val cost = post.cost
                            val verifyGoReturn = post.verifyGoReturn

                            taxiAllData.add(
                                PostData(
                                    post.user.studentId,
                                    post.postId,
                                    title,
                                    content,
                                    post.createDate,
                                    targetDate2,
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
                                )
                            )
                        }

                        // 첫 호출이 아닐 때는 전체 데이터를 반영
                        if (taxiAllData.isEmpty()) {
                            taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                            taxiTabBinding.noticeBoardRV.visibility = View.GONE

                            if (isFirst) {
                                isFirst = false
                            } else {
                                Toast.makeText(requireContext(), "선택하신 날의 게시글이 존재하지 않습니다 더보기를 통해 게시글을 확인해주세요", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                            taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                        }

                        // 어댑터에 전체 데이터를 적용
                        noticeBoardAdapter?.let { adapter ->
                            adapter.postItemData = taxiAllData
                            adapter.notifyDataSetChanged()
                        }

                        LoadingProgressDialogManager.hide()
                    }
                } else {
                    LoadingProgressDialogManager.hide()
                    if (taxiAllData.isEmpty()) {
                        taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                        taxiTabBinding.noticeBoardRV.visibility = View.GONE
                    } else {
                        taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                        taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                if (taxiAllData.isEmpty()) {
                    taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                    taxiTabBinding.noticeBoardRV.visibility = View.GONE
                } else {
                    taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                    taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                }
                Toast.makeText(requireContext(), "예상치 못한 오류가 발생했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setMyAreaData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val myAreaData = saveSharedPreferenceGoogleLogin.getSharedArea(requireActivity()).toString()

        if (myAreaData.isEmpty() || myAreaData == "") {
            CoroutineScope(Dispatchers.Main).launch {
                taxiTabBinding.nonAreaRvTv.text = "계정에 등록된 활동 지역이 없습니다!"
                taxiTabBinding.nonAreaRvTv2.text = "계정에서 활동 지역을 등록해 주세요!"

                taxiTabBinding.areaRvLl.visibility = View.GONE
                taxiTabBinding.nonAreaRvTv.visibility = View.VISIBLE
                taxiTabBinding.nonAreaRvTv2.visibility = View.VISIBLE
            }
        } else {
            RetrofitServerConnect.create(requireActivity()).getActivityLocation("createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        if (responseData != null) {
                            for (i in responseData.content.filter { it.isDeleteYN == "N" && it.postType == "BEFORE_DEADLINE" }.indices) {
                                myAreaItemData.add(
                                    Content(
                                        responseData.content[i].postId,
                                        responseData.content[i].title,
                                        responseData.content[i].content,
                                        responseData.content[i].createDate,
                                        responseData.content[i].targetDate,
                                        responseData.content[i].targetTime,
                                        responseData.content[i].category,
                                        responseData.content[i].verifyGoReturn,
                                        responseData.content[i].numberOfPassengers,
                                        responseData.content[i].user,
                                        responseData.content[i].viewCount,
                                        responseData.content[i].verifyFinish,
                                        responseData.content[i].participants,
                                        responseData.content[i].latitude,
                                        responseData.content[i].longitude,
                                        responseData.content[i].bookMarkCount,
                                        responseData.content[i].participantsCount,
                                        responseData.content[i].location,
                                        responseData.content[i].cost,
                                        responseData.content[i].isDeleteYN,
                                        responseData.content[i].postType,
                                    )
                                )
                            }

                            noticeBoardMyAreaAdapter!!.notifyDataSetChanged()

                            LoadingProgressDialogManager.hide()
                        }

                        if (myAreaItemData.isNotEmpty()) {
                            taxiTabBinding.areaRvLl.visibility = View.VISIBLE
                            taxiTabBinding.nonAreaRvTv.visibility = View.GONE
                            taxiTabBinding.nonAreaRvTv2.visibility = View.GONE
                        } else {
                            taxiTabBinding.areaRvLl.visibility = View.GONE
                            taxiTabBinding.nonAreaRvTv.visibility = View.VISIBLE
                            taxiTabBinding.nonAreaRvTv2.visibility = View.VISIBLE
                        }

                    } else {
                        if (myAreaItemData.isNotEmpty()) {
                            taxiTabBinding.areaRvLl.visibility = View.VISIBLE
                            taxiTabBinding.nonAreaRvTv.visibility = View.GONE
                            taxiTabBinding.nonAreaRvTv2.visibility = View.GONE
                        } else {
                            taxiTabBinding.areaRvLl.visibility = View.GONE
                            taxiTabBinding.nonAreaRvTv.visibility = View.VISIBLE
                            taxiTabBinding.nonAreaRvTv2.visibility = View.VISIBLE
                        }
                        LoadingProgressDialogManager.hide()
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    LoadingProgressDialogManager.hide()
                    if (myAreaItemData.isNotEmpty()) {
                        taxiTabBinding.areaRvLl.visibility = View.VISIBLE
                        taxiTabBinding.nonAreaRvTv.visibility = View.GONE
                        taxiTabBinding.nonAreaRvTv2.visibility = View.GONE
                    } else {
                        taxiTabBinding.areaRvLl.visibility = View.GONE
                        taxiTabBinding.nonAreaRvTv.visibility = View.VISIBLE
                        taxiTabBinding.nonAreaRvTv2.visibility = View.VISIBLE
                    }
                }
            })
        }


    }

    private fun setCurrentTaxiData() {
        RetrofitServerConnect.create(requireActivity()).getMyParticipantsUserData().enqueue(object : Callback<List<Content>> {
            override fun onResponse(call: Call<List<Content>>, response: Response<List<Content>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    //데이터 청소
                    currentTaxiAllData.clear()

                    if (responseData != null) {
                        for (i in responseData) {
                            if (i.isDeleteYN != "Y") {
                                currentTaxiAllData.add(PostData(
                                    i.user.studentId,
                                    i.postId,
                                    i.title,
                                    i.content,
                                    i.createDate,
                                    i.targetDate,
                                    i.targetTime,
                                    i.category.categoryName,
                                    i.location,
                                    //participantscount가 현재 참여하는 인원들
                                    i.participantsCount,
                                    //numberOfPassengers은 총 탑승자 수
                                    i.numberOfPassengers,
                                    i.cost,
                                    i.verifyGoReturn,
                                    i.user,
                                    i.latitude,
                                    i.longitude
                                ))
                                taxiParticipantsData.add(i.participants)
                            }
                        }
                    }
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    val sortedTargets = currentTaxiAllData.sortedByDescending {
                        // 날짜와 시간을 각각 파싱하고 결합하여 내림차순으로 정렬
                        val targetDate = LocalDate.parse(it.postTargetDate, dateFormatter) // 날짜 파싱
                        val targetTime = LocalTime.parse(it.postTargetTime, timeFormatter) // 시간 파싱
                        targetDate.atTime(targetTime) // 날짜와 시간을 결합하여 정렬 기준 생성
                    }
                    viewModel.setTaxiCurrentData(sortedTargets.toMutableList())

                    if (currentTaxiAllData.isEmpty()) {
                        taxiTabBinding.currentRv.visibility = View.GONE
                        taxiTabBinding.nonCurrentRvTv.text = "예약된 게시글이 없습니다"
                        taxiTabBinding.nonCurrentRvTv2.text = "미오에서 카풀,택시를 구해보세요!"
                        taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                        taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE
                    } else {
                        taxiTabBinding.currentRv.visibility = View.VISIBLE
                        taxiTabBinding.nonCurrentRvTv.visibility = View.GONE
                        taxiTabBinding.nonCurrentRvTv2.visibility = View.GONE
                    }

                    LoadingProgressDialogManager.hide()

                } else {
                    if (response.code().toString() == "500") {
                        if (response.errorBody()?.string() != null) {
                            taxiTabBinding.currentRv.visibility = View.GONE
                            taxiTabBinding.nonCurrentRvTv.text = "예약된 게시글이 없습니다"
                            taxiTabBinding.nonCurrentRvTv2.text = "미오에서 카풀,택시를 구해보세요!"
                            taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                            taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE
                        } else {
                            taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                            taxiTabBinding.nonCurrentRvTv.text = "예상치 못한 오류가 발생했습니다"
                            taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE
                            taxiTabBinding.nonCurrentRvTv2.text = "이곳을 눌러 새로고침 해주세요"

                            taxiTabBinding.nonCurrentRvTv2.setOnClickListener {
                                lifecycleScope.launch {
                                    setCurrentTaxiData()
                                }
                            }
                            taxiTabBinding.carpoolText.setOnClickListener {
                                Log.d("carpoolText", "clcickckckc")
                                lifecycleScope.launch {
                                    setCurrentTaxiData()
                                }
                            }
                        }
                    }

                    if (currentTaxiAllData.isEmpty()) {
                        taxiTabBinding.currentRv.visibility = View.GONE
                        taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                        taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE

                    } else {
                        taxiTabBinding.currentRv.visibility = View.VISIBLE
                        taxiTabBinding.nonCurrentRvTv.visibility = View.GONE
                        taxiTabBinding.nonCurrentRvTv2.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                if (currentTaxiAllData.isEmpty()) {
                    taxiTabBinding.currentRv.visibility = View.GONE
                    taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                    taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE

                } else {
                    taxiTabBinding.currentRv.visibility = View.VISIBLE
                    taxiTabBinding.nonCurrentRvTv.visibility = View.GONE
                    taxiTabBinding.nonCurrentRvTv2.visibility = View.GONE
                }
            }
        })
    }

    //오늘날짜에 선택되게
    private fun triggerFirstItemOfCalendarAdapter(currentDatePos: Int) {
        taxiTabBinding.calendarRV.post {
            taxiTabBinding.calendarRV.findViewHolderForAdapterPosition(currentDatePos)?.itemView?.performClick()
        }
    }


    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val post = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra("postData")
                } else {
                    it.data?.getParcelableExtra("postData", PostData::class.java)
                }
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
                            setCurrentTaxiData()
                            //setData()
                        }
                        //livemodel을 통해 저장
                        //sharedViewModel!!.setCalendarLiveData("add", selectCalendarData)
                    }
                    //edit
                    1 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            taxiAllData[dataPosition] = post!!
                        }
                        noticeBoardAdapter!!.notifyItemChanged(post?.postID!!)
                    }

                    123 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            setCurrentTaxiData()
                        }
                    }

                }


            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[CurrentDataViewModel::class.java]
        setCurrentTaxiData()

        viewModel.currentTaxiLiveData.observe(viewLifecycleOwner) { postDataList ->
            currentTaxiAllData.sortByDescending {item -> item.postCreateDate}
            currentNoticeBoardAdapter?.updateDataList(postDataList.toList())
        }
        /*

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // 로딩 다이얼로그를 생성하고 표시
                loadingDialog = LoadingProgressDialog(requireActivity())
                loadingDialog.setCancelable(false)
                loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                loadingDialog.window?.attributes?.windowAnimations = R.style.FullScreenDialog
                loadingDialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                loadingDialog.show()
            } else {
                // 로딩 다이얼로그를 해제
                loadingDialog.dismiss()
            }
        }*/



        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        //이건 나중에
        val addObserver = androidx.lifecycle.Observer<ArrayList<String>> { textValue ->
            calendarTempData = textValue
        }
        sharedViewModel!!.getLiveData().observe(viewLifecycleOwner, addObserver)


        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        //저장된 거 가져옴
        val editObserver = androidx.lifecycle.Observer<HashMap<String, ArrayList<PostData>>> { textValue ->
            testselectCalendarData = textValue
        }
        sharedViewModel!!.getCalendarLiveData().observe(viewLifecycleOwner, editObserver)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TaxiTabFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TaxiTabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}