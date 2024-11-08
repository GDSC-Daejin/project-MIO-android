package com.example.mio

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mio.model.*
import com.example.mio.noticeboard.NoticeBoardReadActivity
import com.example.mio.databinding.ActivityApplyNextBinding
import com.example.mio.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ApplyNextActivity : AppCompatActivity() {
    private lateinit var anaBinding : ActivityApplyNextBinding
    private lateinit var myViewModel : SharedViewModel

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
        postData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("postData")
        } else {
            intent.getParcelableExtra("postData", PostData::class.java)
        }


        //뒤로가기
        anaBinding.applyBackArrow.setOnClickListener {
            val intent = Intent(this@ApplyNextActivity, NoticeBoardReadActivity::class.java).apply {
                putExtra("flag", 389)
            }
            setResult(RESULT_OK, intent)
            finish() // 액티비티 종료
        }
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ApplyNextActivity, NoticeBoardReadActivity::class.java).apply {
                    putExtra("flag", 389)
                }
                setResult(RESULT_OK, intent)
                finish() // 액티비티 종료
            }
        })


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
                    // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                    val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                    if (inputMethodManager.isActive) {
                        // 가상 키보드가 올라가 있다면 내립니다.
                        inputMethodManager.hideSoftInputFromWindow(anaBinding.applyNext.windowToken, 0)
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    anaBinding.applyNext.apply {
                        setBackgroundResource(R.drawable.btn_default_background)
                        setTextColor(ContextCompat.getColor(this@ApplyNextActivity ,R.color.mio_gray_8))
                    }
                }
                // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                if (inputMethodManager.isActive) {
                    // 가상 키보드가 올라가 있다면 내립니다.
                    inputMethodManager.hideSoftInputFromWindow(anaBinding.applyNext.windowToken, 0)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
                setBackgroundResource(R.drawable.edit_check_btn)
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
            }
        })
    }

    private fun applyThirdVF() {
        val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 가상 키보드가 올라가 있는지 여부를 확인합니다.
        if (inputMethodManager.isActive) {
            // 가상 키보드가 올라가 있다면 내립니다.
            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        anaBinding.applyCompleteBtn.setOnClickListener {
            val temp = ParticipateData(applyEditContent)

            RetrofitServerConnect.create(this@ApplyNextActivity).addParticipate(postId, temp).enqueue(object : Callback<ParticipationData> {
                override fun onResponse(
                    call: Call<ParticipationData>,
                    response: Response<ParticipationData>
                ) {
                    if (response.isSuccessful) {
                        //postData?.let { it1 -> setNotification("참여 신청이 완료되었습니다!", it1) }
                        //sendAlarmData()
                        Toast.makeText(this@ApplyNextActivity, "참여 신청이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ApplyNextActivity, "참여 신청에 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ParticipationData>, t: Throwable) {
                    Toast.makeText(this@ApplyNextActivity, "참여 신청에 실패했습니다. 다시 시도해주세요 ${t.message}", Toast.LENGTH_SHORT).show()

                }
            })

            val intent = Intent(this@ApplyNextActivity, NoticeBoardReadActivity::class.java).apply {
                putExtra("flag", 389)
            }
            setResult(RESULT_OK, intent)
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
                myViewModel.postCheckPage(currentPage)
            }
        }
        anaBinding.applyNext.isEnabled = shouldEnableButton
    }

}