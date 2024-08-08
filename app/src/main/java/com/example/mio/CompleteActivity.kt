package com.example.mio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.example.mio.Model.PostData
import com.example.mio.Model.User
import com.example.mio.TabCategory.CarpoolTabFragment
import com.example.mio.TabCategory.TaxiTabFragment
import com.example.mio.databinding.ActivityCompleteBinding

class CompleteActivity : AppCompatActivity() {
    private val cBinding by lazy {
        ActivityCompleteBinding.inflate(layoutInflater)
    }

    private var type = ""
    private var postCost : Int? = null
    private var driverData : User? = null
    private var postData : PostData? = null
    private var category : String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cBinding.root)

        type = intent.getStringExtra("type") as String
        category = intent.getStringExtra("category") as String
        if (type == "PASSENGER") {
            postData = intent.getSerializableExtra("postData") as PostData
            driverData = intent.getSerializableExtra("postDriver") as User
            postCost = postData?.postCost

            var driverName = driverData?.name
            val sb = driverName?.let { it -> StringBuilder(it).also { it.setCharAt(1, '*') } }
            driverName = sb.toString()

            cBinding.completeDriverAccountNumber.text = try {
                "${driverData?.accountNumber} $driverName"
            } catch (e : NullPointerException) {
                "null ${driverData?.name}"
            }

            cBinding.completePassengerCost.text = postCost.toString()

            cBinding.completeDriverAccountNumber.paintFlags = Paint.UNDERLINE_TEXT_FLAG  //밑줄긋기
            cBinding.completeDriverAccountNumber.setOnClickListener {
                createClipData(cBinding.completeDriverAccountNumber.text as String)
            }
            cBinding.completeCostLl.visibility = View.VISIBLE
            cBinding.completeBankLl.visibility = View.VISIBLE
            cBinding.completeEntireLl.visibility = View.VISIBLE
            cBinding.completeEnd2MessageTv.text = "아래 계좌에 본인 학번으로 입금해주세요!"
            cBinding.completeDivideView.visibility = View.VISIBLE
            cBinding.completeDivideView2.visibility = View.VISIBLE
            cBinding.tossBankLl.setOnClickListener {
                createClipData(cBinding.completeDriverAccountNumber.text as String)
                if (postCost != null) {
                    deepLink("viva.republica.toss", postCost!!)
                }

            }

            cBinding.kakaoPayLl.setOnClickListener {
                createClipData(cBinding.completeDriverAccountNumber.text as String)
                if (postCost != null) {
                    deepLink("com.kakao.talk", postCost!!)
                }
            }

            cBinding.accountTransferLl.setOnClickListener {
                createClipData(cBinding.completeDriverAccountNumber.text as String)
            }
        } else if (type == "DRIVER") {
            val layoutParams = cBinding.completeEntireLl.layoutParams as ViewGroup.MarginLayoutParams
            val newMarginTop = 180
            layoutParams.topMargin = newMarginTop
            cBinding.completeEntireLl.layoutParams = layoutParams

            postData = intent.getSerializableExtra("postData") as PostData

            cBinding.completeEntireLl.visibility = View.VISIBLE
            cBinding.completeEnd2MessageTv.text = "입금여부를 확인하고 후기를 작성하세요"

            cBinding.completeCostLl.visibility = View.GONE
            cBinding.completeBankLl.visibility = View.GONE
            cBinding.completeDivideView.visibility = View.GONE
            cBinding.completeDivideView2.visibility = View.GONE
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


    private fun createClipData(message : String) {
        val clipManager = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("message", message)

        clipManager.setPrimaryClip(clipData)

        Toast.makeText(this, "계좌번호가 복사되었어요", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun deepLink(packageName: String, cost : Int) {
        // 딥링크 URI를 정확히 작성해야 합니다.
        /*val uri = Uri.parse("miopay://deeplink?cost=$${cost}")

        val intent = Intent(Intent.ACTION_VIEW, uri)*/

        // 토스 앱의 패키지 이름을 알아야 합니다. -> packageName

        val intentPackageName = packageManager.getLaunchIntentForPackage(packageName)

        // 토스 앱이 설치되어 있는지 확인하고 실행합니다.
        if (isPackageInstalled(packageName)) {
            startActivity(intentPackageName)
        } else {
            if (packageName == "viva.republica.toss") {
                // 토스 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                Toast.makeText(this, "토스 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 카톡 앱이 설치되어 있지 않은 경우 처리할 내용을 여기에 추가합니다.
                Toast.makeText(this, "카카오톡 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
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