package com.gdsc.mio.util

import android.content.Context
import android.provider.Settings

object DebuggingCheck {
    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) > 0
        } catch (e: Exception) {
            false
        }
    }
}