package com.example.mio

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.mio.Model.*
import com.example.mio.databinding.ActivityPassengersReviewBinding
import com.google.android.material.chip.Chip
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

class PassengersReviewActivity : AppCompatActivity() {
    private lateinit var prBinding : ActivityPassengersReviewBinding
    //edittext
    private var reviewEditText = ""

    private var mannerCount = ""

    private var type = ""
    private var passengersData: ArrayList<Participants>? = null
    private var driverData : User? = null
    private var passengersChipList = ArrayList<Chip>()
    private var passengersChipItemData = ArrayList<ChipData>()
    private var passengersReviewData = ArrayList<String?>()

    private var postData : PostData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prBinding = ActivityPassengersReviewBinding.inflate(layoutInflater)

        setIcon()

        type = intent.getStringExtra("type") as String

        if (type == "PASSENGER") { //내가 손님일때
            driverData = intent.getSerializableExtra("postDriver") as User
            postData = intent.getSerializableExtra("Data") as PostData?

        } else if (type == "DRIVER") { //내가 운전자일때
            postData = intent.getSerializableExtra("Data") as PostData?
            passengersData = intent.getSerializableExtra("postPassengers") as ArrayList<Participants>?

            if (passengersData != null) {
                //여기에 인원 수 만큼 chip? 추가하기 Todo
                for (j in passengersData!!.indices) {
                    passengersChipList.add(createNewChip(
                        passengersData!![j].studentId
                    ))
                    passengersChipItemData.add(ChipData(passengersData!![j].studentId, j))
                }

                for (i in passengersChipList.indices) {
                    // 마지막 Chip 뷰의 인덱스를 계산
                    val lastChildIndex = prBinding.reviewSetPassengersCg.childCount - 1

                    // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                    // 현재 Chip을 바로 그 앞에 추가
                    if (lastChildIndex >= 0) {
                        prBinding.reviewSetPassengersCg.addView(passengersChipList[i], lastChildIndex)
                    } else {
                        // ChipGroup에 자식이 없는 경우, 그냥 추가
                        prBinding.reviewSetPassengersCg.addView(passengersChipList[i])
                    }
                }
            }
        }


        prBinding.passengersReviewEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                reviewEditText = editable.toString()
                /*if (editable.isEmpty()) {
                    nbrBinding.readSendComment.visibility = View.GONE
                    nbrBinding.readEditSendComment.visibility = View.GONE
                } else if (getBottomSheetCommentData != "수정"){
                    nbrBinding.readSendComment.visibility = View.VISIBLE
                    nbrBinding.readEditSendComment.visibility = View.GONE
                } else {
                    nbrBinding.readSendComment.visibility = View.GONE
                    nbrBinding.readEditSendComment.visibility = View.VISIBLE
                }*/
            }
        })

        prBinding.passengersReviewRegistrationBtn.setOnClickListener {
            sendReviewData()
        }

        prBinding.reviewSetPassengersCg.setOnCheckedStateChangeListener { group, checkedId ->
            val selectedChip = prBinding.reviewSetPassengersCg.findViewById<Chip>(prBinding.reviewSetPassengersCg.checkedChipId)
            val chipName = selectedChip?.text


            if (passengersChipItemData.find { it.chipName == chipName } != null) {
                passengersReviewData.add(reviewEditText)
                reviewEditText = ""
                prBinding.passengersReviewEt.text.clear()
                //background 변경
                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this , R.color.mio_blue_1))
                selectedChip.backgroundTintList = colorStateList
                //테두리 변경
                selectedChip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mio_blue_4))
                //텍스트컬러 변경
                selectedChip.setTextColor(ContextCompat.getColor(this, R.color.mio_blue_4))
                //체크 아이콘 색변경?
                val drawable = selectedChip.chipIcon
                drawable?.setTint(ContextCompat.getColor(this, R.color.mio_blue_4))
            } else {
                //background 변경
                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this , R.color.mio_gray_1))
                selectedChip.backgroundTintList = colorStateList
                //테두리 변경
                selectedChip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.mio_gray_5))
                //텍스트컬러 변경
                selectedChip.setTextColor(ContextCompat.getColor(this, R.color.mio_gray_7))
                //체크 아이콘 색변경?
                val drawable = selectedChip.chipIcon
                drawable?.setTint(ContextCompat.getColor(this, R.color.mio_gray_7))
            }

            Log.d("TAG", "currentCategory: $chipName")
        }


        prBinding.backArrow.setOnClickListener {
            this.finish()
        }

        setContentView(prBinding.root)
    }

    private fun setIcon() {
        prBinding.passengersSatisfactionIv.setOnClickListener {
            mannerCount = "good"

            prBinding.passengersSatisfactionIv.apply {
                setImageResource(R.drawable.review_satisfaction_update_icon)
            }
            prBinding.passengersSatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_9))
            }

            prBinding.passengersCommonlyIv.apply {
                setImageResource(R.drawable.review_commonly_icon)
            }
            prBinding.passengersCommonlyTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }

            prBinding.passengersDissatisfactionIv.apply {
                setImageResource(R.drawable.review_dissatisfaction_icon)
            }
            prBinding.passengersDissatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }
        }

        prBinding.passengersCommonlyIv.setOnClickListener {
            mannerCount = "normal"

            prBinding.passengersCommonlyIv.apply {
                setImageResource(R.drawable.review_commonly_update_icon)
            }
            prBinding.passengersCommonlyTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_9))
            }

            prBinding.passengersDissatisfactionIv.apply {
                setImageResource(R.drawable.review_dissatisfaction_icon)
            }
            prBinding.passengersDissatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }

            prBinding.passengersSatisfactionIv.apply {
                setImageResource(R.drawable.review_satisfaction_icon)
            }
            prBinding.passengersSatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }
        }



        prBinding.passengersDissatisfactionIv.setOnClickListener {
            mannerCount = "bad"

            prBinding.passengersDissatisfactionIv.apply {
                setImageResource(R.drawable.review_dissatisfaction_update_icon)
            }
            prBinding.passengersDissatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_9))
            }

            prBinding.passengersSatisfactionIv.apply {
                setImageResource(R.drawable.review_satisfaction_icon)
            }
            prBinding.passengersSatisfactionTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }

            prBinding.passengersCommonlyIv.apply {
                setImageResource(R.drawable.review_commonly_icon)
            }
            prBinding.passengersCommonlyTv.apply {
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity , R.color.mio_gray_6))
            }
        }

    }

    private fun sendReviewData() {
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
                    val intent = Intent(this@PassengersReviewActivity, LoginActivity::class.java)
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
        ///
        if (type == "DRIVER") { //내가 운전자일 때 손님들의 리뷰데이터를 전송
            if (passengersData != null) {
                for (i in passengersData!!.indices) {
                    val temp = PassengersReviewData(mannerCount, reviewEditText, postData?.postID!!)
                    CoroutineScope(Dispatchers.IO).launch {
                        api.addPassengersReview(passengersData!![i].id, temp).enqueue(object : Callback<PassengersReviewData> {
                            override fun onResponse(
                                call: Call<PassengersReviewData>,
                                response: Response<PassengersReviewData>
                            ) {
                                if (response.isSuccessful) {
                                    Log.d("SUCCESS review", "탑승자들의 review를 잘보냄 : ${response.code()}")


                                } else {
                                    Log.e("ERROR", "review1 : ${response.errorBody()?.string()!!}")
                                    Log.e("ERROR", "review2 : ${response.message().toString()}")
                                    Log.e("ERROR", "review3 : ${response.code().toString()}")
                                }
                            }

                            override fun onFailure(call: Call<PassengersReviewData>, t: Throwable) {
                                Log.e("ERROR", "review : ${t.message.toString()}")
                            }
                        })
                    }
                }
            }
        } else { //내가 손님일 때 운전자의 리뷰데이터를 전송
            val temp = DriversReviewData(mannerCount, reviewEditText)
            CoroutineScope(Dispatchers.IO).launch {
                api.addDriversReview(postData?.postID!!, temp).enqueue(object : Callback<PassengersReviewData> {
                    override fun onResponse(
                        call: Call<PassengersReviewData>,
                        response: Response<PassengersReviewData>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("SUCCESS review", "운전자의 review를 잘보냄 : ${response.code()}")


                        } else {
                            Log.e("ERROR", "review1 : ${response.errorBody()?.string()!!}")
                            Log.e("ERROR", "review2 : ${response.message().toString()}")
                            Log.e("ERROR", "review3 : ${response.code().toString()}")
                        }
                    }

                    override fun onFailure(call: Call<PassengersReviewData>, t: Throwable) {
                        Log.e("ERROR", "review : ${t.message.toString()}")
                    }
                })
            }
        }

    }

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.review_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }
}