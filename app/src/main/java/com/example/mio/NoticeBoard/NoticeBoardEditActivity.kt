package com.example.mio.NoticeBoard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mio.R
import com.example.mio.databinding.ActivityNoticeBoardEditBinding

class NoticeBoardEditActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityNoticeBoardEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityNoticeBoardEditBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


    }
}