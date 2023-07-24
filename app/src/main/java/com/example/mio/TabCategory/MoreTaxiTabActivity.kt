package com.example.mio.TabCategory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.MoreTaxiTabAdapter
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Model.PostData
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.Model.SharedViewModel
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

    private var moreTaxiAllData = ArrayList<PostData>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mttBinding = ActivityMoreTaxiTabBinding.inflate(layoutInflater)
        //뷰의 이벤트 리스너
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        initRecyclerView()

        mttBinding.moreFilterBtn.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                    }
                })
            }
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

        myViewModel.checkSearchFilter.observe(this) {
            when(it) {
                "최신 순" -> {
                    mttBinding.moreSearchTv.text = "최신 순"
                    mttBinding.moreSearchTv.setTextColor(ContextCompat.getColor(this ,R.color.mio_blue_4))
                    val call = RetrofitServerConnect.service
                    CoroutineScope(Dispatchers.IO).launch {
                        call.getServerPostData().enqueue(object : Callback<PostReadAllResponse> {
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
                                            cost
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
                                            cost
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
                                            cost
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

    private fun setSelectData() {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getServerPostData().enqueue(object : Callback<PostReadAllResponse> {
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
                                cost
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