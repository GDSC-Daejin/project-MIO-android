package com.example.mio.NoticeBoard

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.mio.*
import com.example.mio.Adapter.NoticeBoardReadAdapter
import com.example.mio.BottomSheetFragment.BottomSheetCommentFragment
import com.example.mio.BottomSheetFragment.ReadSettingBottomSheet2Fragment
import com.example.mio.Helper.*
import com.example.mio.Model.*
import com.example.mio.TabAccount.ProfileActivity
import com.example.mio.TabCategory.MoreCarpoolTabActivity
import com.example.mio.TabCategory.MoreTaxiTabActivity
import com.example.mio.databinding.ActivityNoticeBoardReadBinding
import com.example.mio.sse.SSEClient
import com.example.mio.sse.SSEData
import com.example.mio.sse.SseHandler
import com.google.android.gms.ads.AdRequest
import com.google.android.material.chip.Chip
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import kotlinx.coroutines.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class NoticeBoardReadActivity : AppCompatActivity() {
    companion object {
        private var previousHeight = 0
    }
    private lateinit var nbrBinding : ActivityNoticeBoardReadBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    //private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private var noticeBoardReadAdapter : NoticeBoardReadAdapter? = null
    //private var replyCommentAdapter : ReplyCommentAdapter? = null

    //댓글 저장 전체 데이터
    private var commentAllData = mutableListOf<CommentData?>()
    //대댓글 용
    private var replyCommentAllData = kotlin.collections.ArrayList<CommentData?>()

    private var commentEditText = ""
    private var childComments = ArrayList<CommentData>()
    private var childCommentsSize = 0
    //모든 댓글
    private var realCommentAllData = kotlin.collections.ArrayList<CommentData?>()

    //자신이 참여한 모든 게시글
    private var participationTempData = ArrayList<PostData>()
    //자기가 참여한 게시글에 들어와있는지 체크
    private var isParticipation : Boolean? = null

    //클릭한 포스트(게시글)의 데이터 임시저장
    private var temp : PostData? = null
    //받아온 proflie
    private var tempProfile : String? = null
    //포스트의 카테고리
    private var isCategory : Boolean? = null //true = 카풀, false = 택시
    //deadline 체크
    private var isDeadLineCheck2 : Boolean? = null //현재 시간이 타겟 시간 이전이면 true, 그렇지 않으면 false
    private var isNotDeadLine2 : Boolean? = null //현재 시간이 타겟 시간 이전이면 true, 그렇지 않으면 false


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
    private val channelID = "NOTIFICATION_CHANNEL"
    private val channelName = "NOTIFICATION"

    private var sharedViewModel : SharedViewModel? = null
    private var sharedPref : SharedPref? = null

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
    //칩 생성
    private var chipList = kotlin.collections.ArrayList<Chip>()

    //어디서 이동되었는지
    private var tabType : String? = ""

    private var adRequest : AdRequest? = null

    //private var sss = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbrBinding = ActivityNoticeBoardReadBinding.inflate(layoutInflater)
        sharedPref = SharedPref(this)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.toString()

        /////
        val type = intent.getStringExtra("type")
        tabType = intent.getStringExtra("tabType").toString()
        temp = intent.getSerializableExtra("postItem") as PostData?
        //isBeforeDeadLine()
        //someFunction()
        // Observe LiveData
        // Fetch data from the ViewModel
        sharedViewModel!!.fetchIsBeforeDeadLine(this, temp!!.postID)

        // Launch a coroutine to collect state changes
        lifecycleScope.launch {
            sharedViewModel!!.isNotDeadLine.collect { isNotDeadLine ->
                val isBeforeTarget = isBeforeTarget(temp!!.postTargetDate, temp!!.postTargetTime)
                Log.e("sharedViewmodel", isNotDeadLine.toString())
                val isDeadLineCheck3 = isBeforeTarget && isNotDeadLine
                Log.e("sharedViewmodel", isDeadLineCheck3.toString())

                // Update UI based on isDeadLineCheck2 value
                isDeadLineCheck2 = isDeadLineCheck3
                Log.e("sharedViewmodel", isDeadLineCheck2.toString())
                updateUI(isDeadLineCheck3)
            }
        }



        Log.e("isDeadLineCheck", isDeadLineCheck2.toString())
        //startEventSource(temp?.user?.id!!.toLong())
        /*val eventSource: BackgroundEventSource = BackgroundEventSource //백그라운드에서 이벤트를 처리하기위한 EVENTSOURCE의 하위 클래스
            .Builder(
                SseHandler(this@NoticeBoardReadActivity),
                EventSource.Builder(
                    ConnectStrategy
                        .http(URL("https://mioserver.o-r.kr/subscribe/${temp?.user?.id}"))
                        // 서버와의 연결을 설정하는 타임아웃
                        .connectTimeout(3, TimeUnit.SECONDS)
                        // 서버로부터 데이터를 읽는 타임아웃 시간
                        .readTimeout(600, TimeUnit.SECONDS)
                )
            )
            .threadPriority(Thread.MAX_PRIORITY) //백그라운드 이벤트 처리를 위한 스레드 우선 순위를 최대로 설정합니다.
            .build()
        // EventSource 연결 시작
        eventSource.start()*/

        if (type.equals("READ")) {

            writerEmail = temp!!.user.email
            //tempProfile = intent.getSerializableExtra("uri") as String
            tempProfile = temp?.user?.profileImageUrl.toString()
            isCategory = temp!!.postCategory == "carpool"

            //initParticipationCheck()
            if (temp?.user?.gender != null && temp?.user?.verifySmoker != null && temp?.postVerifyGoReturn != null) {
                chipList.add(createNewChip(text = if (temp?.user?.verifySmoker == true) {
                    "흡연 O"
                } else {
                    "흡연 X"
                }))
                chipList.add(createNewChip(text = if (temp?.user?.gender == true) {
                    "여성"
                } else {
                    "남성"
                }))
                chipList.add(createNewChip(text = if (temp?.postVerifyGoReturn == true) {
                    "등교"
                } else {
                    "하교"
                }))


                for (i in chipList.indices) {
                    // 마지막 Chip 뷰의 인덱스를 계산
                    val lastChildIndex = nbrBinding.readSetFilterCg.childCount - 1

                    // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                    // 현재 Chip을 바로 그 앞에 추가
                    if (lastChildIndex >= 0) {
                        nbrBinding.readSetFilterCg.addView(chipList[i], lastChildIndex)
                    } else {
                        // ChipGroup에 자식이 없는 경우, 그냥 추가
                        nbrBinding.readSetFilterCg.addView(chipList[i])
                    }
                }
            }

            val imageUrl = Uri.parse(tempProfile)
            CoroutineScope(Dispatchers.Main).launch {
                Glide.with(this@NoticeBoardReadActivity)
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


            initMyBookmarkData()
            nbrBinding.readContent.text = temp!!.postContent
            nbrBinding.readUserId.text = temp!!.accountID
            nbrBinding.readCost.text = temp!!.postCost.toString()
            nbrBinding.readTitle.text = temp!!.postTitle
            nbrBinding.readNumberOfPassengersTotal.text = temp!!.postParticipationTotal.toString()
            nbrBinding.readNumberOfPassengers.text = temp!!.postParticipation.toString()
            Log.e("READ", temp!!.postLocation.split(" ").toString())
            nbrBinding.readLocation.text = if (temp!!.postLocation.split("/").last().isEmpty()) {
                temp!!.postLocation.split("/").first()
            } else {
                temp!!.postLocation.split("/").last().toString()
            }
            nbrBinding.readDetailLocation.text = temp!!.postLocation.split("/").dropLast(1).joinToString(" ")
            nbrBinding.readDateTime.text = this.getString(R.string.setText, temp!!.postTargetDate, temp!!.postTargetTime)



            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = "${temp?.postCreateDate}".replace("T", " ").split(".")[0] ?: ""

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))

            if (diffMilliseconds != null && diffSeconds != null && diffMinutes != null && diffHours != null && diffDays != null) {
                when {
                    diffSeconds <= 0 -> nbrBinding.readTimeCheck.text = "방금전"
                    diffSeconds < 60 -> nbrBinding.readTimeCheck.text = "${diffSeconds}초전"
                    diffMinutes < 60 -> nbrBinding.readTimeCheck.text = "${diffMinutes}분전"
                    diffHours < 24 -> nbrBinding.readTimeCheck.text = "${diffHours}시간전"
                    else -> nbrBinding.readTimeCheck.text = "${diffDays}일전"
                }
            }
            //true면 before, false면 before아닌것들
            //isNotDeadLine = isBeforeTarget(temp?.postTargetDate.toString(), temp?.postTargetTime.toString()) && isNotDeadLine2 == true
            Log.e("isNotDeadLine", isDeadLineCheck2.toString())
            Log.e("isNotDeadLine", isBeforeTarget(temp?.postTargetDate.toString(), temp?.postTargetTime.toString()).toString())
            Log.e("isNotDeadLine", isNotDeadLine2.toString())


            // 글쓴이가 자기자신 이라면 , 게시글 참가 + 글쓴이가 자기 자신이라면 받은 신청 보러가기고
            Log.e("NoticeBoardRead WriterCheck", email)
            Log.e("NoticeBoardRead WriterCheck", temp!!.user.email)
            Log.e("NoticeBoardRead WriterCheck", isParticipation.toString())
            Log.e("NoticeBoardRead WriterCheck", (email == temp!!.user.email).toString())
        }

        //setCommentData()
        initCommentRecyclerView()


        nbrBinding.goLocation.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            intent.putExtra("latitude", temp!!.postlatitude)
            intent.putExtra("longitude", temp!!.postlongitude)
            startActivity(intent)
        }

        if (email == temp!!.user.email) {
            nbrBinding.readSetting.visibility = View.VISIBLE
        } else {
            nbrBinding.readSetting.visibility = View.GONE
        }
        nbrBinding.readSetting.setOnClickListener {
            if (email == temp!!.user.email) {
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
                                    requestActivity.launch(intent)
                                }

                                "삭제" -> {

                                    //사용할 곳
                                    val layoutInflater = LayoutInflater.from(context)
                                    val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                                    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                                        .setView(view)
                                        .create()
                                    val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                                    val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                                    val dialogRightBtn =  view.findViewById<View>(R.id.dialog_right_btn)
// 다이얼로그가 보여진 후에 루트 뷰의 레이아웃 파라미터를 수정
                                    alertDialog.setOnShowListener {
                                        val window = alertDialog.window
                                        window?.setBackgroundDrawableResource(android.R.color.transparent)

                                        val layoutParams = window?.attributes
                                        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT // 다이얼로그의 폭을 MATCH_PARENT로 설정
                                        window?.attributes = layoutParams

                                        // 루트 뷰의 마진을 설정
                                        val rootView = view.parent as View
                                        val params = rootView.layoutParams as ViewGroup.MarginLayoutParams
                                        val marginInDp = 20
                                        val scale = this@NoticeBoardReadActivity.resources.displayMetrics.density
                                        val marginInPx = (marginInDp * scale + 0.5f).toInt()
                                        params.setMargins(marginInPx, 0, marginInPx, 0)
                                        rootView.layoutParams = params
                                    }
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
            } else { //게시글 작성자가 아니라면
                val bottomSheet = ReadSettingBottomSheet2Fragment()
                bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
                bottomSheet.apply {
                    setCallback(object : ReadSettingBottomSheet2Fragment.OnSendFromBottomSheetDialog{
                        override fun sendValue(value: String) {
                            Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                            getBottomSheetData = value

                            when(value) {
                                "신고" -> {
                                    /*val intent = Intent(this@NoticeBoardReadActivity, NoticeBoardEditActivity::class.java).apply {
                                        putExtra("type", "EDIT")
                                        putExtra("editPostData", temp)
                                    }
                                    startActivity(intent)*/

                                    //신고 어케 할건지 Todo
                                }

                                "북마크" -> {
                                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                                    val token = saveSharedPreferenceGoogleLogin.getToken(this@NoticeBoardReadActivity).toString()
                                    val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this@NoticeBoardReadActivity).toString()
                                    val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this@NoticeBoardReadActivity)!!.split("@").map { it }.first()
                                    val userId = saveSharedPreferenceGoogleLogin.getUserId(this@NoticeBoardReadActivity)!!

                                    val interceptor = Interceptor { chain ->
                                        var newRequest: Request
                                        if (token != null && token != "") { // 토큰이 없는 경우
                                            // Authorization 헤더에 토큰 추가
                                            newRequest =
                                                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                                            val expireDate: Long = getExpireDate.toLong()
                                            if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                                                //refresh 들어갈 곳
                                                /*newRequest =
                                                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                                val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                                startActivity(intent)
                                                finish()
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
                                    Log.d("NoticeReadGetParticipation", userId.toString())
                                    api.addBookmark(postId = temp?.postID!!).enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d("noticeboardread", response.code().toString())
                                            } else {
                                                Log.e("read addbookmark",response.errorBody()?.string()!!)
                                                println(response.message().toString())
                                                println("실패")
                                                println("faafa")
                                                Log.d("add", response.errorBody()?.string()!!)
                                                Log.d("message", call.request().toString())
                                                Log.d("f", response.code().toString())
                                            }
                                        }

                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            Log.d("error", t.toString())
                                        }
                                    })
                                }
                            }

                        }
                    })
                }
            }
        }

        nbrBinding.readCommentTv.setOnClickListener {
            val bottomSheet = BottomSheetCommentFragment(null, null)
            //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                        commentEditText = value
                        Log.e("readSendComment", commentEditText)
                        Log.e("nbrBinding.readSendComment", isReplyComment.toString())
                        // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                        val inputMethodManager = this@NoticeBoardReadActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                        if (inputMethodManager.isActive) {
                            // 가상 키보드가 올라가 있다면 내립니다.
                            inputMethodManager.hideSoftInputFromWindow(nbrBinding.readCommentTv.windowToken, 0)
                        }
                        if (isReplyComment) { //대댓글
                            //서버에서 원하는 형식으로 날짜 설정
                            /*val now = System.currentTimeMillis()
                            val date = Date(now)
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                            val currentDate = sdf.format(date)
                            val formatter = DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")
                                .withZone(ZoneId.systemDefault())
                            val result: Instant = Instant.from(formatter.parse(currentDate))*/
                            val nowInKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                            // KST 시간을 UTC 시간대로 변환
                            val nowInUTC2 = nowInKST.withZoneSameInstant(ZoneId.of("UTC"))
                            // ISO 8601 형식으로 변환 (UTC 시간대, 'Z' 포함)
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                            val result = nowInKST.format(formatter)

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
                                        /*newRequest =
                                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                        val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                        startActivity(intent)
                                        finish()
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
                                            fetchAllComments()
                                            println("대댓글달기성공")
                                            //한 번 달고 끝내야하니 false전달
                                            sharedViewModel!!.postReply(reply = false)
                                            //sendAlarmData ("댓글 ", commentEditText, temp)
                                            commentEditText = ""
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

                            //nbrBinding.readCommentTotal.text = (commentAllData.size + )

                        } else {  //댓글
                            //서버에서 원하는 형식으로 날짜 설정
                            /*val now = System.currentTimeMillis()
                            val date = Date(now)
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                            val currentDate = sdf.format(date)
                            val formatter = DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")
                                .withZone(ZoneId.systemDefault())
                            val result: Instant = Instant.from(formatter.parse(currentDate))*/
                            val nowInKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                            // ISO 8601 형식으로 변환 (UTC 시간대, 'Z' 포함)
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                            val result = nowInKST.format(formatter)


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
                                        /*newRequest =
                                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                        val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                        startActivity(intent)
                                        finish()
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
                            Log.d("commentTemt", "$commentTemp")
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
                                            //sendAlarmData("댓글", commentEditText, temp)
                                            commentEditText = ""
                                            nbrBinding.readCommentTotal.text = commentAllData.size.toString()
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
                        }
                    }
                })
            }


        }
        //대댓글 수정 및 삭제
        /*noticeBoardReadAdapter?.setCommentClickListener(object : NoticeBoardReadAdapter.CommentClickListener {

        })*/


        //대댓글 달기 또는 다른 기능 추가 예정
        noticeBoardReadAdapter?.setItemClickListener(object : NoticeBoardReadAdapter.ItemClickListener {
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
                    vibration.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                Log.e("setItemClickListener", "clclclclclclcl")
                val parent = commentAllData.find { it?.commentId == itemId }?.user?.studentId
                val bottomSheet = BottomSheetCommentFragment(null, parent)
                //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                bottomSheet.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet.tag)
                bottomSheet.apply {
                    setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                        override fun sendValue(value: String) {
                            Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                            commentEditText = value
                            sharedViewModel!!.postReply(reply = true) //수정


                            //어떤 댓글을 선택했는지 확인
                            parentPosition = itemId

                            if (isReplyComment) { //대댓글
                                //서버에서 원하는 형식으로 날짜 설정
                                /*val now = System.currentTimeMillis()
                                val date = Date(now)
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                                val currentDate = sdf.format(date)
                                val formatter = DateTimeFormatter
                                    .ofPattern("yyyy-MM-dd HH:mm:ss")
                                    .withZone(ZoneId.systemDefault())
                                val result: Instant = Instant.from(formatter.parse(currentDate))*/
                                val nowInKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                // KST 시간을 UTC 시간대로 변환
                                val nowInUTC2 = nowInKST.withZoneSameInstant(ZoneId.of("UTC"))
                                // ISO 8601 형식으로 변환 (UTC 시간대, 'Z' 포함)
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                val result = nowInKST.format(formatter)

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
                                            /*newRequest =
                                                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                            val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                            startActivity(intent)
                                            finish()
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
                                                fetchAllComments()
                                                println("대댓글달기성공")
                                                //한 번 달고 끝내야하니 false전달
                                                sharedViewModel!!.postReply(reply = false)
                                                //sendAlarmData("댓글 ", commentEditText, temp)
                                                commentEditText = ""
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

                                //nbrBinding.readCommentTotal.text = (commentAllData.size + )

                            }
                        }
                    })
                }
            }
            //댓글 수정 및 삭제할 때
            override fun onLongClick(view: View, position: Int, itemId: Int) {
                val checkData = commentAllData.find { it?.commentId == itemId}
                if (email == checkData?.user?.email && checkData.content != "삭제된 댓글입니다.") {
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
                                    "수정" -> { //부모댓글 수정용
                                        //자동으로 포커스 줘서 대댓글 달게 하기
                                        val filterCommentText = realCommentAllData.find { it!!.commentId == commentPosition }
                                        val bottomSheet2 = BottomSheetCommentFragment(filterCommentText, null)
                                        //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                                        bottomSheet2.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet2.tag)
                                        bottomSheet2.apply {
                                            setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                                                override fun sendValue(value: String) {
                                                    Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                                                    commentEditText = value

                                                    Log.e("readEditSendCommentgetBottomSheetCommentData" , getBottomSheetCommentData)
                                                    val now = System.currentTimeMillis()
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
                                                                /*newRequest =
                                                                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                                                val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                                                startActivity(intent)
                                                                finish()
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
                                                                    // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                                                                    val inputMethodManager = this@NoticeBoardReadActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                                    // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                                                                    if (inputMethodManager.isActive) {
                                                                        // 가상 키보드가 올라가 있다면 내립니다.
                                                                        inputMethodManager.hideSoftInputFromWindow(nbrBinding.readCommentTv.windowToken, 0)
                                                                    }
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
                                                }
                                            })
                                        }


                                    }

                                    "삭제" -> {
                                        if (commentAllData.find { it?.commentId == itemId }?.content != "삭제된 댓글입니다.") {
                                            deleteCommentData(itemId)
                                            fetchAllComments()
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }

            override fun onReplyClicked(
                status: String?,
                commentId: Int?,
                commentData: CommentData?
            ) {
                if (status == "수정") {
                    println(status)

                    getBottomSheetCommentData = "수정"
                    val bottomSheet2 = BottomSheetCommentFragment(commentData, null)
                    //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                    bottomSheet2.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet2.tag)
                    bottomSheet2.apply {
                        setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                            override fun sendValue(value: String) {
                                Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                                commentEditText = value
                                println(commentId) //이건 부모 댓글 id
                                println(commentData?.content.toString()) //이건 수정할 대댓글의 정보
                                Log.e("setCommentClickListener", "clclclclclc2lcl")
                            }
                        })
                    }



                } else {
                    if (commentId != null) {
                        deleteCommentData(commentId)
                        fetchAllComments()
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
            Log.e("sharedViewmodel" , isReplyComment.toString())
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

        nbrBinding.readBookmark.setOnClickListener {
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            val token = saveSharedPreferenceGoogleLogin.getToken(this@NoticeBoardReadActivity).toString()
            val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this@NoticeBoardReadActivity).toString()
            val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this@NoticeBoardReadActivity)!!.split("@").map { it }.first()
            val userId = saveSharedPreferenceGoogleLogin.getUserId(this@NoticeBoardReadActivity)!!

            val interceptor = Interceptor { chain ->
                var newRequest: Request
                if (token != null && token != "") { // 토큰이 없는 경우
                    // Authorization 헤더에 토큰 추가
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                    val expireDate: Long = getExpireDate.toLong()
                    if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                        //refresh 들어갈 곳
                        /*newRequest =
                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                        val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                        startActivity(intent)
                        finish()
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


            Log.d("NoticeReadGetParticipation", temp?.postID!!.toString())
            api.addBookmark(postId = temp?.postID!!).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("noticeboardread", response.code().toString())
                        initMyBookmarkData()
                    } else {
                        Log.e("read addbookmark", response.errorBody()?.string()!!)
                        println(response.message().toString())
                        println("실패")
                        println("faafa")
                        Log.d("add", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }


        //뒤로가기
        nbrBinding.backArrow.setOnClickListener {
            val intent = Intent(this@NoticeBoardReadActivity, MainActivity::class.java).apply {
                //flag넣고 resultok
                putExtra("flag", 22)
                putExtra("selectedTab", tabType)
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@NoticeBoardReadActivity, MainActivity::class.java).apply {
                    //flag넣고 resultok
                    putExtra("flag", 22)
                    putExtra("selectedTab", tabType)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        })

        setContentView(nbrBinding.root)
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val post = it.data?.getSerializableExtra("postData") as PostData?
                Log.d("read post", post.toString())
                when(it.data?.getIntExtra("flag", -1)) {
                    //edit 게시글 수정 시
                    0 -> {
                        if (post != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                temp = post
                                nbrBinding.readContent.text = post.postContent
                                nbrBinding.readUserId.text = post.accountID
                                nbrBinding.readCost.text = post.postCost.toString()
                                nbrBinding.readTitle.text = post.postTitle
                                nbrBinding.readNumberOfPassengersTotal.text = post.postParticipationTotal.toString()
                                nbrBinding.readNumberOfPassengers.text = post.postParticipation.toString()
                                nbrBinding.readLocation.text = if (post.postLocation.split("/").last().isEmpty()) {
                                    temp!!.postLocation.split("/").first()
                                } else {
                                    temp!!.postLocation.split("/").last().toString()
                                }
                                nbrBinding.readDetailLocation.text = post.postLocation.split("/").dropLast(1).joinToString(" ")
                                nbrBinding.readDateTime.text = this@NoticeBoardReadActivity.getString(R.string.setText, post.postTargetDate, post.postTargetTime)
/*
                                chipList.clear()
                                nbrBinding.readSetFilterCg.removeAllViewsInLayout()
                                chipList.add(createNewChip(text = if (post.user?.verifySmoker == true) {
                                    "흡연 O"
                                } else {
                                    "흡연 X"
                                }))
                                chipList.add(createNewChip(text = if (post.user?.gender == true) {
                                    "여성"
                                } else {
                                    "남성"
                                }))
                                chipList.add(createNewChip(text = if (post.postVerifyGoReturn == true) {
                                    "등교"
                                } else {
                                    "하교"
                                }))


                                for (i in chipList.indices) {
                                    // 마지막 Chip 뷰의 인덱스를 계산
                                    val lastChildIndex = nbrBinding.readSetFilterCg.childCount - 1

                                    // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                                    // 현재 Chip을 바로 그 앞에 추가
                                    if (lastChildIndex >= 0) {
                                        nbrBinding.readSetFilterCg.addView(chipList[i], lastChildIndex)
                                    } else {
                                        // ChipGroup에 자식이 없는 경우, 그냥 추가
                                        nbrBinding.readSetFilterCg.addView(chipList[i])
                                    }
                                }*/
                            }
                        }

                    }
                    //taxi
                    1 -> {
                        if (post != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                temp = post
                                nbrBinding.readContent.text = post.postContent
                                nbrBinding.readUserId.text = post.accountID
                                nbrBinding.readCost.text = post.postCost.toString()
                                nbrBinding.readTitle.text = post.postTitle
                                nbrBinding.readNumberOfPassengersTotal.text = post.postParticipationTotal.toString()
                                nbrBinding.readNumberOfPassengers.text = post.postParticipation.toString()
                                nbrBinding.readLocation.text = if (post.postLocation.split("/").last().isEmpty()) {
                                    temp!!.postLocation.split("/").first()
                                } else {
                                    temp!!.postLocation.split("/").last().toString()
                                }
                                nbrBinding.readDetailLocation.text = post.postLocation.split("/").dropLast(1).joinToString(" ")
                                nbrBinding.readDateTime.text = this@NoticeBoardReadActivity.getString(R.string.setText, post.postTargetDate, post.postTargetTime)

                                chipList.clear()
                                nbrBinding.readSetFilterCg.removeAllViewsInLayout()
                                chipList.add(createNewChip(text = if (post.user?.verifySmoker == true) {
                                    "흡연 O"
                                } else {
                                    "흡연 X"
                                }))
                                chipList.add(createNewChip(text = if (post.user?.gender == true) {
                                    "여성"
                                } else {
                                    "남성"
                                }))
                                chipList.add(createNewChip(text = if (post.postVerifyGoReturn == true) {
                                    "등교"
                                } else {
                                    "하교"
                                }))


                                for (i in chipList.indices) {
                                    // 마지막 Chip 뷰의 인덱스를 계산
                                    val lastChildIndex = nbrBinding.readSetFilterCg.childCount - 1

                                    // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                                    // 현재 Chip을 바로 그 앞에 추가
                                    if (lastChildIndex >= 0) {
                                        nbrBinding.readSetFilterCg.addView(chipList[i], lastChildIndex)
                                    } else {
                                        // ChipGroup에 자식이 없는 경우, 그냥 추가
                                        nbrBinding.readSetFilterCg.addView(chipList[i])
                                    }
                                }
                            }
                        }
                    }

                    33 -> {
                        refreshNoticeBoardReadData()
                    }

                }
            }
        }
    }

    private fun refreshNoticeBoardReadData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()


        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    this.finish()
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
        /////

        api.getPostIdDetailSearch(temp?.postID!!).enqueue(object : Callback<Content> {
            override fun onResponse(call: Call<Content>, response: Response<Content>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null && responseData.isDeleteYN == "N") {
                        responseData.let {
                            temp = PostData(
                                it.user.studentId,
                                it.postId,
                                it.title,
                                it.content,
                                it.createDate,
                                it.targetDate,
                                it.targetTime,
                                it.category.categoryName,
                                it.location,
                                //participantscount가 현재 참여하는 인원들
                                it.participantsCount,
                                //numberOfPassengers은 총 탑승자 수
                                it.numberOfPassengers,
                                it.cost,
                                it.verifyGoReturn,
                                it.user,
                                it.latitude,
                                it.longitude
                            )
                            // 글쓴이가 자기자신 이라면 , 게시글 참가 + 글쓴이가 자기 자신이라면 받은 신청 보러가기고
                            Log.e("NoticeBoardRead refresh", email)
                            Log.e("NoticeBoardRead refresh", temp!!.user.email)
                            Log.e("NoticeBoardRead refresh", isParticipation.toString())
                            Log.e("NoticeBoardRead refresh", (email == temp!!.user.email).toString())
                            initParticipationCheck(it.postType == "BEFORE_DEADLINE")
                            writerEmail = temp!!.user.email
                            //tempProfile = intent.getSerializableExtra("uri") as String
                            tempProfile = temp?.user?.profileImageUrl.toString()
                            isCategory = temp!!.postCategory == "carpool"
                            /*if (temp?.user?.gender != null && temp?.user?.verifySmoker != null && temp?.postVerifyGoReturn != null) {
                                chipList.add(createNewChip(text = if (temp?.user?.verifySmoker == true) {
                                    "흡연 O"
                                } else {
                                    "흡연 X"
                                }))
                                chipList.add(createNewChip(text = if (temp?.user?.gender == true) {
                                    "여성"
                                } else {
                                    "남성"
                                }))
                                chipList.add(createNewChip(text = if (temp?.postVerifyGoReturn == true) {
                                    "등교"
                                } else {
                                    "하교"
                                }))


                                for (i in chipList.indices) {
                                    // 마지막 Chip 뷰의 인덱스를 계산
                                    val lastChildIndex = nbrBinding.readSetFilterCg.childCount - 1

                                    // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                                    // 현재 Chip을 바로 그 앞에 추가
                                    if (lastChildIndex >= 0) {
                                        nbrBinding.readSetFilterCg.addView(chipList[i], lastChildIndex)
                                    } else {
                                        // ChipGroup에 자식이 없는 경우, 그냥 추가
                                        nbrBinding.readSetFilterCg.addView(chipList[i])
                                    }
                                }
                            }*/

                            /*val imageUrl = Uri.parse(tempProfile)
                            CoroutineScope(Dispatchers.Main).launch {
                                Glide.with(this@NoticeBoardReadActivity)
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
                            }*/
                            nbrBinding.readContent.text = temp!!.postContent
                            nbrBinding.readUserId.text = temp!!.accountID
                            nbrBinding.readCost.text = temp!!.postCost.toString()
                            nbrBinding.readTitle.text = temp!!.postTitle
                            nbrBinding.readNumberOfPassengersTotal.text = temp!!.postParticipationTotal.toString()
                            nbrBinding.readNumberOfPassengers.text = temp!!.postParticipation.toString()
                            //Log.e("READ", temp!!.postLocation.split(" ").toString())
                            nbrBinding.readLocation.text = if (temp!!.postLocation.split("/").last().isEmpty()) {
                                temp!!.postLocation.split("/").first()
                            } else {
                                temp!!.postLocation.split("/").last().toString()
                            }
                            nbrBinding.readDetailLocation.text = temp!!.postLocation.split("/").dropLast(1).joinToString(" ")
                            nbrBinding.readDateTime.text = this@NoticeBoardReadActivity.getString(R.string.setText, temp!!.postTargetDate, temp!!.postTargetTime)



                            val now = System.currentTimeMillis()
                            val date = Date(now)
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                            val currentDate = sdf.format(date)


                            val postDateTime = "${temp?.postCreateDate}".replace("T", " ").split(".")[0] ?: ""

                            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
                            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
                            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
                            val diffSeconds = diffMilliseconds?.div(1000)
                            val diffMinutes = diffMilliseconds?.div((60 * 1000))
                            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
                            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))

                            if (diffMilliseconds != null && diffSeconds != null && diffMinutes != null && diffHours != null && diffDays != null) {
                                when {
                                    diffSeconds <= 0 -> nbrBinding.readTimeCheck.text = "방금전"
                                    diffSeconds < 60 -> nbrBinding.readTimeCheck.text = "${diffSeconds}초전"
                                    diffMinutes < 60 -> nbrBinding.readTimeCheck.text = "${diffMinutes}분전"
                                    diffHours < 24 -> nbrBinding.readTimeCheck.text = "${diffHours}시간전"
                                    else -> nbrBinding.readTimeCheck.text = "${diffDays}일전"
                                }
                            }
                        }
                    }
                /*else { //지워지거나 마감기한지나거나 이미 완료된 게시글
                        finish()
                    }*/
                } else {
                    Log.e("notice read detail", response.code().toString())
                    Log.e("notice read detail", response.errorBody()?.string()!!)
                }
            }

            override fun onFailure(call: Call<Content>, t: Throwable) {
                Log.e("notice read onFailure", t.toString())
            }
        })
    }

    private fun initMyBookmarkData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()


        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    this.finish()
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
        var thisData : kotlin.collections.List<BookMarkResponseData>? = null
        api.getBookmark().enqueue(object : Callback<List<BookMarkResponseData>> {
            override fun onResponse(call: Call<List<BookMarkResponseData>>, response: Response<List<BookMarkResponseData>>) {
                if (response.isSuccessful) {
                    Log.e("success getBookmark", response.code().toString())
                    val responseData = response.body()
                    Log.d("success getBookmark", responseData.toString())
                    responseData.let {
                        thisData = it
                    }
                    if (thisData?.find { it.postId == temp?.postID } != null) {
                        nbrBinding.readBookmark.apply {
                            setBackgroundResource(R.drawable.read_update_bookmark_icon)
                        }
                    } else {
                        nbrBinding.readBookmark.apply {
                            setBackgroundResource(R.drawable.read_bookmark_icon)
                        }
                    }
                } else {
                    Log.e("f", response.code().toString())
                }
            }

            override fun onFailure(call: Call<List<BookMarkResponseData>>, t: Throwable) {
                Log.d("error", t.toString())
            }
        })
    }
    private fun initParticipationCheck(isDeadLineCheck2: Boolean) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
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
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    finish()
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
        Log.d("NoticeReadGetParticipation", userId.toString())
        api.getParticipationData(postId = temp?.postID!!).enqueue(object : Callback<List<ParticipationData>> {
            override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                if (response.isSuccessful) {
                    Log.d("NoticeReadGetParticipation", "suceessssssss")
                    if (response.body()?.find { it.userId == userId} != null){
                        isParticipation = true
                        Log.d("NoticeReadGetParticipation", isParticipation.toString())
                        participantApplyBtnSet(isParticipation!!, isDeadLineCheck2)
                        Log.d("NoticeReadGetParticipation", isDeadLineCheck2.toString())
                    } else {
                        isParticipation = false
                        Log.d("NoticeReadGetParticipation", isParticipation.toString())
                        //participantApplyBtnSet(isParticipation!!)
                        participantApplyBtnSet(isParticipation!!, isDeadLineCheck2)
                        Log.d("NoticeReadGetParticipation", isDeadLineCheck2.toString())
                    }
                } else {
                    Log.e("read participation", response.errorBody()?.string()!!)
                    println(response.message().toString())
                    Log.d("f", response.code().toString())
                }
            }

            override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                Log.d("error", t.toString())
            }
        })
    }

    private fun updateUI(isDeadLineCheck2: Boolean) {
        // Implement your UI update logic based on isDeadLineCheck2
        if (isDeadLineCheck2) {
            // Actions when deadline check is true
            Log.d("NoticeBoardReadActivity", "Deadline check is true")
            initParticipationCheck(isDeadLineCheck2)
        } else {
            // Actions when deadline check is false
            Log.d("NoticeBoardReadActivity", "Deadline check is false")
            initParticipationCheck(isDeadLineCheck2)
        }
    }

    private fun participantApplyBtnSet(isParticipation : Boolean, isDeadLineCheck2: Boolean) {
        //작성자로 참여되어있을 때
        if (email == temp!!.user.email) {
            val typeface = resources.getFont(R.font.pretendard_medium)
            Log.e("participantApplyBtnSet", isParticipation.toString())
            Log.e("participantApplyBtnSet", email)
            Log.e("participantApplyBtnSet", temp!!.user.email)
            Log.e("participantApplyBtnSet", "first")
            nbrBinding.readApplyBtn.text = "받은 신청 보러가기"
            CoroutineScope(Dispatchers.Main).launch {
                nbrBinding.readApplyBtn.apply {
                    setBackgroundResource(R.drawable.read_apply_btn_update_layout)
                    setTypeface(typeface)
                    nbrBinding.readApplyBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@NoticeBoardReadActivity, R.color.mio_gray_11))
                }
            }

            nbrBinding.readApplyBtn.setOnClickListener {
                Log.e("readApplyBtn1", "clickckckckckckckckck")
                val intent = Intent(this@NoticeBoardReadActivity, ParticipationReceiveActivity::class.java).apply {
                    putExtra("postId", temp!!.postID)
                }
                requestActivity.launch(intent)
            }
        } else if (!isParticipation && email != temp!!.user.email) { //게시글에 참여하지도 않았고 글쓴이도 아니라면? 신청하기
            Log.e("participantApplyBtnSet", isParticipation.toString())
            Log.e("participantApplyBtnSet", email)
            Log.e("participantApplyBtnSet", temp!!.user.email)
            Log.e("participantApplyBtnSet", "second")
            val typeface = resources.getFont(R.font.pretendard_medium)

            nbrBinding.readApplyBtn.text = "신청하기"
            nbrBinding.readApplyBtn.setTextColor(ContextCompat.getColor(this@NoticeBoardReadActivity ,R.color.mio_gray_3))

            CoroutineScope(Dispatchers.Main).launch {
                nbrBinding.readApplyBtn.apply {
                    setBackgroundResource(R.drawable.read_apply_btn_layout)
                    setTypeface(typeface)
                    nbrBinding.readApplyBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@NoticeBoardReadActivity, R.color.mio_blue_5))
                }
            }

            if (isDeadLineCheck2) {
                nbrBinding.readApplyBtn.setOnClickListener {
                    Log.e("readApplyBtn2", "clickckckckckckckckck")
                    setApplyNoticeBoard()
                }
            } else {
                nbrBinding.readApplyBtn.setOnClickListener {
                    checkResponseBody = "마감된 게시글입니다."
                    //사용할 곳
                    val layoutInflater = LayoutInflater.from(this@NoticeBoardReadActivity)
                    val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                    val alertDialog = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                        .setView(view)
                        .create()
                    val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                    val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                    val dialogRightBtn =  view.findViewById<Button>(R.id.dialog_right_btn)
                    dialogLeftBtn.visibility = View.GONE
                    dialogRightBtn.text = "확인"

                    // 다이얼로그의 버튼을 가운데 정렬
                    val params = dialogRightBtn.layoutParams as LinearLayout.LayoutParams
                    params.width = LayoutParams.MATCH_PARENT
                    dialogRightBtn.layoutParams = params


                    dialogContent.text = checkResponseBody //"이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"
                    //확인
                    dialogRightBtn.setOnClickListener {
                        alertDialog.dismiss()
                    }
                    alertDialog.show()
                }
            }

        } else if (isParticipation && email != temp?.user?.email) { //신청은 되있으나 글쓴이가 아닐 때는 신청취소쪽으로
            Log.e("participantApplyBtnSet", isParticipation.toString())
            Log.e("participantApplyBtnSet", email)
            Log.e("participantApplyBtnSet", temp!!.user.email)
            Log.e("participantApplyBtnSet", "third")
            val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
            nbrBinding.readApplyBtn.text = "신청 취소하기"
            CoroutineScope(Dispatchers.Main).launch {
                nbrBinding.readApplyBtn.apply {
                    setBackgroundResource(R.drawable.read_apply_btn_update_layout)
                    setTypeface(typeface)
                    nbrBinding.readApplyBtn.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@NoticeBoardReadActivity,
                            R.color.mio_gray_7
                        )
                    )
                }
            }

            if (isDeadLineCheck2 == true) {
                nbrBinding.readApplyBtn.setOnClickListener {
                    Log.e("readApplyBtn3", "clickckckckckckckckck")
                    val layoutInflater = LayoutInflater.from(this@NoticeBoardReadActivity)
                    val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                    val alertDialog = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                        .setView(view)
                        .create()
                    val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                    val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                    val dialogRightBtn = view.findViewById<View>(R.id.dialog_right_btn)

// 다이얼로그가 보여진 후에 루트 뷰의 레이아웃 파라미터를 수정
                    alertDialog.setOnShowListener {
                        val window = alertDialog.window
                        window?.setBackgroundDrawableResource(android.R.color.transparent)

                        val layoutParams = window?.attributes
                        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT // 다이얼로그의 폭을 MATCH_PARENT로 설정
                        window?.attributes = layoutParams

                        // 루트 뷰의 마진을 설정
                        val rootView = view.parent as View
                        val params = rootView.layoutParams as ViewGroup.MarginLayoutParams
                        val marginInDp = 20
                        val scale = this@NoticeBoardReadActivity.resources.displayMetrics.density
                        val marginInPx = (marginInDp * scale + 0.5f).toInt()
                        params.setMargins(marginInPx, 0, marginInPx, 0)
                        rootView.layoutParams = params
                    }
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
                                    /*newRequest =
                                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                    startActivity(intent)
                                    finish()
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
                        RetrofitServerConnect.create(this).deleteParticipate(postId = temp!!.postID).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d("check", response.code().toString())
                                    Toast.makeText(this@NoticeBoardReadActivity, "성공적으로 취소되었습니다", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (response.code() == 500 && response.errorBody()?.string()!!.any { it in '\uAC00'..'\uD7AF' }) {
                                        //Toast.makeText(this@NoticeBoardReadActivity)
                                        //사용할 곳
                                        val layoutInflater1 = LayoutInflater.from(this@NoticeBoardReadActivity)
                                        val view1 = layoutInflater1.inflate(R.layout.dialog_layout, null)
                                        val alertDialog1 = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                                            .setView(view1)
                                            .create()
                                        val dialogContent1 = view1.findViewById<TextView>(R.id.dialog_tv)
                                        val dialogLeftBtn1 = view1.findViewById<View>(R.id.dialog_left_btn)
                                        val dialogRightBtn1 =  view1.findViewById<Button>(R.id.dialog_right_btn)
                                        dialogLeftBtn1.visibility = View.GONE
                                        dialogRightBtn1.text = "확인"

                                        // 다이얼로그의 버튼을 가운데 정렬
                                        val params = dialogRightBtn1.layoutParams as LinearLayout.LayoutParams
                                        params.width = LayoutParams.MATCH_PARENT
                                        dialogRightBtn1.layoutParams = params


                                        dialogContent1.text = response.errorBody()?.string()!!.toString() //"이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"
                                        //확인
                                        dialogRightBtn1.setOnClickListener {
                                            alertDialog1.dismiss()
                                        }
                                        alertDialog1.show()
                                    } else {
                                        Log.e("cancel error", response.errorBody()?.string()!!)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.d("error", t.toString())
                            }
                        })
                        alertDialog.dismiss()
                        refreshNoticeBoardReadData()
                    }
                    alertDialog.show()
                }
            } else {
                nbrBinding.readApplyBtn.setOnClickListener {
                    checkResponseBody = "마감된 게시글입니다."
                    //사용할 곳
                    val layoutInflater = LayoutInflater.from(this@NoticeBoardReadActivity)
                    val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                    val alertDialog = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                        .setView(view)
                        .create()
                    val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                    val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                    val dialogRightBtn =  view.findViewById<Button>(R.id.dialog_right_btn)
                    dialogLeftBtn.visibility = View.GONE
                    dialogRightBtn.text = "확인"

                    // 다이얼로그의 버튼을 가운데 정렬
                    val params = dialogRightBtn.layoutParams as LinearLayout.LayoutParams
                    params.width = LayoutParams.MATCH_PARENT
                    dialogRightBtn.layoutParams = params


                    dialogContent.text = checkResponseBody //"이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"
                    //확인
                    dialogRightBtn.setOnClickListener {
                        alertDialog.dismiss()
                    }
                    alertDialog.show()
                }
            }
        }
    }

    fun isBeforeTarget(targetDate: String, targetTime: String): Boolean {
        // 타겟 날짜와 시간을 문자열로 결합
        val targetDateTimeStr = "${targetDate}T${targetTime}"

        // 날짜와 시간 포맷을 정의
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        // 타겟 날짜와 시간을 LocalDateTime 객체로 변환
        val targetDateTime = LocalDateTime.parse(targetDateTimeStr, formatter)

        // 현재 날짜와 시간을 LocalDateTime 객체로 가져옴
        val currentDateTime = LocalDateTime.now()

        // 현재 시간이 타겟 시간 이전인지 확인
        return currentDateTime.isBefore(targetDateTime)
    }

    /*@OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun isBeforeDeadLine(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            RetrofitServerConnect.create(this@NoticeBoardReadActivity)
                .getPostIdDetailSearch(temp!!.postID)
                .enqueue(object : Callback<Content> {
                    override fun onResponse(call: Call<Content>, response: Response<Content>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            Log.e("isbefore", response.code().toString())
                            if (responseData != null && responseData.isDeleteYN != "Y") {
                                continuation.resume(responseData.postType == "BEFORE_DEADLINE")
                            } else {
                                continuation.resume(false)
                            }
                        } else {
                            Log.e("isbefore", response.code().toString())
                            Log.e("isbefore", response.errorBody()?.string()!!)
                            continuation.resume(false)
                        }
                    }

                    override fun onFailure(call: Call<Content>, t: Throwable) {
                        Log.e("isbefore", t.toString())
                        continuation.resume(false)
                    }
                })
        }
    }*/

    /*private suspend fun checkConditions(): Boolean {
        val postTargetDate = temp?.postTargetDate.toString()
        val postTargetTime = temp?.postTargetTime.toString()
        val isNotDeadLine1 = isBeforeTarget(postTargetDate, postTargetTime)
        val isBeforeDeadlineResult = isBeforeDeadLine()
        return isNotDeadLine1 && isBeforeDeadlineResult
    }

    private fun someFunction() {
        lifecycleScope.launch {
            val isDeadLineCheck = checkConditions()
            isDeadLineCheck2 = isDeadLineCheck
        }
    }*/

    private fun setApplyNoticeBoard() {
        Log.d("setApplyNoticeBoard", "setApplyNoticeBoard")
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
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    finish()
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

        api.checkParticipate(postId = temp!!.postID).enqueue(object : Callback<CheckParticipateData> {
            override fun onResponse(call: Call<CheckParticipateData>, response: Response<CheckParticipateData>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    //true가 예약된 게 없음
                    if (responseData?.check == true) {
                        Log.d("setApplyNoticeBoard", "API call success")
                        Log.d("setApplyNoticeBoard", "Response body: ${response.body()}")
                        Log.d("setApplyNoticeBoard", "Response body: $isDeadLineCheck2")
                        //예약된 게 없음
                        //현재 시간이 타겟 시간 이전이면 true, 그렇지 않으면 false
                        if (isDeadLineCheck2 == true) {
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
                                    requestActivity.launch(intent)
                                    //finish 해보기
                                    //this@NoticeBoardReadActivity.finish()
                                }
                                alertDialog.show()
                            } else {
                                checkResponseBody = "마감된 게시글입니다."
                                //사용할 곳
                                val layoutInflater = LayoutInflater.from(this@NoticeBoardReadActivity)
                                val view = layoutInflater.inflate(R.layout.dialog_layout, null)
                                val alertDialog = AlertDialog.Builder(this@NoticeBoardReadActivity, R.style.CustomAlertDialog)
                                    .setView(view)
                                    .create()
                                val dialogContent = view.findViewById<TextView>(R.id.dialog_tv)
                                val dialogLeftBtn = view.findViewById<View>(R.id.dialog_left_btn)
                                val dialogRightBtn =  view.findViewById<Button>(R.id.dialog_right_btn)
                                dialogLeftBtn.visibility = View.GONE
                                dialogRightBtn.text = "확인"

                                // 다이얼로그의 버튼을 가운데 정렬
                                val params = dialogRightBtn.layoutParams as LinearLayout.LayoutParams
                                params.width = LayoutParams.MATCH_PARENT
                                dialogRightBtn.layoutParams = params


                                dialogContent.text = checkResponseBody //"이미 같은 시간에 예약이 되어있습니다. \n 그래도 예약하시겠습니까?"
                                //확인
                                dialogRightBtn.setOnClickListener {
                                    alertDialog.dismiss()
                                }
                                alertDialog.show()
                            }
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
                                finish()
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

            override fun onFailure(call: Call<CheckParticipateData>, t: Throwable) {
                Log.d("error", t.toString())
            }
        })
    }

    private fun setCommentData() {
        //val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(this@NoticeBoardReadActivity).getCommentData(temp!!.postID).enqueue(object : Callback<List<CommentResponseData>> {
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
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(this@NoticeBoardReadActivity).getCommentData(temp!!.postID).enqueue(object : Callback<List<CommentResponseData>> {
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

                                // 댓글 수정을 위한 모든 데이터 리스트
                                realCommentAllData.add(childCommentData)

                                childCommentsSize += 1
                            }
                            commentData.childComments = childComments

                            updatedCommentAllData.add(commentData)
                            realCommentAllData.add(commentData)
                        }

                        // 댓글 데이터를 갱신
                        commentAllData = updatedCommentAllData

                        // UI 업데이트
                        runOnUiThread {
                            val totalSize = commentAllData.size + childCommentsSize
                            nbrBinding.readCommentTotal.text = totalSize.toString()

                            if (commentAllData.isEmpty()) {
                                nbrBinding.commentRV.visibility = View.GONE
                                nbrBinding.notCommentTv.visibility = View.VISIBLE
                            } else {
                                nbrBinding.commentRV.visibility = View.VISIBLE
                                nbrBinding.notCommentTv.visibility = View.GONE
                            }

                            // 어댑터 갱신
                            (nbrBinding.commentRV.adapter as? NoticeBoardReadAdapter)?.let { adapter ->
                                adapter.commentItemData = commentAllData
                                adapter.notifyDataSetChanged()
                            } ?: run {
                                // 어댑터 초기화 및 리스너 설정
                                val commentAdapter = NoticeBoardReadAdapter()
                                commentAdapter.commentItemData = commentAllData
                                nbrBinding.commentRV.adapter = commentAdapter


                                commentAdapter.setItemClickListener(object : NoticeBoardReadAdapter.ItemClickListener {
                                    @RequiresApi(Build.VERSION_CODES.S)
                                    override fun onClick(view: View, position: Int, itemId: Int) {
                                        val vibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            val vbManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                            vbManager.defaultVibrator
                                        } else {
                                            @Suppress("DEPRECATION")
                                            getSystemService(VIBRATOR_SERVICE) as Vibrator
                                        }
                                        if (vibration.hasVibrator()) {
                                            vibration.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                        }
                                        Log.e("setItemClickListener", "clclclclclclcl")
                                        val parent = commentAllData.find { it?.commentId == itemId }?.user?.studentId
                                        val bottomSheet = BottomSheetCommentFragment(null, parent)
                                        //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                                        bottomSheet.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet.tag)
                                        bottomSheet.apply {
                                            setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                                                override fun sendValue(value: String) {
                                                    Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                                                    commentEditText = value
                                                    sharedViewModel!!.postReply(reply = true) //수정


                                                    //어떤 댓글을 선택했는지 확인
                                                    parentPosition = itemId

                                                    if (isReplyComment) { //대댓글
                                                        //서버에서 원하는 형식으로 날짜 설정
                                                        /*val now = System.currentTimeMillis()
                                                        val date = Date(now)
                                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                                                        val currentDate = sdf.format(date)
                                                        val formatter = DateTimeFormatter
                                                            .ofPattern("yyyy-MM-dd HH:mm:ss")
                                                            .withZone(ZoneId.systemDefault())
                                                        val result: Instant = Instant.from(formatter.parse(currentDate))*/
                                                        val nowInKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                                        // KST 시간을 UTC 시간대로 변환
                                                        val nowInUTC2 = nowInKST.withZoneSameInstant(ZoneId.of("UTC"))
                                                        // ISO 8601 형식으로 변환 (UTC 시간대, 'Z' 포함)
                                                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                                        val result = nowInKST.format(formatter)

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
                                                                    /*newRequest =
                                                                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                                                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                                                    startActivity(intent)
                                                                    finish()
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
                                                                        fetchAllComments()
                                                                        println("대댓글달기성공")
                                                                        //한 번 달고 끝내야하니 false전달
                                                                        sharedViewModel!!.postReply(reply = false)
                                                                        //sendAlarmData("댓글 ", commentEditText, temp)
                                                                        commentEditText = ""
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

                                                        //nbrBinding.readCommentTotal.text = (commentAllData.size + )

                                                    }
                                                }
                                            })
                                        }
                                    }

                                    override fun onLongClick(view: View, position: Int, itemId: Int) {
                                        val checkData = commentAllData.find { it?.commentId == itemId}
                                        if (email == checkData?.user?.email && checkData.content != "삭제된 댓글입니다.") {
                                            commentPosition = itemId
                                            val bottomSheet = ReadSettingBottomSheetFragment()
                                            bottomSheet.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet.tag)
                                            bottomSheet.apply {
                                                setCallback(object : ReadSettingBottomSheetFragment.OnSendFromBottomSheetDialog {
                                                    override fun sendValue(value: String) {
                                                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                                                        getBottomSheetCommentData = value
                                                        when (value) {
                                                            "수정" -> { //부모댓글 수정용
                                                                //자동으로 포커스 줘서 대댓글 달게 하기
                                                                val filterCommentText = realCommentAllData.find { it!!.commentId == commentPosition }
                                                                val bottomSheet2 = BottomSheetCommentFragment(filterCommentText, null)
                                                                //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                                                                bottomSheet2.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet2.tag)
                                                                bottomSheet2.apply {
                                                                    setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                                                                        override fun sendValue(value: String) {
                                                                            Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                                                                            commentEditText = value

                                                                            Log.e("readEditSendCommentgetBottomSheetCommentData" , getBottomSheetCommentData)
                                                                            val now = System.currentTimeMillis()
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
                                                                                        /*newRequest =
                                                                                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                                                                                        val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                                                                                        startActivity(intent)
                                                                                        finish()
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
                                                                                            // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                                                                                            val inputMethodManager = this@NoticeBoardReadActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                                                            // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                                                                                            if (inputMethodManager.isActive) {
                                                                                                // 가상 키보드가 올라가 있다면 내립니다.
                                                                                                inputMethodManager.hideSoftInputFromWindow(nbrBinding.readCommentTv.windowToken, 0)
                                                                                            }
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
                                                                        }
                                                                    })
                                                                }


                                                            }
                                                            "삭제" -> {
                                                                if (commentAllData.find { it?.commentId == itemId }?.content != "삭제된 댓글입니다.") {
                                                                    deleteCommentData(itemId)
                                                                    fetchAllComments()
                                                                }
                                                            }
                                                        }
                                                    }
                                                })
                                            }
                                        }
                                    }

                                    override fun onReplyClicked(
                                        status: String?,
                                        commentId: Int?,
                                        commentData: CommentData?
                                    ) {
                                        if (status == "수정") {
                                            getBottomSheetCommentData = "수정"
                                            val bottomSheet2 = BottomSheetCommentFragment(commentData, null)
                                            //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                                            bottomSheet2.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet2.tag)
                                            bottomSheet2.apply {
                                                setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                                                    override fun sendValue(value: String) {
                                                        Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                                                        commentEditText = value
                                                    }
                                                })
                                            }

                                            println(commentId) //이건 부모 댓글 id
                                            println(commentData?.content.toString()) //이건 수정할 대댓글의 정보
                                            Log.e("setCommentClickListener", "clclclclclc2lcl")
                                        } else {
                                            if (commentId != null) {
                                                deleteCommentData(commentId)
                                                fetchAllComments()
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    } else {
                        Log.e("ERROR COMMENT", response.errorBody()?.string()!!)
                    }
                }

                override fun onFailure(call: Call<List<CommentResponseData>>, t: Throwable) {
                    Log.d("ERROR COMMENT FAILURE", t.toString())
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
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    finish()
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
            api.deletePostData(temp!!.postID).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        println("scssucsucsucsdelte")
                        this@NoticeBoardReadActivity.finish()
                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
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
        noticeBoardReadAdapter!!.supportFragment = this@NoticeBoardReadActivity.supportFragmentManager
        noticeBoardReadAdapter!!.getWriter = temp?.user?.studentId!!
        nbrBinding.commentRV.adapter = noticeBoardReadAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nbrBinding.commentRV.setHasFixedSize(true)
        nbrBinding.commentRV.layoutManager = manager
    }



    private fun deleteCommentData(deleteCommentId : Int) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()

        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    finish()
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
                        fetchAllComments()
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

    /*private fun writerIdentification() {
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
                            response.body()?.mannerCount!!.toInt()
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
    }*/

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.notice_board_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }

   /* private fun sendAlarmData(status : String, content : String?, data : PostData?) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        //.client(clientBuilder)

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
                    *//*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    finish()
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
        ///////////////////////////////
        //userId 가 알람 받는 사람
        val temp = AddAlarmData("${status}${content}", data?.postID!!, data.user.id)

        //entity가 알람 받는 사람, user가 알람 전송한 사람
        CoroutineScope(Dispatchers.IO).launch {
            api.addAlarm(temp).enqueue(object : Callback<AddAlarmResponseData?> {
                override fun onResponse(
                    call: Call<AddAlarmResponseData?>,
                    response: Response<AddAlarmResponseData?>
                ) {
                    if (response.isSuccessful) {
                        println("succcc send alarm")
                    } else {
                        println("faafa alarm")
                        Log.d("alarm", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<AddAlarmResponseData?>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }*/

   /* private fun getManager() : NotificationManager {
        return getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }*/



    /*@RequiresApi(Build.VERSION_CODES.O)
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
    }*/


    private fun initAd() {
        adRequest = AdRequest.Builder().build()
        nbrBinding.readAd.loadAd(adRequest!!)
    }

    override fun onStart() {
        super.onStart()
        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.toString()
        Log.d("NoticeRead", email)
    }


    override fun onPause() {
        super.onPause()
        stopAdLoading()
    }

    private fun loadAdIfNeeded() {
        if (isScreenOn()) {
            initAd()
        }
    }

    private fun stopAdLoading() {
        adRequest = null
    }

    private fun isScreenOn(): Boolean {
        val powerManager = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
    }

    override fun onResume() {
        super.onResume()
        loadAdIfNeeded()
    }
}