package com.example.mio.Helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mio.BuildConfig
import com.example.mio.MioInterface
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.AlarmPost
import com.example.mio.Navigation.NotificationFragment
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

class MyWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    //전에 받은 알람의 데이터 비교하기 위해 생성 -> 전에꺼 보다 이번에 받은 데이터 크기가 더 크다면 알람을 받은 것이니 갱신해줘야함
    private var beforeNotificationAllData : ArrayList<AddAlarmResponseData> = ArrayList()
    private var notificationAllData : ArrayList<AddAlarmResponseData> = ArrayList()
    private var notificationPostAllData : ArrayList<AlarmPost> = ArrayList()
    var workContext = appContext
    var workParams = params

    //전에 저장한 알람 체크
    private var sharedPref : SharedPref? = null

    //알람이 왔는지 체크
    private var isSendCheck = false

    //알람
    private val channelID = "NOTIFICATION_CHANNEL"
    private val channelName = "NOTIFICATION"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            sharedPref = SharedPref(workContext)

            if (sharedPref != null) {
                beforeNotificationAllData = sharedPref!!.getNotify(workContext) as ArrayList<AddAlarmResponseData>
            }
            //어차피 한 번만 실행됨
            createChannel()

            // 백그라운드 작업을 여기에 정의

            // 서버에 알림 요청 보내기
            setMyNotificationData()

            if (isSendCheck) {
                setNotification("새로운 알람이 도착했습니다!", workContext)
            }

            Log.d("Success", "success")
            return@withContext Result.success()
        } catch (e: Exception) {
            // 예외 처리
            Log.d("ERROR", e.toString())
            return@withContext Result.failure()
        }
    }

    private fun setMyNotificationData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(workContext).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(workContext).toString()

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
                            notificationAllData.add(
                                AddAlarmResponseData(
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
                                )
                            )
                        }


                        if (beforeNotificationAllData.size < notificationAllData.size) {
                            beforeNotificationAllData.clear()
                            beforeNotificationAllData.addAll(notificationAllData)

                            sharedPref!!.setNotify(workContext, beforeNotificationAllData)

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
            getManager().createNotificationChannel(channel)
        }
    }



    private fun setNotification(content : String?, context: Context?) {

        //var alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationChannelID = System.currentTimeMillis().toInt()
        //36


        /*val value = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분", Locale.getDefault()).format( Calendar.getInstance().timeInMillis )

        var bundle = Bundle()
        bundle.putString("time", value)
        bundle.putString("content", content)

        var intent = Intent(context, AlertReceiver::class.java).apply {
            putExtra("bundle",bundle)
        }*/

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


        getManager().notify(notificationChannelID, notificationCreate)
        //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, setAlarmTime!!.timeInMillis, pendingIntent)
    }

    private fun getManager() : NotificationManager {
        return workContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
