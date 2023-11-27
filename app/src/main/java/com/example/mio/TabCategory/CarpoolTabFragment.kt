package com.example.mio.TabCategory

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.autofill.Validators.or
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.CalendarAdapter
import com.example.mio.Adapter.CurrentNoticeBoardAdapter
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Adapter.NoticeBoardMyAreaAdapter
import com.example.mio.Model.*
import com.example.mio.NoticeBoard.NoticeBoardEditActivity
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.databinding.FragmentCarpoolTabBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
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

    private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private var currentNoticeBoardAdapter : CurrentNoticeBoardAdapter? = null
    private var noticeBoardMyAreaAdapter : NoticeBoardMyAreaAdapter? = null

    //나의 활동 지역
    private var myAreaItemData : ArrayList<PostData> = ArrayList()


    //캘린더
    private var calendarAdapter : CalendarAdapter? = null
    private var calendarItemData : MutableList<DateData?> = mutableListOf()

    //게시글 전체 데이터 및 adapter와 공유하는 데이터
    private var carpoolAllData : ArrayList<PostData> = ArrayList()
    private var currentTaxiAllData = ArrayList<PostData>()
    private var carpoolParticipantsData = ArrayList<ArrayList<Participants>?>()
    //게시글 선택 시 위치를 잠시 저장하는 변수
    private var dataPosition = 0
    //게시글 위치
    private var position = 0
    //게시글과 targetDate를 받아
    private var sharedViewModel: SharedViewModel? = null
    private var calendarTempData = ArrayList<String>()
    //이건 캘린더에 적용될 모든 데이터
    private var calendarTaxiAllData : ArrayList<PostData> = ArrayList()
    //캘린더에서 선택된거 저장하고 없어지고 할 데이터
    private var selectCalendarCarpoolData : ArrayList<PostData> = ArrayList()
    //edit에서 받은 값
    //private var selectCalendarData = HashMap<String, ArrayList<PostData>>()
    private var testselectCalendarData = HashMap<String, ArrayList<PostData>>()

    //뒤로 가기 받아오기
    private lateinit var callback : OnBackPressedCallback
    var backPressedTime : Long = 0

    //로딩창
    private lateinit var loadingDialog : LoadingProgressDialog


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
    ): View? {
        // Inflate the layout for this fragment
        taxiTabBinding = FragmentCarpoolTabBinding.inflate(inflater, container, false)

        initNoticeBoardRecyclerView()
        initCurrentNoticeBoardRecyclerView()
        initCalendarRecyclerView()
        initSwipeRefresh()
        initMyAreaRecyclerView()


        //recyclerview item클릭 시
        noticeBoardAdapter!!.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
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
                            }
                            sendAlarmData("PASSENGER", position, currentTaxiAllData[position])
                        }

                        CurrentNoticeBoardAdapter.PostStatus.Driver -> { //내가 운전자로 카풀이 완료되었을 떄
                            intent = Intent(activity, CompleteActivity::class.java).apply {
                                putExtra("type", "DRIVER")
                                //putExtra("postPassengers", carpoolParticipantsData)
                            }
                            sendAlarmData("DRIVER", position, currentTaxiAllData[position])
                        }

                        CurrentNoticeBoardAdapter.PostStatus.Neither -> {
                            intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                putExtra("type", "READ")
                                putExtra("postItem", temp)
                                putExtra("uri", temp.user.profileImageUrl)
                            }
                        }
                        else -> {

                        }
                    }
                    requestActivity.launch(intent)
                }
            }
        })


        calendarAdapter!!.setItemClickListener(object : CalendarAdapter.ItemClickListener {
            //여기서 position = 0시작은 date가 되야함 itemId=1로 시작함
            override fun onClick(view: View, position: Int, itemId: String) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (calendarTaxiAllData.isNotEmpty()) {
                        try {
                            val selectDateData = calendarTaxiAllData.filter { it.postTargetDate == itemId }
                            val mainHandler = Handler(Looper.getMainLooper())
                            mainHandler.post {
                                if (selectDateData.isNotEmpty()) {
                                    noticeBoardAdapter = NoticeBoardAdapter()
                                    noticeBoardAdapter!!.postItemData = selectCalendarCarpoolData
                                    taxiTabBinding.noticeBoardRV.adapter = noticeBoardAdapter
                                    //레이아웃 뒤집기 안씀
                                    //manager.reverseLayout = true
                                    //manager.stackFromEnd = true
                                    taxiTabBinding.noticeBoardRV.setHasFixedSize(true)
                                    taxiTabBinding.noticeBoardRV.layoutManager = manager

                                    CoroutineScope(Dispatchers.Main).launch {
                                        taxiTabBinding.refreshSwipeLayout.visibility = View.VISIBLE
                                        taxiTabBinding.nonCalendarDataTv.visibility = View.GONE
                                    }
                                    selectCalendarCarpoolData.clear()

                                    /* for (i in selectDateData.indices) {
                                         calendarTaxiAllData.add(selectDateData[i])
                                     }*/
                                    for (select in selectDateData) {
                                        selectCalendarCarpoolData.add(select)
                                    }


                                    noticeBoardAdapter!!.notifyDataSetChanged()

                                    //recyclerview item클릭 시
                                    noticeBoardAdapter!!.setItemClickListener(object : NoticeBoardAdapter.ItemClickListener {
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

                                } else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        taxiTabBinding.refreshSwipeLayout.visibility = View.GONE
                                        taxiTabBinding.nonCalendarDataTv.visibility = View.VISIBLE
                                    }
                                }
                            }
                        } catch (e: java.lang.IndexOutOfBoundsException) {
                            println("tesetstes")
                        }
                    } else {
                        println("null")
                    }
                    /*
                    calendarAdapter!!.notifyItemChanged(oldSelectedPostion)
                    calendarAdapter!!.notifyItemChanged(selectedPostion)
                    */
                }
                /* calendarAdapter!!.notifyItemChanged(selectedPostion)
                calendarAdapter!!.notifyItemChanged(oldSelectedPostion)*/

                noticeBoardAdapter!!.notifyDataSetChanged()
                Toast.makeText(activity, calendarItemData[position]!!.day, Toast.LENGTH_SHORT).show()
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

        /*taxiTabBinding.filterBtn.setOnClickListener {
            *//*val bottomSheetDialog = BottomSheetDialog(
                requireActivity(), R.style.BottomSheetDialogTheme
            ).apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = true
            }

            val bottomView = LayoutInflater.from(requireActivity()).inflate(
                R.layout.bottom_sheet_dialog, null
            )


            //bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            // bottomSheetDialog 뷰 생성
            bottomSheetDialog.setContentView(bottomView)

            // bottomSheetDialog 호출
            bottomSheetDialog.show()*//*

            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                    }
                })
            }

        }*/


        return taxiTabBinding.root
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
        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog.window?.attributes?.windowAnimations = R.style.FullScreenDialog // 위에서 정의한 스타일을 적용


        loadingDialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        loadingDialog.show()

        setData()


        noticeBoardAdapter = NoticeBoardAdapter()
        noticeBoardAdapter!!.postItemData = carpoolAllData
        taxiTabBinding.noticeBoardRV.adapter = noticeBoardAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        taxiTabBinding.noticeBoardRV.setHasFixedSize(true)
        taxiTabBinding.noticeBoardRV.layoutManager = manager

        /*taxiTabBinding.noticeBoardRV.itemAnimator =  SlideInUpAnimator(OvershootInterpolator(1f))
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
        setCurrentCarpoolData()
        currentNoticeBoardAdapter = CurrentNoticeBoardAdapter()
        currentNoticeBoardAdapter!!.currentPostItemData = currentTaxiAllData
        taxiTabBinding.currentRv.adapter = currentNoticeBoardAdapter

        taxiTabBinding.currentRv.setHasFixedSize(true)
    }

    private fun initCalendarRecyclerView() {
        setCalendarData()
        calendarAdapter = CalendarAdapter()
        CalendarUtil.selectedDate = LocalDate.now()
        calendarAdapter!!.calendarItemData = calendarItemData
        taxiTabBinding.calendarRV.adapter = calendarAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        taxiTabBinding.calendarRV.setHasFixedSize(true)
        //taxiTabBinding.calendarRV.addItemDecoration(RecyclerviewDecoration())

        horizonManager.orientation = LinearLayoutManager.HORIZONTAL
        taxiTabBinding.calendarRV.layoutManager = horizonManager

        if (calendarItemData.isNotEmpty()) {
            val s = calendarItemData.indexOf(calendarItemData.find { it?.day == "오늘" })
            triggerFirstItemOfCalendarAdapter(s)
        }
    }

    private fun initSwipeRefresh() {
        taxiTabBinding.refreshSwipeLayout.setOnRefreshListener {
            //새로고침 시 터치불가능하도록
            activity?.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 화면 터치 못하게 하기
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                setData()
                noticeBoardAdapter!!.postItemData = carpoolAllData
                //noticeBoardAdapter.recyclerView.startLayoutAnimation()
                taxiTabBinding.refreshSwipeLayout.isRefreshing = false
                noticeBoardAdapter!!.notifyDataSetChanged()
                activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
            //터치불가능 해제ss
            //activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getCategoryPostData(1,"createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {

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

                        //데이터 청소
                        carpoolAllData.clear()

                        for (i in response.body()!!.content.indices) {
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
                                    response.body()!!.content[i].participants!!.isEmpty()
                                    response.body()!!.content[i].participants!!.size
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                location = try {
                                    response.body()!!.content[i].location.isEmpty()
                                    response.body()!!.content[i].location
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "수락산역 3번 출구"
                                }
                                title = try {
                                    response.body()!!.content[i].title.isEmpty()
                                    response.body()!!.content[i].title
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                content = try {
                                    response.body()!!.content[i].content.isEmpty()
                                    response.body()!!.content[i].content
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetDate = try {
                                    response.body()!!.content[i].targetDate.isEmpty()
                                    response.body()!!.content[i].targetDate
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetTime = try {
                                    response.body()!!.content[i].targetTime.isEmpty()
                                    response.body()!!.content[i].targetTime
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                categoryName = try {
                                    response.body()!!.content[i].category.categoryName.isEmpty()
                                    response.body()!!.content[i].category.categoryName
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                cost = try {
                                    response.body()!!.content[i].cost
                                    response.body()!!.content[i].cost
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                verifyGoReturn = try {
                                    response.body()!!.content[i].verifyGoReturn
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    false
                                }
                            }

                            carpoolAllData.add(
                                PostData(
                                    response.body()!!.content[i].user.studentId,
                                    response.body()!!.content[i].postId,
                                    title,
                                    content,
                                    targetDate,
                                    targetTime,
                                    categoryName,
                                    location,
                                    //participantscount가 현재 참여하는 인원들
                                    part,
                                    //numberOfPassengers은 총 탑승자 수
                                    response.body()!!.content[i].numberOfPassengers,
                                    cost,
                                    verifyGoReturn,
                                    response.body()!!.content[i].user
                            ))

                            //println(response!!.body()!!.content[i].user.studentId)
                            /*part?.let {
                                PostData(
                                    response.body()!!.content[i].user.studentId,
                                    response.body()!!.content[i].postId,
                                    title,
                                    content,
                                    targetDate,
                                    targetTime,
                                    categoryName,
                                    location,
                                    //participantscount가 현재 참여하는 인원들
                                    it,
                                    //numberOfPassengers은 총 탑승자 수
                                    response.body()!!.content[i].numberOfPassengers,
                                    cost,
                                    verifyGoReturn,
                                    response.body()!!.content[i].user
                                )
                            }?.let { carpoolAllData.add(it) }*/
                            println(carpoolAllData)
                        }
                        noticeBoardAdapter!!.notifyDataSetChanged()

                        if (carpoolAllData.isEmpty()) {
                            taxiTabBinding.nonCarpoolData.visibility = View.VISIBLE
                            taxiTabBinding.refreshSwipeLayout.visibility = View.GONE
                        } else {
                            taxiTabBinding.nonCarpoolData.visibility = View.GONE
                            taxiTabBinding.refreshSwipeLayout.visibility = View.VISIBLE
                        }

                        calendarTaxiAllData = carpoolAllData
                        selectCalendarCarpoolData = calendarTaxiAllData

                        calendarAdapter!!.notifyDataSetChanged()

                        loadingDialog.dismiss()

                    } else {
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun setMyAreaData() { //나중에 여기 위치 받아오면 데이터 변경하기 TODO
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getCategoryPostData(1,"createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {

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

                        //데이터 청소
                        myAreaItemData.clear()

                        for (i in response.body()!!.content.indices) {
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
                                    response.body()!!.content[i].participants!!.isEmpty()
                                    response.body()!!.content[i].participants!!.size
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                location = try {
                                    response.body()!!.content[i].location.isEmpty()
                                    response.body()!!.content[i].location
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "수락산역 3번 출구"
                                }
                                title = try {
                                    response.body()!!.content[i].title.isEmpty()
                                    response.body()!!.content[i].title
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                content = try {
                                    response.body()!!.content[i].content.isEmpty()
                                    response.body()!!.content[i].content
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetDate = try {
                                    response.body()!!.content[i].targetDate.isEmpty()
                                    response.body()!!.content[i].targetDate
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetTime = try {
                                    response.body()!!.content[i].targetTime.isEmpty()
                                    response.body()!!.content[i].targetTime
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                categoryName = try {
                                    response.body()!!.content[i].category.categoryName.isEmpty()
                                    response.body()!!.content[i].category.categoryName
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                cost = try {
                                    response.body()!!.content[i].cost
                                    response.body()!!.content[i].cost
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                verifyGoReturn = try {
                                    response.body()!!.content[i].verifyGoReturn
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    false
                                }
                            }

                            //println(response!!.body()!!.content[i].user.studentId)
                            myAreaItemData.add(
                                PostData(
                                    response.body()!!.content[i].user.studentId,
                                    response.body()!!.content[i].postId,
                                    title,
                                    content,
                                    targetDate,
                                    targetTime,
                                    categoryName,
                                    location,
                                    //participantscount가 현재 참여하는 인원들
                                    part,
                                    //numberOfPassengers은 총 탑승자 수
                                    response.body()!!.content[i].numberOfPassengers,
                                    cost,
                                    verifyGoReturn,
                                    response.body()!!.content[i].user
                                ))

                            noticeBoardMyAreaAdapter!!.notifyDataSetChanged()
                        }
                        loadingDialog.dismiss()

                    } else {
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun setCurrentCarpoolData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(activity).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(activity).toString()

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
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)

        //println(userId)

        CoroutineScope(Dispatchers.IO).launch {
            api.getMyParticipantsData(0, 20).enqueue(object : Callback<List<Content>> {
                override fun onResponse(call: Call<List<Content>>, response: Response<List<Content>>) {
                    if (response.isSuccessful) {
                        val responseData = response.body().isNullOrEmpty()
                        println("예약 정보")
                        //데이터 청소
                        currentTaxiAllData.clear()

                        for (i in response.body()!!.indices) {
                            //println(response!!.body()!!.content[i].user.studentId)
                            currentTaxiAllData.add(PostData(
                                response.body()!![i].user.studentId,
                                response.body()!![i].postId,
                                response.body()!![i].title,
                                response.body()!![i].content,
                                response.body()!![i].targetDate,
                                response.body()!![i].targetTime,
                                response.body()!![i].category.categoryName,
                                response.body()!![i].location,
                                //participantscount가 현재 참여하는 인원들
                                response.body()!![i].participantsCount,
                                //numberOfPassengers은 총 탑승자 수
                                response.body()!![i].numberOfPassengers,
                                response.body()!![i].cost,
                                response.body()!![i].verifyGoReturn,
                                response.body()!![i].user
                            ))
                            currentNoticeBoardAdapter!!.notifyDataSetChanged()
                        }
                        //val list : ArrayList<Participants> = ArrayList()
                        if (responseData) {
                            CoroutineScope(Dispatchers.IO).launch {
                                for (i in response.body()!!.indices) {

                                    carpoolParticipantsData.add(response.body()!![i].participants)

                                    /*for (j in response.body()!![i].participants?.indices!!) {
                                        tempData.add(Participants(
                                            response.body()!![i].participants?.get(j)?.id!!,
                                            response.body()!![i].participants?.get(j)?.email!!,
                                            response.body()!![i].participants?.get(j)?.studentId!!,
                                            response.body()!![i].participants?.get(j)?.profileImageUrl!!,
                                            response.body()!![i].participants?.get(j)?.name!!,
                                            response.body()!![i].participants?.get(j)?.accountNumber!!,
                                            response.body()!![i].participants?.get(j)?.gender!!,
                                            response.body()!![i].participants?.get(j)?.verifySmoker!!,
                                            response.body()!![i].participants?.get(j)?.roleType!!,
                                            response.body()!![i].participants?.get(j)?.status!!,
                                            response.body()!![i].participants?.get(j)?.mannerCount!!,
                                            response.body()!![i].participants?.get(j)?.grade!!,
                                        ))
                                    }
                                    s.add(tempData)*/
                                }
                                /*for (i in response.body()!!.indices) {
                                    for (j in response.body()!![i].participants.indices) {
                                        carpoolParticipantsData.add(Participants(
                                            response.body()!![i].participants[j].id,
                                            response.body()!![i].participants[j].email,
                                            response.body()!![i].participants[j].studentId,
                                            response.body()!![i].participants[j].profileImageUrl,
                                            response.body()!![i].participants[j].name,
                                            response.body()!![i].participants[j].accountNumber,
                                            response.body()!![i].participants[j].gender,
                                            response.body()!![i].participants[j].verifySmoker,
                                            response.body()!![i].participants[j].roleType,
                                            response.body()!![i].participants[j].status,
                                            response.body()!![i].participants[j].mannerCount,
                                            response.body()!![i].participants[j].grade,
                                        ))
                                    }
                                }*/
                            }
                        /*CoroutineScope(Dispatchers.IO).launch {
                                for (i in response.body()?.indices!!) { //List<content>
                                    for (j in response.body()?.get(i)?.participants?.indices!!) {
                                        list.add(Participants(
                                            response.body()!![i].participants!![j].id,
                                            response.body()!![i].participants!![j].email,
                                            response.body()!![i].participants!![j].studentId,
                                            response.body()!![i].participants!![j].profileImageUrl,
                                            response.body()!![i].participants!![j].name,
                                            response.body()!![i].participants!![j].accountNumber,
                                            response.body()!![i].participants!![j].gender,
                                            response.body()!![i].participants!![j].verifySmoker,
                                            response.body()!![i].participants!![j].roleType,
                                            response.body()!![i].participants!![j].status,
                                            response.body()!![i].participants!![j].mannerCount,
                                            response.body()!![i].participants!![j].grade,
                                        ))
                                    }
                                    carpoolParticipantsData.add()
                                }
                            }*/
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

                        loadingDialog.dismiss()

                    } else {
                        println(response.errorBody().toString())
                        println(response.message().toString())
                        println("실패")
                        println("faafa")
                        Log.d("add", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        Log.d("f", response.code().toString())

                        taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                        taxiTabBinding.nonCurrentRvTv.text = "예상치 못한 오류가 발생했습니다"
                        taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE
                        taxiTabBinding.nonCurrentRvTv2.text = "이곳을 눌러 새로고침 해주세요"

                        taxiTabBinding.nonCurrentRvTv.setOnClickListener {
                            setCurrentCarpoolData()
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
                    Log.d("error", t.toString())
                }
            })
        }
        /*val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getCurrentServerPostData("createDate,desc").enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        val temp = kotlin.collections.ArrayList<PostData>()
                        //데이터 청소
                        currentTaxiAllData.clear()

                        for (i in response.body()!!.content.indices) {
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
                                    response.body()!!.content[i].participants.isEmpty()
                                    response.body()!!.content[i].participants.size
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                location = try {
                                    response.body()!!.content[i].location.isEmpty()
                                    response.body()!!.content[i].location
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "수락산역 3번 출구"
                                }
                                title = try {
                                    response.body()!!.content[i].title.isEmpty()
                                    response.body()!!.content[i].title
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                content = try {
                                    response.body()!!.content[i].content.isEmpty()
                                    response.body()!!.content[i].content
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetDate = try {
                                    response.body()!!.content[i].targetDate.isEmpty()
                                    response.body()!!.content[i].targetDate
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetTime = try {
                                    response.body()!!.content[i].targetTime.isEmpty()
                                    response.body()!!.content[i].targetTime
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                categoryName = try {
                                    response.body()!!.content[i].category.categoryName.isEmpty()
                                    response.body()!!.content[i].category.categoryName
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                cost = try {
                                    response.body()!!.content[i].cost
                                    response.body()!!.content[i].cost
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                verifyGoReturn = try {
                                    response.body()!!.content[i].verifyGoReturn
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    false
                                }
                            }

                            //println(response!!.body()!!.content[i].user.studentId)
                            temp.add(PostData(
                                response.body()!!.content[i].user.studentId,
                                response.body()!!.content[i].postId,
                                title,
                                content,
                                targetDate,
                                targetTime,
                                categoryName,
                                location,
                                //participantscount가 현재 참여하는 인원들
                                part,
                                //numberOfPassengers은 총 탑승자 수
                                response.body()!!.content[i].numberOfPassengers,
                                cost,
                                verifyGoReturn,
                                response.body()!!.content[i].user
                            ))
                        }
                        val now = System.currentTimeMillis()
                        val date = Date(now)
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                        val currentDate = sdf.format(date)
                        val selectFormattedDate = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).format(
                            DateTimeFormatter.ISO_DATE)
                        val currentTempData = temp.filter { it.postTargetDate == selectFormattedDate}
                        for (j in currentTempData.indices) {
                            currentTaxiAllData.add(currentTempData[j])
                        }
                        currentNoticeBoardAdapter!!.notifyDataSetChanged()


                        if (currentTaxiAllData.isEmpty()) {
                            taxiTabBinding.currentRv.visibility = View.GONE
                            taxiTabBinding.nonCurrentRvTv.visibility = View.VISIBLE
                            taxiTabBinding.nonCurrentRvTv2.visibility = View.VISIBLE
                        } else {
                            taxiTabBinding.currentRv.visibility = View.VISIBLE
                            taxiTabBinding.nonCurrentRvTv.visibility = View.GONE
                            taxiTabBinding.nonCurrentRvTv2.visibility = View.GONE
                        }

                        loadingDialog.dismiss()
                    } else {
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }*/
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
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
                            setData()
                        }
                        //livemodel을 통해 저장
                        //sharedViewModel!!.setCalendarLiveData("add", selectCalendarData)
                    }
                    //edit
                    1 -> {

                    }

                    9 -> {

                    }

                }
                //getSerializableExtra = intent의 값을 보내고 받을때사용
                //타입 변경을 해주지 않으면 Serializable객체로 만들어지니 as로 캐스팅해주자
                /*val pill = it.data?.getSerializableExtra("pill") as PillData
                val selectCategory = it.data?.getSerializableExtra("cg") as String*/

                //선택한 카테고리 및 데이터 추가


                /*if (selectCategory.isNotEmpty()) {
                    selectCategoryData[selectCategory] = categoryArr
                }*/


                //api 33이후 아래로 변경됨
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getSerializable(key, T::class.java)
                } else {
                    getSerializable(key) as? T
                }*/
                /*when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            data.add(pill)
                            categoryArr.add(pill)
                            //add면 그냥 추가
                            selectCategoryData[selectCategory] = categoryArr
                            //전
                            //println( categoryArr[dataPosition])
                        }
                        println("전 ${selectCategoryData[selectCategory]}")
                        //livedata
                        sharedViewModel!!.setCategoryLiveData("add", selectCategoryData)


                        homeAdapter!!.notifyDataSetChanged()
                        Toast.makeText(activity, "추가되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    //edit
                    1 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            data[dataPosition] = pill
                            categoryArr[dataPosition] = pill
                            selectCategoryData.clear()
                            selectCategoryData[selectCategory] = categoryArr
                            //후
                            //println(categoryArr[dataPosition])
                        }
                        println("선택 $selectCategory")
                        //livedata
                        sharedViewModel!!.categoryLiveData.value = selectCategoryData
                        println(testselectCategoryData)
                        homeAdapter!!.notifyDataSetChanged()
                        //Toast.makeText(activity, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                        Toast.makeText(activity, "$testselectCategoryData", Toast.LENGTH_SHORT).show()
                    }
                }*/
            }
        }
    }

    //오늘날짜에 선택되게
    private fun triggerFirstItemOfCalendarAdapter(currentDatePos : Int) {
        taxiTabBinding.calendarRV.post {
            taxiTabBinding.calendarRV.findViewHolderForAdapterPosition(currentDatePos)?.itemView?.performClick()
        }
    }

    private fun sendAlarmData(status:String , dataPos : Int, postData : PostData) { //카풀이 종료(종료버튼 눌렀을 때) 되었을 때 후기 알림
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val myId = saveSharedPreferenceGoogleLogin.getUserId(requireActivity()).toString()
        val token = saveSharedPreferenceGoogleLogin.getToken(requireActivity()).toString()
        val identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(requireActivity()).toString().substring(0..7)
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(requireActivity()).toString()
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
        ///////////////////////////////
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val currentDate = sdf.format(date)
        val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
        val nowDate = nowFormat?.toString()



        //내가 운전자 일때 후기를 받을 손님들한테 알림 전송
        if (status == "DRIVER") {
            for (i in carpoolParticipantsData[dataPos]?.indices!!) {
                //userId 가 알람 받는 사람
                val temp = AddAlarmData(nowDate!!, "${carpoolParticipantsData[dataPos]?.get(i)?.id!!}님이 후기를 기다리고 있어요", postData.postID, myId.toInt())

                //entity가 알람 받는 사람, user가 알람 전송한 사람
                CoroutineScope(Dispatchers.IO).launch {
                    api.addAlarm(temp).enqueue(object : Callback<AddAlarmResponseData?> {
                        override fun onResponse(
                            call: Call<AddAlarmResponseData?>,
                            response: Response<AddAlarmResponseData?>
                        ) {
                            if (response.isSuccessful) {
                                println("succcc send alarm")
                            } else {
                                println("faafa alarm")
                                Log.d("alarm", response.errorBody()?.string()!!)
                                Log.d("message", call.request().toString())
                                println(response.code())
                            }
                        }

                        override fun onFailure(call: Call<AddAlarmResponseData?>, t: Throwable) {
                            Log.d("error", t.toString())
                        }

                    })
                }
            }
        } else {
            //내가 손님일 때 후기를 써주길 원하는 운전자에게 쓰도록 유도
            val temp = AddAlarmData(nowDate!!, "${postData.user.studentId}님이 후기를 기다리고 있어요", postData.postID, myId.toInt())

            //entity가 알람 받는 사람, user가 알람 전송한 사람
            CoroutineScope(Dispatchers.IO).launch {
                api.addAlarm(temp).enqueue(object : Callback<AddAlarmResponseData?> {
                    override fun onResponse(
                        call: Call<AddAlarmResponseData?>,
                        response: Response<AddAlarmResponseData?>
                    ) {
                        if (response.isSuccessful) {
                            println("succcc send alarm")
                        } else {
                            println("faafa alarm")
                            Log.d("alarm", response.errorBody()?.string()!!)
                            Log.d("message", call.request().toString())
                            println(response.code())
                        }
                    }

                    override fun onFailure(call: Call<AddAlarmResponseData?>, t: Throwable) {
                        Log.d("error", t.toString())
                    }

                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //저장된 livemodel들을 가져옴

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

    override fun onStart() {
        super.onStart()
        val localDate = LocalDate.now().toString()
        val currentDate = localDate.substring(8..9).toInt()
        triggerFirstItemOfCalendarAdapter(currentDate)
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