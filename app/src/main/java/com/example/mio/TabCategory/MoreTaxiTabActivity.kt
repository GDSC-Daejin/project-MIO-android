package com.example.mio.TabCategory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.Adapter.MoreTaxiTabAdapter
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Model.MyResponse
import com.example.mio.Model.PostData
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.Model.SharedViewModel
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.RetrofitServerConnect.service
import com.example.mio.databinding.ActivityMoreTaxiTabBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class MoreTaxiTabActivity : AppCompatActivity() {
    private lateinit var mttBinding : ActivityMoreTaxiTabBinding
    private lateinit var myViewModel : SharedViewModel
    private var mtAdapter : MoreTaxiTabAdapter? = null
    private var getBottomSheetData = ""
    private var manager : LinearLayoutManager = LinearLayoutManager(this)

    private var moreTaxiAllData = ArrayList<PostData?>()
    private var dataPosition = 0

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

        initSwipeRefresh()
        initRecyclerView()
        initScrollListener()

        mttBinding.moreFilterBtn.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        println(value.split(" "))
                        val selectFilterData = moreTaxiAllData.filter { it!!.postContent == value }
                    }
                })
            }
            mttBinding.moreFilterTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
            //mttBinding.moreFilterBtn.setBackgroundColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
        }

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

        myViewModel.checkSearchFilter.observe(this) {
            when(it) {
                "최신 순" -> {
                    mttBinding.moreSearchTv.text = "최신 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    val call = RetrofitServerConnect.service
                    CoroutineScope(Dispatchers.IO).launch {
                        call.getServerPostData("createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
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

                                    moreTaxiAllData.clear()
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
                                        moreTaxiAllData.add(PostData(
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
                                        mtAdapter!!.notifyDataSetChanged()
                                    }
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
                "마감 임박 순" -> {
                    mttBinding.moreSearchTv.text = "마감 임박 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    val call = RetrofitServerConnect.service
                    CoroutineScope(Dispatchers.IO).launch {
                        call.getServerDateData().enqueue(object : Callback<PostReadAllResponse> {
                            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                                if (response.isSuccessful) {
                                    println("마감")
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

                                    moreTaxiAllData.clear()
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
                                        moreTaxiAllData.add(PostData(
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
                                        mtAdapter!!.notifyDataSetChanged()
                                    }
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
                "낮은 가격 순" -> {
                    mttBinding.moreSearchTv.text = "낮은 가격 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    val call = RetrofitServerConnect.service
                    CoroutineScope(Dispatchers.IO).launch {
                        call.getServerCostData().enqueue(object : Callback<PostReadAllResponse> {
                            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                                if (response.isSuccessful) {
                                    println("cost")
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

                                    moreTaxiAllData.clear()
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
                                        moreTaxiAllData.add(PostData(
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
                                        println(moreTaxiAllData)
                                        mtAdapter!!.notifyDataSetChanged()
                                    }
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
            //새로고침 시 터치불가능하도록
            this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 화면 터치 못하게 하기
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                setSelectData()
                mtAdapter!!.moreTaxiData = moreTaxiAllData
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

                val layoutm = mttBinding.moreTaxiTabRv.layoutManager as LinearLayoutManager
                //화면에 보이는 마지막 아이템의 position
                // 어댑터에 등록된 아이템의 총 개수 -1
                //데이터의 마지막이 아이템의 화면에 뿌려졌는지
                if (currentPage < totalPages - 1) {
                    if (!isLoading) {
                        if (!mttBinding.moreTaxiTabRv.canScrollVertically(1)) {
                            //가져온 data의 크기가 5와 같을 경우 실행
                            if (moreTaxiAllData.size == 5) {
                                isLoading = true
                                getMoreItem()
                            }
                        }
                    }
                    if (!isLoading) {
                        if (layoutm.findLastCompletelyVisibleItemPosition() == moreTaxiAllData.size-1) {
                            isLoading = true
                            getMoreItem()
                        }
                    }
                } else {
                    isLoading = false
                }
            }
        })
    }

    private fun getMoreItem() {
        moreTaxiAllData.add(null)
        //null을 감지 했으니
        //이 부분에 프로그래스바가 들어올거라 알림
        mttBinding.moreTaxiTabRv.adapter!!.notifyItemInserted(moreTaxiAllData.size-1)
        //성공//
        val call = RetrofitServerConnect.service

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(java.lang.Runnable {
            //null추가한 거 삭제
            moreTaxiAllData.removeAt(moreTaxiAllData.size - 1)

            //data.clear()

            //page수가 totalpages 보다 작거나 같다면 데이터 더 가져오기 가능
            if (currentPage < totalPages - 1) {
                currentPage += 1
                CoroutineScope(Dispatchers.IO).launch {
                    call.getServerPostData("createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
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
                                    moreTaxiAllData.add(PostData(
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

                                    mtAdapter!!.notifyDataSetChanged()
                                }
                            } else {
                                Log.d("f", response.code().toString())
                            }
                        }

                        override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                            Log.d("error", t.toString())
                        }
                    })
                }
            } else {

            }

            println(moreTaxiAllData)
            isLoading = false
        }, 2000)
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val post = it.data?.getSerializableExtra("postData") as PostData
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

                }
            }
        }
    }

    private fun setSelectData() {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getServerPostData("createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
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

                        moreTaxiAllData.clear()
                        totalPages = response.body()!!.totalPages
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
                            moreTaxiAllData.add(PostData(
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

                            mtAdapter!!.notifyDataSetChanged()
                        }
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

    private fun initRecyclerView() {
        setSelectData()
        mtAdapter = MoreTaxiTabAdapter()
        mtAdapter!!.moreTaxiData = moreTaxiAllData
        mttBinding.moreTaxiTabRv.adapter = mtAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        mttBinding.moreTaxiTabRv.setHasFixedSize(true)
        mttBinding.moreTaxiTabRv.layoutManager = manager
    }
}