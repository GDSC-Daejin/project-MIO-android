package com.gdsc.mio.tabaccount

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.gdsc.mio.adapter.AccountSelectBankAdapter
import com.gdsc.mio.model.BankItemData
import com.gdsc.mio.viewmodel.SharedViewModel
import com.gdsc.mio.R
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin
import com.gdsc.mio.databinding.ActivityAccountSelectBankBinding
import com.gdsc.mio.util.AESKeyStoreUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.crypto.SecretKey

class AccountSelectBankActivity : AppCompatActivity() {
    private val  binding by lazy {
        ActivityAccountSelectBankBinding.inflate(layoutInflater)
    }
    private val layoutManager = GridLayoutManager(this@AccountSelectBankActivity, 2)
    private lateinit var adapter : AccountSelectBankAdapter
    private var bankData = ArrayList<BankItemData>()
    private var userAccountNumber : String? = null
    private var userBank : String? = null
    private var currentPage = 0
    private lateinit var myViewModel : SharedViewModel
    private var sharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private val secretKey: SecretKey by lazy {
        AESKeyStoreUtil.getOrCreateAESKey()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //뷰의 이벤트 리스너
        myViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        initRecyclerView()

        binding.accountNumberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                val inputText = editable.toString()
                if (inputText.isNotEmpty()) {
                    userAccountNumber = inputText
                    myViewModel.postCheckComplete(complete = true)
                } else {
                    myViewModel.postCheckComplete(complete = false)
                }
            }
        })

        adapter.setItemClickListener(object : AccountSelectBankAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: String) {
                when (itemId) {
                    "카카오뱅크" -> {
                        userBank = " 카카오뱅크"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.kakaobank.channel", secretKey)
                    }

                    "토스뱅크" -> {
                        userBank = " 토스뱅크"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "viva.republica.toss", secretKey)
                    }

                    "국민은행" -> {
                        userBank = " 국민은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.kbstar.kbbank", secretKey)
                    }

                    "하나은행" -> {
                        userBank = " 하나은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.kebhana.hanapush", secretKey)
                    }

                    "신한은행" -> {
                        userBank = " 신한은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.shinhan.sbanking", secretKey)
                    }

                    "기업은행" -> {
                        userBank = " 기업은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.ibk.android.ionebank", secretKey)
                    }

                    "우리은행" -> {
                        userBank = " 우리은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.wooribank.smart.npib", secretKey)
                    }

                    "농협은행" -> {
                        userBank = " 농협은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "nh.smart.banking", secretKey)
                    }
                }
            }
        })

        binding.backArrow.setOnClickListener {
            if (currentPage > 0) {
                binding.accountVf.showPrevious()
                currentPage -= 1
                // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                if (inputMethodManager.isActive) {
                    // 가상 키보드가 올라가 있다면 내립니다.
                    inputMethodManager.hideSoftInputFromWindow(binding.backArrow.windowToken, 0)
                }
            } else {
                // InputMethodManager를 통해 가상 키보드의 상태를 관리합니다.
                val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                // 가상 키보드가 올라가 있는지 여부를 확인합니다.
                if (inputMethodManager.isActive) {
                    // 가상 키보드가 올라가 있다면 내립니다.
                    inputMethodManager.hideSoftInputFromWindow(binding.backArrow.windowToken, 0)
                }
                this@AccountSelectBankActivity.finish()
            }
        }

        binding.accountSelectBankBtn.setOnClickListener {
            if (userAccountNumber == null) {
                Toast.makeText(this@AccountSelectBankActivity, "계좌번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this@AccountSelectBankActivity, AccountSettingActivity::class.java).apply {
                    putExtra("flag", 2)
                    putExtra("AccountNumber", (userAccountNumber ?: "") + userBank)
                    //println("useran " + (userAccountNumber ?: "") + userBank)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }


        myViewModel.checkComplete.observe(this) {
            if (it) {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.accountSelectBankBtn.apply {
                        setBackgroundResource(R.drawable.round_btn_update_layout)
                        setTextColor(ContextCompat.getColor(this@AccountSelectBankActivity, R.color.mio_gray_3))
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this@AccountSelectBankActivity , R.color.mio_gray_6)) //승인
                    binding.accountSelectBankBtn.apply {
                        setBackgroundResource(R.drawable.round_btn_layout)
                        // 배경색 변경
                        backgroundTintList = colorStateList
                        setTextColor(ContextCompat.getColor(this@AccountSelectBankActivity, R.color.white))
                    }
                }
            }
        }
    }

    private fun setData() {
        bankData.add(BankItemData(R.drawable.bank_kakao, "카카오뱅크"))
        bankData.add(BankItemData(R.drawable.toss, "토스뱅크"))
        bankData.add(BankItemData(R.drawable.bank_kb, "국민은행"))
        bankData.add(BankItemData(R.drawable.bank_hana, "하나은행"))
        bankData.add(BankItemData(R.drawable.bank_shinhan, "신한은행"))
        bankData.add(BankItemData(R.drawable.bank_ibk, "기업은행"))
        bankData.add(BankItemData(R.drawable.bank_woori, "우리은행"))
        bankData.add(BankItemData(R.drawable.bank_nh, "농협은행"))
    }

    private fun initRecyclerView() {
        setData()
        adapter = AccountSelectBankAdapter()
        adapter.itemData = bankData
        binding.accountSelectBankRv.adapter = adapter
        binding.accountSelectBankRv.setHasFixedSize(true)
        binding.accountSelectBankRv.layoutManager = layoutManager
    }
}