package com.example.mio

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService

class VibratorController(context : Context) {
    val vibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vbManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vbManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}