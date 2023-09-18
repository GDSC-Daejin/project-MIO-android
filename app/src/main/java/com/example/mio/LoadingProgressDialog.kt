package com.example.mio

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window


class LoadingProgressDialog(context: Context?) : Dialog(context!!) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 다이얼 로그 제목을 안보이게...
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val window = this.window

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        setContentView(R.layout.loading_dialog_layout)
        // AnimationDrawable 시작
        /*val loadingProgress = findViewById<ProgressBar>(R.id.loading_progress)
        (loadingProgress.progressDrawable as AnimationDrawable).start()*/
    }
}