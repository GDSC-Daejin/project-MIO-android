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
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("정보태그", "RestartService")

        // 알림 생성
        val builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(com.example.mio.R.drawable.top_icon_vector) // 유효한 아이콘 설정
        builder.setContentTitle("SSE Restart Service")
        builder.setContentText("서비스가 재시작되었습니다.")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)

        // 알림 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "channelId",
                "Channel Name",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
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
    }
}