package com.example.mio

import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.mio.model.*
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
    //chip 생성용
    private var passengersChipList = ArrayList<Chip>()
    //chip에 담긴 내용
    private var passengersChipItemData = ArrayList<ChipData>()
    //private var passengersReviewData = ArrayList<String?>()
    private var passengerUserData = ArrayList<User?>() //유저들의 정보

    private var passengerReviewHashMapData = HashMap<Int?, PassengersReviewData?>() //유저id 와 유저에따른리뷰정보

    private var postData : PostData? = null

    private var currentUser = ""
    private var currentContent = ""
    private var currentMannerCount = ""
    private lateinit var loadingDialog : LoadingProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prBinding = ActivityPassengersReviewBinding.inflate(layoutInflater)
        loadingDialog = LoadingProgressDialog(this)
        setIcon()

        type = intent.getStringExtra("type") as String

        if (type == "PASSENGER") { //내가 손님일때
            //Log.e("review", "PASSENGER")
            //driverData = intent.getSerializableExtra("postDriver") as User
            //postData = intent.getSerializableExtra("Data") as PostData?
            postData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("Data")
            } else {
                intent.getParcelableExtra("Data", PostData::class.java)
            }

            driverData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("postDriver")
            } else {
                intent.getParcelableExtra("postDriver", User::class.java)
            }

        } else if (type == "DRIVER") { //내가 운전자일때
            //Log.e("review", "DRIVER")
            postData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("Data")
            } else {
                intent.getParcelableExtra("Data", PostData::class.java)
            }
            passengersData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("postPassengers")
            } else {
                intent.getParcelableArrayListExtra("postPassengers", ParticipationData::class.java)
            }

            //passengersData = intent.getSerializableExtra("postPassengers") as ArrayList<ParticipationData>?
            prBinding.passengersReviewEt.isClickable = false
            passengersData?.forEach {
                passengerReviewHashMapData[it.userId] = null
            }

            userInfo(passengersData)
        }


        prBinding.passengersReviewEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                reviewEditText = editable.toString()
                currentContent = reviewEditText
                //Log.e("afterTextChanged", currentContent)
            }
        })

        prBinding.passengersReviewRegistrationBtn.setOnClickListener {
            if (type == "DRIVER") {
                val chipId = passengersChipItemData.find { it.chipName == currentUser }?.chipId

                if (chipId != null) {
                    passengerReviewHashMapData[chipId] = PassengersReviewData(
                        currentMannerCount,
                        currentContent,
                        postData?.postID
                    )
                    var isNull = false
                    passengerReviewHashMapData.forEach {
                        if (it.value == null || it.value?.content.isNullOrEmpty()) { // 내용도 비어있는지 확인
                            isNull = true
                        }
                    }

                    if (isNull) {
                        Toast.makeText(this@PassengersReviewActivity, "모든 사람의 리뷰를 등록해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        sendReviewData()
                    }
                } else {
                    Toast.makeText(this@PassengersReviewActivity, "사용자의 리뷰 데이터를 찾을 수 없습니다. \n학생 아이디를 클릭하여 등록해주세요", Toast.LENGTH_SHORT).show()

                }


            } else {
                sendReviewData()
            }
        }


        val rootView = prBinding.rootLayout
        val rootLayout = prBinding.passengersReviewLl

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                // If keyboard is visible, move the view up
                rootLayout.translationY = -keypadHeight.toFloat()
            } else {
                // If keyboard is hidden, reset the view position
                rootLayout.translationY = 0f
            }
        }

        prBinding.backArrow.setOnClickListener {
            this.finish()
        }

        setContentView(prBinding.root)
    }

    private fun setupChipListeners() {
        prBinding.reviewSetPassengersCg.children.forEachIndexed { _, view ->
            if (view is Chip) {
                /*if (index == 0 && passengersChipItemData.isNotEmpty()) {
                    val firstChip = passengersChipItemData.first()
                    // 첫 번째 Chip의 스타일 적용
                    currentUser = firstChip.chipName.toString()
                    view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_1))
                    view.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_4))
                    view.setTextColor(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_4))

                    // Chip drawableEnd 변경
                    val drawableEnd = ContextCompat.getDrawable(this@PassengersReviewActivity, R.drawable.review_check_icon)?.mutate()
                    drawableEnd?.setTint(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_4))
                    view.chipIcon = drawableEnd
                }*/
                // Chip에 공통적인 클릭 리스너 설정
                view.setOnClickListener { clickedChip ->
                    handleChipClick(clickedChip as Chip)
                }
            }
        }
    }

    private fun handleChipClick(clickedChip: Chip) {
        val chipName = clickedChip.text.toString()


        // Chip 이름을 기준으로 리뷰 데이터를 추가합니다.
        if (passengersChipItemData.find { it.chipName == chipName } != null) {
            prBinding.passengersReviewEt.isClickable = true

            if (currentContent.isNotEmpty() && currentMannerCount.isNotEmpty() && currentUser.isNotEmpty()) {
                // 모든 필드가 채워진 경우의 로직
                passengerReviewHashMapData[passengersChipItemData.find { it.chipName == currentUser }!!.chipId] = PassengersReviewData(
                    currentMannerCount,
                    currentContent,
                    postData?.postID
                )
            } else {
                // 비어있는 필드가 있는 경우 적절한 Toast 메시지를 표시합니다.
                when {
                    currentContent.isEmpty() && currentMannerCount.isEmpty() -> {
                        Toast.makeText(this, "내용과 매너 점수를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    currentContent.isEmpty() -> {
                        Toast.makeText(this, "내용을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    currentMannerCount.isEmpty() -> {
                        Toast.makeText(this, "매너 점수를 선택해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    currentUser.isEmpty() -> {
                        Toast.makeText(this, "평가를 등록할 사용자를 선택해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // 리뷰 데이터 초기화
            reviewEditText = ""
            currentContent = ""
            currentMannerCount = ""
            // Chip의 아이콘과 스타일 변경
            prBinding.passengersSatisfactionIv.setImageResource(R.drawable.review_satisfaction_icon)
            prBinding.passengersSatisfactionTv.setTextColor(ContextCompat.getColor(this, R.color.mio_gray_6))

            prBinding.passengersCommonlyIv.setImageResource(R.drawable.review_commonly_icon)
            prBinding.passengersCommonlyTv.setTextColor(ContextCompat.getColor(this, R.color.mio_gray_6))

            prBinding.passengersDissatisfactionIv.setImageResource(R.drawable.review_dissatisfaction_icon)
            prBinding.passengersDissatisfactionTv.setTextColor(ContextCompat.getColor(this, R.color.mio_gray_6))

            prBinding.passengersReviewEt.text.clear()


            // Chip의 아이콘과 스타일 변경
            clickedChip.apply {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_1))
                chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_4))
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_4))

                // Chip drawableEnd 변경
                val drawableEnd = ContextCompat.getDrawable(this@PassengersReviewActivity, R.drawable.review_check_icon)
                drawableEnd?.setTint(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_4))
                chipIcon = drawableEnd

                // ChipIcon 색상 변경
                chipIcon?.setTint(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_blue_4))
            }
        } else {
            // Chip 스타일을 기본으로 변경합니다.
            clickedChip.apply {
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_gray_1))
                chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_gray_5))
                setTextColor(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_gray_7))

                chipIcon?.setTint(ContextCompat.getColor(this@PassengersReviewActivity, R.color.mio_gray_7))
            }
        }
        currentUser = chipName
    }

    private fun setIcon() {
        prBinding.passengersSatisfactionIv.setOnClickListener {
            mannerCount = "GOOD"
            currentMannerCount = mannerCount

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
            currentMannerCount = mannerCount
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
            currentMannerCount = mannerCount
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
        loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()
        if (passengersData?.isNotEmpty() == true) {
            Log.e("userInfo", "$passengersData")
            for (i in passengersData) {
                RetrofitServerConnect.create(this@PassengersReviewActivity)
                    .getUserProfileData(i.userId)
                    .enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            if (response.isSuccessful) {
                                val user = response.body()
                                val cGroup = prBinding.reviewSetPassengersCg
                                user?.let {
                                    passengerUserData.add(it)
                                    val newChip = createNewChip(it.studentId, cGroup)
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
                                    /*// 첫 번째 Chip의 이름을 currentUser로 설정
                                    if (passengersChipItemData.isNotEmpty()) {
                                        currentUser = passengersChipItemData.first().chipName.toString()
                                    }*/
                                    setupChipListeners()
                                } ?: run {
                                    Log.e("userInfo", "Response body is null")
                                    loadingDialog.dismiss()
                                }
                            } else {
                                Log.e("userInfo", response.code().toString())
                                response.errorBody()?.string()?.let { errorMsg ->
                                    Log.e("userInfo", errorMsg)
                                }
                                loadingDialog.dismiss()
                            }
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) {
                            Log.e("userInfo", t.toString())
                            loadingDialog.dismiss()
                        }
                    })
            }
        }
    }


    private fun sendReviewData() {
        loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()
        if (type == "DRIVER") { //내가 운전자일 때 손님들의 리뷰데이터를 전송
            if (passengersData != null) {
                for (i in passengerReviewHashMapData) {
                    //val temp = PassengersReviewData(mannerCount, reviewEditText, postData?.postID!!)
                    RetrofitServerConnect.create(this@PassengersReviewActivity).addPassengersReview(i.key!!, i.value!!).enqueue(object : Callback<PassengersReviewData> {
                        override fun onResponse(
                            call: Call<PassengersReviewData>,
                            response: Response<PassengersReviewData>
                        ) {
                            if (response.isSuccessful) {
                                loadingDialog.dismiss()
                                Toast.makeText(this@PassengersReviewActivity, "후기 감사드립니다!", Toast.LENGTH_SHORT).show()
                                this@PassengersReviewActivity.finish()
                            } else {
                                loadingDialog.dismiss()
                                Toast.makeText(this@PassengersReviewActivity, "이미 평가한 유저입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<PassengersReviewData>, t: Throwable) {
                            loadingDialog.dismiss()
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
                        loadingDialog.dismiss()
                        Toast.makeText(this@PassengersReviewActivity, "후기 감사드립니다!", Toast.LENGTH_SHORT).show()
                        this@PassengersReviewActivity.finish()
                    } else {
                        loadingDialog.dismiss()
                        Toast.makeText(this@PassengersReviewActivity,
                            "이미 평가한 운전자입니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PassengersReviewData>, t: Throwable) {
                    Log.e("ERROR", "review : ${t.message.toString()}")
                    loadingDialog.dismiss()
                    Toast.makeText(this@PassengersReviewActivity, "후기 전송에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    private fun createNewChip(text: String, parent : ViewGroup): Chip {
        val chip = layoutInflater.inflate(R.layout.review_chip_layout, parent, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return super.dispatchTouchEvent(ev)
    }
}