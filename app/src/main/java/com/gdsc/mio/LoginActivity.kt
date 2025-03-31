package com.gdsc.mio

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.gdsc.mio.databinding.ActivityLoginBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.AccountStatus
import com.gdsc.mio.model.LoginResponsesData
import com.gdsc.mio.model.TokenRequest
import com.gdsc.mio.model.User
import com.gdsc.mio.util.AESKeyStoreUtil
import com.gdsc.mio.util.AppUpdateManager
import com.gdsc.mio.util.DebuggingCheck
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
    private val clientWebKey = BuildConfig.client_web_release_id_key
    //받은 계정 정보
    private var userEmail = ""
    //
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private lateinit var appUpdateLauncher: ActivityResultLauncher<IntentSenderRequest>
    //체크용
    private var isPolicyAllow : Boolean? = null
    private var isFirst : Boolean? = null

    private val secretKey: SecretKey by lazy {
        AESKeyStoreUtil.getOrCreateAESKey()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        //상태바 지우기(이 activity만)
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        //MobileAds.initialize(this@LoginActivity) {}

        setResultSignUp()
        saveSettingData()

        try {
            (mBinding.googleSign.getChildAt(0) as TextView).setText(R.string.sign_in)
        } catch (e: ClassCastException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        /*mBinding.logoIv.setOnClickListener {
            AESKeyStoreUtil.deleteAESKeyFromKeystore()
        }*/

        mBinding.privacyPolicy.setOnClickListener {
            val url = "https://github.com/MIO-Privacy-Policy-for-Android"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        mBinding.googleSign.setOnClickListener {
            LoadingProgressDialogManager.show(this@LoginActivity)
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
            if (result.resultCode == RESULT_OK) {
                //업데이트 성공 후 앱 재시작
                restartApp()
            } else {
                // 업데이트 실패 처리
                Toast.makeText(this@LoginActivity, "앱의 업데이트가 취소되거나 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 앱 업데이트 확인 및 유도
        checkForAppUpdate()
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Runtime.getRuntime().exit(0)  // 현재 프로세스를 종료하고 앱 재시작
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
    }

    private fun initPersonalInformationConsent() {
        val sharedPref = this.getSharedPreferences("privacyPolicySettingCheck", Context.MODE_PRIVATE)

        val layoutInflater = LayoutInflater.from(this@LoginActivity)
        val dialogView = layoutInflater.inflate(R.layout.privacy_policy_dialog_layout, null)
        val alertDialog = android.app.AlertDialog.Builder(this@LoginActivity, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        val dialogContent = dialogView.findViewById<TextView>(R.id.message_text)
        val dialogLeftBtn = dialogView.findViewById<View>(R.id.dialog_left_btn)
        val dialogRightBtn = dialogView.findViewById<View>(R.id.dialog_right_btn)

        val window = alertDialog.window
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val x = (screenWidth * 0.8f).toInt()
        val y = (screenHeight * 0.4f).toInt()

        window?.setLayout(x, y) // 다이얼로그 크기 조절

        dialogContent.setOnClickListener {
            val url = "https://github.com/MIO-Privacy-Policy-for-Android"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        dialogLeftBtn.setOnClickListener {
            LoadingProgressDialogManager.hide()
            Toast.makeText(this@LoginActivity, "서비스 이용이 제한됩니다.", Toast.LENGTH_SHORT).show()
            with(sharedPref.edit()) {
                putBoolean("isPolicyAllow", false)
                apply()
            }
            isPolicyAllow = false
            alertDialog.dismiss()
        }

        dialogRightBtn.setOnClickListener {
            with(sharedPref.edit()) {
                putBoolean("isPolicyAllow", true)
                apply()
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

                    //Toast.makeText(this@LoginActivity, "로그인이 완료되었습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    this@LoginActivity.finish()
                } else {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@LoginActivity, "승인 확인 데이터 전송에 실패하였습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(this@LoginActivity, "예상치 못한 오류가 발생했습니다. ${t.message}}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun signInCheck(userInfoToken : TokenRequest) {
        LoadingProgressDialogManager.show(this@LoginActivity)
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

                        LoadingProgressDialogManager.hide()

                        if (isFirst != true) {
                            val sharedPref = this@LoginActivity.getSharedPreferences("privacyPolicySettingCheck", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putBoolean("isPolicyConnectFirstCheck", true)
                                apply() // 비동기적으로 데이터를 저장
                            }
                            isPolicyArrowRetrofitConnect()
                        } else {
                            //Toast.makeText(this@LoginActivity, "로그인이 완료되었습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            this@LoginActivity.finish()
                        }
                    }
                } else if (response.code() == 403) { //탈퇴사용자
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this@LoginActivity, "계정을 탈퇴한 후 재가입 및 로그인이 제한됩니다.", Toast.LENGTH_SHORT).show()
                } else {
                    LoadingProgressDialogManager.hide()
                    /*Log.e("signInCheck", response.errorBody()?.string()!!)
                    Log.e("signInCheck", response.code().toString())*/
                    Toast.makeText(this@LoginActivity, "로그인에 실패했습니다. 네트워크 및 계정 확인 후 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponsesData>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                val message = t.message
                Toast.makeText(this@LoginActivity, "로그인에 오류가 발생하였습니다. 다시 로그인해주세요. $message", Toast.LENGTH_SHORT).show()
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
                handleSignInResult(task)
                /*if (userEmailMap?.contains("daejin.ac.kr") == true || userEmailMap?.contains("anes53027") == true || userEmailMap?.contains("end90le51") == true || userEmailMap?.contains("sonms5676") == true) {
                    handleSignInResult(task)
                } else {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(this, "대진대학교 계정으로 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    mGoogleSignInClient.signOut().addOnCompleteListener {
                        signIn() // Prompt for sign-in again
                    }
                }*/
            } else {
                LoadingProgressDialogManager.hide()
                //Log.e("Login", result.resultCode.toString())
                Toast.makeText(this@LoginActivity, "로그인에 실패했습니다. 네트워크 및 계정 확인 후 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
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
            //Log.e("idToken", idToken.toString())
            signInCheck(userInfoToken)

        } catch (e: ApiException) {
            LoadingProgressDialogManager.hide()
            //Log.e("handleSignInResult", e.statusCode.toString())
            Toast.makeText(this, "로그인에 오류가 발생했습니다. ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn() {
        val signIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signIntent)
    }

   override fun onResume() {
        super.onResume()
        if (DebuggingCheck.isUsbDebuggingEnabled(this)) {
            Toast.makeText(this, "USB 디버깅이 감지되어 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        LoadingProgressDialogManager.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        this@LoginActivity.finish()
    }
}