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
    private var participantsUserAllData = ArrayList<User?>()

    //read에서 받아온 postId 저장
    private var postId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pBinding = ActivityParticipationReceiveBinding.inflate(layoutInflater)
        setContentView(pBinding.root)
        postId = intent.getIntExtra("postId", 0) as Int
        Log.d("ParticipationReceiveActivity PostId Test", postId.toString()) //ok완료

        initParticipationRecyclerView()

        pBinding.backArrow.setOnClickListener {
            finish()
        }


    }

    private fun initParticipationRecyclerView() {
        //로딩창 실행
        loadingDialog = LoadingProgressDialog(this)
        loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()

        setParticipationData()
        //setParticipantsUserData()

        participationAdapter = ParticipationAdapter()

        //val filteredList = participationItemAllData.filter { it.content != "작성자" }
        participationAdapter.participationItemData = participationItemAllData
        //participationAdapter.participationItemData = participationItemAllData
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
                        if (response.body()!![i].content != "작성자") {
                            participationItemAllData.add(
                                ParticipationData(
                                    response.body()!![i].participantId,
                                    response.body()!![i].postId,
                                    response.body()!![i].userId,
                                    response.body()!![i].postUserId,
                                    response.body()!![i].content,
                                    response.body()!![i].approvalOrReject,
                                    response.body()!![i].driverMannerFinish,
                                    response.body()!![i].passengerMannerFinish,
                                    response.body()!![i].verifyFinish
                                )
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                setParticipantsUserData(userId = response.body()!![i].userId)
                            }
                        } else {
                            loadingDialog.dismiss()
                            pBinding.nonParticipation.visibility = View.VISIBLE
                            pBinding.participationRv.visibility = View.GONE
                        }
                    }



                    //participationItemAllData = participationItemAllData.filter { it.content != "작성자" }


                    Log.e("ParticipationReceiveActivity PostId Test", participationItemAllData.toString())

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

    private fun setParticipantsUserData(userId : Int) {
        Log.e("ParticipationReceiveActivity PostId Test", "진입완료")
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        /*val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.substring(0 until 8)
        val profileUserId = saveSharedPreferenceGoogleLogin.getProfileUserId(this)!!*/

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
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
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
        ///////////////////////////////////////////////////
        ///
        //val call = RetrofitServerConnect.service
        api.getUserProfileData(userId).enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    Log.d("part response", "success ${response.code()}")
                    response.body().let {
                        participantsUserAllData.add(it)
                    }
                    Log.e("PARTICIPATION RESPONSE DATA", participantsUserAllData.toString())
                    if (participantsUserAllData.isNotEmpty()) {
                        pBinding.participationRv.visibility = View.VISIBLE
                        pBinding.nonParticipation.visibility = View.GONE
                    } else {
                        pBinding.participationRv.visibility = View.GONE
                        pBinding.nonParticipation.visibility = View.VISIBLE
                    }

                    participationAdapter.notifyDataSetChanged()
                    pBinding.nonParticipation.visibility = View.GONE
                    pBinding.participationRv.visibility = View.VISIBLE
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