package com.example.mio.TabAccount

import android.content.Intent
import android.os.Build.VERSION_CODES.M
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mio.*
import com.example.mio.Model.*
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
    private var aBinding: ActivityAccountSettingBinding? = null
    private var type: String? = null
    private var email = ""

    private var sendAccountData: EditAccountData? = null
    private var isGender = false
    private var isSmoker = false
    private var setLocation: Place? = null
    private var setLocation2: String? = null
    private var setAccountNumber : String? = null

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
                                aBinding?.asGenderTv?.text = "남성"
                            }

                            "여성" -> { //true
                                isGender = true
                                aBinding?.asGenderTv?.text = "여성"
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
                                aBinding?.asSmokeTv?.text = "O"
                            }

                            "x" -> { //비흡
                                isSmoker = false
                                aBinding?.asSmokeTv?.text = "X"
                            }
                        }
                    }
                })
            }
        }

        aBinding!!.accountAccountNumberBtn.setOnClickListener {
            val intent =
                Intent(this@AccountSettingActivity, AccountSelectBankActivity::class.java)
            requestActivity.launch(intent)
        }

        aBinding!!.accountActivityLocationBtn.setOnClickListener {
            val intent = Intent(
                this@AccountSettingActivity,
                AccountSearchLocationActivity::class.java
            ).apply {
                putExtra("type", "account")
            }
            requestActivity.launch(intent)
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

        //여기 계좌까지 추가하면 활성화하기 Todo
        sendAccountData = if (setLocation != null) {
            EditAccountData(isGender, isSmoker, setAccountNumber.toString(), setLocation?.road_address_name.toString())
        } else {
            EditAccountData(isGender, isSmoker, setAccountNumber.toString(), setLocation2.toString())
        }

        Log.e("ACCountSEttingSENDTEST", sendAccountData.toString())

        CoroutineScope(Dispatchers.IO).launch {
            api.editMyAccountData(userId, sendAccountData!!).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        Log.d("Success", response.code().toString())
                        Log.d("Account Setting", "Account Setting Response Success")
                        val intent = Intent(this@AccountSettingActivity, MainActivity::class.java).apply {
                            putExtra("flag", 6)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
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


    private val requestActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            when (it.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val locationData : Place? = it.data?.getSerializableExtra("locationData") as Place?
                    val locationData2 = it.data?.getStringExtra("locationData2")

                    Log.e("AccountSettingREquestAc", locationData2.toString())
                    Log.e("AccountSettingREquestAc", locationData.toString())

                    val accountNumber = it.data?.getStringExtra("AccountNumber") ?: ""
                    val handler = Handler(Looper.getMainLooper())
                    when (it.data?.getIntExtra("flag", -1)) {
                        //add
                       /* 0 -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                setLocation = locationData
                            }
                        }*/
                        2 -> {
                            handler.post {
                                setAccountNumber = accountNumber
                                aBinding?.asAccountTv?.text = setAccountNumber.toString()
                            }
                        }


                        //location 세팅
                        3 -> {
                            handler.post {
                                setLocation = locationData
                                aBinding?.asLocationTv?.text = setLocation?.road_address_name.toString() + " " + setLocation?.place_name.toString()
                                Log.e("AccountSettingREquestAc3", locationData2.toString())
                                Log.e("AccountSettingREquestAc3", locationData.toString())
                            }
                        }

                        4 -> {
                            handler.post {
                                setLocation2 = locationData2.toString()
                                aBinding?.asLocationTv?.text = setLocation2?.toString()
                                Log.e("AccountSettingREquestAc4", locationData2.toString())
                                Log.e("AccountSettingREquestAc4", locationData.toString())
                            }
                        }
                    }
                }
            }
        }
}