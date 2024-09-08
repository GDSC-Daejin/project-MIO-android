package com.example.mio.TabAccount

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mio.*
import com.example.mio.Model.AddressData
import com.example.mio.Model.EditAccountData
import com.example.mio.Model.Place
import com.example.mio.Model.User
import com.example.mio.Navigation.AccountFragment
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
    private var isGender = false //false남 true여
    private var isSmoker = false //false비흡, true 흡
    private var setLocation: AddressData? = null
    private var setLocation2: String? = null
    private var setAccountNumber : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        aBinding = ActivityAccountSettingBinding.inflate(layoutInflater)
        type = intent.getStringExtra("type")

        if (type.equals("ACCOUNT")) {
            email = intent.getStringExtra("accountData").toString()
            initAccountSet()
        }
        aBinding?.asProfileUseridTv?.text = email.split("@").map { it }.first()

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

    private fun initAccountSet() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()

        /*val interceptor = Interceptor { chain ->
            val newRequest: Request
            if (token != null && token != "") { // 토큰이 없지 않은 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    *//*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                    Log.e("settting", "setting1")
                    val intent = Intent(this@AccountSettingActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    Toast.makeText(this@AccountSettingActivity, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
        val api = retrofit2.create(MioInterface::class.java)*/

        RetrofitServerConnect.create(this@AccountSettingActivity).getAccountData(userEmail = email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    if (response.body()?.gender != null) {
                        aBinding?.asGenderTv?.text = if (response.body()?.gender!!) {
                            "여성"
                        } else {
                            "남성"
                        }
                    } else {
                        aBinding?.asGenderTv?.text = "이곳을 눌러 선택해주세요"
                    }

                    if (response.body()?.verifySmoker != null) {
                        aBinding?.asSmokeTv?.text = if (response.body()?.verifySmoker!!) {
                            "O"
                        } else {
                            "X"
                        }
                    } else {
                        aBinding?.asSmokeTv?.text = "이곳을 눌러 선택해주세요"
                    }

                    if (response.body()?.accountNumber.isNullOrEmpty()) {
                        aBinding?.asAccountTv?.text = "화살표를 눌러 계좌를 등록해주세요"
                    } else {
                        aBinding?.asAccountTv?.text = response.body()?.accountNumber
                        setAccountNumber = response.body()?.accountNumber
                    }

                    if (response.body()?.activityLocation.isNullOrEmpty()) {
                        aBinding?.asLocationTv?.text = "화살표를 눌러 지역을 검색해주세요"
                    } else {
                        aBinding?.asLocationTv?.text = response.body()?.activityLocation
                        setLocation2 = response.body()?.activityLocation
                    }

                } else {
                    Log.e("Account Set ERROR", response.errorBody()?.string()!!)
                    Log.e("Account Set Error", response.code().toString())
                    Log.e("Account Set Error", response.message().toString())
                    Log.e("setting", "setting2")
                    Toast.makeText(this@AccountSettingActivity, "사용자 정보를 불러오지 못했습니다. 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("Account Failure", t.message.toString())
                Toast.makeText(this@AccountSettingActivity, "사용자 정보를 불러오지 못했습니다. 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editAccountData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()
        val userId = saveSharedPreferenceGoogleLogin.getUserId(this)!!

        /*val interceptor = Interceptor { chain ->
            val newRequest: Request
            if (token != null && token != "") { // 토큰이 없지 않은 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    *//*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                    Log.e("setting", "setting3")
                    val intent = Intent(this@AccountSettingActivity, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    Toast.makeText(this@AccountSettingActivity, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
        val api = retrofit2.create(MioInterface::class.java)*/

        //println(userId)

        sendAccountData = if (setLocation != null) {
            EditAccountData(isGender, isSmoker, setAccountNumber.toString(), setLocation?.address?.region_3depth_name.toString())
        } else {
            EditAccountData(isGender, isSmoker, setAccountNumber.toString(), setLocation2.toString())
        }

        Log.e("ACCountSEttingSENDTEST", sendAccountData.toString())

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(this@AccountSettingActivity).editMyAccountData(userId, sendAccountData!!).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            // call the invalidate()
                            Log.d("Success", response.code().toString())
                            Log.d("Account Setting", "Account Setting Response Success")

                            saveSharedPreferenceGoogleLogin.setArea(this@AccountSettingActivity, sendAccountData?.activityLocation)

                            val intent = Intent(this@AccountSettingActivity, AccountFragment::class.java).apply {
                                putExtra("flag", 6)
                            }

                            setResult(RESULT_OK, intent)
                            finish() // 액티비티 종료
                        }
                    } else {
                        Log.d("f", response.code().toString())
                        Log.e("error", response.errorBody()?.string()!!)
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
                    val locationData : AddressData? = it.data?.getSerializableExtra("locationData") as AddressData?
                    val locationData2 = it.data?.getStringExtra("locationData2")

                    Log.e("AccountSettingREquestAc", locationData2.toString()) //서울 노원구 상계동
                    Log.e("AccountSettingREquestAc", locationData.toString()) //AddressData

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
                        3 -> { //검색해서 선택
                            handler.post {
                                setLocation = locationData
                                aBinding?.asLocationTv?.text = locationData?.address?.region_3depth_name//setLocation?.road_address_name.toString() + " " + setLocation?.place_name.toString()
                                Log.e("AccountSettingREquestAc3", locationData2.toString())
                                Log.e("AccountSettingREquestAc3", locationData.toString())
                            }
                        }

                        4 -> { //최근검색어에서 바로선택
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