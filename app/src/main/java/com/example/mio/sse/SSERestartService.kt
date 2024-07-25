package com.example.mio.sse

import android.R
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mio.MainActivity


class SSERestartService : Service() {
    //클라이언트와 상호작용하기 위한 곳
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    //클라랑 상호작용없이 그냥 서비스만 이용하니 위것은 사용x
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("정보태그", "RestartService")

        val builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(com.example.mio.R.drawable.top_icon_vector)
        builder.setContentTitle(null)
        builder.setContentText(null)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        builder.setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    "default",
                    "기본 채널",
                    NotificationManager.IMPORTANCE_NONE
                )
            )
        }

        val notification: Notification = builder.build()
        startForeground(9, notification)

        val tempIntent = Intent(this, SSEForegroundService::class.java)
        startService(tempIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(Service.STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
        stopSelf()

        return START_NOT_STICKY
        //return super.onStartCommand(intent, flags, startId)
    }
}