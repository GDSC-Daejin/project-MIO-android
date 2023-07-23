package com.example.mio.NoticeBoard

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.NoticeBoardReadAdapter
import com.example.mio.Helper.AlertReceiver
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.*
import com.example.mio.databinding.ActivityNoticeBoardReadBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List


class NoticeBoardReadActivity : AppCompatActivity() {
    private lateinit var nbrBinding : ActivityNoticeBoardReadBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    //private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private var noticeBoardReadAdapter : NoticeBoardReadAdapter? = null

    //댓글 저장 전체 데이터
    private var commentAllData = mutableListOf<CommentData?>()
    private var commentEditText = ""

    //클릭한 포스트(게시글)의 데이터 임시저장
    private var temp : PostData? = null

    //알람설정
    private var setNotificationTime : Calendar? = null
    private val channelID = "NOTIFICATION_CHANNEL"
    private val channelName = "NOTIFICATION"
    private var requestCode = 1
    //알림 데이터 세팅을 위한 뷰모델 - 나중에 서버에서 불러올 예정(Todo)
    private var sharedViewModel : SharedViewModel? = null
    var sharedPref : SharedPref? = null
    private var setKey = "setting_history"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbrBinding = ActivityNoticeBoardReadBinding.inflate(layoutInflater)
        sharedPref = SharedPref(this)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]


        createChannel()
        //sendComment()
        //btnViewChanger()

        commentSetting()

        val type = intent.getStringExtra("type")

        if (type.equals("READ")) {
            temp = intent.getSerializableExtra("postItem") as PostData?
            nbrBinding.readContent.text = temp!!.postContent
            nbrBinding.readUserId.text = temp!!.accountID
            nbrBinding.readCost.text = temp!!.postCost.toString()
            nbrBinding.readTitle.text = temp!!.postTitle.toString()
            nbrBinding.readNumberOfPassengersTotal.text = temp!!.postParticipationTotal.toString()
            nbrBinding.readNumberOfPassengers.text = temp!!.postParticipation.toString()
            nbrBinding.readDetailLocation.text = temp!!.postLocation.toString()
            nbrBinding.readDateTime.text = this.getString(R.string.setText, temp!!.postTargetDate, temp!!.postTargetTime)
        }
        setCommentData()
        initRecyclerView()







        //뒤로가기
        nbrBinding.backArrow.setOnClickListener {
            val intent = Intent(this@NoticeBoardReadActivity, MainActivity::class.java).apply {

            }
            setResult(8, intent)
            finish()
        }
        setContentView(nbrBinding.root)
    }

    //edittext가 아닌 다른 곳 클릭 시 내리기
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView: View? = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm != null) imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun commentSetting() {
        nbrBinding.readCommentEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                nbrBinding.readSendComment.visibility = View.GONE
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                commentEditText = editable.toString()
                if (editable.isEmpty()) {
                    nbrBinding.readSendComment.visibility = View.GONE
                } else {
                    nbrBinding.readSendComment.visibility = View.VISIBLE
                }
            }
        })

        nbrBinding.readSendComment.setOnClickListener {
            //서버에서 원하는 형식으로 날짜 설정
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)
            val formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            val result: Instant = Instant.from(formatter.parse(currentDate))





            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
            val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()

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

            //댓글 잠시 저장
            val commentTemp = SendCommentData(commentEditText, result.toString() , temp!!.postID)
            println("ct $commentTemp")
            CoroutineScope(Dispatchers.IO).launch {
                api.addCommentData(commentTemp, temp!!.postID).enqueue(object : Callback<CommentData> {
                    override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                        if (response.isSuccessful) {
                            commentAllData.add(CommentData(
                                response.body()!!.commentId,
                                response.body()!!.content,
                                response.body()!!.createDate,
                                response.body()!!.postId,
                                response.body()!!.user,
                                response.body()!!.childComments,
                            ))
                            println("scucuc")
                            noticeBoardReadAdapter!!.notifyDataSetChanged()
                        } else {
                            //interceptor만 해줌 될듯? 500error발생해서
                            println("faafa")
                            Log.d("comment", response.errorBody()?.string()!!)
                            Log.d("message", call.request().toString())
                            println(response.code())
                        }
                    }

                    override fun onFailure(call: Call<CommentData>, t: Throwable) {
                        Log.d("error", t.toString())
                    }
                })
            }
        }
    }

    private fun setCommentData() {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getCommentData(temp!!.postID).enqueue(object : Callback<List<CommentResponseData>> {
                override fun onResponse(call: Call<List<CommentResponseData>>, response: Response<List<CommentResponseData>>) {
                    if (response.isSuccessful) {
                        println("scssucsucsucs")


                        for (i in response.body()!!.indices) {
                            //val te = CommentData(1,"2","3",4,)
                            //탑승자 null체크
                            /*var part = 0
                            var location = ""
                            var title = ""
                            var content = ""
                            var targetDate = ""
                            var targetTime = ""
                            var categoryName = ""*/
                            if (response.isSuccessful) {
                                commentAllData.add(CommentData(
                                    response.body()!![i].commentId,
                                    response.body()!![i].content,
                                    response.body()!![i].createDate,
                                    response.body()!![i].postId,
                                    response.body()!![i].user,
                                    response.body()!![i].childComments
                                ))
                            }
                            noticeBoardReadAdapter!!.notifyDataSetChanged()
                        }
                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }

                    nbrBinding.readCommentTotal.text = commentAllData.size.toString()

                    if (commentAllData.size == 0) {
                        nbrBinding.commentRV.visibility = View.GONE
                        nbrBinding.notCommentTv.visibility = View.VISIBLE
                    } else {
                        nbrBinding.commentRV.visibility = View.VISIBLE
                        nbrBinding.notCommentTv.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<CommentResponseData>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun initRecyclerView() {
        setCommentData()
        noticeBoardReadAdapter = NoticeBoardReadAdapter()
        noticeBoardReadAdapter!!.commentItemData = commentAllData
        nbrBinding.commentRV.adapter = noticeBoardReadAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nbrBinding.commentRV.setHasFixedSize(true)
        nbrBinding.commentRV.layoutManager = manager

        nbrBinding.commentRV.itemAnimator =  SlideInUpAnimator(OvershootInterpolator(1f))
        nbrBinding.commentRV.itemAnimator?.apply {
            addDuration = 1000
            removeDuration = 100
            moveDuration = 1000
            changeDuration = 100
        }
    }

    /*private fun btnViewChanger() {
        nbrBinding.favoriteBtn.setOnClickListener {
            isFavoriteBtn = !isFavoriteBtn

            //여기에 누가 버튼을 눌렀는지 데이터 저장하는 함수도 필요함 Todo

            if (isFavoriteBtn) {
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.favoriteBtn.setBackgroundResource(R.drawable.baseline_favorite_24)
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.favoriteBtn.setBackgroundResource(R.drawable.baseline_favorite_border_24)
                }
            }
        }

        nbrBinding.applyBtn.setOnClickListener {
            isApplyBtn = !isApplyBtn
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            //현재 로그인된 유저 email 가져오기
            val userEmail = saveSharedPreferenceGoogleLogin.getUserEMAIL(this).toString()

            if (isApplyBtn) {
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.applyBtn.setBackgroundResource(R.drawable.apply_button_update_background)
                    nbrBinding.applyBtn.text = "참여 신청 완료"
                }
                //참석 이후 동의화면으로 이동
                setNotification("${userEmail} 님이 참여 하셨습니다 즐거운 카풀되세요", temp!!)
                //시간
                val value = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분", Locale.getDefault()).format( Calendar.getInstance().timeInMillis )
                //저장할 데이터
                val notifyTempArr : ArrayList<NotificationData> = ArrayList()
                notifyTempArr.add( NotificationData(0, userEmail, temp!!, isApplyBtn, value) )

                tempArr.add(NotificationData(0, userEmail, temp!!, isApplyBtn, value))

                sharedViewModel!!.setNotificationLiveData("add", notifyTempArr)
                println(tempArr)
                sharedPref!!.setNotify(this@NoticeBoardReadActivity, setKey, tempArr)

                //val tempNoti = NotificationData(0, userEmail, temp!!, isApplyBtn, value)

               *//* val bundle = Bundle()
                bundle.putString("title", "test")

                val sendFragment = NotificationFragment()
                sendFragment.arguments = bundle*//*


                val intent = Intent(this, ApplyNextActivity::class.java)
                startActivity(intent)
            } else { //참석 눌렀을 떼
                val builder = AlertDialog.Builder(this)
                val ad : AlertDialog = builder.create()
                builder.setTitle("취소 알림")
                builder.setIcon(R.drawable.baseline_info_24)
                val dialogView = layoutInflater.inflate(R.layout.apply_alert_dialog, null)
                builder.setView(dialogView)
                // p0에 해당 AlertDialog가 들어온다. findViewById를 통해 view를 가져와서 사용
                *//*var listener = DialogInterface.OnClickListener { applyAnswer, p1 ->
                    var alert = applyAnswer as AlertDialog

                    //나중에 받고 싶은 값 받기
                    *//**//*var edit1: EditText? = alert.findViewById<EditText>(R.id.editText)
                    var edit2: EditText? = alert.findViewById<EditText>(R.id.editText2)

                    tv1.text = "${edit1?.text}"
                    tv1.append("${edit2?.text}")*//**//*
                }*//*
                builder.setNegativeButton("예",
                    DialogInterface.OnClickListener { dialog, which ->
                        ad.dismiss()
                        CoroutineScope(Dispatchers.Main).launch {
                            nbrBinding.applyBtn.setBackgroundResource(R.drawable.apply_button_background)
                            nbrBinding.applyBtn.text = "참여 신청 하기"
                        }
                    })

                builder.setPositiveButton("아니오",
                    DialogInterface.OnClickListener { dialog, which ->
                        ad.dismiss()
                    })


                builder.show()
            }


        }

        *//*isBtnClick = !isBtnClick

        if (isBtnClick) {
            CoroutineScope(Dispatchers.Main).launch {
                val color = getColor(R.color.black)
                mBinding.button.setBackgroundColor(color)
                mBinding.button.text = "실시간 위치 확인"
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                val color = getColor(R.color.teal_200)
                mBinding.button.setBackgroundColor(color)
                mBinding.button.text = "위치 확인 종료"
            }
        }*//*
    }



    private fun sendComment() {
        var et = ""
        nbrBinding.messageET.setOnClickListener {
            nbrBinding.messageET.isCursorVisible = true
        }
        nbrBinding.messageET.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                et = nbrBinding.messageET.text.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        nbrBinding.messageSendIV.setOnClickListener {
            if (et.isEmpty()) {
                Toast.makeText(this@NoticeBoardReadActivity, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                //나중에 여기서 데이터 변경하기 현재 로그인된 계정정보로 Todo
                commentAllData.add(CommentData("2020202", et, 0))
                //noticeBoardReadAdapter!!.notifyItemInserted(commentAllData[0]!!.commentPosition)
                noticeBoardReadAdapter!!.notifyDataSetChanged()
                nbrBinding.messageET.text.clear()
            }
            //커서 깜빡이 없앰
            nbrBinding.messageET.isCursorVisible = false
        }
        *//*nbrBinding.messageSendIV.setOnClickListener {
            if (et.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                //나중에 여기서 데이터 변경하기 현재 로그인된 계정정보로 Todo
                commentAllData.add(CommentData("2020202", et, 0))
                noticeBoardReadAdapter!!.notifyDataSetChanged()
            }
        }*//*
    }*/
    private fun getManager() : NotificationManager {
        return getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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



    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNotification(content : String?, data : PostData) {

        //var alarmManager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationChannelID = 36
        val value = SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분", Locale.getDefault()).format( Calendar.getInstance().timeInMillis )

        var bundle = Bundle()
        bundle.putString("time", value)
        bundle.putString("content", content)

        var intent = Intent(this, AlertReceiver::class.java).apply {
            putExtra("bundle",bundle)
        }

        val tapResultIntent = Intent(this, NoticeBoardReadActivity::class.java).apply {
            putExtra("type", "READ")
            putExtra("postItem", data)
            //이전에 실행된 액티비티들을 모두 없앤 후 새로운 액티비티 실행 플래그
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        //intent를 당장 수행하지 않고 특정시점에 수행하도록 미룰 수 있는 intent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapResultIntent,
            //PendingIntent.FLAG_UPDATE_CURRENT,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,

        )

        val notificationCreate = NotificationCompat.Builder(this@NoticeBoardReadActivity, channelID)
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
}