package com.example.mio.NoticeBoard

import android.animation.ObjectAnimator
import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.mio.*
import com.example.mio.Adapter.NoticeBoardReadAdapter
import com.example.mio.Helper.AlertReceiver
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.*
import com.example.mio.TabAccount.ProfileActivity
import com.example.mio.TabCategory.MoreCarpoolTabActivity
import com.example.mio.TabCategory.MoreTaxiTabActivity
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
    //private var replyCommentAdapter : ReplyCommentAdapter? = null

    //댓글 저장 전체 데이터
    private var commentAllData = mutableListOf<CommentData?>()
    //대댓글 용
    private var replyCommentAllData = kotlin.collections.ArrayList<CommentData?>()
    private var replyCommentTemp = ArrayList<CommentData>()
    private var commentEditText = ""
    private var childComments = ArrayList<CommentData>()
    private var childCommentsSize = 0
    //모든 댓글
    private var realCommentAllData = kotlin.collections.ArrayList<CommentData?>()

    //자신이 참여한 모든 게시글
    private var participationTempData = ArrayList<PostData>()
    //자기가 참여한 게시글에 들어와있는지 체크
    private var isParticipation = false

    //클릭한 포스트(게시글)의 데이터 임시저장
    private var temp : PostData? = null
    //받아온 proflie
    private var tempProfile : String? = null
    //포스트의 카테고리
    private var isCategory : Boolean? = null //true = 카풀, false = 택시


    //유저확인
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var email = ""
    private var writerEmail = ""
    private var gender : Boolean? = null //false 남, true 여
    private var accountNumber : String? = null
    private var verifySmoker : Boolean? = null //false 비흡, true 흡
    private var mannerCount = 0
    private var grade : String? = null
    private var activityLocation : String? = null

    private var tempGender = ""
    private var tempSmoke = ""
    private var tempVerifyGoReturn = ""



    //알람설정
    private var setNotificationTime : Calendar? = null
    private val channelID = "NOTIFICATION_CHANNEL"
    private val channelName = "NOTIFICATION"
    private var requestCode = 1

    private var sharedViewModel : SharedViewModel? = null
    var sharedPref : SharedPref? = null
    private var setKey = "setting_history"

    //댓글진동
    private lateinit var vib: Vibrator

    //바텀시트에서 가져온 데이터 type (ex 수정, 삭제)
    //게시글용
    private var getBottomSheetData = ""
    //댓글용
    private var getBottomSheetCommentData = ""

    //댓글 쓰는 건지 대댓글 쓰는 건지 체크 true = 댓글 , false = 대댓글
    private var isReplyComment = false

    //댓글 id값 임시저장
    private var parentPosition = 0
    //수정하기위한 댓글 포지션 위치 임시저장
    private var commentPosition = 0

    //같은 시간대에 예약한 것이 있는지 체크할 때 가져온 응답
    private var checkResponseBody = ""

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
            writerEmail = temp!!.user.email
            tempProfile = intent.getSerializableExtra("uri") as String
            isCategory = temp!!.postCategory == "carpool"

            val imageUrl = Uri.parse(tempProfile)
            CoroutineScope(Dispatchers.Main).launch {
                GlideApp.with(this@NoticeBoardReadActivity)
                    .load(imageUrl)
                    .error(R.drawable.top_icon_vector)
                    .centerCrop()
                    .override(40,40)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("Glide", "Image load failed: ${e?.message}")
                            println(e?.message.toString())
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            println("glide")
                            return false
                        }
                    })
                    .into(nbrBinding.readUserProfile)
            }

            initParticipationCheck()
            nbrBinding.readContent.text = temp!!.postContent
            nbrBinding.readUserId.text = temp!!.accountID
            nbrBinding.readCost.text = temp!!.postCost.toString()
            nbrBinding.readTitle.text = temp!!.postTitle.toString()
            nbrBinding.readNumberOfPassengersTotal.text = temp!!.postParticipationTotal.toString()
            nbrBinding.readNumberOfPassengers.text = temp!!.postParticipation.toString()
            nbrBinding.readDetailLocation.text = temp!!.postLocation.toString()
            nbrBinding.readDateTime.text = this.getString(R.string.setText, temp!!.postTargetDate, temp!!.postTargetTime)



            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = this.getString(R.string.setText, temp!!.postTargetDate, temp!!.postTargetTime)

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {

                if(diffSeconds > -1){
                    nbrBinding.readTimeCheck.text = "방금전"
                }
                if (diffSeconds > 0) {
                    nbrBinding.readTimeCheck.text = "${diffSeconds.toString()}초전"
                }
                if (diffMinutes > 0) {
                    nbrBinding.readTimeCheck.text = "${diffMinutes.toString()}분전"
                }
                if (diffHours > 0) {
                    nbrBinding.readTimeCheck.text = "${diffHours.toString()}시간전"
                }
                if (diffDays > 0) {
                    nbrBinding.readTimeCheck.text = "${diffDays.toString()}일전"
                }
            }

            email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.toString()

            // 글쓴이가 자기자신 이라면
            if (email == temp!!.user.email) {
                val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
                nbrBinding.readApplyBtn.text = "받은 신청 보러가기"
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.readApplyBtn.apply {
                        setBackgroundResource(R.drawable.read_apply_btn_update_layout)
                        setTypeface(typeface)
                        nbrBinding.readApplyBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@NoticeBoardReadActivity, R.color.mio_gray_11))
                    }
                }

                nbrBinding.readApplyBtn.setOnClickListener {
                    val intent = Intent(this@NoticeBoardReadActivity, ParticipationReceiveActivity::class.java).apply {
                        putExtra("postId", temp!!.postID)
                    }
                    startActivity(intent)
                }
            } else if (!isParticipation) {
                nbrBinding.readApplyBtn.setOnClickListener {
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

                    //여기 데이터 나오는 대로 처리 내일 수정 Todo
                    CoroutineScope(Dispatchers.IO).launch {
                        api.checkParticipate(postId = temp!!.postID).enqueue(object : Callback<Boolean> {
                            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                                if (response.isSuccessful) {
                                    println("가져오기 성공")
                                    println(response.body()!!.toString())
                                    if (response.body()!!) {
                                        //예약된 게 없음
                                        checkResponseBody = "신청하시려는 게시글과 같은 날짜에 승인된 카풀이 없습니다. \n 계속하시겠습니까?"

                                        //사용할 곳
                                        val layoutInflater = LayoutInflater.from(this@NoticeBoardReadActivity)
                                        val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                                        val alertDialog = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                                            .setView(view)
                                            .create()
                                        val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                                        val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                                        val dialogRightBtn =  view.findViewById<View>(R.id.dialog_right_btn)

                                        dialogContent.text = checkResponseBody //"이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"
                                        //아니오
                                        dialogLeftBtn.setOnClickListener {
                                            alertDialog.dismiss()
                                        }
                                        //예
                                        dialogRightBtn.setOnClickListener {
                                            alertDialog.dismiss()
                                            val intent = Intent(this@NoticeBoardReadActivity, ApplyNextActivity::class.java).apply {
                                                putExtra("postId", temp!!.postID)
                                                putExtra("postData", temp)
                                            }
                                            startActivity(intent)
                                            //finish 해보기
                                            //this@NoticeBoardReadActivity.finish()
                                        }
                                        alertDialog.show()
                                    } else {
                                        //예약된 것이 있음
                                        checkResponseBody = "이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"

                                        val layoutInflater = LayoutInflater.from(this@NoticeBoardReadActivity)
                                        val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                                        val alertDialog = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                                            .setView(view)
                                            .create()
                                        val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                                        val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                                        val dialogRightBtn =  view.findViewById<View>(R.id.dialog_right_btn)

                                        dialogContent.text = checkResponseBody //"이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"
                                        //아니오
                                        dialogLeftBtn.setOnClickListener {
                                            alertDialog.dismiss()
                                        }
                                        //예
                                        dialogRightBtn.setOnClickListener {
                                            alertDialog.dismiss()
                                            val intent = Intent(this@NoticeBoardReadActivity, ApplyNextActivity::class.java).apply {
                                                putExtra("postId", temp!!.postID)
                                            }
                                            startActivity(intent)
                                            //finish 해보기 Todo
                                            //this@NoticeBoardReadActivity.finish()
                                        }
                                        alertDialog.show()
                                    }

                                } else {
                                    println("faafa")
                                    Log.d("comment", response.errorBody()?.string()!!)
                                    Log.d("message", call.request().toString())
                                    println(response.code())
                                }
                            }

                            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                                Log.d("error", t.toString())
                            }
                        })
                    }
                }
            } else {
                val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
                nbrBinding.readApplyBtn.text = "신청 취소하기"
                CoroutineScope(Dispatchers.Main).launch {
                    nbrBinding.readApplyBtn.apply {
                        setBackgroundResource(R.drawable.read_apply_btn_update_layout)
                        setTypeface(typeface)
                        nbrBinding.readApplyBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@NoticeBoardReadActivity, R.color.mio_gray_11))
                    }
                }

                val layoutInflater = LayoutInflater.from(this@NoticeBoardReadActivity)
                val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                val alertDialog = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                    .setView(view)
                    .create()
                val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                val dialogRightBtn =  view.findViewById<View>(R.id.dialog_right_btn)

                dialogContent.text = "신청을 취소하시겠습니까?" //"이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"
                //아니오
                dialogLeftBtn.setOnClickListener {
                    alertDialog.dismiss()
                }
                //예
                dialogRightBtn.setOnClickListener {
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
                    CoroutineScope(Dispatchers.IO).launch {
                        api.deleteParticipate(postId = temp!!.postID).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d("check", response.code().toString())
                                } else {
                                    println("faafa")
                                    Log.d("comment", response.errorBody()?.string()!!)
                                    Log.d("message", call.request().toString())
                                    println(response.code())
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.d("error", t.toString())
                            }
                        })
                    }
                }
                alertDialog.show()
            }
        }

        //setCommentData()
        initCommentRecyclerView()

        nbrBinding.readSetting.setOnClickListener {
            if (email == temp!!.user.email) {
               /* val popUpMenu = PopupMenu(this, nbrBinding.readSetting)
                popUpMenu.menuInflater.inflate(R.menu.noticeboard_option_menu, popUpMenu.menu)
                popUpMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.read_menu_edit -> {
                            Toast.makeText(this, "수정", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, NoticeBoardEditActivity::class.java).apply {
                                putExtra("type", "EDIT")
                                putExtra("editPostData", temp)
                            }
                            startActivity(intent)
                        }

                        R.id.read_menu_delete -> {
                            Toast.makeText(this, "삭제", Toast.LENGTH_SHORT).show()
                            val builder : AlertDialog.Builder = AlertDialog.Builder(this)
                            val ad : AlertDialog = builder.create()
                            val deleteData = temp
                            builder.setMessage("정말로 삭제하시겠습니까?")
                            builder.setNegativeButton("예",
                                DialogInterface.OnClickListener { dialog, which ->
                                    ad.dismiss()
                                    //temp = listData[holder.adapterPosition]!!
                                    //extraditeData()
                                    //testData.add(temp)
                                    //deleteServerData = tempServerData[holder.adapterPosition]!!.api_id



                                    //removeServerData(deleteServerData!!)
                                    //println(deleteServerData)
                                    deletePostData()
                                })

                            builder.setPositiveButton("아니오",
                                DialogInterface.OnClickListener { dialog, which ->
                                    ad.dismiss()
                                })
                            builder.show()
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                popUpMenu.show()*/

                //이건 내 게시글 눌렀을 때
                val bottomSheet = ReadSettingBottomSheetFragment()
                bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
                bottomSheet.apply {
                    setCallback(object : ReadSettingBottomSheetFragment.OnSendFromBottomSheetDialog{
                        override fun sendValue(value: String) {
                            Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                            getBottomSheetData = value

                            when(value) {
                                "수정" -> {
                                    val intent = Intent(this@NoticeBoardReadActivity, NoticeBoardEditActivity::class.java).apply {
                                        putExtra("type", "EDIT")
                                        putExtra("editPostData", temp)
                                    }
                                    startActivity(intent)
                                }

                                "삭제" -> {
                                    /*val now = System.currentTimeMillis()
                                    val date = Date(now)
                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                                    val currentDate = sdf.format(date)
                                    val formatter = DateTimeFormatter
                                        .ofPattern("yyyy-MM-dd HH:mm:ss")
                                        .withZone(ZoneId.systemDefault())
                                    val result: Instant = Instant.from(formatter.parse(currentDate))

                                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                                    val token = saveSharedPreferenceGoogleLogin.getToken(this@NoticeBoardReadActivity).toString()
                                    val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this@NoticeBoardReadActivity).toString()

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
                                    val editCommentTemp = SendCommentData(commentEditText, result.toString() ,temp!!.postID)
                                    //37.65426254272461, 127.06022644042969

                                    CoroutineScope(Dispatchers.IO).launch {
                                        api.getMyLocation(37.65426254272461, 127.06022644042969).enqueue(object : Callback<List<LocationReadAllResponse>> {
                                            override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                                                if (response.isSuccessful) {
                                                    //fetchAllComments()
                                                    println("dfdfd")
                                                } else {
                                                    println("faafa")
                                                    Log.d("comment", response.errorBody()?.string()!!)
                                                    Log.d("message", call.request().toString())
                                                    println(response.code())
                                                }
                                            }

                                            override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                                                Log.d("error", t.toString())
                                            }
                                        })
                                    }*/

                                   /* val builder : AlertDialog.Builder = AlertDialog.Builder(this@NoticeBoardReadActivity)
                                    val ad : AlertDialog = builder.create()
                                    val view = LayoutInflater.from(this@NoticeBoardReadActivity).inflate(
                                        R.layout.dialog_layout, null
                                    )
                                    builder.setView(view)
                                    view.findViewById<TextView>(R.id.dialog_tv).text = "삭제하시겠습니까?"

                                    //아니요
                                    view.findViewById<Button>(R.id.dialog_left_btn).setOnClickListener {
                                        ad.dismiss()
                                    }
                                    //예
                                    view.findViewById<Button>(R.id.dialog_right_btn).setOnClickListener {
                                        ad.dismiss()
                                        deletePostData()
                                        this@NoticeBoardReadActivity.finish()
                                    }

                                    builder.show()*/

                                    //사용할 곳
                                    val layoutInflater = LayoutInflater.from(context)
                                    val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                                    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                                        .setView(view)
                                        .create()
                                    val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                                    val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                                    val dialogRightBtn =  view.findViewById<View>(R.id.dialog_right_btn)

                                    dialogContent.text = "정말로 삭제하시겠습니까?"
                                    //아니오
                                    dialogLeftBtn.setOnClickListener {
                                        alertDialog.dismiss()
                                    }
                                    //예
                                    dialogRightBtn.setOnClickListener {
                                        alertDialog.dismiss()
                                        if (isCategory == true) { //카풀
                                            val intent = Intent(this@NoticeBoardReadActivity, MoreCarpoolTabActivity::class.java).apply {
                                                putExtra("flag", 2)
                                            }
                                            setResult(RESULT_OK, intent)
                                            deletePostData()
                                        } else { //택시
                                            val intent = Intent(this@NoticeBoardReadActivity, MoreTaxiTabActivity::class.java).apply {
                                                putExtra("flag", 2)
                                            }
                                            setResult(RESULT_OK, intent)
                                            deletePostData()
                                        }

                                        this@NoticeBoardReadActivity.finish()
                                    }
                                    alertDialog.show()
                                    ////////////////

                                    /* builder.setNegativeButton("예",
                                         DialogInterface.OnClickListener { dialog, which ->
                                             ad.dismiss()
                                             deletePostData()
                                             this@NoticeBoardReadActivity.finish()
                                         })

                                     builder.setPositiveButton("아니오",
                                         DialogInterface.OnClickListener { dialog, which ->
                                             ad.dismiss()
                                         })
                                     builder.show()*/
                                }
                            }

                        }
                    })
                }
            }
        }


        //대댓글 달기 또는 다른 기능 추가 예정
        noticeBoardReadAdapter!!.setItemClickListener(object : NoticeBoardReadAdapter.ItemClickListener {
            //대댓글 쓸 때
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onClick(view: View, position: Int, itemId: Int) {
                val vibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vbManager =
                        getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vbManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                }
                if (vibration.hasVibrator()) {
                    vibration.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                }

                //자동으로 포커스 줘서 대댓글 달게 하기
                nbrBinding.readCommentEt.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nbrBinding.readCommentEt.findFocus(), InputMethodManager.SHOW_IMPLICIT)

                sharedViewModel!!.postReply(reply = true) //수정


                //어떤 댓글을 선택했는지 확인
                parentPosition = itemId

            }
            //댓글 수정 및 삭제할 때
            override fun onLongClick(view: View, position: Int, itemId: Int) {
                if (email == temp!!.user.email) {
                    //수정용
                    commentPosition = itemId
                    val bottomSheet = ReadSettingBottomSheetFragment()
                    bottomSheet.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet.tag)
                    bottomSheet.apply {
                        setCallback(object : ReadSettingBottomSheetFragment.OnSendFromBottomSheetDialog{
                            override fun sendValue(value: String) {
                                Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                                getBottomSheetCommentData = value
                                //댓글 부분 고치기 Todo
                                when(value) {
                                    "수정" -> {
                                        println(realCommentAllData)
                                        val filterCommentText = realCommentAllData.filter { it!!.commentId == commentPosition }
                                        nbrBinding.readCommentEt.setText(filterCommentText[0]!!.content)
                                        println(filterCommentText)
                                        println(commentPosition)
                                        //자동으로 포커스 줘서 대댓글 달게 하기
                                        nbrBinding.readCommentEt.requestFocus()
                                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.showSoftInput(nbrBinding.readCommentEt.findFocus(), InputMethodManager.SHOW_IMPLICIT)
                                    }

                                    "삭제" -> {
                                        deleteCommentData(itemId)
                                        fetchAllComments()
                                    }
                                }
                            }
                        })
                    }
                }
            }
        })

        sharedViewModel!!.isReply.observe(this@NoticeBoardReadActivity) {
            isReplyComment = when(it) {
                true -> {
                    true
                }

                false -> {
                    false
                }
            }
            println(isReplyComment)
        }

        nbrBinding.readUserId.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("studentId", temp!!.user.id)
            }
            startActivity(intent)
        }

        nbrBinding.readUserProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("studentId", temp!!.user.id)
            }
            startActivity(intent)
        }


        //뒤로가기
        nbrBinding.backArrow.setOnClickListener {
            val intent = Intent(this@NoticeBoardReadActivity, MainActivity::class.java).apply {
                //flag넣고 resultok
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

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.comment_option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.comment_menu_write -> {
                Toast.makeText(this, "대댓글쓰기", Toast.LENGTH_SHORT).show()
            }
            R.id.comment_menu_edit -> {
                Toast.makeText(this, "수정", Toast.LENGTH_SHORT).show()
            }
            R.id.comment_menu_delete -> {
                Toast.makeText(this, "삭제", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    private fun commentSetting() {
        nbrBinding.readCommentEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                nbrBinding.readSendComment.visibility = View.GONE
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                commentEditText = editable.toString()
                if (editable.isEmpty() && getBottomSheetCommentData != "수정") {
                    nbrBinding.readSendComment.visibility = View.GONE
                    nbrBinding.readEditSendComment.visibility = View.GONE
                } else if (getBottomSheetCommentData != "수정"){
                    nbrBinding.readSendComment.visibility = View.VISIBLE
                    nbrBinding.readEditSendComment.visibility = View.GONE
                } else {
                    nbrBinding.readSendComment.visibility = View.GONE
                    nbrBinding.readEditSendComment.visibility = View.VISIBLE
                }
            }
        })


        //댓글 수정하는 곳
        nbrBinding.readEditSendComment.setOnClickListener {
            println(getBottomSheetCommentData)
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
            val editCommentTemp = SendCommentData(commentEditText, result.toString() ,temp!!.postID)


            CoroutineScope(Dispatchers.IO).launch {
                api.editCommentData(editCommentTemp, commentPosition).enqueue(object : Callback<CommentData> {
                    override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                        if (response.isSuccessful) {
                            fetchAllComments()
                            println("수정성공")
                        } else {
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
            nbrBinding.readCommentEt.text = null

        }

        nbrBinding.readSendComment.setOnClickListener {
            if (isReplyComment) { //대댓글
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
                val childCommentTemp = SendChildCommentData(commentEditText, result.toString() ,temp!!.postID, parentPosition)
                //println("ct $commentTemp")
                ///

                //대댓글 추가하기
                CoroutineScope(Dispatchers.IO).launch {
                    api.addChildCommentData(childCommentTemp, parentPosition).enqueue(object : Callback<CommentData> {
                        override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                            if (response.isSuccessful) {
                                /* val commentNullCheck  = try {
                                     response.body()!!.childComments.isEmpty()
                                     response.body()!!.childComments
                                 } catch (e : java.lang.NullPointerException) {
                                     Log.d("null", e.toString())
                                     arrayListOf("null")
                                 }

                                 if (response.body()!!.childComments.isNotEmpty()) {
                                     for (i in response.body()!!.childComments.indices) {

                                     }
                                 }

                                 //null체크?
                                 if (response.body()!!.childComments.isNotEmpty()) {
                                     for (i in response.body()!!.childComments.indices) {
                                         replyCommentAllData.add(
                                             CommentData(
                                                 response.body()!!.commentId,
                                                 response.body()!!.content,
                                                 response.body()!!.createDate,
                                                 response.body()!!.postId,
                                                 response.body()!!.user,
                                                 response.body()!!.childComments,
                                             )
                                         )
                                     }
                                 }



                                 noticeBoardReadAdapter!!.notifyDataSetChanged()*/
                                fetchAllComments()
                                println("대댓글달기성공")
                                //한 번 달고 끝내야하니 false전달
                                sharedViewModel!!.postReply(reply = false)
                            } else {
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
                nbrBinding.readCommentEt.text = null
                //nbrBinding.readCommentTotal.text = (commentAllData.size + )

            } else {  //댓글
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
                val commentTemp = SendCommentData(commentEditText, result.toString(), temp!!.postID)
                //println("ct $commentTemp")
                CoroutineScope(Dispatchers.IO).launch {
                    api.addCommentData(commentTemp, temp!!.postID).enqueue(object : Callback<CommentData> {
                        override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                            if (response.isSuccessful) {
                                /*val commentNullCheck  = try {
                                    response.body()!!.childComments.isEmpty()
                                    response.body()!!.childComments
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    arrayListOf<CommentData>(CommentData(
                                        0,
                                        "null",
                                        "null",
                                        0,
                                        CommentUser(),

                                    ))
                                }*/
                                /*commentAllData.add(CommentData(
                                    response.body()!!.commentId,
                                    response.body()!!.content,
                                    response.body()!!.createDate,
                                    response.body()!!.postId,
                                    response.body()!!.user,
                                    response.body()!!.childComments,
                                ))

                                noticeBoardReadAdapter!!.notifyDataSetChanged()*/
                                //setCommentData()
                                println("scucuc")
                                fetchAllComments()
                            } else {
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
                nbrBinding.readCommentTotal.text = commentAllData.size.toString()
                nbrBinding.readCommentEt.text = null
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
                        commentAllData.clear()
                        childComments.clear()
                        childCommentsSize = 0
                        for (i in response.body()!!.indices) {
                            val commentResponse = response.body()!![i]
                           /* commentAllData.add(
                                CommentData(
                                    response.body()!![i].commentId,
                                    response.body()!![i].content,
                                    response.body()!![i].createDate,
                                    response.body()!![i].postId,
                                    response.body()!![i].user,
                                    response.body()!![i].childComments
                                ))
                            noticeBoardReadAdapter!!.notifyDataSetChanged()*/

                            val commentData = CommentData(
                                commentResponse.commentId,
                                commentResponse.content,
                                commentResponse.createDate,
                                commentResponse.postId,
                                commentResponse.user,
                                commentResponse.childComments
                            )

                            // 대댓글 추가 부분
                            childComments = kotlin.collections.ArrayList<CommentData>()
                            for (j in commentResponse.childComments.indices) {
                                val childCommentResponse = commentResponse.childComments[j]
                                val childCommentData = CommentData(
                                    childCommentResponse.commentId,
                                    childCommentResponse.content,
                                    childCommentResponse.createDate,
                                    childCommentResponse.postId,
                                    childCommentResponse.user,
                                    childCommentResponse.childComments
                                )
                                childComments.add(childCommentData)
                                //모든 댓글 정보를 얻기 위한 리스트
                                realCommentAllData.add(childCommentData)
                                childCommentsSize += 1
                            }
                            commentData.childComments = childComments

                            commentAllData.add(commentData)
                            realCommentAllData.add(commentData)
                        }
                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }

                    val totalSize = commentAllData.size + childCommentsSize
                    nbrBinding.readCommentTotal.text = totalSize.toString()
                    println("total" + totalSize +  childCommentsSize)


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

    // 서버에서 모든 댓글 정보를 가져오고 UI를 업데이트하는 함수
    private fun fetchAllComments() {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getCommentData(temp!!.postID).enqueue(object : Callback<List<CommentResponseData>> {
                override fun onResponse(
                    call: Call<List<CommentResponseData>>,
                    response: Response<List<CommentResponseData>>
                ) {
                    if (response.isSuccessful) {
                        commentAllData.clear()
                        childComments.clear()
                        childCommentsSize = 0
                        val updatedCommentAllData = mutableListOf<CommentData?>()

                        for (i in response.body()!!.indices) {
                            val commentResponse = response.body()!![i]

                            val commentData = CommentData(
                                commentResponse.commentId,
                                commentResponse.content,
                                commentResponse.createDate,
                                commentResponse.postId,
                                commentResponse.user,
                                commentResponse.childComments
                            )

                            // 대댓글 추가 부분
                            childComments = kotlin.collections.ArrayList<CommentData>()
                            for (j in commentResponse.childComments.indices) {
                                val childCommentResponse = commentResponse.childComments[j]
                                val childCommentData = CommentData(
                                    childCommentResponse.commentId,
                                    childCommentResponse.content,
                                    childCommentResponse.createDate,
                                    childCommentResponse.postId,
                                    childCommentResponse.user,
                                    childCommentResponse.childComments
                                )
                                childComments.add(childCommentData)

                                //댓글 수정을 위한 모든 데이터 리스트
                                realCommentAllData.add(childCommentData)

                                childCommentsSize += 1
                            }
                            commentData.childComments = childComments

                            updatedCommentAllData.add(commentData)
                            realCommentAllData.add(commentData)
                        }

                        // 댓글 데이터를 갱신하고 RecyclerView 어댑터를 새로 설정
                        commentAllData = updatedCommentAllData

                        val commentAdapter = NoticeBoardReadAdapter()
                        commentAdapter!!.commentItemData = commentAllData
                        nbrBinding.commentRV.adapter = commentAdapter

                        // UI 업데이트
                        val totalSize = commentAllData.size + childCommentsSize
                        nbrBinding.readCommentTotal.text = totalSize.toString()

                        if (commentAllData.isEmpty()) {
                            nbrBinding.commentRV.visibility = View.GONE
                            nbrBinding.notCommentTv.visibility = View.VISIBLE
                        } else {
                            nbrBinding.commentRV.visibility = View.VISIBLE
                            nbrBinding.notCommentTv.visibility = View.GONE
                        }
                    } else {
                        // 에러 처리 코드
                        println("FAFAFAFF")
                    }
                }

                override fun onFailure(call: Call<List<CommentResponseData>>, t: Throwable) {
                    Log.d("ERROR", t.toString())
                }
            })
        }
    }

    private fun deletePostData() {
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

        CoroutineScope(Dispatchers.IO).launch {
            api.deletePostData(temp!!.postID).enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        println("scssucsucsucsdelte")
                        this@NoticeBoardReadActivity.finish()
                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun initCommentRecyclerView() {
        setCommentData()
        noticeBoardReadAdapter = NoticeBoardReadAdapter()
        noticeBoardReadAdapter!!.commentItemData = commentAllData
        noticeBoardReadAdapter!!.replyCommentItemData = replyCommentAllData
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

    private fun initParticipationCheck() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.substring(0 until 8)
        val userId = saveSharedPreferenceGoogleLogin.getUserId(this)!!

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
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)

        //println(userId)

        CoroutineScope(Dispatchers.IO).launch {
            api.getMyParticipantsData(0, 20).enqueue(object : Callback<List<Content>> {
                override fun onResponse(call: Call<List<Content>>, response: Response<List<Content>>) {
                    if (response.isSuccessful) {
                        println("예약 정보")
                        //데이터 청소
                        participationTempData.clear()

                        for (i in response.body()!!.indices) {
                            //println(response!!.body()!!.content[i].user.studentId)
                            participationTempData.add(PostData(
                                response.body()!![i].user.studentId,
                                response.body()!![i].postId,
                                response.body()!![i].title,
                                response.body()!![i].content,
                                response.body()!![i].targetDate,
                                response.body()!![i].targetTime,
                                response.body()!![i].category.categoryName,
                                response.body()!![i].location,
                                //participantscount가 현재 참여하는 인원들
                                response.body()!![i].participantsCount,
                                //numberOfPassengers은 총 탑승자 수
                                response.body()!![i].numberOfPassengers,
                                response.body()!![i].cost,
                                response.body()!![i].verifyGoReturn,
                                response.body()!![i].user
                            ))
                        }

                        if (participationTempData.isNotEmpty()) {
                            val temp = participationTempData.filter { it.postID == temp!!.postID }
                            if (temp.isNotEmpty()) {
                                isParticipation = true
                                CoroutineScope(Dispatchers.Main).launch {
                                    nbrBinding.readApplyBtn.visibility = View.GONE
                                    nbrBinding.readCancelBtn.visibility = View.VISIBLE
                                }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    nbrBinding.readApplyBtn.visibility = View.VISIBLE
                                    nbrBinding.readCancelBtn.visibility = View.GONE
                                }
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                nbrBinding.readApplyBtn.visibility = View.VISIBLE
                                nbrBinding.readCancelBtn.visibility = View.GONE
                            }
                        }
                    } else {
                        println(response.errorBody().toString())
                        println(response.message().toString())
                        println("실패")
                        println("faafa")
                        Log.d("add", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun deleteCommentData(deleteCommentId : Int) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.substring(0 until 8)
        val userId = saveSharedPreferenceGoogleLogin.getUserId(this)!!

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
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        ///////////////////////////////////////////////////////
        CoroutineScope(Dispatchers.IO).launch {
            api.deleteCommentData(deleteCommentId).enqueue(object : Callback<CommentData> {
                override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                    if (response.isSuccessful) {
                        println("ssssssss")
                        println(response.code())
                    } else {
                        println("faafa")
                        Log.d("add", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<CommentData>, t: Throwable) {
                    Log.e("ERROR", t.toString())
                }
            })
        }
    }

    private fun writerIdentification() {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getAccountData(email).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {

                        gender = try {
                            response.body()!!.gender
                        } catch (e: java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }

                        accountNumber = try {
                            response.body()!!.accountNumber
                        } catch (e: java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }

                        verifySmoker = try {
                            response.body()!!.verifySmoker
                        } catch (e: java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }

                        mannerCount = try {
                            response.body()!!.mannerCount
                        } catch (e: java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            0
                        }

                        grade = try {
                            response.body()!!.grade
                        } catch (e: java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            "F"
                        }

                        activityLocation = try {
                            response.body()!!.activityLocation
                        } catch (e: java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }

                        tempGender = if (gender!!) {
                            "여성"
                        } else {
                            "남성"
                        }

                        tempSmoke = if (verifySmoker!!) {
                            "흡연 X"
                        } else {
                            "흡연 O"
                        }

                        tempVerifyGoReturn = if (temp!!.postVerifyGoReturn) {
                            "등교"
                        } else {
                            "하교"
                        }

                        val textView = arrayListOf<String>()
                        textView.add(tempGender)
                        textView.add(tempSmoke)
                        textView.add(tempVerifyGoReturn)

                        for (text in textView) {
                            val addTextView = TextView(this@NoticeBoardReadActivity)
                            addTextView.text = text
                            addTextView.textSize = 12F
                            addTextView.setBackgroundResource(R.drawable.round_filter_btn)
                            addTextView.setTextColor(ContextCompat.getColor(this@NoticeBoardReadActivity ,R.color.mio_blue_4))

                            nbrBinding.readSetFilterLl.addView(addTextView)
                        }

                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.d("error", "error $t")
                }
            })
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