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
    //알람
    private val channelID = "NOTIFICATION_CHANNEL"
    private val channelName = "NOTIFICATION"
    //전에 받은 알람의 데이터 비교하기 위해 생성 -> 전에꺼 보다 이번에 받은 데이터 크기가 더 크다면 알람을 받은 것이니 갱신해줘야함
    private var beforeNotificationAllData : ArrayList<AddAlarmResponseData> = ArrayList()
    private var notificationAllData : ArrayList<AddAlarmResponseData> = ArrayList()
    private var notificationPostAllData : ArrayList<AlarmPost> = ArrayList()
    private var alarmContext : Context? = null

    //알람이 왔는지 체크
    private var isSendCheck = false
    override fun onReceive(context: Context?, intent: Intent?) {
        alarmContext = context
        createChannel()
        // 폴링 주기 설정 (예: 1분마다 폴링)
        val pollingInterval = 60000 // 밀리초 단위, 1분
        val handler = Handler(Looper.getMainLooper())

        // 주기적으로 서버에 폴링 요청을 보내고 알림을 표시 -> handler 대신에 workmanager사용하기
        handler.postDelayed(object : Runnable {
            override fun run() {
                // 서버에 알림 요청 보내기
                setMyNotificationData()

                if (isSendCheck) {
                    // 알림 표시
                    setNotification("새로운 알림 도착!", context)
                }

                // 다음 폴링 실행
                handler.postDelayed(this, pollingInterval.toLong())
            }
        }, pollingInterval.toLong())
    }

    /*private fun getManager() : NotificationManager {
        return getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
    }*/

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelID, channelName,
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "참여 알림"
            }
            //이 채널에 게시된 알림이 해당 기능을 지원하는 장치에서 알림 표시등을 표시할지 여부를 설정합니다.
            channel.enableLights(true)
            //이 채널에 게시된 알림이 해당 기능을 지원하는 장치에서 진동 등을 표시할지 여부를 설정합니다.
            channel.enableVibration(true)
            //이 채널에 게시된 알림에 대한 알림 표시등 색상을 설정
            channel.lightColor = Color.GREEN
            //이 채널에 게시된 알림이 전체 또는 수정된 형태로 잠금 화면에 표시되는지 여부를 설정
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            //채널생성
            //getManager().createNotificationChannel(channel)
        }
    }



    private fun setNotification(content : String?, context: Context?) {

        //var alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationChannelID = 36
        val value = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분", Locale.getDefault()).format( Calendar.getInstance().timeInMillis )

        var bundle = Bundle()
        bundle.putString("time", value)
        bundle.putString("content", content)

        var intent = Intent(context, AlertReceiver::class.java).apply {
            putExtra("bundle",bundle)
        }

        val tapResultIntent = Intent(context, NotificationFragment::class.java).apply {
            //fragment이동안되면 그냥 flag 설정 TODO

            //이전에 실행된 액티비티들을 모두 없앤 후 새로운 액티비티 실행 플래그
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        //intent를 당장 수행하지 않고 특정시점에 수행하도록 미룰 수 있는 intent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapResultIntent,
            //PendingIntent.FLAG_UPDATE_CURRENT,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,

            )

        val notificationCreate = NotificationCompat.Builder(context!!, channelID)
            .setContentTitle("알람")
            .setContentText(content)
            .setSmallIcon(R.drawable.top_icon_vector)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()


        //getManager().notify(notificationChannelID, notificationCreate)
        //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, setAlarmTime!!.timeInMillis, pendingIntent)
    }

    private fun setMyNotificationData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(alarmContext).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(alarmContext).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        //Authorization jwt토큰 로그인
        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                    return@Interceptor chain.proceed(newRequest)
                }
            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////////
        CoroutineScope(Dispatchers.IO).launch {
            api.getMyAlarm().enqueue(object : Callback<List<AddAlarmResponseData>> {
                override fun onResponse(call: Call<List<AddAlarmResponseData>>, response: Response<List<AddAlarmResponseData>>) {
                    if (response.isSuccessful) {
                        println("scssucsucsucs")

                        notificationAllData.clear()

                        for (i in response.body()!!.indices) {
                            notificationAllData.add(AddAlarmResponseData(
                                response.body()!![i].id,
                                response.body()!![i].createDate,
                                response.body()!![i].content,
                                response.body()!![i].post,
                                response.body()!![i].userEntity
                            ))

                            notificationPostAllData.add(
                                AlarmPost(
                                response.body()!![i].post.id,
                                response.body()!![i].post.title,
                                response.body()!![i].post.content,
                                response.body()!![i].post.createDate,
                                response.body()!![i].post.targetDate,
                                response.body()!![i].post.targetTime,
                                response.body()!![i].post.verifyGoReturn,
                                response.body()!![i].post.numberOfPassengers,
                                response.body()!![i].post.viewCount,
                                response.body()!![i].post.verifyFinish,
                                response.body()!![i].post.latitude,
                                response.body()!![i].post.longitude,
                                response.body()!![i].post.bookMarkCount,
                                response.body()!![i].post.participantsCount,
                                response.body()!![i].post.location,
                                response.body()!![i].post.cost,
                                response.body()!![i].post.category,
                                response.body()!![i].post.commentList,
                                response.body()!![i].post.user,
                                response.body()!![i].post.participants
                            ))
                        }


                        if (beforeNotificationAllData.size < notificationAllData.size) {
                            beforeNotificationAllData.clear()
                            beforeNotificationAllData.addAll(notificationAllData)
                            isSendCheck = true
                        } else {
                            isSendCheck = false
                        }

                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<List<AddAlarmResponseData>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }
}