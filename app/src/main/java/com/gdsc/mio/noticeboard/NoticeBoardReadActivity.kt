package com.gdsc.mio.noticeboard

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.gdsc.mio.*
import com.gdsc.mio.adapter.NoticeBoardReadAdapter
import com.gdsc.mio.bottomsheetfragment.BottomSheetCommentFragment
import com.gdsc.mio.bottomsheetfragment.ReadSettingBottomSheet2Fragment
import com.gdsc.mio.databinding.ActivityNoticeBoardReadBinding
import com.gdsc.mio.helper.*
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.*
import com.gdsc.mio.tabaccount.ProfileActivity
import com.gdsc.mio.tabcategory.MoreCarpoolTabActivity
import com.gdsc.mio.tabcategory.MoreTaxiTabActivity
import com.gdsc.mio.viewmodel.CommentsViewModel
import com.gdsc.mio.viewmodel.SharedViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.*
import retrofit2.*
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


class NoticeBoardReadActivity : AppCompatActivity() {
    private lateinit var nbrBinding : ActivityNoticeBoardReadBinding
    private var manager : LinearLayoutManager = LinearLayoutManager(this)
    //private var noticeBoardAdapter : NoticeBoardAdapter? = null
    private lateinit var noticeBoardReadAdapter : NoticeBoardReadAdapter
    //private var replyCommentAdapter : ReplyCommentAdapter? = null
    // private lateinit var nAdapter : NotificationAdapter

    //댓글 저장 전체 데이터
    //private var commentParentAllData = kotlin.collections.ArrayList<CommentData>()
    //대댓글 용
    //private var replyCommentAllData = kotlin.collections.ArrayList<CommentData>()

    private var commentEditText = ""
    //자식댓글
    //private var childCommentAllData = ArrayList<CommentData>()
    private var childCommentsSize = 0
    //모든 댓글
    //private var realCommentAllData = kotlin.collections.ArrayList<CommentData>()

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
    //private var isNotDeadLine2 : Boolean? = null //현재 시간이 타겟 시간 이전이면 true, 그렇지 않으면 false


    //유저확인
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var email = ""

    private var writerEmail = ""

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

    private lateinit var commentsViewModel : CommentsViewModel

    //private var sss = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nbrBinding = ActivityNoticeBoardReadBinding.inflate(layoutInflater)
        sharedPref = SharedPref(this)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        commentsViewModel = ViewModelProvider(this)[CommentsViewModel::class.java]

        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.toString()
        /////
        val type = intent.getStringExtra("type")
        tabType = intent.getStringExtra("tabType").toString()

        temp = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("postItem")
        } else {
            intent.getParcelableExtra("postItem", PostData::class.java)
        }

        sharedViewModel!!.fetchIsBeforeDeadLine(this, temp?.postID!!)

        lifecycleScope.launch {
            sharedViewModel!!.isNotDeadLine.collect { isNotDeadLine ->
                //게시글의 정해진 시간이 넘었는지
                val isBeforeTarget = isBeforeTarget(temp!!.postTargetDate, temp!!.postTargetTime)
                //responseData?.postType == "BEFORE_DEADLINE" && responseData.isDeleteYN != "Y" 인지
                //true면 마감이 아님
                val isDeadLineCheck3 = isBeforeTarget && isNotDeadLine

                updateUI(isDeadLineCheck3)
            }
        }

        commentsViewModelObserve()

        if (type.equals("READ")) {

            writerEmail = temp!!.user.email
            //tempProfile = intent.getSerializableExtra("uri") as String
            tempProfile = temp?.user?.profileImageUrl.toString()
            isCategory = temp!!.postCategory == "carpool"
            nbrBinding.readCategory.text = if (isCategory == true) {
                "카테고리: 카풀"
            } else {
                "카테고리: 택시"
            }
            //initParticipationCheck()
            val chipGroup = nbrBinding.readSetFilterCg
            if (temp?.user?.gender != null && temp?.user?.verifySmoker != null && temp?.postVerifyGoReturn != null) {
                chipList.add(createNewChip(text = if (temp?.user?.verifySmoker == true) {
                    "흡연 O"
                } else {
                    "흡연 X"
                }, chipGroup))
                chipList.add(createNewChip(text = if (temp?.user?.gender == true) {
                    "여성"
                } else {
                    "남성"
                }, chipGroup))
                chipList.add(createNewChip(text = if (temp?.postVerifyGoReturn == true) {
                    "등교"
                } else {
                    "하교"
                }, chipGroup))


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
                    .fitCenter()
                    .circleCrop()
                    .override(28,28)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            nbrBinding.readUserCheckIv.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
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


            val postDateTime = "${temp?.postCreateDate}".replace("T", " ").split(".")[0]

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
                    diffSeconds < 60 -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextSeconds, "$diffSeconds")//"${diffSeconds}초전"
                    diffMinutes < 60 -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextMinutes, "$diffMinutes")//"${diffMinutes}분전"
                    diffHours < 24 -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextHours, "$diffHours")//"${diffHours}시간전"
                    else -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextDays, "$diffDays")//"${diffDays}일전"
                }
            }
            //true면 before, false면 before아닌것들
            //isNotDeadLine = isBeforeTarget(temp?.postTargetDate.toString(), temp?.postTargetTime.toString()) && isNotDeadLine2 == true


            // 글쓴이가 자기자신 이라면 , 게시글 참가 + 글쓴이가 자기 자신이라면 받은 신청 보러가기고
        }

        //setCommentData()
        initCommentRecyclerView()
        initSwipeRefresh()

        nbrBinding.goLocation.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            intent.putExtra("latitude", temp!!.postlatitude)
            intent.putExtra("longitude", temp!!.postlongitude)
            startActivity(intent)
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
                                        layoutParams?.width = LayoutParams.MATCH_PARENT // 다이얼로그의 폭을 MATCH_PARENT로 설정
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
                                        deletePostData()

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
                                    RetrofitServerConnect.create(this@NoticeBoardReadActivity).addBookmark(postId = temp?.postID!!).enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d("noticeboardread", response.code().toString())
                                            } else {
                                                LoadingProgressDialogManager.hide()
                                                Toast.makeText(this@NoticeBoardReadActivity, "북마크 등록에 실패했습니다 ${response.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<Void>, t: Throwable) {
                                            LoadingProgressDialogManager.hide()
                                            Toast.makeText(this@NoticeBoardReadActivity, "연결에 실패했습니다 ${t.message}", Toast.LENGTH_SHORT).show()
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
                        commentEditText = value
                        // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                        val inputMethodManager = this@NoticeBoardReadActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                        if (inputMethodManager.isActive) {
                            // 가상 키보드가 올라가 있다면 내립니다.
                            inputMethodManager.hideSoftInputFromWindow(nbrBinding.readCommentTv.windowToken, 0)
                        }
                        if (isReplyComment) { //대댓글
                            commentsViewModel.setLoading(true)
                            val nowInKST = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                            // ISO 8601 형식으로 변환 (UTC 시간대, 'Z' 포함)
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                            val result = nowInKST.format(formatter)

                            //댓글 잠시 저장
                            val childCommentTemp = SendChildCommentData(commentEditText, result.toString() ,temp!!.postID, parentPosition)
                            //println("ct $commentTemp")
                            ///

                            //대댓글 추가하기
                            RetrofitServerConnect.create(this@NoticeBoardReadActivity).addChildCommentData(childCommentTemp, parentPosition).enqueue(object : Callback<CommentData> {
                                override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                                    if (response.isSuccessful) {
                                        commentsViewModel.setLoading(false)
                                        response.body()?.let {
                                            //commentsViewModel.addChildComment(parentPosition, it)
                                            val temp = CommentData(
                                                it.commentId,
                                                it.content,
                                                it.createDate,
                                                it.postId,
                                                it.user,
                                                it.childComments,
                                                isParent = false
                                            )
                                            commentsViewModel.addChildComment(temp, parentPosition)
                                            commentsViewModel.addComment(temp)
                                        }
                                        sharedViewModel!!.postReply(reply = false)
                                        //sendAlarmData ("댓글 ", commentEditText, temp)
                                        commentEditText = ""
                                    } else {
                                        commentsViewModel.setLoading(false)
                                        commentsViewModel.setError(response.errorBody()?.string()!!)
                                    }
                                }

                                override fun onFailure(call: Call<CommentData>, t: Throwable) {
                                    commentsViewModel.setLoading(false)
                                    commentsViewModel.setError(t.toString())
                                    Log.d("error", t.toString())
                                }
                            })

                            //nbrBinding.readCommentTotal.text = (commentAllData.size + )

                        } else {  //댓글
                            commentsViewModel.setLoading(true)

                            //댓글 잠시 저장
                            val commentTemp = SendCommentData(commentEditText)

                            RetrofitServerConnect.create(this@NoticeBoardReadActivity).addCommentData(commentTemp, temp!!.postID).enqueue(object : Callback<CommentData> {
                                override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                                    if (response.isSuccessful) {
                                        commentsViewModel.setLoading(false)
                                        response.body()?.let {
                                            val temp = CommentData(
                                                it.commentId,
                                                it.content,
                                                it.createDate,
                                                it.postId,
                                                it.user,
                                                it.childComments,
                                                isParent = true
                                            )
                                            commentsViewModel.addParentComment(temp)
                                            commentsViewModel.addComment(temp)
                                        }
                                        //sendAlarmData("댓글", commentEditText, temp)
                                        commentEditText = ""
                                        //nbrBinding.readCommentTotal.text = rea.size.toString()
                                    } else {
                                        println("faafa")
                                        commentsViewModel.setLoading(false)
                                        commentsViewModel.setError(response.errorBody()?.string()!!)
                                        Log.d("comment", response.errorBody()?.string()!!)
                                        Log.d("message", call.request().toString())
                                        println(response.code())
                                    }
                                }

                                override fun onFailure(call: Call<CommentData>, t: Throwable) {
                                    commentsViewModel.setLoading(false)
                                    commentsViewModel.setError(t.toString())
                                    Log.d("error", t.toString())
                                }
                            })
                        }
                    }
                })
            }
        }

        //대댓글 달기 또는 다른 기능 추가 예정
        noticeBoardReadAdapter.setItemClickListener(object : NoticeBoardReadAdapter.ItemClickListener {
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
                val parent = commentsViewModel.allComments.value?.find { it.commentId == itemId }?.user?.studentId
                val bottomSheet = BottomSheetCommentFragment(null, parent)
                //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                bottomSheet.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet.tag)
                bottomSheet.apply {
                    setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                        override fun sendValue(value: String) {
                            commentsViewModel.setLoading(true)
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

                                // ISO 8601 형식으로 변환 (UTC 시간대, 'Z' 포함)
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                val result = nowInKST.format(formatter)

                                //댓글 잠시 저장
                                val childCommentTemp = SendChildCommentData(commentEditText, result.toString() ,temp!!.postID, parentPosition)
                                //println("ct $commentTemp")
                                ///

                                //대댓글 추가하기
                                RetrofitServerConnect.create(this@NoticeBoardReadActivity).addChildCommentData(childCommentTemp, parentPosition).enqueue(object : Callback<CommentData> {
                                    override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                                        if (response.isSuccessful) {
                                            commentsViewModel.setLoading(false)
                                            response.body()?.let {
                                                val temp = CommentData(
                                                    it.commentId,
                                                    it.content,
                                                    it.createDate,
                                                    it.postId,
                                                    it.user,
                                                    it.childComments,
                                                    isParent = false
                                                )
                                                commentsViewModel.addComment(temp)
                                                commentsViewModel.addChildComment(temp, parentPosition)
                                            }
                                            println("대댓글달기성공")
                                            //한 번 달고 끝내야하니 false전달
                                            sharedViewModel!!.postReply(reply = false)
                                            //sendAlarmData("댓글 ", commentEditText, temp)
                                            commentEditText = ""
                                        } else {
                                            commentsViewModel.setLoading(false)
                                            commentsViewModel.setError(response.errorBody()?.string()!!)
                                        }
                                    }

                                    override fun onFailure(call: Call<CommentData>, t: Throwable) {
                                        commentsViewModel.setLoading(false)
                                        commentsViewModel.setError(t.toString())
                                        Log.d("error", t.toString())
                                    }
                                })
                            }
                        }
                    })
                }
            }
            //댓글 수정 및 삭제할 때
            override fun onLongClick(view: View, position: Int, itemId: Int) {
                val checkData = commentsViewModel.allComments.value?.find { it.commentId == itemId}
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
                                        val filterCommentText = commentsViewModel.allComments.value?.find { it.commentId == commentPosition }
                                        val bottomSheet2 = BottomSheetCommentFragment(filterCommentText, null)
                                        //bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
                                        bottomSheet2.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet2.tag)
                                        bottomSheet2.apply {
                                            setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                                                override fun sendValue(value: String) {
                                                    Log.d("test", "BottomSheetCommentFragment -> 액티비티로 전달된 값 : $value")
                                                    commentEditText = value
                                                    commentsViewModel.setLoading(true)
                                                    val editCommentTemp = SendCommentData(commentEditText)


                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        RetrofitServerConnect.create(this@NoticeBoardReadActivity).editCommentData(editCommentTemp, commentPosition).enqueue(object : Callback<CommentData> {
                                                            override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                                                                if (response.isSuccessful) {
                                                                    //fetchAllComments()
                                                                    // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                                                                    val inputMethodManager = this@NoticeBoardReadActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                                    // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                                                                    if (inputMethodManager.isActive) {
                                                                        // 가상 키보드가 올라가 있다면 내립니다.
                                                                        inputMethodManager.hideSoftInputFromWindow(nbrBinding.readCommentTv.windowToken, 0)
                                                                    }
                                                                    commentsViewModel.setLoading(false)
                                                                    response.body()?.let {
                                                                        val temp = CommentData(
                                                                            it.commentId,
                                                                            it.content,
                                                                            it.createDate,
                                                                            it.postId,
                                                                            it.user,
                                                                            it.childComments,
                                                                            isParent = true
                                                                        )
                                                                        commentsViewModel.updateComment(temp)
                                                                    }
                                                                    println("수정성공")
                                                                } else {
                                                                    println("faafa")
                                                                    commentsViewModel.setLoading(false)
                                                                    commentsViewModel.setError(response.errorBody()?.string()!!)
                                                                    Log.d("comment", response.errorBody()?.string()!!)
                                                                    Log.d("message", call.request().toString())
                                                                    println(response.code())
                                                                }
                                                            }

                                                            override fun onFailure(call: Call<CommentData>, t: Throwable) {
                                                                commentsViewModel.setLoading(false)
                                                                commentsViewModel.setError(t.toString())
                                                                Log.d("error", t.toString())
                                                            }
                                                        })
                                                    }
                                                }
                                            })
                                        }


                                    }

                                    "삭제" -> {
                                        if (commentsViewModel.allComments.value?.find { it.commentId == itemId }?.content != "삭제된 댓글입니다.") {
                                            deleteCommentData(itemId)
                                            //fetchAllComments()
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
                    bottomSheet2.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet2.tag)
                    bottomSheet2.apply {
                        setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                            override fun sendValue(value: String) {
                                commentEditText = value
                                commentsViewModel.setLoading(true)
                                val editCommentTemp = SendCommentData(commentEditText)
                                RetrofitServerConnect.create(this@NoticeBoardReadActivity).editCommentData(editCommentTemp, commentId!!).enqueue(object : Callback<CommentData> {
                                    override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                                        if (response.isSuccessful) {
                                            //fetchAllComments()
                                            // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                                            val inputMethodManager = this@NoticeBoardReadActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                            // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                                            if (inputMethodManager.isActive) {
                                                // 가상 키보드가 올라가 있다면 내립니다.
                                                inputMethodManager.hideSoftInputFromWindow(nbrBinding.readCommentTv.windowToken, 0)
                                            }
                                            commentsViewModel.setLoading(false)
                                            response.body()?.let {
                                                commentsViewModel.updateComment(it)
                                            }

                                        } else {

                                            commentsViewModel.setLoading(false)
                                            commentsViewModel.setError(response.errorBody()?.string()!!)

                                        }
                                    }

                                    override fun onFailure(call: Call<CommentData>, t: Throwable) {
                                        commentsViewModel.setLoading(false)
                                        commentsViewModel.setError(t.toString())
                                        Log.d("error", t.toString())
                                    }
                                })
                            }
                        })
                    }

                } else {
                    if (commentId != null) {
                        deleteCommentData(commentId)
                        //fetchAllComments()
                    }

                    /*if (commentsViewModel.allComments.value?.find { it.commentId == itemId }?.content != "삭제된 댓글입니다.") {
                                            deleteCommentData(itemId)
                                            //fetchAllComments()
                                        }*/
                }
            }
        })

        noticeBoardReadAdapter.setChildItemClickListener(object : NoticeBoardReadAdapter.ChildClickListener {
            //수정,삭제
            override fun onLongClick(view: View, position: Int, itemId: Int, comment: CommentData?) {
                //위에 요거는 itemId가 10인 commentItemData의 childComment를 찾고 아래가 부모댓글찾는거
                //즉 이거는 클릭한 대댓글의 모든 정보

                //any 함수는 하나라도 만족하는지 체크합니다.
                //이거는 클릭한 대댓글의 부모 댓글의 모든 정보

                /*val parentComment = parentComments.find { comment ->
                comment?.childComments?.any { it.commentId == itemId }!! } //부모 댓글 찾기*/

                if (saveSharedPreferenceGoogleLogin.getUserEMAIL(this@NoticeBoardReadActivity)!!
                        .split("@")
                        .first() == comment!!.user.studentId && comment.content != "삭제된 댓글입니다.") {
                    //수정용
                    val bottomSheet = ReadSettingBottomSheetFragment()
                    bottomSheet.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet.tag)
                    bottomSheet.apply {
                        setCallback(object : ReadSettingBottomSheetFragment.OnSendFromBottomSheetDialog{
                            override fun sendValue(value: String) {
                                getBottomSheetData = value
                                when(value) {
                                    "수정" -> {
                                        bottomSheet.dismiss()

                                        getBottomSheetCommentData = "수정"
                                        val bottomSheet2 = BottomSheetCommentFragment(comment, null)
                                        bottomSheet2.show(this@NoticeBoardReadActivity.supportFragmentManager, bottomSheet2.tag)
                                        bottomSheet2.apply {
                                            setCallback(object : BottomSheetCommentFragment.OnSendFromBottomSheetDialog{
                                                override fun sendValue(value: String) {

                                                    commentEditText = value
                                                    commentsViewModel.setLoading(true)

                                                    val editCommentTemp = SendCommentData(commentEditText)
                                                    RetrofitServerConnect.create(this@NoticeBoardReadActivity).editCommentData(editCommentTemp, itemId).enqueue(object : Callback<CommentData> {
                                                        override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                                                            if (response.isSuccessful) {
                                                                //fetchAllComments()
                                                                // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                                                                val inputMethodManager = this@NoticeBoardReadActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                                                // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                                                                if (inputMethodManager.isActive) {
                                                                    // 가상 키보드가 올라가 있다면 내립니다.
                                                                    inputMethodManager.hideSoftInputFromWindow(nbrBinding.readCommentTv.windowToken, 0)
                                                                }
                                                                commentsViewModel.setLoading(false)
                                                                response.body()?.let {
                                                                    commentsViewModel.updateComment(it)
                                                                }
                                                                println("대댓글수정성공")
                                                            } else {
                                                                println("faafa")
                                                                commentsViewModel.setLoading(false)
                                                                commentsViewModel.setError(response.errorBody()?.string()!!)
                                                                Log.d("comment", response.errorBody()?.string()!!)
                                                                Log.d("message", call.request().toString())
                                                                println(response.code())
                                                            }
                                                        }

                                                        override fun onFailure(call: Call<CommentData>, t: Throwable) {
                                                            commentsViewModel.setLoading(false)
                                                            commentsViewModel.setError(t.toString())
                                                            Log.d("error", t.toString())
                                                        }
                                                    })
                                                }
                                            })
                                        }
                                    }

                                    "삭제" -> {
                                        deleteCommentData(itemId)
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
        }

        nbrBinding.readUserId.setOnClickListener {
            saveSharedPreferenceGoogleLogin.setProfileUserId(this, temp!!.user.id)
            if (temp?.user?.name != "(알 수 없음)") {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("studentId", temp!!.user.id)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@NoticeBoardReadActivity, "탈퇴한 사용자입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        nbrBinding.readUserProfile.setOnClickListener {
            saveSharedPreferenceGoogleLogin.setProfileUserId(this, temp!!.user.id)
            if (temp?.user?.name != "(알 수 없음)") {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("studentId", temp!!.user.id)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@NoticeBoardReadActivity, "탈퇴한 사용자입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        nbrBinding.readBookmark.setOnClickListener {
            RetrofitServerConnect.create(this@NoticeBoardReadActivity).addBookmark(postId = temp?.postID!!).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        initMyBookmarkData()
                    } else {
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(this@NoticeBoardReadActivity, "북마크 등록에 실패하였습니다 ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@NoticeBoardReadActivity, "연결에 실패하였습니다 ${t.message}", Toast.LENGTH_SHORT).show()
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

    private fun commentsViewModelObserve() {
        // Observe LiveData from the ViewModel
        commentsViewModel.allComments.observe(this) { comments ->
            // Update UI with the list of comments
            updateUIWithComments(comments)
        }

        commentsViewModel.parentComments.observe(this) { parentComments ->
            noticeBoardReadAdapter.updateComments(parentComments)
        }

        commentsViewModel.childCommentsMap.observe(this) { childComments ->
            noticeBoardReadAdapter.updateChildComments(childComments)
        }

        commentsViewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                LoadingProgressDialogManager.show(this@NoticeBoardReadActivity)
            } else {
                LoadingProgressDialogManager.hide()
                nbrBinding.readSwipe.isRefreshing = false
                this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }

        commentsViewModel.error.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this@NoticeBoardReadActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        setCommentData()
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            RESULT_OK -> {
                val post = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra("postData")
                } else {
                    it.data?.getParcelableExtra("postData", PostData::class.java)
                }
                // = it.data?.getSerializableExtra("postData") as PostData?

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
                            }
                        }
                    }
                    //taxi
                    1 -> {
                        if (post != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                temp = post
                                val chipGroup = nbrBinding.readSetFilterCg
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
                                chipList.add(createNewChip(text = if (post.user.verifySmoker == true) {
                                    "흡연 O"
                                } else {
                                    "흡연 X"
                                }, chipGroup))
                                chipList.add(createNewChip(text = if (post.user.gender == true) {
                                    "여성"
                                } else {
                                    "남성"
                                }, chipGroup))
                                chipList.add(createNewChip(text = if (post.postVerifyGoReturn) {
                                    "등교"
                                } else {
                                    "하교"
                                }, chipGroup))


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
                        if (post != null) {
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
                        }
                    }

                    389 -> {
                        refreshNoticeBoardReadData()
                    }

                }
            }
        }
    }

    private fun refreshNoticeBoardReadData() {
        RetrofitServerConnect.create(this@NoticeBoardReadActivity).getPostIdDetailSearch(temp?.postID!!).enqueue(object : Callback<Content> {
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


                            if (it.postType == "BEFORE_DEADLINE") {
                                initParticipationCheck(true)
                            } else {
                                initParticipationCheck(false)
                            }

                            writerEmail = temp!!.user.email
                            //tempProfile = intent.getSerializableExtra("uri") as String
                            tempProfile = temp?.user?.profileImageUrl.toString()
                            isCategory = temp!!.postCategory == "carpool"
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


                            val postDateTime = "${temp?.postCreateDate}".replace("T", " ").split(".")[0]

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
                                    diffSeconds < 60 -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextSeconds, "$diffSeconds")//"${diffSeconds}초전"
                                    diffMinutes < 60 -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextMinutes, "$diffMinutes")//"${diffMinutes}분전"
                                    diffHours < 24 -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextHours, "$diffHours")//"${diffHours}시간전"
                                    else -> nbrBinding.readTimeCheck.text = getString(R.string.setTimeTextDays, "$diffDays")//"${diffDays}일전"
                                }
                            }
                        }
                    }
                } else {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@NoticeBoardReadActivity, "게시글 정보를 가져오는데 실패하였습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Content>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(this@NoticeBoardReadActivity, "연결에 실패하였습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initMyBookmarkData() {
        //println(userId)
        var thisData: List<BookMarkResponseData>?
        RetrofitServerConnect.create(this@NoticeBoardReadActivity).getBookmark().enqueue(object : Callback<List<BookMarkResponseData>> {
            override fun onResponse(call: Call<List<BookMarkResponseData>>, response: Response<List<BookMarkResponseData>>) {
                if (response.isSuccessful) {

                    val responseData = response.body()
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
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@NoticeBoardReadActivity, "북마크 정보를 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BookMarkResponseData>>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(this@NoticeBoardReadActivity, "북마크 정보를 가져오는데 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initParticipationCheck(isDeadLineCheck2: Boolean) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val userId = saveSharedPreferenceGoogleLogin.getUserId(this)

        RetrofitServerConnect.create(this@NoticeBoardReadActivity).getParticipationData(postId = temp?.postID!!).enqueue(object : Callback<List<ParticipationData>> {
            override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                if (response.isSuccessful) {
                    if (response.body()?.find { it.userId == userId} != null){
                        isParticipation = true
                        participantApplyBtnSet(isParticipation!!, isDeadLineCheck2)
                    } else {
                        isParticipation = false

                        //participantApplyBtnSet(isParticipation!!)
                        participantApplyBtnSet(isParticipation!!, isDeadLineCheck2)
                    }
                } else {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@NoticeBoardReadActivity, "참여 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(this@NoticeBoardReadActivity, "참여 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(isDeadLineCheck: Boolean) {
        // Implement your UI update logic based on isDeadLineCheck2
        isDeadLineCheck2 = isDeadLineCheck
        if (isDeadLineCheck) {
            nbrBinding.readSetting.visibility = View.VISIBLE
            initParticipationCheck(true)
        } else {
            nbrBinding.readSetting.visibility = View.GONE
            initParticipationCheck(false)
        }
    }

    private fun participantApplyBtnSet(isParticipation : Boolean, isDeadLineCheck2: Boolean) {
        //작성자로 참여되어있을 때
        if (email == temp!!.user.email) {
            val typeface = resources.getFont(R.font.pretendard_medium)
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
                    putExtra("targetDate", "${temp!!.postTargetDate} ${temp!!.postTargetTime}")
                }
                requestActivity.launch(intent)
            }
        } else if (!isParticipation && email != temp!!.user.email) { //게시글에 참여하지도 않았고 글쓴이도 아니라면? 신청하기
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
                    setApplyNoticeBoard()
                }
            } else {
                nbrBinding.readSetting.visibility = View.GONE
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
            val typeface = resources.getFont(R.font.pretendard_medium)
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

            if (isDeadLineCheck2) {
                nbrBinding.readApplyBtn.setOnClickListener {
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
                        layoutParams?.width = LayoutParams.MATCH_PARENT // 다이얼로그의 폭을 MATCH_PARENT로 설정
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
                        /////////
                        RetrofitServerConnect.create(this).deleteParticipate(postId = temp!!.postID).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d("check", response.code().toString())
                                    Toast.makeText(this@NoticeBoardReadActivity, "성공적으로 취소되었습니다", Toast.LENGTH_SHORT).show()
                                    nbrBinding.readNumberOfPassengers.text = if (temp?.postParticipation == 0 || temp?.postParticipation!!.toInt() - 1 <= 0) {
                                        "0"
                                    } else {
                                        (temp?.postParticipation!!.toInt() - 1).toString()
                                    }
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
                                        Toast.makeText(this@NoticeBoardReadActivity, "참여 취소에 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(this@NoticeBoardReadActivity, "참여 취소에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                        alertDialog.dismiss()
                        refreshNoticeBoardReadData()
                    }
                    alertDialog.show()
                }
            } else {
                nbrBinding.readSetting.visibility = View.GONE
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

    private fun isBeforeTarget(targetDate: String, targetTime: String): Boolean {
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
        RetrofitServerConnect.create(this@NoticeBoardReadActivity).checkParticipate(postId = temp!!.postID).enqueue(object : Callback<CheckParticipateData> {
            override fun onResponse(call: Call<CheckParticipateData>, response: Response<CheckParticipateData>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    //true가 예약된 게 없음
                    if (responseData?.check == true) {
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
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@NoticeBoardReadActivity, "연결에 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CheckParticipateData>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(this@NoticeBoardReadActivity, "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setCommentData() {
        commentsViewModel.setLoading(true)
        val allCommentsList = mutableListOf<CommentData>()
        val parentCommentsList = mutableListOf<CommentData>()
        val childCommentsMap = mutableMapOf<Int, MutableList<CommentData>>()
        //val call = RetrofitServerConnect.service
        RetrofitServerConnect.create(this@NoticeBoardReadActivity).getCommentData(temp!!.postID).enqueue(object : Callback<List<CommentResponseData>> {
            override fun onResponse(call: Call<List<CommentResponseData>>, response: Response<List<CommentResponseData>>) {
                if (response.isSuccessful) {
                    println("scssucsucsucs")
                    /*realCommentAllData.clear()
                    childCommentAllData.clear()*/
                    childCommentsSize = 0

                    response.body()?.let { commentsList ->
                        for (commentResponse in commentsList) {
                            // Create parent comment data
                            val parentComment = CommentData(
                                commentId = commentResponse.commentId,
                                content = commentResponse.content,
                                createDate = commentResponse.createDate,
                                postId = commentResponse.postId,
                                user = commentResponse.user,
                                childComments = commentResponse.childComments, // Initialize with empty, we'll fill it later
                                isParent = true
                            )

                            // Add to all comments and parent comments lists
                            allCommentsList.add(parentComment)
                            parentCommentsList.add(parentComment)

                            // Process child comments
                            val childComments = ArrayList<CommentData>()
                            commentResponse.childComments?.forEach { childCommentResponse ->
                                val childCommentData = CommentData(
                                    childCommentResponse.commentId,
                                    childCommentResponse.content,
                                    childCommentResponse.createDate,
                                    childCommentResponse.postId,
                                    childCommentResponse.user,
                                    childCommentResponse.childComments,
                                    isParent = false
                                )
                                /*childCommentAllData.add(childCommentData)
                                                        realCommentAllData.add(childCommentData)*/
                                childCommentsSize++
                                childComments.add(childCommentData)
                                allCommentsList.add(childCommentData)
                            }

                            // Update the child comments map if there are child comments
                            if (childComments.isNotEmpty()) {
                                childCommentsMap[parentComment.commentId] = childComments
                            }
                        }
                    }
                    commentsViewModel.setLoading(false)
                    commentsViewModel.setAllComments(allCommentsList.toList())
                    commentsViewModel.setChildComments(childCommentsMap)
                    commentsViewModel.setParentComments(parentCommentsList.toList())

                    /*for (i in response.body()!!.indices) {
                        val commentResponse = response.body()!![i]
                        *//* commentAllData.add(
                             CommentData(
                                 response.body()!![i].commentId,
                                 response.body()!![i].content,
                                 response.body()!![i].createDate,
                                 response.body()!![i].postId,
                                 response.body()!![i].user,
                                 response.body()!![i].childComments
                             ))
                         noticeBoardReadAdapter!!.notifyDataSetChanged()*//*

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
                    }*/
                } else {
                    commentsViewModel.setLoading(false)
                    commentsViewModel.setError(response.errorBody()?.string()!!)
                    println("faafa")
                    Log.d("comment", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    println(response.code())
                }

                /*val totalSize = commentAllData.size + childCommentsSize
                nbrBinding.readCommentTotal.text = totalSize.toString()
                println("total" + totalSize +  childCommentsSize)


                if (commentAllData.size == 0) {
                    nbrBinding.commentRV.visibility = View.GONE
                    nbrBinding.notCommentTv.visibility = View.VISIBLE
                } else {
                    nbrBinding.commentRV.visibility = View.VISIBLE
                    nbrBinding.notCommentTv.visibility = View.GONE
                }*/
            }

            override fun onFailure(call: Call<List<CommentResponseData>>, t: Throwable) {
                commentsViewModel.setLoading(false)
                commentsViewModel.setError(t.toString())
            }
        })
    }

    private fun updateUIWithComments(comments: List<CommentData>) {
        if (comments.isEmpty()) {
            nbrBinding.commentRV.visibility = View.GONE
            nbrBinding.notCommentTv.visibility = View.VISIBLE
        } else {
            nbrBinding.commentRV.visibility = View.VISIBLE
            nbrBinding.notCommentTv.visibility = View.GONE
            // Update your RecyclerView adapter with the comments data
            // noticeBoardReadAdapter.updateData(comments)
        }

        // Update total size
        val totalSize = comments.size
        nbrBinding.readCommentTotal.text = totalSize.toString()
    }

    // 서버에서 모든 댓글 정보를 가져오고 UI를 업데이트하는 함수
    /* private fun fetchAllComments() {
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
                                                     *//*val now = System.currentTimeMillis()
                                                    val date = Date(now)
                                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                                                    val currentDate = sdf.format(date)
                                                    val formatter = DateTimeFormatter
                                                        .ofPattern("yyyy-MM-dd HH:mm:ss")
                                                        .withZone(ZoneId.systemDefault())
                                                    val result: Instant = Instant.from(formatter.parse(currentDate))*//*
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
                                                                *//*newRequest =
                                                                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                                                                Log.e("read", "read9")
                                                                val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                                Toast.makeText(this@NoticeBoardReadActivity, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
                                                                                    *//*newRequest =
                                                                                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                                                                                    Log.e("read", "read10")
                                                                                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)

                                                                                    Toast.makeText(this@NoticeBoardReadActivity, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
    }*/


    private fun deletePostData() {
        RetrofitServerConnect.create(this).deletePostData(temp!!.postID).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    println("scssucsucsucsdelte")
                    if (isCategory == true) { //카풀
                        val intent = Intent(this@NoticeBoardReadActivity, MoreCarpoolTabActivity::class.java).apply {
                            putExtra("flag", 2)
                            putExtra("type", "DATE")
                            putExtra("date", LocalDate.now().monthValue.toString())
                        }
                        setResult(RESULT_OK, intent)
                        this@NoticeBoardReadActivity.finish()
                    } else { //택시
                        val intent = Intent(this@NoticeBoardReadActivity, MoreTaxiTabActivity::class.java).apply {
                            putExtra("flag", 2)
                            putExtra("type", "DATE")
                            putExtra("date", LocalDate.now().monthValue.toString())
                        }
                        setResult(RESULT_OK, intent)
                        this@NoticeBoardReadActivity.finish()
                    }

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

    private fun initCommentRecyclerView() {
        //setCommentData()
        noticeBoardReadAdapter = NoticeBoardReadAdapter()
        //noticeBoardReadAdapter!!.commentItemData = commentAllData
        //noticeBoardReadAdapter!!.replyCommentItemData = replyCommentAllData
        noticeBoardReadAdapter.supportFragment = this@NoticeBoardReadActivity.supportFragmentManager
        noticeBoardReadAdapter.getWriter = temp?.user?.studentId!!.toString()
        nbrBinding.commentRV.adapter = noticeBoardReadAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nbrBinding.commentRV.setHasFixedSize(true)
        nbrBinding.commentRV.layoutManager = manager
    }

    private fun initSwipeRefresh() {
        nbrBinding.readSwipe.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            this.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            commentsViewModel.setLoading(true)
            // 데이터 새로 고침
            refreshData()

            /* // 새로 고침 완료 및 터치 가능하게 설정
             mttBinding.moreRefreshSwipeLayout.isRefreshing = false
             this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/
        }
    }

    private fun refreshData() {
        //commentsViewModel.setLoading(false)
        refreshNoticeBoardReadData()
        initMyBookmarkData()
        setCommentData()
    }



    private fun deleteCommentData(deleteCommentId : Int) {
        commentsViewModel.setLoading(true)
        /*val interceptor = Interceptor { chain ->
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
                    Log.e("read", "read12")
                    val intent = Intent(this@NoticeBoardReadActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    Toast.makeText(this@NoticeBoardReadActivity, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
        val api = retrofit2.create(MioInterface::class.java)*/
        ///////////////////////////////////////////////////////
        RetrofitServerConnect.create(this@NoticeBoardReadActivity).deleteCommentData(deleteCommentId).enqueue(object : Callback<CommentData> {
            override fun onResponse(call: Call<CommentData>, response: Response<CommentData>) {
                if (response.isSuccessful) {
                    println("ssssssss")
                    println(response.code())
                    commentsViewModel.setLoading(false)
                    response.body()?.let {
                        commentsViewModel.removeComment(deleteCommentId)
                    }
                } else {
                    commentsViewModel.setLoading(false)
                    commentsViewModel.setError(response.errorBody()?.string()!!)
                }
            }

            override fun onFailure(call: Call<CommentData>, t: Throwable) {
                commentsViewModel.setLoading(false)
                commentsViewModel.setError(t.toString())
            }
        })
    }

    private fun createNewChip(text: String, parent : ViewGroup): Chip {
        val chip = layoutInflater.inflate(R.layout.notice_board_chip_layout, parent, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        return chip
    }

    override fun onStart() {
        super.onStart()
        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.toString()
    }
}