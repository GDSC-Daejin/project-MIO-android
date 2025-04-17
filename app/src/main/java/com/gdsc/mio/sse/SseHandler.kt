package com.gdsc.mio.sse

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gdsc.mio.LoginActivity
import com.gdsc.mio.R
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler

class SseHandler(private val context: Context) : BackgroundEventHandler {
    private val channelId = "channelId"
    private val channelName = "Channel Name"

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

        /*val eventId = messageEvent.lastEventId
        val eventType = messageEvent.eventName*/
        val eventData = messageEvent.data

        val comment = if (eventData.contains(":")) {
            messageEvent.data.split(":").map { it }.last()
        } else {
            eventData
        }

        if (!comment.contains("EventStream")) {
            // Android 8.0 이상에서는 Notification Channel을 설정해야 합니다.
            val tapResultIntent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapResultIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )


            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "descriptionText"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.top_icon_vector)
                .setContentTitle("알림")
                .setContentText(comment)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            try {
                notificationManager.notify(1, builder.build())
            } catch (e: Exception) {
                Log.e("Notification", "Failed to create notification: ${e.message}")
            }
        }
    }

    override fun onComment(comment: String?) {
    }

    override fun onError(t: Throwable?) {
        Log.e("SSE", t.toString())
        //java.net.SocketTimeoutException: timeout
    }
}