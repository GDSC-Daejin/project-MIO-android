package com.gdsc.mio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.gdsc.mio.model.PostData
import com.gdsc.mio.model.User
import com.gdsc.mio.databinding.ActivityCompleteBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.Content
import com.gdsc.mio.util.AESKeyStoreUtil
import com.gdsc.mio.util.AESUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.crypto.SecretKey

class CompleteActivity : AppCompatActivity() {
    private val cBinding by lazy {
        ActivityCompleteBinding.inflate(layoutInflater)
    }

    private var type = ""
    private var postCost : Int? = null
    private var driverData : User? = null
    private var postData : PostData? = null
    private var category : String? = null
    private var sharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var userAccount = ""

    private val secretKey: SecretKey by lazy {
        AESKeyStoreUtil.getOrCreateAESKey()
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cBinding.root)

        type = intent.getStringExtra("type") as String
        category = intent.getStringExtra("category") as String
        userAccount = sharedPreferenceGoogleLogin.getAccount(this@CompleteActivity, secretKey).toString()


        if (type == "PASSENGER") {
            postData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("postData")
            } else {
                intent.getParcelableExtra("postData", PostData::class.java)
            }
            driverData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("postDriver")
            } else {
                intent.getParcelableExtra("postDriver", User::class.java)
            }

            postCost = postData?.postCost


            var sb = ""
            if (driverData?.name == "(알 수 없음)") {
                sb = "(알 수 없음)"
                Toast.makeText(this@CompleteActivity, "탈퇴한 사용자입니다.", Toast.LENGTH_SHORT).show()
            } else {
                sb = driverData?.name?.let { it -> StringBuilder(it).also { it.setCharAt(1, '*') } }.toString()
            }
            var deText : List<String> = ArrayList()
            if (driverData?.accountNumber?.contains(" ") == true || driverData?.accountNumber.isNullOrEmpty() || driverData?.email == "(알 수 없음)") {
                if (driverData?.email == "(알 수 없음)") {
                    cBinding.completeDriverAccountNumber.text = "탈퇴한 사용자입니다."
                } else {
                    try {
                        deText = driverData?.accountNumber?.split(" ")?.map { it } ?: listOf("1","test")
                        if (deText == listOf("1", "test")) {
                            cBinding.completeDriverAccountNumber.text = getString(R.string.setCompleteActivityAcNum, sb)
                        } else {
                            cBinding.completeDriverAccountNumber.text = try {
                                "$sb \n${deText[1]} ${deText[0]}" //학번 , 계좌정보
                            } catch (e : NullPointerException) {
                                "$sb \n계좌정보를 등록하지 않은 운전자입니다.\n운전자에게 계좌정보를 확인하세요"
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // 복호화 실패 처리, 오류 메시지 출력 또는 기본 메시지 설정
                        cBinding.completeDriverAccountNumber.text = this@CompleteActivity.getString(R.string.AccountSettingText2)
                    }
                }
            } else {
                try {
                    deText = driverData?.accountNumber.toString().split(",").map { it }
                    val deAText = AESUtil.decryptAES(secretKey, deText[0], deText[1]).split(" ").map { it }
                    //val protectDeText = deText.let { it -> StringBuilder(it).also { it.setCharAt(, '*') } }
                    // 5번째 이후 문자열을 모두 '*'로 바꿈
                    val protectDeText = deAText[0].substring(0, 5) + "*".repeat(deAText[0].length - 5)

                    cBinding.completeDriverAccountNumber.text = try {
                        "$sb \n${protectDeText} ${deAText[0]}" //학번 , 계좌정보
                    } catch (e : NullPointerException) {
                        "$sb \n계좌정보를 등록하지 않은 운전자입니다.\n운전자에게 계좌정보를 확인하세요"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 복호화 실패 처리, 오류 메시지 출력 또는 기본 메시지 설정
                    cBinding.completeDriverAccountNumber.text = this@CompleteActivity.getString(R.string.AccountSettingText2)
                }
            }

            cBinding.completePassengerCost.text = getString(R.string.setCost, "$postCost")//"${postCost.toString()}원"

            cBinding.completeDriverAccountNumber.paintFlags = Paint.UNDERLINE_TEXT_FLAG  //밑줄긋기
            cBinding.completeDriverAccountNumber.setOnClickListener {
                //createClipData("${deText[0]} ${deText[1]}")
                Toast.makeText(this@CompleteActivity, "계좌이체 버튼을 통해 운전자의 계좌를 복사하여 은행앱으로 이동됩니다.", Toast.LENGTH_SHORT).show()
            }
            cBinding.completeCostLl.visibility = View.VISIBLE
            cBinding.completeBankLl.visibility = View.VISIBLE
            cBinding.completeEntireLl.visibility = View.VISIBLE
            cBinding.completeEnd2MessageTv.text = "아래 계좌에 본인 학번으로 입금해주세요!"
            cBinding.completeDivideView.visibility = View.VISIBLE
            cBinding.completeDivideView2.visibility = View.VISIBLE
            cBinding.completeDeadlineBtn.visibility = View.GONE

            cBinding.tossBankLl.setOnClickListener {
                if (deText.isNotEmpty()) {
                    createClipData("${deText[0]} ${deText[1]}")
                }
                if (postCost != null) {
                    deepLink("viva.republica.toss")
                }
            }

            cBinding.kakaoPayLl.setOnClickListener {
                if (deText.isNotEmpty()) {
                    createClipData("${deText[0]} ${deText[1]}")
                }
                if (postCost != null) {
                    deepLink("com.kakao.talk")
                }
            }

            cBinding.accountTransferLl.setOnClickListener {
                if (deText.isNotEmpty()) {
                    createClipData("${deText[0]} ${deText[1]}")
                }
                if (postCost != null) {
                    //패키지 이름으로 변경해야함
                    deepLink(userAccount)
                }
            }

        } else if (type == "DRIVER") {
            val layoutParams = cBinding.completeEntireLl.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = 180
            layoutParams.topMargin = newMarginTop
            cBinding.completeEntireLl.layoutParams = layoutParams

            postData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("postData")
            } else {
                intent.getParcelableExtra("postData", PostData::class.java)
            }
            initPostDeadLine()

            cBinding.completeEntireLl.visibility = View.VISIBLE
            cBinding.completeEnd2MessageTv.text = "입금여부를 확인하고 후기를 작성하세요"

            cBinding.completeCostLl.visibility = View.GONE
            cBinding.completeBankLl.visibility = View.GONE
            cBinding.completeDivideView.visibility = View.GONE
            cBinding.completeDivideView2.visibility = View.GONE
            cBinding.completeDeadlineBtn.visibility = View.VISIBLE
        }

        cBinding.closeScreen.setOnClickListener {
            val intent = Intent(this@CompleteActivity, MainActivity::class.java).apply {
                putExtra("flag", 1234)
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        // 뒤로가기 동작 핸들링
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@CompleteActivity, MainActivity::class.java).apply {
                    putExtra("flag", 1234)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        })
    }

    private fun initPostDeadLine() {
        RetrofitServerConnect.create(this@CompleteActivity).getPostIdDetailSearch(postData?.postID!!).enqueue(object :
            Callback<Content> {
            override fun onResponse(call: Call<Content>, response: Response<Content>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData != null) {
                        when (responseData.postType) {
                            "BEFORE_DEADLINE" -> {
                                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_blue_5)) //마감
                                cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                cBinding.completeDeadlineBtn.text = "마감하기"
                                postDeadLineClick(true)
                            }
                            "DEADLINE" -> {
                                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_blue_5)) //마감
                                cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                cBinding.completeDeadlineBtn.text = "운행 완료"
                                postComplete()
                            }
                            else -> { //completed
                                val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_gray_4)) //마감
                                cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                cBinding.completeDeadlineBtn.text = "운행 종료"
                                cBinding.completeDeadlineBtn.isClickable = false
                            }
                        }
                    }
                } else {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@CompleteActivity, "게시글 정보를 불러오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Content>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(this@CompleteActivity, "게시글 정보를 불러오는데 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun postDeadLineClick(deadLine : Boolean) {
        cBinding.completeDeadlineBtn.setOnClickListener {
            //deadLine
            if (deadLine) {
                RetrofitServerConnect.create(this@CompleteActivity).patchDeadLinePost(postData?.postID!!).enqueue(object : Callback<Content> {
                    override fun onResponse(call: Call<Content>, response: Response<Content>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            if (responseData != null) {
                                //BEFORE_DEADLINE, DEADLINE, COMPLETED
                                when (responseData.postType) {
                                    "DEADLINE" -> {
                                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_blue_5)) //마감
                                        cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                        cBinding.completeDeadlineBtn.text = "운행 완료"
                                        postComplete()
                                    }
                                    else -> { //completed
                                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_gray_4)) //마감
                                        cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                        cBinding.completeDeadlineBtn.text = "운행 종료"
                                        cBinding.completeDeadlineBtn.isClickable = false
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@CompleteActivity, "게시글 상태 변경에 실패했습니다. 다시 시도해주세요. ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Content>, t: Throwable) {
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(this@CompleteActivity, "게시글 상태 변경에 실패했습니다. 다시 시도해주세요. ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun postComplete() {
        cBinding.completeDeadlineBtn.setOnClickListener {
            RetrofitServerConnect.create(this@CompleteActivity).patchCompletePost(postData?.postID!!).enqueue(object : Callback<Content> {
                override fun onResponse(call: Call<Content>, response: Response<Content>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()
                        if (responseData != null) {
                            //BEFORE_DEADLINE, DEADLINE, COMPLETED
                            when (responseData.postType) {
                                "BEFORE_DEADLINE" -> {
                                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_blue_5)) //마감
                                    cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                    cBinding.completeDeadlineBtn.text = "마감하기"
                                }
                                "DEADLINE" -> {
                                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_blue_5)) //마감
                                    cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                    cBinding.completeDeadlineBtn.text = "운행 완료"
                                }
                                else -> { //completed
                                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@CompleteActivity , R.color.mio_gray_4)) //마감
                                    cBinding.completeDeadlineBtn.backgroundTintList = colorStateList
                                    cBinding.completeDeadlineBtn.text = "운행 종료"
                                    cBinding.completeDeadlineBtn.isClickable = false
                                }
                            }
                        }
                    } else {
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(this@CompleteActivity, "게시글 상태 변경에 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Content>, t: Throwable) {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@CompleteActivity, "게시글 상태 변경에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun createClipData(message : String) {
        val clipManager = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("message", message)

        clipManager.setPrimaryClip(clipData)

        Toast.makeText(this, "계좌번호가 복사되었어요", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun deepLink(packageName: String) {
        // 딥링크 URI를 정확히 작성해야 합니다.
        /*val uri = Uri.parse("miopay://deeplink?cost=$${cost}")

        val intent = Intent(Intent.ACTION_VIEW, uri)*/

        // 토스 앱의 패키지 이름을 알아야 합니다. -> packageName

        val intentPackageName = packageManager.getLaunchIntentForPackage(packageName)

        // 토스 앱이 설치되어 있는지 확인하고 실행합니다.
        if (isPackageInstalled(packageName)) {
            startActivity(intentPackageName)
        } else {
            when (packageName) {
                "viva.republica.toss" -> {
                    // 토스 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                    Toast.makeText(this, "토스은행 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                "com.kakao.talk" -> {
                    // 카톡 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                    Toast.makeText(this, "카카오톡 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                "com.kakaobank.channel" -> {
                    // 카톡 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                    Toast.makeText(this, "카카오뱅크가 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                "com.kbstar.kbbank" -> {
                    // 카톡 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                    Toast.makeText(this, "국민은행 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                "com.kebhana.hanapush" -> {
                    // 카톡 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                    Toast.makeText(this, "하나은행 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                "com.shinhan.sbanking" -> {
                    // 카톡 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                    Toast.makeText(this, "신한은행 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                "com.wooribank.smart.npib" -> {
                    Toast.makeText(this, "우리은행 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
                "nh.smart.banking" -> {
                    Toast.makeText(this, "농협은행 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /*@RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun kakaoDeepLink(cost : Int) {
        // 딥링크 URI를 정확히 작성해야 합니다.
        val uri = Uri.parse("miopay://deeplink?cost=$${cost}")

        val intent = Intent(Intent.ACTION_VIEW, uri)

        // 토스 앱의 패키지 이름을 알아야 합니다.
        val kakaoPackageName = "com.kakao.talk"

        // 토스 앱이 설치되어 있는지 확인하고 실행합니다.
        if (isPackageInstalled(kakaoPackageName)) {
            intent.setPackage(kakaoPackageName)
            startActivity(intent)
        } else {
            // 토스 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
            Toast.makeText(this, "카카오톡 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isKakaoPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }*/

   /* fun initViews() {
        WEB_VIEW.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return shouldOverrideUrlLoading(view, request)
            }
        }
    }

    fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        url.let {
            if (!URLUtil.isNetworkUrl(url) && !URLUtil.isJavaScriptUrl(url)) {
                // 딥링크로 URI 객체 만들기
                val uri = try {
                    Uri.parse(url)
                } catch (e: Exception) {
                    return false
                }

                return when (uri.scheme) {
                    "intent" -> {
                        startSchemeIntent(it) // Intent 스킴인 경우
                    }
                    else -> {
                        return try {
                            startActivity(Intent(Intent.ACTION_VIEW, uri)) // 다른 딥링크 스킴이면 실행
                            true
                        } catch (e: java.lang.Exception) {
                            false
                        }
                    }
                }
            } else {
                return false
            }
        }
    }


    *//*Intent 스킴을 처리하는 함수*//*
    fun startSchemeIntent(url: String): Boolean {
        val schemeIntent: Intent = try {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME) // Intent 스킴을 파싱
        } catch (e: URISyntaxException) {
            return false
        }
        try {
            startActivity(schemeIntent) // 앱으로 이동
            return true
        } catch (e: ActivityNotFoundException) { // 앱이 설치 안 되어 있는 경우
            val packageName = schemeIntent.getPackage()

            if (!packageName.isNullOrBlank()) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName") // 스토어로 이동
                    )
                )
                return true
            }
        }
        return false
    }*/
}