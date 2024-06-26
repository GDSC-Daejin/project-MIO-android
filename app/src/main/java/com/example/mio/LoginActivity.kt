package com.example.mio

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mio.Model.LoginResponsesData
import com.example.mio.Model.LoginGoogleResponse
import com.example.mio.Model.RefreshTokenRequest
import com.example.mio.Model.TokenRequest
import com.example.mio.databinding.ActivityLoginBinding
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.example.mio.R
import java.security.MessageDigest


class LoginActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val mBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private val CLIENT_WEB_ID_KEY = BuildConfig.client_web_id_key
    //private val CLIENT_AND_ID_KEY = BuildConfig.client_id_key
    private val SERVER_URL = BuildConfig.server_URL
    private var user_info : ArrayList<LoginGoogleResponse> = ArrayList<LoginGoogleResponse>()
    private lateinit var currentUser : LoginGoogleResponse
    //받은 계정 정보
    private var userEmail = ""
    //데이터 받아오기 준비
    private var isReady = false
    //
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    //로딩
    private var loadingDialog : LoadingProgressDialog? = null


    //init 벡엔드 연결
    /*val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(MioInterface::class.java)
    val interceptor = HttpLoggingInterceptor()*/
    /* interceptor = interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
     val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
 */

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        //상태바 지우기(이 activity만)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        //상태바 지우기 테스트
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }*/
        initSplashScreen()
        super.onCreate(savedInstanceState)

        setContentView(mBinding.root)
        MobileAds.initialize(this@LoginActivity) {}
        setResultSignUp()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(CLIENT_WEB_ID_KEY)
            .requestServerAuthCode(CLIENT_WEB_ID_KEY)
            .requestProfile()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mBinding.signInLl.setOnClickListener {
            //로딩창 실행
            loadingDialog = LoadingProgressDialog(this@LoginActivity)
            loadingDialog?.setCancelable(false)
            //loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            //로딩창
            loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog?.window?.attributes?.windowAnimations = R.style.FullScreenDialog // 위에서 정의한 스타일을 적용
            loadingDialog?.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            loadingDialog?.show()
            signIn()
        }

    }

    private fun initData() {
        // 별도의 데이터 처리가 없기 때문에 3초의 딜레이를 줌.
        // 선행되어야 하는 작업이 있는 경우, 이곳에서 처리 후 isReady를 변경.
        CoroutineScope(Dispatchers.IO).launch {
            delay(3000)
        }
        isReady = true
    }
    private fun initSplashScreen() {
        initData()
        val splashScreen = installSplashScreen()
        val content: View = findViewById(android.R.id.content)
        // SplashScreen이 생성되고 그려질 때 계속해서 호출된다.
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isReady) {
                        // 3초 후 Splash Screen 제거
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready
                        false
                    }
                }
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.iconView?.let { iconView ->
                    val animScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 8f)
                    val animScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 8f)
                    val animAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)

                    ObjectAnimator.ofPropertyValuesHolder(
                        iconView,
                        animAlpha,
                        animScaleX,
                        animScaleY
                    ).apply {
                        interpolator = AnticipateInterpolator()
                        duration = 300L
                        doOnEnd { splashScreenView.remove() }
                        start()
                    }
                }
            }
        }
    }

    private fun signInCheck(userInfoToken : TokenRequest) {
        println("signInCheck")
        val call = RetrofitServerConnect.service

        call.addUserInfoData(userInfoToken).enqueue(object : retrofit2.Callback<LoginResponsesData> {
            override fun onResponse(
                call: retrofit2.Call<LoginResponsesData>,
                response: retrofit2.Response<LoginResponsesData?>
            ) {
                if (response.isSuccessful) {
                    Log.d("Login success", "success")
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Log.d("Login success", "is not null")
                        //Log.d("Login success", )
                        val accessToken = loginResponse.accessToken
                        val accessTokenExpiresIn = loginResponse.accessTokenExpiresIn
                        val refreshToken = loginResponse.refreshToken

                        // AccessToken, ExpireDate, RefreshToken 저장
                        saveSharedPreferenceGoogleLogin.setToken(this@LoginActivity, accessToken)
                        saveSharedPreferenceGoogleLogin.setExpireDate(this@LoginActivity, accessTokenExpiresIn.toString())
                        saveSharedPreferenceGoogleLogin.setRefreshToken(this@LoginActivity, refreshToken)


                        //5초
                        val builder =  OkHttpClient.Builder()
                            .connectTimeout(3, TimeUnit.SECONDS)
                            .readTimeout(3, TimeUnit.SECONDS)
                            .writeTimeout(3, TimeUnit.SECONDS)
                            .addInterceptor(HeaderInterceptor(response.body()!!.accessToken))
                        /*intent.apply {
                            putExtra("accessToken", saveSharedPreferenceGoogleLogin.setToken(this@LoginActivity, response.body()!!.accessToken).toString())
                            putExtra("expireDate", saveSharedPreferenceGoogleLogin.setExpireDate(this@LoginActivity, response.body()!!.accessTokenExpiresIn.toString()).toString())
                        }*/
                        builder.build()

                        Log.d("Login accessTokenExpiresIn check", accessTokenExpiresIn.toString())
                        Log.d("Login RefreshToken check", refreshToken)
                        // AccessToken 만료 시간 확인
                        if (accessTokenExpiresIn <= System.currentTimeMillis()) {
                            Log.d("Login accessTokenExpiresIn", accessTokenExpiresIn.toString())
                            // AccessToken 만료된 경우 refreshToken을 사용하여 갱신
                            Log.d("Login RefreshToken", refreshToken)
                            refreshAccessToken(refreshToken)
                        } else {
                            // AccessToken 유효한 경우 MainActivity로 이동
                            Log.d("Login accessTokenExpiresIn 유효", accessTokenExpiresIn.toString())
                            Log.d("Login RefreshToken 유효", refreshToken)
                            loadingDialog?.dismiss()
                            if (loadingDialog != null && loadingDialog!!.isShowing) {
                                loadingDialog?.dismiss()
                                loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                            }

                            Toast.makeText(this@LoginActivity, "로그인이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            this@LoginActivity.finish()
                        }
                    }
                } else {
                    println("fail")
                    Log.e("LoginTestResponseError", response.errorBody()?.string()!!)
                    Log.e("LoginTestResponseError", response.code().toString())
                    Log.e("LoginTestResponseError", response.message().toString())

                    loadingDialog?.dismiss()
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }
                    Toast.makeText(this@LoginActivity, "로그인이 취소되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<LoginResponsesData>, t: Throwable) {
                println("실패" + t.message.toString())
                loadingDialog?.dismiss()
                if (loadingDialog != null && loadingDialog!!.isShowing) {
                    loadingDialog?.dismiss()
                    loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                }
                Toast.makeText(this@LoginActivity, "로그인이 취소되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
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
        println("setResultSignUp")
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }
    }


    private fun handleSignInResult(completedTask : Task<GoogleSignInAccount>) {
        try {
            Log.d("handleSignInResult", "handleSignInResult")
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val authCode = account.serverAuthCode
            val idToken = account.idToken

            userEmail = email
            val userEmailMap = userEmail.split("@").map { it.toString() }

            //회원가입과 함께 새로운 계정 정보 저장
            if (userEmailMap.contains("daejin.ac.kr")) {
                //Toast.makeText(this, "로그인이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                saveSharedPreferenceGoogleLogin.setUserEMAIL(this@LoginActivity, email)

                Log.d("LoginActivity", "새로운유저, ${saveSharedPreferenceGoogleLogin.getUserEMAIL(this@LoginActivity).toString()}")
                /*val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                this@LoginActivity.finish()*/

                println(email)
                println(authCode.toString())
                println("idToken " + idToken)
                //getAccessToken(authCode!!)
                val userInfoToken = TokenRequest(idToken.toString())
                signInCheck(userInfoToken)

                /*val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                this@LoginActivity.finish()*/
            } else if (userEmailMap.contains("anes53027")) {
                saveSharedPreferenceGoogleLogin.setUserEMAIL(this@LoginActivity, email)

                Log.d("LoginActivity", "새로운유저, ${saveSharedPreferenceGoogleLogin.getUserEMAIL(this@LoginActivity).toString()}")
                /*val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                this@LoginActivity.finish()*/

                println(email)
                println(authCode.toString())
                println("idToken " + idToken)
                //getAccessToken(authCode!!)
                val userInfoToken = TokenRequest(idToken.toString())
                signInCheck(userInfoToken)
            } else {
                loadingDialog?.dismiss()
                Toast.makeText(this, "대진대학교 계정으로 로그인해주세요.", Toast.LENGTH_SHORT).show()
            }

        } catch (e : ApiException) {
            Log.e("catch failed", "signinresultfalied code = " + e.statusCode)
            loadingDialog?.dismiss()
            Toast.makeText(this, "로그인에 오류가 발생했습니다. ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signIn() {
        Log.d("signIn", "signin")
        //setData()
        val signIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signIntent)
    }

    private fun refreshAccessToken(refreshToken: String) {
        val call = RetrofitServerConnect.service
        val refreshTokenRequest = RefreshTokenRequest(refreshToken)
        Log.e("LoginActivity", "순서체크refresh")
        Log.e("LoginActivity", "${refreshToken}")

        call.refreshTokenProcess(refreshTokenRequest).enqueue(object : retrofit2.Callback<LoginResponsesData> {
            override fun onResponse(
                call: retrofit2.Call<LoginResponsesData>,
                response: retrofit2.Response<LoginResponsesData?>
            ) {
                if (response.isSuccessful) {
                    /*val builder =  OkHttpClient.Builder()
                        .connectTimeout(1, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(HeaderInterceptor(response.body()!!.accessToken))
                    intent.apply {
                        putExtra("accessToken", saveSharedPreferenceGoogleLogin.setToken(this@LoginActivity, response.body()!!.accessToken).toString())
                        putExtra("expireDate", saveSharedPreferenceGoogleLogin.setExpireDate(this@LoginActivity, response.body()!!.accessTokenExpiresIn.toString()).toString())
                    }
                    builder.build()*/

                    saveSharedPreferenceGoogleLogin.setToken(this@LoginActivity, response.body()!!.accessToken).toString()
                    saveSharedPreferenceGoogleLogin.setExpireDate(this@LoginActivity, response.body()!!.accessTokenExpiresIn.toString())
                    saveSharedPreferenceGoogleLogin.setRefreshToken(this@LoginActivity, response.body()!!.refreshToken).toString()
                    loadingDialog?.dismiss()
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog?.setCancelable(true)
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }

                    Log.d("LoginActivity RefreshTokenRequest" ,saveSharedPreferenceGoogleLogin.getRefreshToken(this@LoginActivity).toString())
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    this@LoginActivity.finish()

                } else {
                    Log.e("LoginActivity", "RefreshToken response fail")
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }
                    Toast.makeText(this@LoginActivity, "로그인이 취소되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<LoginResponsesData>, t: Throwable) {
                println("실패" + t.message.toString())
                loadingDialog?.dismiss()
                if (loadingDialog != null && loadingDialog!!.isShowing) {
                    loadingDialog?.dismiss()
                    loadingDialog?.setCancelable(true)
                    loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                }
                Toast.makeText(this@LoginActivity, "로그인이 취소되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
        /*val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", CLIENT_WEB_ID_KEY)
            .add("client_secret", CLIENT_WEB_SECRET_KEY)
            .build()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 실패 시 처리
                Log.e("Refresh Token", "Failed to refresh access token")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    *//*val jsonResponse = JSONObject(response.body!!.string())
                    val newAccessToken = jsonResponse.getString("access_token")

                    // TODO: 새로운 액세스 토큰을 저장하고 사용
                    // 여기에서 새로운 액세스 토큰을 저장하고 필요한 요청에 사용합니다.
                    println(newAccessToken)*//*
                    try {
                        val jsonObject = JSONObject(response.body!!.string())
                        val message = jsonObject.keys() //.toString(5)

                        //json파일 키와 벨류를 잠시담는 변수
                        val tempKey = ArrayList<String>()
                        val tempValue = ArrayList<String>()
                        //정리한번
                        user_info.clear()

                        while (message.hasNext()) {
                            val s = message.next().toString()
                            tempKey.add(s)

                        }

                        for (i in tempKey.indices) {
                            //fruitValueList.add(fruitObject.getString(fruitKeyList.get(j)));
                            tempValue.add(jsonObject.getString(tempKey[i]))
                            println(tempKey[i] + "/" + jsonObject.getString(tempKey[i]))
                        }

                        user_info.add(LoginGoogleResponse(tempValue[0], tempValue[1].toInt(), tempValue[2], tempValue[3], tempValue[4]))
                        currentUser = user_info[0]
                        println(message)
                        println(user_info[0].id_token)
                        //createClipData(user_info[0].id_token)
                        signInCheck(TokenRequest(currentUser.id_token, "/auth/google", "POST"))
                        tempKey.clear()
                        tempValue.clear()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    // 실패 시 처리
                    Log.e("Refresh Token", "Failed to refresh access token")
                }
            }
        })*/
    }

    //클립보드에 복사하기
    /*private fun createClipData(message : String) {
        val clipManager = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("message", message)

        clipManager.setPrimaryClip(clipData)

        //Toast.makeText(this, "복사되었습니다.", Toast.LENGTH_SHORT).show()
    }*/

    override fun onStart() {
        super.onStart()
        Log.d("LoginActivity", "start")
        /*val account = this?.let { GoogleSignIn.getLastSignedInAccount(it) }
        if (account!=null){
            ...
        }*/
    }

    override fun onPause() {
        super.onPause()
        Log.d("LoginActivity", "pause")
        /*if (saveSharedPreferenceGoogleLogin.getUserEMAIL(this@LoginActivity) != null) {
            if (saveSharedPreferenceGoogleLogin.getExpireDate(this@LoginActivity) != null) {
                val expireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this@LoginActivity)?.toLong()

                if (expireDate != null && expireDate <= System.currentTimeMillis()) {
                    //여기서 accessToken 토큰 만료 시 refreshToken으로 다시 처리
                    val refreshToken = saveSharedPreferenceGoogleLogin.getRefreshToken(this@LoginActivity)
                    Log.d("LoginActivity Start", refreshToken.toString())
                    if (refreshToken != null) {
                        refreshAccessToken(refreshToken.toString())
                    }
                    Log.e("ERROR", "EXPIRED")
                }
            }
        }*/
    }

    override fun onResume() {
        super.onResume()
        Log.d("LoginActivity", "onresume")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LoginActivity", "stop")
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog?.dismiss()
            loadingDialog = null // 다이얼로그 인스턴스 참조 해제
            Toast.makeText(this@LoginActivity, "로그인이 취소되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this@LoginActivity.finish()
    }
}