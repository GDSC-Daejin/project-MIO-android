package com.example.mio.loading

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.example.mio.R


class LoadingProgressDialog(context: Context?) : Dialog(context!!) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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