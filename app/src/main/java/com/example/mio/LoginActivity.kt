package com.example.mio

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mio.Model.LoginResponsesData
import com.example.mio.Model.LoginGoogleResponse
import com.example.mio.Model.TokenRequest
import com.example.mio.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class LoginActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val mBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private val CLIENT_WEB_ID_KEY = BuildConfig.client_web_id_key
    private val CLIENT_WEB_SECRET_KEY = BuildConfig.client_web_secret_key
    private val SERVER_URL = BuildConfig.server_URL
    private var user_info : ArrayList<LoginGoogleResponse> = ArrayList<LoginGoogleResponse>()
    private lateinit var currentUser : LoginGoogleResponse
    //받은 계정 정보
    private var userEmail = ""
    //데이터 받아오기 준비
    private var isReady = false

    //init 벡엔드 연결
    /*val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(MioInterface::class.java)
    val interceptor = HttpLoggingInterceptor()*/
   /* interceptor = interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
*/

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

        setResultSignUp()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            //.requestIdToken(R.string.defalut_client_id.toString())
            .requestServerAuthCode(CLIENT_WEB_ID_KEY)
            .requestProfile()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mBinding.signInButton.setOnClickListener {
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
            splashScreen.setOnExitAnimationListener {splashScreenView ->
                val animScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 8f)
                val animScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 8f)
                val animAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)

                ObjectAnimator.ofPropertyValuesHolder(
                    splashScreenView.iconView,
                    animAlpha,
                    animScaleX,
                    animScaleY
                ).run {
                    interpolator = AnticipateInterpolator()
                    duration = 300L
                    doOnEnd { splashScreenView.remove() }
                    start()
                }
            }
        }
    }

    private fun signInCheck(userInfoToken : TokenRequest) {
        /*var interceptor = HttpLoggingInterceptor()
        interceptor = interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()*/

        /*val retrofit = Retrofit.Builder().baseUrl("url 주소")
            .addConverterFactory(GsonConverterFactory.create())
            //.client(client) 이걸 통해 통신 오류 log찍기 가능
            .build()
        val service = retrofit.create(MioInterface::class.java)*/
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.addUserInfoData(userInfoToken).enqueue(object : retrofit2.Callback<LoginResponsesData> {
                override fun onResponse(
                    call: retrofit2.Call<LoginResponsesData>,
                    response: retrofit2.Response<LoginResponsesData?>
                ) {
                    if (response.isSuccessful) {
                        println("success")
                        println(response.code())
                    } else {
                        println("fail")
                    }
                }
                override fun onFailure(call: retrofit2.Call<LoginResponsesData>, t: Throwable) {
                    println("실패" + t.message.toString())
                }
            })
        }

        /*val s = service.addUserInfoData(userInfoToken).execute().code()
        println(s)*/
    }

    private fun setResultSignUp() {
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
                Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun handleSignInResult(completedTask : Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val authCode = account.serverAuthCode
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()

            //회원가입과 함께 새로운 계정 정보 저장
            if (saveSharedPreferenceGoogleLogin.getUserEMAIL(this@LoginActivity)!!.isEmpty()) {
                //나중에 재개편 필요함 -> navigation graph를 정리할 필요성이 있음
                // call Login Activity
                //val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra("email", saveSharedPreferenceGoogleLogin.setUserEMAIL(this, email).toString())
                startActivity(intent)
                finish()

            } else { //현재 로그인, 또는 로그인했던 정보가 저장되어있으면 home으로
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            }

            Toast.makeText(this, "tjd", Toast.LENGTH_SHORT).show()
            println(email)
            println(authCode.toString())

            getAccessToken(authCode!!)

            Toast.makeText(this, "복사되었습니다.", Toast.LENGTH_SHORT).show()


        } catch (e : ApiException) {
            Log.w("failed", "signinresultfalied code = " + e.statusCode)
        }
    }

    private fun getAccessToken(authCode : String) {
        val client = OkHttpClient()
        val requestBody: RequestBody = FormBody.Builder()
            //1시간
            .add("grant_type", "authorization_code")
            .add(
                "client_id",
                CLIENT_WEB_ID_KEY
            )
            .add("client_secret", CLIENT_WEB_SECRET_KEY)
            .add("redirect_uri", "")
            .add("code", authCode)
            //refresh token 필요시
            .add("response_type", "code")
            .add("access_type", "offline")
            .add("approval_prompt", "force")
            .build()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    print("Failed")
                }

                override fun onResponse(call: Call, response: Response) {
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
                        createClipData(user_info[0].id_token)
                        signInCheck(TokenRequest(currentUser.id_token))
                        tempKey.clear()
                        tempValue.clear()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

            })
        }
    }
    private fun signIn() {
        val signIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signIntent)
    }

    private fun createClipData(message : String) {
        val clipManager = applicationContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("message", message)

        clipManager.setPrimaryClip(clipData)

        //Toast.makeText(this, "복사되었습니다.", Toast.LENGTH_SHORT).show()
    }


}