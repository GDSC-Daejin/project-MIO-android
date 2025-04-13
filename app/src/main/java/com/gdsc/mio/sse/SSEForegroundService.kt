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

/*class SSEForegroundService : Service() {
    private var sharedPreferenceGoogleLogin: SaveSharedPreferenceGoogleLogin? = null
    private var userId: Long? = null
    private var eventSource: BackgroundEventSource? = null
    var serviceIntent: Intent? = null
    private var isGetAlarm: Boolean? = null
    private var SERVER_URL = BuildConfig.server_URL
    companion object {
        private var hasNotificationBeenShown: Boolean = false
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        userId = sharedPreferenceGoogleLogin!!.getUserId(this)?.toLong()
        isGetAlarm = sharedPreferenceGoogleLogin!!.getSharedAlarm(this)

        if (!hasNotificationBeenShown && isGetAlarm == true) {
            //startForegroundServiceWithNotification()
            hasNotificationBeenShown = true
        }

        if (userId != null && isGetAlarm == true) {
            eventSource = BackgroundEventSource // 백그라운드에서 이벤트를 처리하기 위한 EVENTSOURCE의 하위 클래스
                .Builder(
                    SseHandler(context = this),
                    EventSource.Builder(
                        ConnectStrategy
                            .http(URL("${SERVER_URL}subscribe/${userId}"))
                            .header("Accept", "text/event-stream")
                            // 서버와의 연결을 설정하는 타임아웃
                            .connectTimeout(10, TimeUnit.SECONDS)
                            // 서버로부터 데이터를 읽는 타임아웃 시간
                            .readTimeout(600, TimeUnit.SECONDS)
                    )
                )
                .threadPriority(Thread.MAX_PRIORITY) // 백그라운드 이벤트 처리를 위한 스레드 우선 순위를 최대로 설정합니다.
                .build()
            // EventSource 연결 시작
            eventSource!!.start()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        eventSource?.close()
        serviceIntent = null
        setAlarmTimer()
    }

    *//*private fun startForegroundServiceWithNotification() {
        val channelID = "NOTIFICATION_CHANNEL"
        val channelName = "NOTIFICATION"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // NotificationChannel 설정 (Android 8.0 이상)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Foreground Service 알림 생성
        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle("SSE Foreground Service")
            .setContentText("서비스가 실행 중입니다...")
            .setSmallIcon(R.drawable.top_icon_vector)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // 포그라운드 서비스 시작
        startForeground(1, notification)
    }*//*

    private fun setAlarmTimer() {
        val c: Calendar = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.add(Calendar.SECOND, 1)
        val intent = Intent(this, SSEAlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, sender)
    }
}*/

class SSEForegroundService : Service() {
    private var sharedPreferenceGoogleLogin: SaveSharedPreferenceGoogleLogin? = null
    private var eventSource: BackgroundEventSource? = null
    var serviceIntent: Intent? = null
    private val SERVER_URL = BuildConfig.server_URL
    private var userId: Long? = null
    private var isGetAlarm: Boolean? = null

    companion object {
        private var hasNotificationBeenShown: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("SSEService", "SSEForegroundService 실행됨")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground Service 유지
        startForegroundServiceWithNotification()

        sharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        userId = sharedPreferenceGoogleLogin!!.getUserId(this).toLong()
        isGetAlarm = sharedPreferenceGoogleLogin!!.getSharedAlarm(this)
        Log.d("SSEService", "userId: $userId, isGetAlarm: $isGetAlarm")
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
        Log.d("SSEService", "서비스가 종료됨 -> 재시작 설정")
        eventSource?.close()
        serviceIntent = null
        restartServiceWithAlarm() // 서비스 종료 시 자동 재시작
    }

    private fun startForegroundServiceWithNotification() {
        val channelID = "NOTIFICATION_CHANNEL"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelID,
                "SSE Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle("알람 서비스 실행 중")
            .setContentText("")
            .setSmallIcon(R.drawable.top_icon_vector)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14 이상
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC // ← 타입 명시
            )
        } else {
            startForeground(1, notification)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(false)
            }
        }, 1000)
    }

    /*private fun restartServiceWithAlarm() {
        val intent = Intent(this, SSEAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 5초 후 서비스 재시작
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)
    }*/
    private fun restartServiceWithAlarm() {
        val intent = Intent(this, SSEAlarmReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

