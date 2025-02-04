package com.gdsc.mio.sse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent




class SSEBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            val serviceIntent = Intent(context, SSERestartService::class.java)
            context?.startForegroundService(serviceIntent)
        }
    }
}