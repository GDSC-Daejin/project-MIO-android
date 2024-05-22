package com.example.mio.TapSearch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NearbyPostAdapter
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.Model.LocationUser
import com.example.mio.Model.PostData
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.RetrofitServerConnect
import com.example.mio.databinding.ActivityNearbypostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NearbypostActivity  : AppCompatActivity() { //게시글 더보기 이동 시
    private lateinit var nbinding : ActivityNearbypostBinding
    private lateinit var adapter: NearbyPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbinding = ActivityNearbypostBinding.inflate(layoutInflater)
        setContentView(nbinding.root)

        // 게시글 ID를 받아옵니다.
        val postId = intent.getIntExtra("POST_ID", -1)

        nbinding.rvNearbypostList.layoutManager = LinearLayoutManager(this)
        adapter = NearbyPostAdapter { post ->
            val postData = PostData(
                accountID = post.user?.studentId!!,
                postID = post.postId,
                postTitle = post.title,
                postContent = post.content,
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

        nbinding.backArrow.setOnClickListener {
            finish() // 액티비티 종료
        }
    }

    private fun loadNearbyPostData(postId: Int) {

        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getNearByPostData(postId).enqueue(object : Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        val posts = response.body()
                        posts?.let { allPosts ->
                            val referencePost = allPosts.find { it.postId == postId }
                            referencePost?.let {
                                nbinding.etSearchField2.setText(it.location)
                                val filteredAndSortedPosts = allPosts.filter { post ->
                                    calculateDistance(it.latitude, it.longitude, post.latitude, post.longitude) <= 3.0
                                }.sortedBy { post ->
                                    calculateDistance(it.latitude, it.longitude, post.latitude, post.longitude)
                                }
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