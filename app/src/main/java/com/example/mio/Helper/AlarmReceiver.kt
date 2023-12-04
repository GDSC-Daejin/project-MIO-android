package com.example.mio.Helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.example.mio.BuildConfig
import com.example.mio.MioInterface
import com.example.mio.Model.AddAlarmData
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.AlarmPost
import com.example.mio.Model.PostData
import com.example.mio.Navigation.NotificationFragment
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    object WorkManagerInitializer {
        private var initialized = false

        fun isInitialized(): Boolean {
            return initialized
        }

        fun setInitialized(value: Boolean) {
            initialized = value
        }
    }


    private var alarmContext : Context? = null


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            alarmContext = context
            initializeWorkManager(context!!)
            scheduleMyWorker(context)

            /*// 폴링 주기 설정 (예: 1분마다 폴링)
            val pollingInterval = 60000 * 5 // 밀리초 단위, 5분
            val handler = Handler(Looper.getMainLooper())

            // 주기적으로 서버에 폴링 요청을 보내고 알림을 표시 -> handler 대신에 workmanager사용하기
            handler.postDelayed(object : Runnable {
                override fun run() {
                    // 서버에 알림 요청 보내기
                    //setMyNotificationData()



                    // 다음 폴링 실행
                    handler.postDelayed(this, pollingInterval.toLong())
                }
            }, pollingInterval.toLong())*/
        }
    }

    private fun initializeWorkManager(context: Context) {
        if (!WorkManagerInitializer.isInitialized()) {
            WorkManager.initialize(context, Configuration.Builder().build())
            WorkManagerInitializer.setInitialized(true)

        }
    }
}
