package com.example.mio.sse

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mio.R
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler

class SseHandler(private val context: Context) : BackgroundEventHandler {

    override fun onOpen() {
        // SSE 연결 성공시 처리 로직 작성
        Log.d("SSE", "SSE연결 성공")
    }

    override fun onClosed() {
        // SSE 연결 종료시 처리 로직 작성
        Log.d("SSE", "SSE 안전 종료")
    }

    override fun onMessage(event: String?, messageEvent: MessageEvent?) {

        val messageData = messageEvent?.data ?: return
        Log.e("SSE", "Received data: $messageData")

        // 받은 메시지를 처리하여 알림을 보냅니다.
        sendNotification("New Message", messageData)
    }

    override fun onComment(comment: String?) {
    }

    override fun onError(t: Throwable?) {
        Log.d("SSE", "SSE연결 실패")
        Log.e("SSE", t.toString())
        //java.net.SocketTimeoutException: timeout
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sse_channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SSE Notifications", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Channel for SSE notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.mio.R.drawable.top_icon_vector)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(0, notification)
    }
}