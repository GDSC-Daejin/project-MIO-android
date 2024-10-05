package com.example.mio

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mio.model.LoginResponsesData
import com.example.mio.model.TokenRequest
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        //상태바 지우기(이 activity만)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        setContentView(mBinding.root)
        MobileAds.initialize(this@LoginActivity) {}
        setResultSignUp()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(clientWebKey)
            .requestServerAuthCode(clientWebKey)
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

    private fun signInCheck(userInfoToken : TokenRequest) {
        val serverUrl = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service: MioInterface = retrofit.create(MioInterface::class.java)
        service.addUserInfoData(userInfoToken).enqueue(object : retrofit2.Callback<LoginResponsesData> {
            override fun onResponse(
                call: retrofit2.Call<LoginResponsesData>,
                response: retrofit2.Response<LoginResponsesData?>
            ) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        val accessToken = loginResponse.accessToken
                        val accessTokenExpiresIn = loginResponse.accessTokenExpiresIn
                        val refreshToken = loginResponse.refreshToken

                        // AccessToken, ExpireDate, RefreshToken 저장
                        saveSharedPreferenceGoogleLogin.setToken(this@LoginActivity, accessToken)
                        saveSharedPreferenceGoogleLogin.setExpireDate(this@LoginActivity, accessTokenExpiresIn.toString())
                        saveSharedPreferenceGoogleLogin.setRefreshToken(this@LoginActivity, refreshToken)


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

                        Toast.makeText(this@LoginActivity, "로그인이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        this@LoginActivity.finish()
                    }
                } else {
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }
                    Toast.makeText(this@LoginActivity, "로그인이 취소되었습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<LoginResponsesData>, t: Throwable) {
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