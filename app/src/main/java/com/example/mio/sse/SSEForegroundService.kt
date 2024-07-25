package com.example.mio.sse

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit


class SSEForegroundService : Service() {
    private var sharedPreferenceGoogleLogin : SaveSharedPreferenceGoogleLogin? = null
    private var userId : Long? = null
    private var eventSource : BackgroundEventSource? = null
    var serviceIntent: Intent? = null



    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        userId = sharedPreferenceGoogleLogin!!.getUserId(this)?.toLong()
        Log.e("Service", "서비스가 실행 중입니다...");
        if (userId != null) {
            eventSource = BackgroundEventSource //백그라운드에서 이벤트를 처리하기위한 EVENTSOURCE의 하위 클래스
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
                .threadPriority(Thread.MAX_PRIORITY) //백그라운드 이벤트 처리를 위한 스레드 우선 순위를 최대로 설정합니다.
                .build()
            // EventSource 연결 시작
            eventSource!!.start()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("SERVICE","MyService onDestroy")

        serviceIntent = null
        setAlarmTimer()
    }

    private fun setAlarmTimer() {
        val c: Calendar = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        c.add(Calendar.SECOND, 1)
        val intent = Intent(this, SSEAlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // 플래그 추가
        )
        val mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, sender)
    }
}