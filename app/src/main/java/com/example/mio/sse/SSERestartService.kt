package com.example.mio.sse

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat


class SSERestartService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 알림 생성
        val builder = NotificationCompat.Builder(this, "channelId")
        /*val builder = NotificationCompat.Builder(this, "channelId")
        builder.setSmallIcon(com.example.mio.R.drawable.top_icon_vector) // 유효한 아이콘 설정
        builder.setContentTitle("SSE Restart Service")
        builder.setContentText("서비스가 재시작되었습니다.")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)*/

        // 알림 채널 생성 (Android 8.0 이상)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "channelId",
            "Channel Name",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        val notification: Notification = builder.build()

        // 포그라운드 서비스로 시작
        startForeground(9, notification)


        val tempIntent = Intent(this, SSEForegroundService::class.java)
        startService(tempIntent)

        stopForeground(STOP_FOREGROUND_DETACH)

        stopSelf()

        return START_NOT_STICKY
    }
}