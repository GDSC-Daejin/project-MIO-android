package com.example.mio.TabAccount

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.mio.*
import com.example.mio.Model.Content
import com.example.mio.Model.EditAccountData
import com.example.mio.Model.PostData
import com.example.mio.Model.User
import com.example.mio.NoticeBoard.NoticeBoardEditActivity
import com.example.mio.TabCategory.TaxiTabFragment
import com.example.mio.databinding.ActivityAccountSettingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountSettingActivity : AppCompatActivity() {
    private var aBinding : ActivityAccountSettingBinding? = null
    private var type : String? = null
    private var email = ""

    private var sendAccountData : EditAccountData? = null
    private var isGender = false
    private var isSmoker = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        aBinding = ActivityAccountSettingBinding.inflate(layoutInflater)
        type = intent.getStringExtra("type")

        if (type.equals("ACCOUNT")) {
            email = intent.getStringExtra("accountData").toString()

        }

        aBinding!!.asGenderLl.setOnClickListener {
            val bottomSheet = AccountSettingBottomSheetFragment(true)
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : AccountSettingBottomSheetFragment.OnSendFromBottomSheetDialog {
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        when (value) {
                            "남성" -> { //false
                                isGender = false
                            }

                            "여성" -> { //true
                                isGender = true
                            }
                        }
                    }
                })
            }
        }

        aBinding!!.asSmokeLl.setOnClickListener {
            val bottomSheet = AccountSettingBottomSheetFragment(false)
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : AccountSettingBottomSheetFragment.OnSendFromBottomSheetDialog {
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        when (value) {
                            "o" -> { //흡
                                isSmoker = true
                            }

                            "x" -> { //비흡
                                isSmoker = false
                            }
                        }
                    }
                })
            }
        }

        aBinding!!.accountAccountNumberBtn.setOnClickListener {
            //Todo 은행 넣기
        }

        aBinding!!.accountActivityLocationBtn.setOnClickListener {
            //Todo 여긴 검색창 넣기

        }



        aBinding!!.completeTv.setOnClickListener {
            /*val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("type", "ACCOUNT")
                putExtra("location", aBinding!!.asLocationTv.text)
                putExtra("account", aBinding!!.asAccountTv.text)
                putExtra("flag", 5)
            }
            setResult(RESULT_OK, intent)
            finish()*/

            editAccountData()
        }

        aBinding!!.backArrow.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("flag", 7)
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        setContentView(aBinding!!.root)
    }

    private fun editAccountData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.substring(0 until 8)
        val userId = saveSharedPreferenceGoogleLogin.getUserId(this)!!

        val interceptor = Interceptor { chain ->
            val newRequest: Request
            if (token != null && token != "") { // 토큰이 없지 않은 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(this@AccountSettingActivity, LoginActivity::class.java)
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
        //sendAccountData = EditAccountData(isGender, isSmoker, )

        CoroutineScope(Dispatchers.IO).launch {
            api.editMyAccountData(userId, sendAccountData!!).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        println("patch 성공")
                    } else {
                        Log.d("f", response.code().toString())
                        Log.d("error", response.errorBody().toString())
                        Log.d("message", call.request().toString())
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }
}