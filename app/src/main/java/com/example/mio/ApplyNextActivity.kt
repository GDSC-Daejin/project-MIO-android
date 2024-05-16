package com.example.mio

import android.animation.ObjectAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mio.Helper.AlertReceiver
import com.example.mio.Model.*
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.databinding.ActivityApplyNextBinding
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ApplyNextActivity : AppCompatActivity() {
    private lateinit var anaBinding : ActivityApplyNextBinding
    private lateinit var myViewModel : SharedViewModel
    private var isPosEnd = false
    //read에서 받아온 postId 저장
    private var postId = 0
    private var postData : PostData? = null



    //체크 변수들 true면 작성완료
    private var isSchool = false
    private var isSmoker = false
    private var isGender = false
    private var isSClicked = false
    private var isSmClicked = false
    private var isGClicked = false
    //현재 페이지
    private var currentPage = 1
    //페이지 작성완료
    private var isComplete = false
    //더 적을 내용
    private var applyEditContent = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        anaBinding = ActivityApplyNextBinding.inflate(layoutInflater)
        setContentView(anaBinding.root)
        //뷰의 이벤트 리스너
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        //val s = savedInstanceState.pu
        postId = intent.getIntExtra("postId", 0)
        postData = intent.getSerializableExtra("postData") as PostData

        //뒤로가기
        anaBinding.applyBackArrow.setOnClickListener {
            finish()
        }


        applyFirstVF()
        applySecondVF()
        applyThirdVF()
        bottomEvent()
        //createChannel()

        myViewModel.isGender.observe(this) {
            isGender = it
        }

        myViewModel.isGSchool.observe(this) {
            isSchool = it
        }

        myViewModel.isSmoker.observe(this) {
            isSmoker = it
        }


        myViewModel.checkComplete.observe(this) {
            if (it) {
                CoroutineScope(Dispatchers.Main).launch {
                    anaBinding.applyNext.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_3))
                    }
                }
                anaBinding.applyNext.setOnClickListener {
                    anaBinding.applyViewflipper.showNext()
                    isComplete = !isComplete
                    myViewModel.postCheckComplete(false)
                    currentPage += 1
                    println(currentPage)
                    myViewModel.postCheckPage(currentPage)
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    anaBinding.applyNext.apply {
                        setBackgroundResource(R.drawable.btn_default_background)
                        setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_8))
                    }
                }
                anaBinding.applyNext.setOnClickListener {

                }
            }
        }


        myViewModel.checkCurrentPage.observe(this) {
            when (it) {
                3 -> {
                    anaBinding.applyNext.visibility = View.GONE
                    anaBinding.applyPre.visibility = View.GONE
                    anaBinding.applyBottomSpace.visibility = View.GONE
                    anaBinding.applyCompleteBtn.visibility = View.VISIBLE

                    anaBinding.applyViewflipper.visibility = View.GONE
                    anaBinding.applyFifthVf.visibility = View.VISIBLE
                    val fadeIn = ObjectAnimator.ofFloat(anaBinding.applyCompleteBtn, "alpha", 0f, 1f)
                    fadeIn.duration = 1500
                    fadeIn.start()
                }
                else -> {
                    anaBinding.applyNext.visibility = View.VISIBLE
                    anaBinding.applyPre.visibility = View.VISIBLE
                    anaBinding.applyBottomSpace.visibility = View.VISIBLE
                    anaBinding.applyCompleteBtn.visibility = View.GONE
                }
            }
        }


    }

    private fun applyFirstVF() {
        //등하교
        anaBinding.applyGtschoolBtn.setOnClickListener {
            myViewModel.postGSchool(GSchool = true)
            isSClicked = true
            updateButtonStatus()
            anaBinding.applyGtschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
            }
            anaBinding.applyAschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_11))
            }
        }

        anaBinding.applyAschoolBtn.setOnClickListener {
            myViewModel.postGSchool(GSchool = false)
            isSClicked = true
            updateButtonStatus()
            anaBinding.applyAschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
            }
            anaBinding.applyGtschoolBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_11))
            }
        }

        //성별
        anaBinding.applyManBtn.setOnClickListener {
            myViewModel.postGender(Gender = true)
            isGClicked = true
            updateButtonStatus()
            anaBinding.applyManBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
            }
            anaBinding.applyWomanBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_11))
            }
        }

        anaBinding.applyWomanBtn.setOnClickListener {
            myViewModel.postGender(Gender = false)
            isGClicked = true
            updateButtonStatus()
            anaBinding.applyWomanBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
            }
            anaBinding.applyManBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_11))
            }
        }

        //흡연여부
        anaBinding.applySmokerBtn.setOnClickListener {
            myViewModel.postSmoker(Smoker = true)
            isSmClicked = true
            updateButtonStatus()
            anaBinding.applySmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
            }
            anaBinding.applyNsmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_11))
            }
        }

        anaBinding.applyNsmokerBtn.setOnClickListener {
            myViewModel.postSmoker(Smoker = false)
            isSmClicked = true
            updateButtonStatus()
            anaBinding.applyNsmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
            }
            anaBinding.applySmokerBtn.apply {
                setBackgroundResource(R.drawable.round_btn_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_11))
            }
        }
    }

    private fun applySecondVF() {
        anaBinding.applyEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {


            }
            override fun afterTextChanged(editable: Editable) {
                applyEditContent = editable.toString()
                /*if (editable.isEmpty()) {
                    Toast.makeText("")
                }*/

                if (editable.isNotEmpty()) {
                    anaBinding.applyNext.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
                    }
                    anaBinding.applyNext.setOnClickListener {
                        anaBinding.applyViewflipper.showNext()
                        isComplete = !isComplete
                        myViewModel.postCheckComplete(false)
                        currentPage += 1
                        println(currentPage)
                        myViewModel.postCheckPage(currentPage)
                    }
                }
                //깜빡임 제거
                anaBinding.applyEt.clearFocus()
                anaBinding.applyEt.movementMethod = null

            }
        })
    }

    private fun applyThirdVF() {
        anaBinding.applyCompleteBtn.setOnClickListener {
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
            val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
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
                        /*newRequest =
                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                        val intent = Intent(this@ApplyNextActivity, LoginActivity::class.java)
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
            ///////////////////////////////
            val temp = ParticipateData(applyEditContent)

            CoroutineScope(Dispatchers.IO).launch {
               api.addParticipate(postId, temp).enqueue(object : Callback<String?> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful) {
                            println("succcc")
                            //postData?.let { it1 -> setNotification("참여 신청이 완료되었습니다!", it1) }
                            sendAlarmData()
                            Toast.makeText(this@ApplyNextActivity, "참여 신청 하셨습니다!", Toast.LENGTH_SHORT).show()
                        } else {
                            println("faafa")
                            Log.d("add", response.errorBody()?.string()!!)
                            Log.d("message", call.request().toString())
                            println(response.code())
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        Log.d("error", t.toString())
                    }

                })
            }
            this@ApplyNextActivity.finish()
        }
    }

    private fun bottomEvent() {
        myViewModel.postCheckPage(currentPage)

        anaBinding.applyPre.setOnClickListener {
            if (currentPage <= 1) {
                //뒤로가기
                finish()
            } else {
                currentPage -= 1
                myViewModel.postCheckComplete(true)
                anaBinding.applyViewflipper.showPrevious()
            }
        }
    }

    private fun updateButtonStatus() {
        val conditions = Conditions(isSClicked, isSmClicked, isGClicked)
        println("s ${conditions.isSClicked}")
        println("sm $${conditions.isSmClicked}")
        println("g ${conditions.isGClicked}")

        val shouldEnableButton = conditions.shouldEnableButton()
        println(shouldEnableButton)
        if (shouldEnableButton) {
            anaBinding.applyNext.apply {
                setBackgroundResource(R.drawable.round_btn_update_layout)
                setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_1))
            }
            anaBinding.applyNext.setOnClickListener {
                anaBinding.applyViewflipper.showNext()
                isComplete = !isComplete
                myViewModel.postCheckComplete(false)
                currentPage += 1
                println(currentPage)
                myViewModel.postCheckPage(currentPage)
            }
        } else {

        }
        anaBinding.applyNext.isEnabled = shouldEnableButton
    }

    private fun sendAlarmData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)?.substring(0..7).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
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
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this@ApplyNextActivity, LoginActivity::class.java)
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
        ///////////////////////////////
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val currentDate = sdf.format(date)
        val formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        val result: Instant = Instant.from(formatter.parse(currentDate))
        //userId 가 알람 받는 사람
        val temp = AddAlarmData(result.toString(), "신청${identification}${applyEditContent}", postId, postData!!.user.id)

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