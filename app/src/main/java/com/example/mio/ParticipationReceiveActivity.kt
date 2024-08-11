package com.example.mio

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Adapter.ParticipationAdapter
import com.example.mio.Model.*
import com.example.mio.databinding.ActivityParticipationReceiveBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        initPostDeadLine()
        //기기의 뒤로가기 콜백
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent().apply {
                    putExtra("flag", 33)
                }
                setResult(RESULT_OK, intent)
                finish() // 액티비티 종료
            }
        })

        initParticipationRecyclerView()

        pBinding.backArrow.setOnClickListener {
            val intent = Intent().apply {
                putExtra("flag", 33)
            }
            setResult(RESULT_OK, intent)
            finish()
        }


        participationAdapter.setItemClickListener(object : ParticipationAdapter.ItemClickListener {
            override fun onApprovalClick(position: Int, participantId: String) {
                loadingDialog = LoadingProgressDialog(this@ParticipationReceiveActivity)
                loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                loadingDialog.show()
                CoroutineScope(Dispatchers.IO).launch {
                    setParticipationData()
                }
            }

            override fun onRefuseClick(position: Int, participantId: String) {
                loadingDialog = LoadingProgressDialog(this@ParticipationReceiveActivity)
                loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                loadingDialog.show()
                CoroutineScope(Dispatchers.IO).launch {
                    setParticipationData()
                }
            }
        })

        pBinding.receiveDeadlineBtn.setOnClickListener {
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
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

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
            RetrofitServerConnect.create(this@ParticipationReceiveActivity).patchDeadLinePost(postId).enqueue(object : Callback<Content> {
                override fun onResponse(call: Call<Content>, response: Response<Content>) {
                    if (response.isSuccessful) {
                        Log.e("RetrofitServerConnect", response.code().toString())
                        val responseData = response.body()
                        if (responseData != null) {
                            if (responseData.postType != "BEFORE_DEADLINE") {
                                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_gray_4)) //마감
                                pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                pBinding.receiveDeadlineBtn.text = "마감완료"
                                pBinding.receiveDeadlineBtn.isClickable = false
                            }
                        }
                    } else {
                        Log.e("fff", response.code().toString())
                        Log.e("fff", response.errorBody()?.string()!!)
                    }
                }

                override fun onFailure(call: Call<Content>, t: Throwable) {
                    Log.e("ffffail", t.toString())
                }
            })

        }

    }

    private fun initPostDeadLine() {
        RetrofitServerConnect.create(this@ParticipationReceiveActivity).getPostIdDetailSearch(postId).enqueue(object : Callback<Content> {
            override fun onResponse(call: Call<Content>, response: Response<Content>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.e("indexout check", response.body().toString())
                    if (responseData != null) {
                        if (responseData.postType != "BEFORE_DEADLINE") {
                            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_gray_4)) //마감
                            pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                            pBinding.receiveDeadlineBtn.text = "마감완료"
                            pBinding.receiveDeadlineBtn.isClickable = false
                        }
                    }
                } else {
                    Log.e("fail receive", response.code().toString())
                    Log.e("fail receive", response.errorBody()?.string()!!)
                }
            }

            override fun onFailure(call: Call<Content>, t: Throwable) {
                Log.e("Failure receive", t.toString())
            }
        })
    }

    private fun initParticipationRecyclerView() {
        //로딩창 실행
        loadingDialog = LoadingProgressDialog(this)
        loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            setParticipationData()
        }
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

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
        val thisData : ArrayList<ParticipationData>? = ArrayList()
        api.getParticipationData(postId).enqueue(object : Callback<List<ParticipationData>> {
            override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    println("scssucsucsucs")
                    Log.d("getParticipationData", response.code().toString())
                    Log.d("getParticipationData", responseData.toString())

                    if (responseData?.isNotEmpty() == true) {
                        responseData.let {
                            thisData?.clear()
                            thisData?.addAll(it)
                            participationItemAllData.clear()
                            participationItemAllData.addAll(it)
                        }
                        Log.d("Notification Fragment Data", "Received data: $participationItemAllData")
                        if (participationItemAllData.any { it.content != "작성자" }) {
                            CoroutineScope(Dispatchers.IO).launch {
                                setParticipantsUserData(postList = participationItemAllData.filter { it.content != "작성자" && it.isDeleteYN != "Y"})
                            }
                        } else {
                            Log.e("updateui", "in ui")
                            Log.e("PARTICIPATION RESPONSE DATA", participantsUserAllData.toString())
                            loadingDialog.dismiss()
                            if (participantsUserAllData.isNotEmpty()) {
                                pBinding.participationRv.visibility = View.VISIBLE
                                pBinding.nonParticipation.visibility = View.GONE
                                //pBinding.receiveDeadlineBtn.visibility = View.GONE
                            } else {
                                pBinding.participationRv.visibility = View.GONE
                                pBinding.nonParticipation.visibility = View.VISIBLE
                            }
                        }
                        Log.e("ParticipationReceiveActivity PostId Test", participationItemAllData.toString())
                    } else {
                        Log.e("updateui", "in ui")
                        Log.e("PARTICIPATION RESPONSE DATA", participantsUserAllData.toString())
                        loadingDialog.dismiss()
                        if (participantsUserAllData.isNotEmpty()) {
                            pBinding.participationRv.visibility = View.VISIBLE
                            pBinding.nonParticipation.visibility = View.GONE
                        } else {
                            pBinding.participationRv.visibility = View.GONE
                            pBinding.nonParticipation.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Log.e("parcici receive", response.errorBody()?.string()!!)
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                Log.d("ERROR", t.toString())
            }
        })

    }

    private fun updateUI() {
        Log.e("updateui", "in ui")
        Log.e("PARTICIPATION RESPONSE DATA", participantsUserAllData.toString())
        loadingDialog.dismiss()
        if (participantsUserAllData.isNotEmpty()) {
            pBinding.participationRv.visibility = View.VISIBLE
            pBinding.nonParticipation.visibility = View.GONE
            //pBinding.receiveDeadlineBtn.visibility = View.GONE
        } else {
            pBinding.participationRv.visibility = View.GONE
            pBinding.nonParticipation.visibility = View.VISIBLE
            //pBinding.receiveDeadlineBtn.visibility = View.VISIBLE
        }
        participationAdapter.notifyDataSetChanged()
    }

    private fun setParticipantsUserData(postList: List<ParticipationData>?) {
        Log.e("ParticipationReceiveActivity PostId Test", "진입완료")
        Log.e("setParticipantsUserData", postList.toString())
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

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
        var shouldBreak = false
        if (postList?.isNotEmpty() == true) {
            Log.e("indexout check post", participantsUserAllData.toString())
            Log.e("indexout check post", postList.toString())
            for (i in postList) {
                if (shouldBreak) break
                api.getUserProfileData(i.userId).enqueue(object : Callback<User>{
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            Log.e("indexout check", response.body().toString())
                            if (responseData != null) {
                                responseData.let {
                                    participantsUserAllData.add(
                                        User(
                                            responseData.id,
                                            responseData.email,
                                            responseData.studentId,
                                            responseData.profileImageUrl,
                                            responseData.name,
                                            responseData.accountNumber,
                                            responseData.gender,
                                            responseData.verifySmoker,
                                            responseData.roleType,
                                            responseData.status,
                                            responseData.mannerCount,
                                            responseData.grade,
                                            responseData.activityLocation,
                                        ))

                                }
                                updateUI()
                            }
                        } else {
                            Log.e("PARTICIPATION RESPONSE ERROR",response.errorBody()?.string()!!)
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
}