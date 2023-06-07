package com.example.mio.NoticeBoard

import android.R.attr.y
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mio.MainActivity
import com.example.mio.Model.PostData
import com.example.mio.Model.SharedViewModel
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.TabCategory.TaxiTabFragment
import com.example.mio.databinding.ActivityNoticeBoardEditBinding
import java.util.*
import kotlin.collections.HashMap


class NoticeBoardEditActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityNoticeBoardEditBinding
    //클릭한 포스트(게시글)의 데이터 임시저장 edit용
    private var temp : PostData? = null
    private var pos = 0
    //받은 계정 정보
    private var userEmail = ""
    private var selectTargetDate = ""
    private var isCheckData = false
    private var categorySelect = ""

    //뷰모델
    private var sharedViewModel: SharedViewModel? = null
    //받아오는 데이터
    private var selectCalendarDataNoticeBoard : HashMap<String, ArrayList<PostData>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityNoticeBoardEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        //저장된 거 가져옴
        val editObserver = androidx.lifecycle.Observer<HashMap<String, ArrayList<PostData>>> { textValue ->
            selectCalendarDataNoticeBoard = textValue
        }
        sharedViewModel!!.getCalendarLiveData().observe(this, editObserver)


        val type = intent.getStringExtra("type")

        /*if (type.equals("ADD")) { //add
            //temp = intent.getSerializableExtra("postItem") as PostData?
            *//*mBinding.editAdd.text = temp!!.postContent
            nbrBinding.readAccountId.text = temp!!.accountID*//*
        } else { //edit

        }*/
        mBinding.datePickerBtn.setOnClickListener {
            val cal = Calendar.getInstance()
            val data = DatePickerDialog.OnDateSetListener { view, year, month, day ->
               selectTargetDate = "${year}년/${month+1}월/${day}일"
            }
            DatePickerDialog(this, data, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        mBinding.categorySelectBtn.setOnClickListener {
            categorySelect = "taxi"
        }


        //카테고리 생각하여 데이터 변경하기 Todo
        mBinding.editAdd.setOnClickListener {
            val contentPost = mBinding.editPostContent.text.toString()
            val contentTitle = mBinding.editPostTitle.text.toString()

            if (type.equals("ADD")) {
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty() && selectTargetDate.isNotEmpty()) {
                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    //현재 로그인된 유저 email 가져오기
                    userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                    //데이터 세팅 후 임시저장
                    temp = PostData(userEmail, pos, contentTitle, contentPost, selectTargetDate, categorySelect)
                    selectCalendarDataNoticeBoard[selectTargetDate] = arrayListOf()
                    selectCalendarDataNoticeBoard[selectTargetDate]!!.add(temp!!)
                    //pos는 현재 저장되지 않지만 나중에 짜피 백엔드에 데이터 넣을 거니 괜찮을듯
                    //나중에 api연결할때 여기 바꾸기
                    sharedViewModel!!.setCalendarLiveData("add", selectCalendarDataNoticeBoard)

                    val intent = Intent().apply {
                        putExtra("postData", temp)
                        putExtra("flag", 0)
                    }
                   /*val intent = Intent(this, TaxiTabFragment::class.java).apply {
                        putExtra("postData", temp)
                        putExtra("flag", 0)
                    }*/
                    pos += 1
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    if (contentTitle.isEmpty()) {
                        Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT) .show()
                    } else if (contentPost.isEmpty()) {
                        Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT) .show()
                    } else if (selectTargetDate.isEmpty()) {
                        Toast.makeText(this, "날짜를 선택해주세요.", Toast.LENGTH_SHORT) .show()
                    }
                }
            } else { //edit
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty()) {
                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    //현재 로그인된 유저 email 가져오기
                    userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                }
            }
        }

        //뒤로가기
        mBinding.backArrow.setOnClickListener {
            val intent = Intent(this@NoticeBoardEditActivity, MainActivity::class.java).apply {

            }
            setResult(8, intent)
            finish()
        }




    }


    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

        }
        return super.onOptionsItemSelected(item)
    }*/
}