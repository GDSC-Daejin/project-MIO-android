package com.example.mio.tabaccount

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mio.*
import com.example.mio.model.AddressData
import com.example.mio.model.EditAccountData
import com.example.mio.model.User
import com.example.mio.navigation.AccountFragment
import com.example.mio.databinding.ActivityAccountSettingBinding
import com.example.mio.util.AESKeyStoreUtil
import com.example.mio.util.AESUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.crypto.SecretKey

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
    private val secretKey: SecretKey by lazy {
        AESKeyStoreUtil.getOrCreateAESKey()
    }

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
        aBinding!!.asAccountTv.setOnClickListener {
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
                        if (response.body()?.accountNumber?.contains(",") != true) {
                            aBinding?.asAccountTv?.text = response.body()?.accountNumber.toString()
                            setAccountNumber = response.body()?.accountNumber.toString() //암호화
                        } else {
                            val splitEnResponse = response.body()?.accountNumber.toString().split(",").map { it }
                            val deText = AESUtil.decryptAES(secretKey, splitEnResponse[0], splitEnResponse[1])
                            aBinding?.asAccountTv?.text = deText
                            setAccountNumber = response.body()?.accountNumber.toString() //암호화
                        }
                    }

                    if (response.body()?.activityLocation.isNullOrEmpty()) {
                        aBinding?.asLocationTv?.text = "화살표를 눌러 지역을 검색해주세요"
                    } else {
                        aBinding?.asLocationTv?.text = response.body()?.activityLocation
                        setLocation2 = response.body()?.activityLocation
                    }

                } else {
                    Toast.makeText(this@AccountSettingActivity, "사용자 정보를 불러오지 못했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@AccountSettingActivity, "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editAccountData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val userId = saveSharedPreferenceGoogleLogin.getUserId(this)
        sendAccountData = if (setLocation != null) {
            EditAccountData(isGender, isSmoker, setAccountNumber.toString(), setLocation?.address?.region_3depth_name.toString())
        } else {
            EditAccountData(isGender, isSmoker, setAccountNumber.toString(), setLocation2.toString())
        }

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(this@AccountSettingActivity).editMyAccountData(userId, sendAccountData!!).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            saveSharedPreferenceGoogleLogin.setArea(this@AccountSettingActivity, sendAccountData?.activityLocation)

                            val intent = Intent(this@AccountSettingActivity, AccountFragment::class.java).apply {
                                putExtra("flag", 6)
                            }

                            setResult(RESULT_OK, intent)
                            finish() // 액티비티 종료
                        }
                    } else {
                        Toast.makeText(this@AccountSettingActivity, "계정의 내용 수정에 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@AccountSettingActivity, "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private val requestActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> {
                    val locationData = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        it.data?.getParcelableExtra("locationData")
                    } else {
                        it.data?.getParcelableExtra("locationData", AddressData::class.java)
                    }
                    //val locationData : AddressData? = it.data?.getSerializableExtra("locationData") as AddressData?
                    val locationData2 = it.data?.getStringExtra("locationData2")

                    //암호화
                    val accountNumber = it.data?.getStringExtra("AccountNumber") ?: "" //account+bank=entext
                    val enAccount = AESUtil.encryptAES(secretKey, accountNumber) //entext+lv
                    Log.e("accountEN", accountNumber)
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
                                setAccountNumber = "${enAccount.first},${enAccount.second}"
                                //수정할 수 있으니 보이는건 보이게
                                aBinding?.asAccountTv?.text = accountNumber
                            }
                        }


                        //location 세팅
                        3 -> { //검색해서 선택
                            handler.post {
                                setLocation = locationData
                                aBinding?.asLocationTv?.text = locationData?.address?.region_3depth_name//setLocation?.road_address_name.toString() + " " + setLocation?.place_name.toString()
                            }
                        }

                        4 -> { //최근검색어에서 바로선택
                            handler.post {
                                setLocation2 = locationData2.toString()
                                aBinding?.asLocationTv?.text = setLocation2
                            }
                        }
                    }
                }
            }
        }
}