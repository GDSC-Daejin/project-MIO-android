package com.example.mio.NoticeBoard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.ActivityNoticeBoardEditBinding

class NoticeBoardEditActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityNoticeBoardEditBinding
    //클릭한 포스트(게시글)의 데이터 임시저장 edit용
    private var temp : PostData? = null
    private var pos = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityNoticeBoardEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val type = intent.getStringExtra("type")

        if (type.equals("ADD")) { //add
            //temp = intent.getSerializableExtra("postItem") as PostData?
            /*mBinding.editAdd.text = temp!!.postContent
            nbrBinding.readAccountId.text = temp!!.accountID*/
        } else { //edit

        }

        mBinding.editAdd.setOnClickListener {
            val contentPost = mBinding.editPostContent.text.toString()
            val contentTitle = mBinding.editPostTitle.text.toString()

            if (type.equals("ADD")) {
                if (contentPost.isNotEmpty() && contentTitle.isNotEmpty()) {
                    //데이터 세팅 후 임시저장
                    val tempData = PostData("accoout", pos, contentTitle, contentPost)

                    val intent = Intent().apply {
                        putExtra("postData", tempData)
                        putExtra("flag", 0)
                    }
                    pos += 1
                    setResult(RESULT_OK, intent)
                    finish()
                }
            } else {

            }
        }
    }
}