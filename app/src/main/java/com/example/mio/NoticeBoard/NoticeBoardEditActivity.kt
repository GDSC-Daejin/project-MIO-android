package com.example.mio.NoticeBoard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.mio.MainActivity
import com.example.mio.MioInterface
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.ActivityNoticeBoardEditBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NoticeBoardEditActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityNoticeBoardEditBinding
    //클릭한 포스트(게시글)의 데이터 임시저장 edit용
    private var temp : PostData? = null
    private var pos = 0
    //받은 계정 정보
    private var userEmail = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityNoticeBoardEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val type = intent.getStringExtra("type")

        /*if (type.equals("ADD")) { //add
            //temp = intent.getSerializableExtra("postItem") as PostData?
            *//*mBinding.editAdd.text = temp!!.postContent
            nbrBinding.readAccountId.text = temp!!.accountID*//*
        } else { //edit

        }*/

        //카테고리 생각하여 데이터 변경하기 Todo
        mBinding.editAdd.setOnClickListener {
            val contentPost = mBinding.editPostContent.text.toString()
            val contentTitle = mBinding.editPostTitle.text.toString()

            if (type.equals("ADD")) {
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty()) {
                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    //현재 로그인된 유저 email 가져오기
                    userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                    //데이터 세팅 후 임시저장
                    val tempData = PostData(userEmail, pos, contentTitle, contentPost)
                    //pos는 현재 저장되지 않지만 나중에 짜피 백엔드에 데이터 넣을 거니 괜찮을듯
                    //나중에 api연결할때 여기 바꾸기


                    val intent = Intent().apply {
                        putExtra("postData", tempData)
                        putExtra("flag", 0)
                    }
                    pos += 1
                    setResult(RESULT_OK, intent)
                    finish()
                }
            } else {
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty()) {
                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    //현재 로그인된 유저 email 가져오기
                    userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()
                }
            }
        }

        //
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