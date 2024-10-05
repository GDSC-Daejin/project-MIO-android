package com.example.mio.tabcategory

import com.example.mio.adapter.CurrentCarpoolAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mio.*
import com.example.mio.adapter.*
import com.example.mio.model.*
import com.example.mio.noticeboard.NoticeBoardEditActivity
import com.example.mio.noticeboard.NoticeBoardReadActivity
import com.example.mio.databinding.FragmentCarpoolTabBinding
import com.example.mio.viewmodel.CurrentDataViewModel
import com.example.mio.viewmodel.SharedViewModel
import com.google.android.gms.ads.AdRequest
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
import kotlin.collections.ArrayList
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TaxiTabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarpoolTabFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var taxiTabBinding: FragmentCarpoolTabBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var areaManager : LinearLayoutManager = LinearLayoutManager(activity)
    private var horizonManager : LinearLayoutManager = LinearLayoutManager(activity)
    private var horizonManager2 : LinearLayoutManager = LinearLayoutManager(activity)
    private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private var currentCarpoolAdapter : CurrentCarpoolAdapter? = null
    private var noticeBoardMyAreaAdapter : NoticeBoardMyAreaAdapter? = null

    //나의 활동 지역
    private var myAreaItemData = ArrayList<Content?>()


    //캘린더
    private var calendarAdapter : CalendarAdapter? = null
    private var calendarItemData : MutableList<DateData?> = mutableListOf()

    //게시글 전체 데이터 및 adapter와 공유하는 데이터
    private var carpoolAllData : ArrayList<PostData> = ArrayList()
    private var currentTaxiAllData = ArrayList<PostData?>()
    //private var carpoolParticipantsData = ArrayList<ArrayList<ParticipationData>?>()
    //게시글 선택 시 위치를 잠시 저장하는 변수
    private var dataPosition = 0

    //게시글과 targetDate를 받아
    private var sharedViewModel: SharedViewModel? = null
    private var calendarTempData = ArrayList<String>()
    //캘린더에서 선택된거 저장하고 없어지고 할 데이터
    private var selectCalendarCarpoolData : ArrayList<PostData> = ArrayList()
    //edit에서 받은 값
    //private var selectCalendarData = HashMap<String, ArrayList<PostData>>()
    private var testselectCalendarData = HashMap<String, ArrayList<PostData>>()

    //로딩창
    private var loadingDialog : LoadingProgressDialog? = null

    //처음 시작 시 계정 수정요청용
    private var isFirstAccountEdit : String? = null

    private var adRequest : AdRequest? = null

    private var isFirst = true

    private lateinit var viewModel: CurrentDataViewModel

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
        taxiTabBinding = FragmentCarpoolTabBinding.inflate(inflater, container, false)
        initNoticeBoardRecyclerView()
        initCurrentNoticeBoardRecyclerView()
        initCalendarRecyclerView()
        initMyAreaRecyclerView()

        //recyclerview item클릭 시
        noticeBoardAdapter?.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = carpoolAllData[position]
                    dataPosition = position
                    val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp.user.profileImageUrl)
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        currentCarpoolAdapter?.setItemClickListener(object : CurrentCarpoolAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int, status : CurrentCarpoolAdapter.PostStatus?) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = currentTaxiAllData[position]
                    var intent : Intent? = null
                    dataPosition = position
                    when(status) {
                        CurrentCarpoolAdapter.PostStatus.Passenger -> {//내가 손님으로 카풀이 완료되었을 떄
                            Log.d("Carpool", "Passenger")
                            intent = Intent(activity, CompleteActivity::class.java).apply {
                                putExtra("type", "PASSENGER")
                                putExtra("postDriver", temp?.user)
                                putExtra("postData", currentTaxiAllData[position])
                                putExtra("category", "carpool")
                            }
                            //sendAlarmData("PASSENGER", position, currentTaxiAllData[position])
                            //patchCompletePost(temp.postID)
                        }

                        CurrentCarpoolAdapter.PostStatus.Driver -> { //내가 운전자로 카풀이 완료되었을 떄
                            Log.d("Carpool", "Driver")
                            intent = Intent(activity, CompleteActivity::class.java).apply {
                                putExtra("type", "DRIVER")
                                putExtra("postData", currentTaxiAllData[position])
                                putExtra("category", "carpool")
                            }
                            //sendAlarmData("DRIVER", position, currentTaxiAllData[position])
                            //patchCompletePost(temp.postID)
                        }

                        CurrentCarpoolAdapter.PostStatus.Neither -> {
                            Log.d("Carpool", "Neither")
                            intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                putExtra("type", "READ")
                                putExtra("postItem", temp)
                                putExtra("uri", temp?.user?.profileImageUrl.toString())
                                putExtra("tabType", "카풀")
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
                if (carpoolAllData.isNotEmpty()) {
                    try {
                        val selectDateData = carpoolAllData.filter { it.postTargetDate == itemId }
                        if (selectDateData.isNotEmpty()) {
                            selectCalendarCarpoolData.clear()

                            /* for (i in selectDateData.indices) {
                                 calendarTaxiAllData.add(selectDateData[i])
                             }*/
                            for (select in selectDateData) {
                                selectCalendarCarpoolData.add(select)
                            }

                            //noticeBoardAdapter = NoticeBoardAdapter()
                            noticeBoardAdapter!!.postItemData = selectCalendarCarpoolData
                            taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                            taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                            noticeBoardAdapter!!.notifyDataSetChanged()

                            /* //recyclerview item클릭 시
                             noticeBoardAdapter?.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
                                 override fun onClick(view: View, position: Int, itemId: Int) {
                                     CoroutineScope(Dispatchers.IO).launch {
                                         val temp = carpoolAllData[position]
                                         dataPosition = position
                                         val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                             putExtra("type", "READ")
                                             putExtra("postItem", temp)
                                             putExtra("uri", temp.user.profileImageUrl)
                                         }
                                         requestActivity.launch(intent)
                                     }
                                 }
                             })*/

                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                taxiTabBinding.noticeBoardRV.visibility = View.GONE
                                taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                            }
                        }

                    } catch (e: java.lang.IndexOutOfBoundsException) {
                        println("tesetstes")
                    }
                } else {
                    println("null")
                }
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
            /*data.add(PostData("2020202", 0, "test", "test"))
            noticeBoardAdapter!!.notifyItemInserted(position)
            position += 1*/
            /*val intent = Intent(activity, NoticeBoardEditActivity::class.java).apply {
                putExtra("type", "ADD")
            }
            requestActivity.launch(intent)
            noticeBoardAdapter!!.notifyDataSetChanged()*/

            val intent = Intent(activity, MoreCarpoolTabActivity::class.java).apply {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[CurrentDataViewModel::class.java]
        setCurrentCarpoolData()
        // LiveData 구독 (데이터 관찰)
        viewModel.currentCarpoolLiveData.observe(viewLifecycleOwner) { data ->
            Log.e("viewcarppol", data.toString())
            currentTaxiAllData.sortByDescending {item -> item?.postCreateDate}
            currentCarpoolAdapter?.updateDataList(data.toList())
        }

        // 로딩 상태 관찰
        /*viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                loadingDialog = LoadingProgressDialog(activity)
                //loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                //로딩창
                loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                loadingDialog?.window?.attributes?.windowAnimations = R.style.FullScreenDialog // 위에서 정의한 스타일을 적용
                loadingDialog?.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                loadingDialog?.show()
            } else {
                loadingDialog?.dismiss()
                loadingDialog = null
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




    private fun initNoticeBoardRecyclerView() {
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        //저장된 거 가져옴
        val editObserver = androidx.lifecycle.Observer<HashMap<String, ArrayList<PostData>>> { textValue ->
            testselectCalendarData = textValue
        }
        sharedViewModel!!.getCalendarLiveData().observe(viewLifecycleOwner, editObserver)
        //로딩창 실행
        loadingDialog = LoadingProgressDialog(activity)
        //loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //로딩창
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog?.window?.attributes?.windowAnimations = R.style.FullScreenDialog // 위에서 정의한 스타일을 적용
        loadingDialog?.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        loadingDialog?.show()

        setData()

        noticeBoardAdapter = NoticeBoardAdapter()
        noticeBoardAdapter!!.postItemData = carpoolAllData
        taxiTabBinding.noticeBoardRV.adapter = noticeBoardAdapter
        taxiTabBinding.noticeBoardRV.setHasFixedSize(true)
        taxiTabBinding.noticeBoardRV.layoutManager = manager


        val sharedPref = requireActivity().getSharedPreferences("saveSetting", Context.MODE_PRIVATE)
        isFirstAccountEdit = sharedPref.getString("isFirstAccountEdit", "") ?: "true"
        Log.e("shaerdPref", isFirstAccountEdit.toString())

        if (isFirstAccountEdit == "true") {
            initSettingDialog()
        }
    }

    private fun initSettingDialog() {
        //사용할 곳
        val layoutInflater = LayoutInflater.from(requireActivity())
        val view = layoutInflater.inflate(R.layout.beginning_dialog_layout, null)
        val alertDialog = AlertDialog.Builder(requireActivity())
            .setView(view)
            .create()
        val dialogMoveBtn = view.findViewById<Button>(R.id.beginning_btn)
        // 다이얼로그 창 배경색 설정
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,WindowManager.LayoutParams.FLAG_BLUR_BEHIND)

        alertDialog.window?.attributes?.windowAnimations = R.style.FullScreenDialog // 위에서 정의한 스타일을 적용
        alertDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        //취소 불가능
        alertDialog.setCancelable(false)
        // 다이얼로그 창 투명도 설정
        val window = alertDialog.window
        window?.setDimAmount(1f) // 0에서 1 사이의 값을 사용하여 투명도 설정 (0이 완전 투명, 1이 완전 불투명)

        alertDialog.show()

        dialogMoveBtn.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("saveSetting", Context.MODE_PRIVATE)
            isFirstAccountEdit = sharedPref.getString("isFirstAccountEdit", "") ?: ""

            if (isFirstAccountEdit?.isEmpty() == true) {
                with(sharedPref.edit()) {
                    putString("isFirstAccountEdit", "true")
                    apply() // 비동기적으로 데이터를 저장
                }
            } else {
                with(sharedPref.edit()) {
                    putString("isFirstAccountEdit", "false")
                    apply() // 비동기적으로 데이터를 저장
                }
            }
            val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottom_navigation_view)
            // 바텀 네비게이션의 다른 메뉴를 선택하도록 설정
            bottomNavigationView.findViewById<View>(R.id.navigation_account).performClick()
            alertDialog.dismiss()
        }
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
        /*CoroutineScope(Dispatchers.IO).launch {
            setCurrentCarpoolData()
        }*/
        val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // 리사이클러뷰가 스크롤되었을 때 뷰페이저의 스크롤을 막음
                val viewpager = requireActivity().findViewById<ViewPager2>(R.id.viewpager)
                viewpager.isUserInputEnabled = newState == RecyclerView.SCROLL_STATE_IDLE
                //println("vuewpager")
            }
        }
        // RecyclerView 스크롤 이벤트 리스너 등록


        taxiTabBinding.currentRv.addOnScrollListener(recyclerViewScrollListener)
        currentCarpoolAdapter = CurrentCarpoolAdapter()
        //currentCarpoolAdapter!!.currentPostItemData = currentTaxiAllData
        taxiTabBinding.currentRv.adapter = currentCarpoolAdapter
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
            Log.e("calendarItemData", "$todayPosition")
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
            val tempDayOfWeek = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN) //요일
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
    }


    private fun setData() {
        RetrofitServerConnect.create(requireContext()).getCategoryPostData(1, "createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseData ->
                        carpoolAllData.clear()

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

                            carpoolAllData.add(
                                PostData(
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
                                )
                            )
                        }

                        if (isFirst) {
                            // 첫 호출일 때, 오늘 날짜에 해당하는 데이터만 필터링
                            isFirst = false
                            val todayDate = LocalDate.now().toString()
                            val todayFilteredList = carpoolAllData.filter {
                                it.postTargetDate == todayDate
                            }

                            if (todayFilteredList.isEmpty()) {
                                taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                                taxiTabBinding.noticeBoardRV.visibility = View.GONE
                            } else {
                                taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                                taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                            }

                            // 어댑터에 필터링된 데이터 적용
                            noticeBoardAdapter?.let { adapter ->
                                adapter.postItemData = todayFilteredList as ArrayList<PostData>
                                adapter.notifyDataSetChanged()
                            }

                        } else {
                            // 첫 호출이 아닐 때는 전체 데이터를 반영
                            if (carpoolAllData.isEmpty()) {
                                taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                                taxiTabBinding.noticeBoardRV.visibility = View.GONE
                            } else {
                                taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                                taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                            }

                            // 어댑터에 전체 데이터를 적용
                            noticeBoardAdapter?.let { adapter ->
                                adapter.postItemData = carpoolAllData
                                adapter.notifyDataSetChanged()
                            }
                        }

                        //calendarAdapter!!.notifyDataSetChanged()
                        loadingDialog?.dismiss()
                    }
                } else {
                    loadingDialog?.dismiss()
                    if (carpoolAllData.isEmpty()) {
                        taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                        taxiTabBinding.noticeBoardRV.visibility = View.GONE
                    } else {
                        taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                        taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                loadingDialog?.dismiss()
                if (carpoolAllData.isEmpty()) {
                    taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                    taxiTabBinding.noticeBoardRV.visibility = View.GONE
                } else {
                    taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                    taxiTabBinding.noticeBoardRV.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setMyAreaData() {
        //저장된 값
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
                        //Log.e("myAreaItemData", )
                        myAreaItemData.clear()
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
                            //Log.e("morearea", moreAreaData.toString())
                            noticeBoardMyAreaAdapter!!.postAreaItemData = myAreaItemData
                            noticeBoardMyAreaAdapter!!.notifyDataSetChanged()
                            loadingDialog?.dismiss()
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
                        /*requireActivity().runOnUiThread {
                            if (isAdded && !requireActivity().isFinishing) {
                                loadingDialog?.dismiss()
                                Toast.makeText(requireActivity(), "지역 정보를 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }*/
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
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                   /* requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            loadingDialog?.dismiss()
                            Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    }*/
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

    private fun setCurrentCarpoolData() {
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
                            }
                        }
                    }
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    val sortedTargets = currentTaxiAllData.sortedByDescending {
                        // 날짜와 시간을 각각 파싱하고 결합하여 내림차순으로 정렬
                        val targetDate = LocalDate.parse(it?.postTargetDate, dateFormatter) // 날짜 파싱
                        val targetTime = LocalTime.parse(it?.postTargetTime, timeFormatter) // 시간 파싱
                        targetDate.atTime(targetTime) // 날짜와 시간을 결합하여 정렬 기준 생성
                    }
                    //currentCarpoolAdapter?.currentPostItemData = sortedTargets.toMutableList()
                    viewModel.setCurrentData(sortedTargets.toMutableList())
                    //currentCarpoolAdapter!!.notifyDataSetChanged()

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

                    loadingDialog?.dismiss()

                } else {
                    println("faafa")
                    Log.d("add", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    Log.d("f", response.code().toString())

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
                                    setCurrentCarpoolData()
                                }
                            }
                            taxiTabBinding.carpoolText.setOnClickListener {
                                Log.d("carpoolText", "clcickckckc")
                                lifecycleScope.launch {
                                    setCurrentCarpoolData()
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
                taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                taxiTabBinding.nonCurrentRvTv.text = "예상치 못한 오류가 발생했습니다"
                taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE
                taxiTabBinding.nonCurrentRvTv2.text = "이곳을 눌러 새로고침 해주세요"

                taxiTabBinding.nonCurrentRvTv2.setOnClickListener {
                    lifecycleScope.launch {
                        setCurrentCarpoolData()
                    }
                }
                taxiTabBinding.carpoolText.setOnClickListener {
                    lifecycleScope.launch {
                        setCurrentCarpoolData()
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
        })
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
                            setCurrentCarpoolData()
                            setData()
                        }
                        //livemodel을 통해 저장
                        //sharedViewModel!!.setCalendarLiveData("add", selectCalendarData)
                    }
                    //edit
                    1 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            carpoolAllData[dataPosition] = post!!
                        }
                        noticeBoardAdapter!!.notifyItemChanged(post?.postID!!)
                    }

                    123 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            /*taxiAllData.add(post)
                            calendarTaxiAllData.add(post) //데이터 전부 들어감

                            //들어간 데이터를 key로 분류하여 저장하도록함
                            selectCalendarData[post.postTargetDate] = arrayListOf()
                            selectCalendarData[post.postTargetDate]!!.add(post)

                            println(selectCalendarData)*/
                            setCurrentCarpoolData()
                        }
                    }

                }
            }
        }
    }

    //오늘날짜에 선택되게
    private fun triggerFirstItemOfCalendarAdapter(currentDatePos: Int) {
        taxiTabBinding.calendarRV.post {
            taxiTabBinding.calendarRV.findViewHolderForAdapterPosition(currentDatePos)?.itemView?.performClick()
        }
    }

    private fun initAd() {
        adRequest = AdRequest.Builder().build()
        taxiTabBinding.carpoolAd.loadAd(adRequest!!)
    }

    override fun onDestroy() {
        super.onDestroy()

        //다이얼로그가 띄워져 있는 상태(showing)인 경우 dismiss() 호출
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog!!.dismiss()
        }
    }
    override fun onResume() {
        super.onResume()
        loadAdIfNeeded()
    }

    override fun onPause() {
        super.onPause()
        stopAdLoading()
    }

    private fun loadAdIfNeeded() {
        if (isScreenOn()) {
            initAd()
        }
    }

    private fun stopAdLoading() {
        adRequest = null
    }

    private fun isScreenOn(): Boolean {
        val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
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