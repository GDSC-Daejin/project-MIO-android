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
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
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
    private var targetDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pBinding = ActivityParticipationReceiveBinding.inflate(layoutInflater)
        setContentView(pBinding.root)
        postId = intent.getIntExtra("postId", 0) as Int
        targetDate = intent.getStringExtra("targetDate").toString()

        Log.d("ParticipationReceiveActivity PostId Test", postId.toString()) //ok완료
        initPostDeadLine()
        //기기의 뒤로가기 콜백
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ParticipationReceiveActivity, NoticeBoardReadActivity::class.java).apply {
                    putExtra("flag", 389)
                }
                setResult(RESULT_OK, intent)
                finish() // 액티비티 종료
            }
        })

        initParticipationRecyclerView()

        pBinding.backArrow.setOnClickListener {
            val intent = Intent(this@ParticipationReceiveActivity, NoticeBoardReadActivity::class.java).apply {
                putExtra("flag", 389)
            }
            setResult(RESULT_OK, intent)
            finish()
        }


        participationAdapter.setItemClickListener(object : ParticipationAdapter.ItemClickListener {
            override fun onApprovalClick(position: Int, participantId: String) {
                /*loadingDialog = LoadingProgressDialog(this@ParticipationReceiveActivity)
                loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                loadingDialog.show()*/
                Log.e("onApprovalClick", "onApprovalClick")
            }

            override fun onRefuseClick(position: Int, participantId: String) {
                /*loadingDialog = LoadingProgressDialog(this@ParticipationReceiveActivity)
                loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                loadingDialog.show()*/
                Log.e("onRefuseClick", "onRefuseClick")
            }
        })
    }

    private fun postDeadLineClick(deadLine : Boolean) {
        pBinding.receiveDeadlineBtn.setOnClickListener {
            //deadLine
            if (deadLine) {
                RetrofitServerConnect.create(this@ParticipationReceiveActivity).patchDeadLinePost(postId).enqueue(object : Callback<Content> {
                    override fun onResponse(call: Call<Content>, response: Response<Content>) {
                        if (response.isSuccessful) {
                            Log.e("deadline", response.code().toString())
                            val responseData = response.body()
                            if (responseData != null) {
                                //BEFORE_DEADLINE, DEADLINE, COMPLETED
                                when (responseData.postType) {
                                    "DEADLINE" -> {
                                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_blue_5)) //마감
                                        pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                        pBinding.receiveDeadlineBtn.text = "운행 완료"
                                        postComplete()
                                    }
                                    else -> { //completed
                                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_gray_4)) //마감
                                        pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                        pBinding.receiveDeadlineBtn.text = "운행 종료"
                                        pBinding.receiveDeadlineBtn.isClickable = false
                                    }
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
    }

    private fun postComplete() {
        pBinding.receiveDeadlineBtn.setOnClickListener {
            RetrofitServerConnect.create(this@ParticipationReceiveActivity).patchCompletePost(postId).enqueue(object : Callback<Content> {
                override fun onResponse(call: Call<Content>, response: Response<Content>) {
                    if (response.isSuccessful) {
                        Log.e("deadlineComplete", response.code().toString())
                        val responseData = response.body()
                        if (responseData != null) {
                            //BEFORE_DEADLINE, DEADLINE, COMPLETED
                            when (responseData.postType) {
                                "BEFORE_DEADLINE" -> {
                                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_blue_5)) //마감
                                    pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                    pBinding.receiveDeadlineBtn.text = "마감하기"
                                }
                                "DEADLINE" -> {
                                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_blue_5)) //마감
                                    pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                    pBinding.receiveDeadlineBtn.text = "운행 완료"
                                }
                                else -> { //completed
                                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_gray_4)) //마감
                                    pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                    pBinding.receiveDeadlineBtn.text = "운행 종료"
                                    setParticipationData()
                                    pBinding.receiveDeadlineBtn.isClickable = false
                                }
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
                        when (responseData.postType) {
                            "BEFORE_DEADLINE" -> {
                                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_blue_5)) //마감
                                pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                pBinding.receiveDeadlineBtn.text = "마감하기"
                                postDeadLineClick(true)
                            }
                            "DEADLINE" -> {
                                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_blue_5)) //마감
                                pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                pBinding.receiveDeadlineBtn.text = "운행 완료"
                                postComplete()
                            }
                            else -> { //completed
                                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@ParticipationReceiveActivity , R.color.mio_gray_4)) //마감
                                pBinding.receiveDeadlineBtn.backgroundTintList = colorStateList
                                pBinding.receiveDeadlineBtn.text = "운행 종료"
                                pBinding.receiveDeadlineBtn.isClickable = false
                            }
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
        participationAdapter.participationItemData = participationItemAllData
        participationAdapter.participantsUserData = participantsUserAllData

        pBinding.participationRv.adapter = participationAdapter

        participationAdapter.target = targetDate
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

        RetrofitServerConnect.create(this@ParticipationReceiveActivity).getParticipationData(postId).enqueue(object : Callback<List<ParticipationData>> {
            override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseData ->
                        participationItemAllData.apply {
                            clear()
                            addAll(responseData.filter { it.isDeleteYN != "Y" && it.content != "작성자" })
                        }

                        if (participationItemAllData.isNotEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                setParticipantsUserData(participationItemAllData)
                            }
                        } else {
                            handleEmptyData()
                        }
                    } ?: handleEmptyData()
                } else {
                    handleError(response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                handleError(t.message)
            }
        })

    }

    private fun updateUI() {
        CoroutineScope(Dispatchers.Main).launch {
            loadingDialog.dismiss()
            if (participantsUserAllData.isNotEmpty()) {
                pBinding.participationRv.visibility = View.VISIBLE
                pBinding.nonParticipation.visibility = View.GONE
            } else {
                pBinding.participationRv.visibility = View.GONE
                pBinding.nonParticipation.visibility = View.VISIBLE
            }
            participationAdapter.notifyDataSetChanged()
        }
    }

    private fun setParticipantsUserData(postList: List<ParticipationData>?) {
        Log.e("ParticipationReceiveActivity PostId Test", "진입완료")
        Log.e("setParticipantsUserData", postList.toString())
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()

        participantsUserAllData.clear()

        if (postList?.isNotEmpty() == true) {
            postList.forEach { participationData ->
                RetrofitServerConnect.create(this@ParticipationReceiveActivity).getUserProfileData(participationData.userId).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            response.body()?.let { user ->
                                participantsUserAllData.add(user)
                            }
                            updateUI()
                        } else {
                            handleError(response.errorBody()?.string())
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        handleError(t.message)
                    }
                })
            }
        } else {
            handleEmptyData()
        }
    }

    private fun handleEmptyData() {
        CoroutineScope(Dispatchers.Main).launch {
            loadingDialog.dismiss()
            pBinding.participationRv.visibility = View.GONE
            pBinding.nonParticipation.visibility = View.VISIBLE
        }
    }

    private fun handleError(error: String?) {
        Log.e("ParticipationReceiveError", error.orEmpty())
        loadingDialog.dismiss()
    }
}