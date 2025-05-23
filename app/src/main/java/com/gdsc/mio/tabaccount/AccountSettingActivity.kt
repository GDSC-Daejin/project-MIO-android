package com.gdsc.mio.tabaccount

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.gdsc.mio.*
import com.gdsc.mio.model.AddressData
import com.gdsc.mio.model.EditAccountData
import com.gdsc.mio.model.User
import com.gdsc.mio.navigation.AccountFragment
import com.gdsc.mio.databinding.ActivityAccountSettingBinding
import com.gdsc.mio.util.AESKeyStoreUtil
import com.gdsc.mio.util.AESUtil
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
                        try {
                            if (response.body()?.accountNumber?.contains(",") != true) {
                                aBinding?.asAccountTv?.text = response.body()?.accountNumber.toString()
                                setAccountNumber = response.body()?.accountNumber.toString()
                            } else {
                                val splitEnResponse = response.body()?.accountNumber.toString().split(",").map { it }
                                // 복호화
                                val deText = AESUtil.decryptAES(secretKey, splitEnResponse[0], splitEnResponse[1])
                                aBinding?.asAccountTv?.text = deText
                                setAccountNumber = deText // 암호화된 값 저장
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // 복호화 실패 처리, 오류 메시지 출력 또는 기본 메시지 설정
                            aBinding?.asAccountTv?.text = this@AccountSettingActivity.getString(R.string.AccountSettingText)
                            setAccountNumber = ""
                        }
                    }

                    if (response.body()?.activityLocation.isNullOrEmpty()) {
                        aBinding?.asLocationTv?.text = "화살표를 눌러 지역을 검색해주세요"
                    } else {
                        try {
                            /*aBinding?.asLocationTv?.text = response.body()?.activityLocation
                            setLocation2 = response.body()?.activityLocation
                            */
                            if (response.body()?.activityLocation?.contains(",") != true) {
                                aBinding?.asLocationTv?.text = response.body()?.activityLocation.toString()
                                setLocation2 = response.body()?.activityLocation.toString()
                            } else {
                                val splitEnResponse = response.body()?.activityLocation.toString().split(",").map { it }
                                // 복호화
                                val deText = AESUtil.decryptAES(secretKey, splitEnResponse[0], splitEnResponse[1])
                                aBinding?.asLocationTv?.text = deText
                                setLocation2 = deText // 암호화된 값 저장
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // 복호화 실패 처리, 오류 메시지 출력 또는 기본 메시지 설정
                            aBinding?.asLocationTv?.text = this@AccountSettingActivity.getString(R.string.AccountSettingText)
                            setLocation2 = ""
                        }
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
        val enAN = AESUtil.encryptAES(secretKey, setAccountNumber.toString())
        val enLocation = if (setLocation != null) {
            AESUtil.encryptAES(secretKey,  setLocation?.address?.region_3depth_name.toString())
        } else {
            AESUtil.encryptAES(secretKey,  setLocation2.toString())
        }

        sendAccountData = if (setLocation != null) {
            EditAccountData(isGender, isSmoker, "${enAN.first},${enAN.second}", "${enLocation.first},${enLocation.second}")
        } else {
            EditAccountData(isGender, isSmoker, "${enAN.first},${enAN.second}", "${enLocation.first},${enLocation.second}")
        }

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