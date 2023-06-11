package com.example.mio.NoticeBoard

import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.NoticeBoardReadAdapter
import com.example.mio.ApplyNextActivity
import com.example.mio.Helper.AlertReceiver
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.CommentData
import com.example.mio.Model.NotificationData
import com.example.mio.Model.PostData
import com.example.mio.Model.SharedViewModel
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.ActivityNoticeBoardReadBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NoticeBoardReadActivity : AppCompatActivity() {
    private lateinit var nbrBinding : ActivityNoticeBoardReadBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    //private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private var noticeBoardReadAdapter : NoticeBoardReadAdapter? = null

    //댓글 저장 전체 데이터
    private var commentAllData = mutableListOf<CommentData?>()

    //클릭한 포스트(게시글)의 데이터 임시저장
    private var temp : PostData? = null
    private var tempArr : kotlin.collections.ArrayList<NotificationData> = ArrayList()
    //버튼 클릭 체크
    private var isFavoriteBtn = false
    private var isApplyBtn = false
    //private var isApplyCancel = false
    //알람설정
    private var setNotificationTime : Calendar? = null
    private val channelID = "NOTIFICATION_CHANNEL"
    private val channelName = "NOTIFICATION"
    private var requestCode = 1
    //알림 데이터 세팅을 위한 뷰모델 - 나중에 서버에서 불러올 예정(Todo)
    private var sharedViewModel : SharedViewModel? = null
    var sharedPref : SharedPref? = null
    private var setKey = "setting_history"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbrBinding = ActivityNoticeBoardReadBinding.inflate(layoutInflater)
        sharedPref = SharedPref(this)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        createChannel()
        sendComment()
        btnViewChanger()

        val type = intent.getStringExtra("type")

        if (type.equals("READ")) {
            temp = intent.getSerializableExtra("postItem") as PostData?
            nbrBinding.readContentText.text = temp!!.postContent
            nbrBinding.readAccountId.text = temp!!.accountID
        }


        initRecyclerView()


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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun btnViewChanger() {
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

               /* val bundle = Bundle()
                bundle.putString("title", "test")

                val sendFragment = NotificationFragment()
                sendFragment.arguments = bundle*/


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
                /*var listener = DialogInterface.OnClickListener { applyAnswer, p1 ->
                    var alert = applyAnswer as AlertDialog

                    //나중에 받고 싶은 값 받기
                    *//*var edit1: EditText? = alert.findViewById<EditText>(R.id.editText)
                    var edit2: EditText? = alert.findViewById<EditText>(R.id.editText2)

                    tv1.text = "${edit1?.text}"
                    tv1.append("${edit2?.text}")*//*
                }*/
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

        /*isBtnClick = !isBtnClick

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
        }*/
    }

    private fun initRecyclerView() {
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
        /*nbrBinding.messageSendIV.setOnClickListener {
            if (et.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                //나중에 여기서 데이터 변경하기 현재 로그인된 계정정보로 Todo
                commentAllData.add(CommentData("2020202", et, 0))
                noticeBoardReadAdapter!!.notifyDataSetChanged()
            }
        }*/
    }
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