package com.example.mio.NoticeBoard

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardReadAdapter
import com.example.mio.Model.CommentData
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.ActivityNoticeBoardReadBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NoticeBoardReadActivity : AppCompatActivity() {
    private lateinit var nbrBinding : ActivityNoticeBoardReadBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    //private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private var noticeBoardReadAdapter : NoticeBoardReadAdapter? = null

    //댓글 저장 전체 데이터
    private var commentAllData = mutableListOf<CommentData?>()

    //클릭한 포스트(게시글)의 데이터 임시저장
    private var temp : PostData? = null

    //버튼 클릭 체크
    private var isFavoriteBtn = false
    private var isApplyBtn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbrBinding = ActivityNoticeBoardReadBinding.inflate(layoutInflater)



        sendComment()
        btnViewChanger()

        val type = intent.getStringExtra("type")

        if (type.equals("READ")) {
            temp = intent.getSerializableExtra("postItem") as PostData?
            nbrBinding.readContentText.text = temp!!.postContent
            nbrBinding.readAccountId.text = temp!!.accountID
        }

        initRecyclerView()


        setContentView(nbrBinding.root)
    }

    //edittext가 아닌 다른 곳 클릭 시 내리기
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView: View? = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm != null) imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun btnViewChanger() {
        nbrBinding.favoriteBtn.setOnClickListener {
            isFavoriteBtn = !isFavoriteBtn

            //여기에 누가 버튼을 눌렀는지 데이터 저장하는 함수도 필요함 Todo

            if (isFavoriteBtn) {
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.favoriteBtn.setBackgroundResource(R.drawable.baseline_favorite_24)
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.favoriteBtn.setBackgroundResource(R.drawable.baseline_favorite_border_24)
                }
            }
        }

        nbrBinding.applyBtn.setOnClickListener {
            isApplyBtn = !isApplyBtn

            if (isApplyBtn) {
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.applyBtn.setBackgroundResource(R.drawable.apply_button_update_background)
                    nbrBinding.applyBtn.text = "참여 신청하기"
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.applyBtn.setBackgroundResource(R.drawable.apply_button_background)
                    nbrBinding.applyBtn.text = "참여 신청 완료"
                }
            }
        }

        /*isBtnClick = !isBtnClick

        if (isBtnClick) {
            CoroutineScope(Dispatchers.Main).launch {
                val color = getColor(R.color.black)
                mBinding.button.setBackgroundColor(color)
                mBinding.button.text = "실시간 위치 확인"
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                val color = getColor(R.color.teal_200)
                mBinding.button.setBackgroundColor(color)
                mBinding.button.text = "위치 확인 종료"
            }
        }*/
    }

    private fun initRecyclerView() {
        noticeBoardReadAdapter = NoticeBoardReadAdapter()
        noticeBoardReadAdapter!!.commentItemData = commentAllData
        nbrBinding.commentRV.adapter = noticeBoardReadAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nbrBinding.commentRV.setHasFixedSize(true)
        nbrBinding.commentRV.layoutManager = manager

        nbrBinding.commentRV.itemAnimator =  SlideInUpAnimator(OvershootInterpolator(1f))
        nbrBinding.commentRV.itemAnimator?.apply {
            addDuration = 1000
            removeDuration = 100
            moveDuration = 1000
            changeDuration = 100
        }
    }

    private fun sendComment() {
        var et = ""
        nbrBinding.messageET.setOnClickListener {
            nbrBinding.messageET.isCursorVisible = true
        }
        nbrBinding.messageET.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                et = nbrBinding.messageET.text.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        nbrBinding.messageSendIV.setOnClickListener {
            if (et.isEmpty()) {
                Toast.makeText(this@NoticeBoardReadActivity, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                //나중에 여기서 데이터 변경하기 현재 로그인된 계정정보로 Todo
                commentAllData.add(CommentData("2020202", et, 0))
                //noticeBoardReadAdapter!!.notifyItemInserted(commentAllData[0]!!.commentPosition)
                noticeBoardReadAdapter!!.notifyDataSetChanged()
                nbrBinding.messageET.text.clear()
            }
            //커서 깜빡이 없앰
            nbrBinding.messageET.isCursorVisible = false
        }
        /*nbrBinding.messageSendIV.setOnClickListener {
            if (et.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                //나중에 여기서 데이터 변경하기 현재 로그인된 계정정보로 Todo
                commentAllData.add(CommentData("2020202", et, 0))
                noticeBoardReadAdapter!!.notifyDataSetChanged()
            }
        }*/
    }
}