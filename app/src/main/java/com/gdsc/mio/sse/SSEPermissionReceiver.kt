package com.gdsc.mio.sse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin

class SSEPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return


        val sharedPreference = SaveSharedPreferenceGoogleLogin()

        if (isPermissionsGranted(context)) {
            sharedPreference.setSharedAlarm(context, true)
            startSSEForegroundService(context)
        } else {
            sharedPreference.setSharedAlarm(context, false)
            stopSSEForegroundService(context)
        }
    }

    private fun isPermissionsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun startSSEForegroundService(context: Context) {
        val serviceIntent = Intent(context, SSEForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun stopSSEForegroundService(context: Context) {
        val serviceIntent = Intent(context, SSEForegroundService::class.java)
        context.stopService(serviceIntent)
    }
}
