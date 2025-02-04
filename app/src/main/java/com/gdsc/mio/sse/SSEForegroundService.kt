package com.gdsc.mio.sse

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

class SSEForegroundService : Service() {
    private var sharedPreferenceGoogleLogin: SaveSharedPreferenceGoogleLogin? = null
    private var userId: Long? = null
    private var eventSource: BackgroundEventSource? = null
    var serviceIntent: Intent? = null
    private var isGetAlarm: Boolean? = null
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
                            .http(URL("https://mioserver.o-r.kr/subscribe/${userId}"))
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

    /*private fun startForegroundServiceWithNotification() {
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
    }*/

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
}
