package com.gdsc.mio.sse

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.gdsc.mio.BuildConfig
import com.gdsc.mio.R
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class SSEForegroundService : Service() {
    private var sharedPreferenceGoogleLogin: SaveSharedPreferenceGoogleLogin? = null
    private var eventSource: BackgroundEventSource? = null
    var serviceIntent: Intent? = null
    private val SERVER_URL = BuildConfig.server_URL
    private var userId: Long? = null
    private var isGetAlarm: Boolean? = null

    companion object {
        private var hasNotificationBeenShown: Boolean = false
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "SSE_NOTIFICATION_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground Service 유지
        startForegroundServiceWithNotification()

        sharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        userId = sharedPreferenceGoogleLogin!!.getUserId(this).toLong()
        isGetAlarm = sharedPreferenceGoogleLogin!!.getSharedAlarm(this)
        if (userId != null && isGetAlarm == true) {
            startSSE()
        }

        return START_STICKY // 서비스가 강제 종료되면 자동 재시작
    }

    private fun startSSE() {
        val url = "${SERVER_URL}subscribe/$userId"

        val request = EventSource.Builder(
            ConnectStrategy.http(URL(url))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
        )

        eventSource = BackgroundEventSource.Builder(SseHandler(this), request)
            .threadPriority(Thread.MAX_PRIORITY)
            .build()
        eventSource?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventSource?.close()
        serviceIntent = null
        restartServiceWithAlarm() // 서비스 종료 시 자동 재시작
    }

    private fun startForegroundServiceWithNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SSE Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "알림 서비스용 채널"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("알람 서비스 실행 중")
            .setContentText("실시간 알림을 수신하는 중입니다")
            .setSmallIcon(R.drawable.top_icon_vector)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // 사용자가 쉽게 닫지 못하도록 설정
            .build()

        // Android 14 (API 34) 이상에서는 FGS 타입을 명시적으로 설정
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (API 29) 이상
            startForeground(NOTIFICATION_ID, notification)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun restartServiceWithAlarm() {
        val intent = Intent(this, SSEAlarmReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= 34) { // Android 14 (API 34) 이상
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    pendingIntent
                )

                try {
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(settingsIntent)
                } catch (e: Exception) {
                    Log.e("SSEService", "알람 설정 권한 요청 화면을 열 수 없습니다", e)
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) 이상
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    pendingIntent
                )
            } else {
                try {
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(settingsIntent)
                } catch (e: Exception) {
                    Log.e("SSEService", "알람 설정 권한 요청 화면을 열 수 없습니다", e)
                }

                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0 (API 23) 이상
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000,
                pendingIntent
            )
        } else {
            // Android 6.0 미만
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000,
                pendingIntent
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}