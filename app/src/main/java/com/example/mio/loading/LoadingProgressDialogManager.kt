package com.example.mio.loading

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.WindowManager
import com.example.mio.R

object LoadingProgressDialogManager {
    private var loadingDialog: LoadingProgressDialog? = null

    fun show(context: Context) {
        if (loadingDialog == null) {
            loadingDialog = LoadingProgressDialog(context).apply {
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.attributes?.windowAnimations = R.style.FullScreenDialog
                window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
        loadingDialog?.show()
    }

    fun hide() {
        loadingDialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}