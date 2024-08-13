package com.example.mio.TapSearch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NearbyPostAdapter
import com.example.mio.BottomSheetFragment.AnotherBottomSheetFragment
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.Model.LocationUser
import com.example.mio.Model.PostData
import com.example.mio.Model.SharedViewModel
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.R
import com.example.mio.RetrofitServerConnect
import com.example.mio.databinding.ActivityNearbypostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class NearbypostActivity  : AppCompatActivity() { //검색에서 게시글 더보기 이동 시
    private lateinit var nbinding : ActivityNearbypostBinding
    private lateinit var adapter: NearbyPostAdapter
    private lateinit var myViewModel : SharedViewModel
    private var nearPostAllData = ArrayList<LocationReadAllResponse>()
    private var firstLatitude =""
    private var firstLongitude =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbinding = ActivityNearbypostBinding.inflate(layoutInflater)
        setContentView(nbinding.root)
        //뷰의 이벤트 리스너
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        // 게시글 ID를 받아옵니다.
        val postId = intent.getIntExtra("POST_ID", -1)
        val searchWord = intent.getStringExtra("searchWord")
        nbinding.searchWordField.text = searchWord

        nbinding.rvNearbypostList.layoutManager = LinearLayoutManager(this)
        adapter = NearbyPostAdapter { post ->
            val postData = PostData(
                accountID = post.user?.studentId!!,
                postID = post.postId,
                postTitle = post.title,
                postContent = post.content,
                postCreateDate = post.createDate,
                postTargetDate = post.targetDate,
                postTargetTime = post.targetTime,
                postCategory = post.category?.categoryName!!,
                postLocation = post.location,
                postParticipation = post.participantsCount,
                postParticipationTotal = post.numberOfPassengers,
                postCost = post.cost,
                postVerifyGoReturn = post.verifyGoReturn,
                user = post.user!!,
                postlatitude = post.latitude,
                postlongitude = post.longitude
            )

            // Intent를 통해 NoticeBoardReadActivity로 전달
            val intent = Intent(this, NoticeBoardReadActivity::class.java)
            intent.putExtra("type", "READ")
            intent.putExtra("postItem", postData)
            startActivity(intent)
        }
        nbinding.rvNearbypostList.adapter = adapter

        loadNearbyPostData(postId)


        nbinding.nearFilter.setOnClickListener {
            val bottomSheet = AnotherBottomSheetFragment()
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : AnotherBottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        myViewModel.postCheckSearchFilter(value)
                    }
                })
            }
        }

        myViewModel.checkSearchFilter.observe(this) {
            when(it) {
                "최신 순" -> {
                    nbinding.nearFilter.text = "최신 순"
                    nbinding.nearFilter.setTextColor(ContextCompat.getColor(this , R.color.mio_blue_4))
                    nearPostAllData.sortByDescending {data ->  data.createDate }
                    adapter.setData(nearPostAllData)
                }
                "마감 임박 순" -> {
                    nbinding.nearFilter.text = "마감 임박 순"
                    nbinding.nearFilter.setTextColor(ContextCompat.getColor(this , R.color.mio_blue_4))
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    // 날짜 및 시간 형식 지정
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    // 정렬 로직
                    val sortedTargets = nearPostAllData.sortedWith { t1, t2 ->
                        // 날짜 비교
                        val dateComparison = LocalDate.parse(t1.targetDate, dateFormatter)
                            .compareTo(LocalDate.parse(t2.targetDate, dateFormatter))

                        // 날짜가 같으면 시간 비교
                        if (dateComparison == 0) {
                            LocalTime.parse(t1.targetTime, timeFormatter)
                                .compareTo(LocalTime.parse(t2.targetTime, timeFormatter))
                        } else {
                            dateComparison
                        }
                    }

                    // 리스트를 날짜(date) 먼저, 시간(time) 다음으로 정렬
                   /* nearPostAllData.sortedWith(compareBy<LocationReadAllResponse?> { sdf.parse(it?.targetDate + " " + it?.targetTime) }
                        .thenBy { it?.targetTime })*/

                    adapter.setData(sortedTargets)
                }
                "낮은 가격 순" -> {
                    nbinding.nearFilter.text = "낮은 가격 순"
                    nbinding.nearFilter.setTextColor(ContextCompat.getColor(this , R.color.mio_blue_4))
                    nearPostAllData.sortBy { it?.cost }
                    adapter.setData(nearPostAllData)
                }
                "가까운 순" -> {
                    nbinding.nearFilter.text = "가까운 순"
                    nbinding.nearFilter.setTextColor(ContextCompat.getColor(this , R.color.mio_blue_4))
                    val filteredAndSortedPosts = nearPostAllData.filter { post ->
                        calculateDistance(firstLatitude.toDouble(), firstLongitude.toDouble(), post.latitude, post.longitude) <= 3.0
                    }.sortedBy { post ->
                        calculateDistance(firstLatitude.toDouble(), firstLongitude.toDouble(), post.latitude, post.longitude)
                    }
                    adapter.setData(filteredAndSortedPosts)
                }
            }
        }


        nbinding.backArrow.setOnClickListener {
            finish() // 액티비티 종료
        }
    }

    private fun loadNearbyPostData(postId: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(this@NearbypostActivity).getNearByPostData(postId).enqueue(object : Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        val posts = response.body()
                        posts?.let { allPosts ->
                            val referencePost = allPosts.find { it.postId == postId }
                            referencePost?.let {
                                val filteredAndSortedPosts = allPosts.filter { post ->
                                    calculateDistance(it.latitude, it.longitude, post.latitude, post.longitude) <= 3.0
                                }.sortedBy { post ->
                                    calculateDistance(it.latitude, it.longitude, post.latitude, post.longitude)
                                }
                                firstLatitude = it.latitude.toString()
                                firstLongitude = it.longitude.toString()
                                nearPostAllData.addAll(filteredAndSortedPosts)
                                adapter.setData(filteredAndSortedPosts)
                            }
                        }
                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    override fun onStart() {
        super.onStart()
        Log.d("nearbyPostActivity", "start")
    }
}