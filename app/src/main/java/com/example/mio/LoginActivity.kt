package com.example.mio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mio.model.LoginResponsesData
import com.example.mio.model.TokenRequest
import com.example.mio.databinding.ActivityLoginBinding
import com.example.mio.model.AccountStatus
import com.example.mio.model.User
import com.example.mio.util.AESKeyStoreUtil
import com.example.mio.util.AppUpdateManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey


class LoginActivity : AppCompatActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val mBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private val clientWebKey = BuildConfig.client_web_id_key
    //받은 계정 정보
    private var userEmail = ""
    //
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    //로딩
    private var loadingDialog : LoadingProgressDialog? = null
    private lateinit var appUpdateLauncher: ActivityResultLauncher<IntentSenderRequest>
    //체크용
    private var isPolicyAllow : Boolean? = null
    private var isFirst : Boolean? = null

    private val secretKey: SecretKey by lazy {
        AESKeyStoreUtil.getOrCreateAESKey()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        //상태바 지우기(이 activity만)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        MobileAds.initialize(this@LoginActivity) {}

        setResultSignUp()
        saveSettingData()



        mBinding.googleSign.setOnClickListener {
            //로딩창 실행
            loadingDialog = LoadingProgressDialog(this@LoginActivity)
            loadingDialog?.setCancelable(false)
            //로딩창
            loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog?.window?.attributes?.windowAnimations = R.style.FullScreenDialog // 위에서 정의한 스타일을 적용
            loadingDialog?.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            loadingDialog?.show()
            //initPersonalInformationConsent()
            if (isPolicyAllow != true) {
                initPersonalInformationConsent()
            } else {
                signIn()
            }
        }

        // ActivityResultLauncher 초기화
        appUpdateLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            // 사용자가 업데이트를 취소하거나 오류가 발생한 경우 처리
            if (result.resultCode != RESULT_OK) {
                // 업데이트 실패 처리 (필요 시 다시 시도하거나 메시지 표시)
                Log.e("LoginActivity", "App Update failed or canceled")
            }
        }

        // 앱 업데이트 확인 및 유도
        checkForAppUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkForAppUpdate() {
        // 앱 업데이트 체크 및 유도 로직
        AppUpdateManager.init(this)  // AppUpdateManager 초기화
        AppUpdateManager.checkUpdate(this, this@LoginActivity)  // 업데이트 확인
    }

    private fun saveSettingData() { //처음 앱 사용 시 저장한 isPolicyAllow 없어서 null이니 true로 저장 후 dialog를 실행토록함
        //다음에는 true가 저장되어있었으니 false로 저장내용을 바꾸고 다시 저장하여 dialog가 나오지 않도록 함
        val sharedPref = this@LoginActivity.getSharedPreferences("privacyPolicySettingCheck", Context.MODE_PRIVATE)
        isFirst = sharedPref.getBoolean("isPolicyConnectFirstCheck", false)
        isPolicyAllow = sharedPref.getBoolean("isPolicyAllow", false)
        Log.e("isPolicyConnectFirstCheck", isFirst.toString())
    }

    private fun initPersonalInformationConsent() {
        val sharedPref = this.getSharedPreferences("privacyPolicySettingCheck", Context.MODE_PRIVATE)
        //isPolicyAllow = sharedPref.getBoolean("isPolicyAllow", false)
        val layoutInflater = LayoutInflater.from(this@LoginActivity)
        val dialogView = layoutInflater.inflate(R.layout.privacy_policy_dialog_layout, null)
        val alertDialog = android.app.AlertDialog.Builder(this@LoginActivity, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()
        val dialogContent = dialogView.findViewById<TextView>(R.id.message_text)
        val dialogLeftBtn = dialogView.findViewById<View>(R.id.dialog_left_btn)
        val dialogRightBtn =  dialogView.findViewById<View>(R.id.dialog_right_btn)

        dialogContent.setOnClickListener {
            val url = "https://sites.google.com/daejin.ac.kr/mio/%ED%99%88"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        dialogLeftBtn.setOnClickListener {
            if (loadingDialog != null && loadingDialog!!.isShowing) {
                loadingDialog?.dismiss()
                loadingDialog = null // 다이얼로그 인스턴스 참조 해제
            }
            Toast.makeText(this@LoginActivity, "서비스 이용이 제한됩니다.", Toast.LENGTH_SHORT).show()
            with(sharedPref.edit()) {
                putBoolean("isPolicyAllow", false)
                apply() // 비동기적으로 데이터를 저장
            }
            isPolicyAllow = false
            alertDialog.dismiss()
        }

        dialogRightBtn.setOnClickListener {
            with(sharedPref.edit()) {
                putBoolean("isPolicyAllow", true)
                apply() // 비동기적으로 데이터를 저장
            }
            isPolicyAllow = true

            signIn()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun isPolicyArrowRetrofitConnect() {
        RetrofitServerConnect.create(this@LoginActivity).postUserAcceptPolicy(AccountStatus("APPROVED")).enqueue(object : retrofit2.Callback<User> {
            override fun onResponse(call: Call<User>, response: retrofit2.Response<User>) {
                if (response.isSuccessful) {
                    isFirst = true

                    Toast.makeText(this@LoginActivity, "로그인이 완료되었습니다. ${response.code()}}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    this@LoginActivity.finish()
                } else {
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }
                    Toast.makeText(this@LoginActivity, "승인 확인 데이터 전송에 실패하였습니다. ${response.code()}}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                if (loadingDialog != null && loadingDialog!!.isShowing) {
                    loadingDialog?.dismiss()
                    loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                }
                Toast.makeText(this@LoginActivity, "예상치 못한 오류가 발생했습니다. ${t.message}}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun signInCheck(userInfoToken : TokenRequest) {
        val serverUrl = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service: MioInterface = retrofit.create(MioInterface::class.java)
        service.addUserInfoData(userInfoToken).enqueue(object : retrofit2.Callback<LoginResponsesData> {
            override fun onResponse(
                call: Call<LoginResponsesData>,
                response: retrofit2.Response<LoginResponsesData?>
            ) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        val accessToken = loginResponse.accessToken

                        val accessTokenExpiresIn = loginResponse.accessTokenExpiresIn

                        // AccessToken, ExpireDate, RefreshToken 저장
                        saveSharedPreferenceGoogleLogin.setToken(this@LoginActivity, accessToken, secretKey)
                        saveSharedPreferenceGoogleLogin.setExpireDate(this@LoginActivity, accessTokenExpiresIn.toString())
                        //saveSharedPreferenceGoogleLogin.setRefreshToken(this@LoginActivity, refreshToken)


                        //5초
                        val builder =  OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(2, TimeUnit.MINUTES)
                            .writeTimeout(2, TimeUnit.MINUTES)
                            .addInterceptor(HeaderInterceptor(response.body()!!.accessToken))
                        builder.build()

                        if (loadingDialog != null && loadingDialog!!.isShowing) {
                            loadingDialog?.dismiss()
                            loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                        }

                        if (isFirst != true) {
                            val sharedPref = this@LoginActivity.getSharedPreferences("privacyPolicySettingCheck", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putBoolean("isPolicyConnectFirstCheck", true)
                                apply() // 비동기적으로 데이터를 저장
                            }
                            isPolicyArrowRetrofitConnect()
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인이 완료되었습니다. ${response.code()}}", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            this@LoginActivity.finish()
                        }
                    }
                } else {
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }
                    Toast.makeText(this@LoginActivity, "로그인이 취소되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponsesData>, t: Throwable) {
                if (loadingDialog != null && loadingDialog!!.isShowing) {
                    loadingDialog?.dismiss()
                    loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                }
                Toast.makeText(this@LoginActivity, "로그인에 오류가 발생하였습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    class HeaderInterceptor constructor(private val token: String) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = "Bearer $token"
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            return chain.proceed(newRequest)
        }
    }

    private fun setResultSignUp() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(clientWebKey)
            .requestServerAuthCode(clientWebKey)
            .requestProfile()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val userEmailMap = task.result.email?.split("@")?.map { it }

                if (userEmailMap?.contains("daejin.ac.kr") == true || userEmailMap?.contains("anes53027") == true || userEmailMap?.contains("sonms5676") == true) {
                    handleSignInResult(task)
                } else {
                    loadingDialog?.dismiss()
                    Toast.makeText(this, "대진대학교 계정으로 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    mGoogleSignInClient.signOut().addOnCompleteListener {
                        signIn() // Prompt for sign-in again
                    }
                }
            } else {
                loadingDialog?.dismiss()
                Toast.makeText(this, "로그인에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    signIn() // Prompt for sign-in again
                }
            }
        }
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val idToken = account.idToken

            userEmail = email
            saveSharedPreferenceGoogleLogin.setUserEMAIL(this@LoginActivity, email)
            val userInfoToken = TokenRequest(idToken.toString())
            Log.e("userInfo", userInfoToken.token.toString())
            signInCheck(userInfoToken)
        } catch (e: ApiException) {
            loadingDialog?.dismiss()
            Toast.makeText(this, "로그인에 오류가 발생했습니다. ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn() {
        val signIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signIntent)
    }


    override fun onStop() {
        super.onStop()
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog?.dismiss()
            loadingDialog = null // 다이얼로그 인스턴스 참조 해제
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this@LoginActivity.finish()
    }
}