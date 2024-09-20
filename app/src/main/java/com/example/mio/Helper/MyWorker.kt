package com.example.mio.Helper

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mio.BuildConfig
import com.example.mio.MioInterface
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.PostData
import com.example.mio.Navigation.NotificationFragment
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class MyWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private var beforeNotificationAllData = ArrayList<AddAlarmResponseData>()
    private var notificationAllData = ArrayList<AddAlarmResponseData>()
    private var notificationPostAllData = ArrayList<PostData>()
    private var workContext: Context = appContext.applicationContext
    private var sharedPreference = SaveSharedPreferenceGoogleLogin()
    private var sharedPref: SharedPref? = SharedPref(workContext)
    private var isSendCheck = false

    private val channelID = "NOTIFICATION_CHANNEL"
    private val channelName = "NOTIFICATION"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // SharedPreference에서 알람 설정 값을 가져옴
            val isAlarmEnabled = sharedPreference.getSharedAlarm(workContext)

            if (isAlarmEnabled) {
                // 백그라운드 작업 수행
                createChannel()
                setMyNotificationData()

                // 알람 설정
                if (isSendCheck) {
                    setNotification("새로운 알람이 도착했습니다!", workContext)
                }

                Log.d("Success", "success")
                Result.success()
            } else {
                Log.d("Skipped", "Alarm is disabled")
                Result.success()
            }
        } catch (e: Exception) {
            // 예외 처리
            Log.e("ERROR", e.toString())
            Result.failure()
        }
    }

    private fun setMyNotificationData() {
        val token = sharedPreference.getToken(workContext).toString()
        val getExpireDate = sharedPreference.getExpireDate(workContext).toString()

        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient(token, getExpireDate))
            .build()

        val api = retrofit.create(MioInterface::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            api.getMyAlarm().enqueue(object : Callback<List<AddAlarmResponseData>> {
                override fun onResponse(call: Call<List<AddAlarmResponseData>>, response: Response<List<AddAlarmResponseData>>) {
                    if (response.isSuccessful) {
                        notificationAllData.clear()
                        notificationPostAllData.clear()

                        response.body()?.let { responseData ->
                            notificationAllData.addAll(responseData)
                        }

                        if (beforeNotificationAllData.size < notificationAllData.size) {
                            beforeNotificationAllData.clear()
                            beforeNotificationAllData.addAll(notificationAllData)
                            sharedPref?.setNotify(workContext, beforeNotificationAllData)
                            isSendCheck = true
                        } else {
                            isSendCheck = false
                        }
                    } else {
                        Log.e("comment", response.errorBody()?.string()!!)
                    }
                }

                override fun onFailure(call: Call<List<AddAlarmResponseData>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun getOkHttpClient(token: String, getExpireDate: String): OkHttpClient {
        val interceptor = Interceptor { chain ->
            var newRequest = chain.request()
            if (token.isNotEmpty()) {
                newRequest = newRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val expireDate = getExpireDate.toLongOrNull() ?: 0L
                if (expireDate <= System.currentTimeMillis()) {
                    newRequest = newRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                }
            }
            chain.proceed(newRequest)
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    private fun createChannel() {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "참여 알림"
            enableLights(true)
            enableVibration(true)
            lightColor = Color.GREEN
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        getManager().createNotificationChannel(channel)
    }

    private fun setNotification(content: String?, context: Context?) {
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
        return NotificationManagerCompat.from(workContext)
    }
}