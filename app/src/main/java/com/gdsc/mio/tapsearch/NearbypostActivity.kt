package com.gdsc.mio.tapsearch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.mio.adapter.NearbyPostAdapter
import com.gdsc.mio.bottomsheetfragment.AnotherBottomSheetFragment
import com.gdsc.mio.model.LocationReadAllResponse
import com.gdsc.mio.model.PostData
import com.gdsc.mio.viewmodel.SharedViewModel
import com.gdsc.mio.noticeboard.NoticeBoardReadActivity
import com.gdsc.mio.R
import com.gdsc.mio.RetrofitServerConnect
import com.gdsc.mio.databinding.ActivityNearbypostBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
                    //nearPostAllData.sortByDescending { it.createDate }


                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    val sortedTargets = nearPostAllData.sortedByDescending {nearData ->
                        // 시간과 날짜를 하나로 결합하여 내림차순으로 정렬
                        LocalDate.parse(nearData.targetDate, dateFormatter).atTime(LocalTime.parse(nearData.targetTime, timeFormatter))
                    }

                    adapter.setData(sortedTargets)
                }
                "마감 임박 순" -> {
                    nbinding.nearFilter.text = "마감 임박 순"
                    nbinding.nearFilter.setTextColor(ContextCompat.getColor(this , R.color.mio_blue_4))

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

                    adapter.setData(sortedTargets)
                }
                "낮은 가격 순" -> {
                    nbinding.nearFilter.text = "낮은 가격 순"
                    nbinding.nearFilter.setTextColor(ContextCompat.getColor(this , R.color.mio_blue_4))
                    nearPostAllData.sortBy { mSort -> mSort.cost }
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
                    Toast.makeText(this@NearbypostActivity, "게시글 정보를 가져오는데 실패하였습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                Toast.makeText(this@NearbypostActivity, "연결에 실패하였습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}