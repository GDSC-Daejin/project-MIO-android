package com.gdsc.mio

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.mio.adapter.ParticipationAdapter
import com.gdsc.mio.model.*
import com.gdsc.mio.noticeboard.NoticeBoardReadActivity
import com.gdsc.mio.databinding.ActivityParticipationReceiveBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParticipationReceiveActivity : AppCompatActivity() {
    private lateinit var pBinding : ActivityParticipationReceiveBinding
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
        postId = intent.getIntExtra("postId", 0)
        targetDate = intent.getStringExtra("targetDate").toString()
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
            }

            override fun onRefuseClick(position: Int, participantId: String) {
                /*loadingDialog = LoadingProgressDialog(this@ParticipationReceiveActivity)
                loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
                loadingDialog.show()*/
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
                            Toast.makeText(this@ParticipationReceiveActivity, "게시글 상태 변경에 실패했습니다. 다시 시도해주세요. ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Content>, t: Throwable) {
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(this@ParticipationReceiveActivity, "게시글 상태 변경에 실패했습니다. 다시 시도해주세요. ${t.message}", Toast.LENGTH_SHORT).show()
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
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(this@ParticipationReceiveActivity, "게시글 상태 변경에 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Content>, t: Throwable) {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@ParticipationReceiveActivity, "게시글 상태 변경에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun initPostDeadLine() {
        RetrofitServerConnect.create(this@ParticipationReceiveActivity).getPostIdDetailSearch(postId).enqueue(object : Callback<Content> {
            override fun onResponse(call: Call<Content>, response: Response<Content>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
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
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@ParticipationReceiveActivity, "게시글 정보를 불러오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Content>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(this@ParticipationReceiveActivity, "게시글 정보를 불러오는데 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initParticipationRecyclerView() {
        LoadingProgressDialogManager.show(this@ParticipationReceiveActivity)

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
                    handleError(response.code().toString())
                }
            }

            override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                handleError(t.message)
            }
        })

    }

    private fun updateUI() {
        CoroutineScope(Dispatchers.Main).launch {
            LoadingProgressDialogManager.hide()
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
            LoadingProgressDialogManager.hide()
            pBinding.participationRv.visibility = View.GONE
            pBinding.nonParticipation.visibility = View.VISIBLE
        }
    }

    private fun handleError(error: String?) {
        LoadingProgressDialogManager.hide()
        Toast.makeText(this, "받아온 신청자 데이터가 없습니다. 다시 시도해주세요 $error", Toast.LENGTH_SHORT).show()
    }
}