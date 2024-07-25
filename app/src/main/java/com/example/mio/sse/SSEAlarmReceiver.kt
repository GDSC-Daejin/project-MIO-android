package com.example.mio.sse

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build




class SSEAlarmReceiver : BroadcastReceiver() { //alarm에 의해 작동하는 리시버, restart실행용
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val tempIntent = Intent(context, SSERestartService::class.java)
            context?.startForegroundService(tempIntent)
        } else {
            val tempIntent = Intent(context, SSEForegroundService::class.java)
            context?.startService(tempIntent)
        }
    }
}