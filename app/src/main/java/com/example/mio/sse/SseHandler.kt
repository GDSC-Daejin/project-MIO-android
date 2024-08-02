package com.example.mio.sse

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mio.Helper.NotificationHelper
import com.example.mio.LoginActivity
import com.example.mio.Navigation.NotificationFragment
import com.example.mio.R
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import org.json.JSONObject

class SseHandler(private val context: Context) : BackgroundEventHandler {
    private val channelId = "channelId"
    private val channelName = "Channel Name"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        if (messageEvent == null) {
            return
        }

        val eventId = messageEvent.lastEventId
        val eventType = messageEvent.eventName
        val eventData = messageEvent.data

        println("Event ID: $eventId")
        println("Event Type: $eventType")
        println("Event Data: $eventData")

        val comment = if (eventData.contains(":")) {
            messageEvent.data.split(":").map { it.toString() }.last()
        } else {
            eventData
        }

        Log.e("SSE Comment", comment.toString())
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
                Log.d("Notification", "Notification created successfully")
            } catch (e: Exception) {
                Log.e("Notification", "Failed to create notification: ${e.message}")
            }
        }



    }

    override fun onComment(comment: String?) {
    }

    override fun onError(t: Throwable?) {
        Log.d("SSE", "SSE연결 실패")
        Log.e("SSE", t.toString())
        //java.net.SocketTimeoutException: timeout
    }

    private fun sendNotification(title: String, message: String) {
        /*createChannel()
        setNotification(message, context)*/
        /*val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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

        notificationManager.notify(0, notification)*/
       /* val builder = NotificationCompat.Builder(context, "channelId")
            .setSmallIcon(R.drawable.top_icon_vector)
            .setContentTitle("알림")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(1, builder.build())
            Log.d("Notification", "Notification created successfully")
        } catch (e: Exception) {
            Log.e("Notification", "Failed to create notification: ${e.message}")
        }*/
    }

    /*private fun createChannel() {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "참여 알림"
            enableLights(true)
            enableVibration(true)
            lightColor = Color.GREEN
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        getManager().createNotificationChannel(channel)
    }*/

    /*private fun setNotification(content: String?, context: Context?) {
        Log.e("notification alarm", content.toString())
        val tapResultIntent = Intent(context, NotificationFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapResultIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context!!, channelID)
            .setContentTitle("알람")
            .setContentText(content)
            .setSmallIcon(R.drawable.top_icon_vector)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        getManager().notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun getManager(): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }*/
}