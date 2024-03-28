package com.example.mio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Adapter.ParticipationAdapter
import com.example.mio.Model.*
import com.example.mio.databinding.ActivityParticipationReceiveBinding
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
import java.util.HashMap

class ParticipationReceiveActivity : AppCompatActivity() {
    private lateinit var pBinding : ActivityParticipationReceiveBinding
    private lateinit var loadingDialog : LoadingProgressDialog
    private lateinit var participationAdapter : ParticipationAdapter

    private var manager : LinearLayoutManager = LinearLayoutManager(this)

    private var participationItemAllData = ArrayList<ParticipationData>()
    private var participantsUserAllData = ArrayList<User>()

    //read에서 받아온 postId 저장
    private var postId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pBinding = ActivityParticipationReceiveBinding.inflate(layoutInflater)

        postId = intent.getIntExtra("postId", 0) as Int

        initParticipationRecyclerView()

        pBinding.backArrow.setOnClickListener {
            finish()
        }

        setContentView(pBinding.root)
    }

    private fun initParticipationRecyclerView() {
        //로딩창 실행
        loadingDialog = LoadingProgressDialog(this)
        loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()

        setParticipationData()
        setParticipantsUserData()

        participationAdapter = ParticipationAdapter()
        participationAdapter.participationItemData = participationItemAllData
        participationAdapter.participantsUserData = participantsUserAllData
        pBinding.participationRv.adapter = participationAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        pBinding.participationRv.setHasFixedSize(true)
        pBinding.participationRv.layoutManager = manager

    }

    private fun setParticipationData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
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
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this@ParticipationReceiveActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
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
        /////////
        //val call = RetrofitServerConnect.service

        api.getParticipationData(postId).enqueue(object : Callback<List<ParticipationData>> {
            override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                if (response.isSuccessful) {
                    participationItemAllData.clear()

                    for (i in response.body()!!.indices) {
                        participationItemAllData.add(
                            ParticipationData(
                                response.body()!![i].postId,
                                response.body()!![i].userId,
                                response.body()!![i].postUserId,
                                response.body()!![i].content,
                                response.body()!![i].approvalOrReject
                            )
                        )
                    }

                    if (participationItemAllData.isNotEmpty()) {
                        pBinding.participationRv.visibility = View.VISIBLE
                        pBinding.nonParticipation.visibility = View.GONE
                    } else {
                        pBinding.participationRv.visibility = View.GONE
                        pBinding.nonParticipation.visibility = View.VISIBLE
                    }
                    println(participationItemAllData)
                    participationAdapter.notifyDataSetChanged()

                } else {
                    println(response.errorBody().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                Log.d("ERROR", t.toString())
            }
        })

    }

    private fun setParticipantsUserData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
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
        ///
        //val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            for (i in participationItemAllData.indices) {
                api.getUserProfileData(participationItemAllData[i].userId).enqueue(object : Callback<User>{
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            Log.d("part response", "success ${response.code()}")
                            participantsUserAllData.add(User(
                                response.body()?.id!!,
                                response.body()?.email!!,
                                response.body()?.studentId!!,
                                response.body()?.profileImageUrl!!,
                                response.body()?.name!!,
                                response.body()?.accountNumber!!,
                                response.body()?.gender!!,
                                response.body()?.verifySmoker!!,
                                response.body()?.roleType!!,
                                response.body()?.status!!,
                                response.body()?.mannerCount!!,
                                response.body()?.grade!!,
                                response.body()?.activityLocation!!
                            ))

                            loadingDialog.dismiss()

                        } else {
                            Log.e("PARTICIPATION RESPONSE ERROR", response.errorBody().toString())
                            Log.i("response code", response.code().toString())
                            Log.d("part", response.message().toString())
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.e("PARTICIPATION ERROR", t.message.toString())
                    }
                })
            }
        }
    }

    /*private fun set() {
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            val token = saveSharedPreferenceGoogleLogin.getToken(activity).toString()
            val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(activity).toString()
            val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(activity)!!.substring(0 until 8)
            val userId = saveSharedPreferenceGoogleLogin.getUserId(activity)!!

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
                api.getMyPostData(userId,"createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
                    override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                        if (response.isSuccessful) {

                            //데이터 청소
                            myAccountPostAllData.clear()

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
                                myAccountPostAllData.add(PostData(
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
                                myAdapter!!.notifyDataSetChanged()
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
        }*/
}