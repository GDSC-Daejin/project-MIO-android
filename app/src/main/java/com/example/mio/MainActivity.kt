package com.example.mio

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mio.Model.LoginGoogleResponse
import com.example.mio.NoticeBoard.NoticeBoardActivity
import com.example.mio.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var resultLauncher: ActivityResultLauncher<Intent>
    lateinit var mBinding : ActivityMainBinding
    private val CLIENT_WEB_ID_KEY = BuildConfig.client_web_id_key
    private val CLIENT_WEB_SECRET_KEY = BuildConfig.client_web_secret_key


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
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

        mBinding.textView.setOnClickListener {
            val intent = Intent(this, NoticeBoardActivity::class.java).apply {
                //putExtra("type", "")
            }
            startActivity(intent)
        }

    }
    private fun setResultSignUp() {
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
                Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
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
            if (saveSharedPreferenceGoogleLogin.getUserEMAIL(this@MainActivity)!!.isEmpty()) {
                //나중에 재개편 필요함 -> navigation graph를 정리할 필요성이 있음
                // call Login Activity
                /*val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.putExtra("email", saveSharedPreferenceGoogleLogin.setUserEMAIL(this, email).toString())
                startActivity(intent)
                finish()*/

            } else { //현재 로그인, 또는 로그인했던 정보가 저장되어있으면 home으로
                val intent = Intent(this, NoticeBoardActivity::class.java)
                startActivity(intent)
                this.finish()
            }

            Toast.makeText(this, "tjd", Toast.LENGTH_SHORT).show()
            println(email)
            println(authCode.toString())

            getAccessToken(authCode!!)

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

                    val user_info = ArrayList<LoginGoogleResponse>()
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
                    println()
                    println(message)
                    println(user_info[0].id_token)
                    tempKey.clear()
                    tempValue.clear()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        })
    }
    private fun signIn() {
        val signIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signIntent)
    }


}