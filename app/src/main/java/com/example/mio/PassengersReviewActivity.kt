package com.example.mio

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mio.Model.*
import com.example.mio.databinding.ActivityPassengersReviewBinding
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PassengersReviewActivity : AppCompatActivity() {
    private lateinit var prBinding : ActivityPassengersReviewBinding
    //edittext
    private var reviewEditText = ""

    private var mannerCount = ""

    private var type = ""
    private var passengersData: ArrayList<ParticipationData>? = null
    private var driverData : User? = null
    private var passengersChipList = ArrayList<Chip>()
    private var passengersChipItemData = ArrayList<ChipData>()
    private var passengersReviewData = ArrayList<String?>()
    private var passengerUserData = ArrayList<User?>()

    private var postData : PostData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prBinding = ActivityPassengersReviewBinding.inflate(layoutInflater)

        setIcon()

        type = intent.getStringExtra("type") as String

        if (type == "PASSENGER") { //내가 손님일때
            Log.e("review", "PASSENGER")
            driverData = intent.getSerializableExtra("postDriver") as User
            postData = intent.getSerializableExtra("Data") as PostData?

        } else if (type == "DRIVER") { //내가 운전자일때
            Log.e("review", "DRIVER")
            postData = intent.getSerializableExtra("Data") as PostData?
            passengersData = intent.getSerializableExtra("postPassengers") as ArrayList<ParticipationData>?
            userInfo(passengersData)
            /*if (passengersData != null) {
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
            }*/
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
            mannerCount = "GOOD"

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
            mannerCount = "NORMAL"

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
            mannerCount = "BAD"

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

    private fun userInfo(passengersData: ArrayList<ParticipationData>?) {
        if (passengersData?.isNotEmpty() == true) {
            for (i in passengersData) {
                RetrofitServerConnect.create(this@PassengersReviewActivity).getUserProfileData(i.userId).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            val user = response.body()
                            user?.let {
                                passengerUserData.add(it)
                                val newChip = createNewChip(it.studentId)
                                passengersChipList.add(newChip)
                                passengersChipItemData.add(ChipData(it.studentId, it.id))

                                // Chip 추가 로직
                                if (prBinding.reviewSetPassengersCg.childCount >= 0) {
                                    // 마지막 Chip 앞에 추가
                                    prBinding.reviewSetPassengersCg.addView(newChip, prBinding.reviewSetPassengersCg.childCount - 1)
                                } else {
                                    // ChipGroup에 자식이 없는 경우, 그냥 추가
                                    prBinding.reviewSetPassengersCg.addView(newChip)
                                }
                            } ?: run {
                                Log.e("userInfo", "Response body is null")
                            }
                        } else {
                            Log.e("userInfo", response.code().toString())
                            response.errorBody()?.string()?.let { errorMsg ->
                                Log.e("userInfo", errorMsg)
                            }
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.e("userInfo", t.toString())
                    }
                })
            }
        }
    }


    private fun sendReviewData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        ///
        if (type == "DRIVER") { //내가 운전자일 때 손님들의 리뷰데이터를 전송
            if (passengersData != null) {
                for (i in passengersData!!.indices) {
                    val temp = PassengersReviewData(mannerCount, reviewEditText, postData?.postID!!)
                    RetrofitServerConnect.create(this@PassengersReviewActivity).addPassengersReview(passengersData!![i].userId, temp).enqueue(object : Callback<PassengersReviewData> {
                        override fun onResponse(
                            call: Call<PassengersReviewData>,
                            response: Response<PassengersReviewData>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("SUCCESS review", "탑승자들의 review를 잘보냄 : ${response.code()}")
                                this@PassengersReviewActivity.finish()
                            } else {
                                Log.e("ERROR", "review1 : ${response.errorBody()?.string()!!}")
                                Log.e("ERROR", "review2 : ${response.message().toString()}")
                                Log.e("ERROR", "review3 : ${response.code().toString()}")
                                Toast.makeText(this@PassengersReviewActivity, response.errorBody()?.string()!!, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<PassengersReviewData>, t: Throwable) {
                            Log.e("ERROR", "review : ${t.message.toString()}")
                        }
                    })
                }
            }
        } else { //내가 손님일 때 운전자의 리뷰데이터를 전송
            val temp = DriversReviewData(mannerCount, reviewEditText)
            RetrofitServerConnect.create(this@PassengersReviewActivity).addDriversReview(postData?.postID!!, temp).enqueue(object : Callback<PassengersReviewData> {
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

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.review_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }
}