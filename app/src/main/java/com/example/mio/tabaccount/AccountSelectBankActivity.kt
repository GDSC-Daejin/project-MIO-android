package com.example.mio.tabaccount

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
import com.example.mio.adapter.AccountSelectBankAdapter
import com.example.mio.model.BankItemData
import com.example.mio.viewmodel.SharedViewModel
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.ActivityAccountSelectBankBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.kakaobank.channel")
                    }

                    "토스뱅크" -> {
                        userBank = " 토스뱅크"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "viva.republica.toss")
                    }

                    "국민은행" -> {
                        userBank = " 국민은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.kbstar.kbbank")
                    }

                    "하나은행" -> {
                        userBank = " 하나은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.kebhana.hanapush")
                    }

                    "신한은행" -> {
                        userBank = " 신한은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.shinhan.sbanking")
                    }

                    "기업은행" -> {
                        userBank = " 기업은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.ibk.android.ionebank")
                    }

                    "우리은행" -> {
                        userBank = " 우리은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "com.wooribank.smart.npib")
                    }

                    "농협은행" -> {
                        userBank = " 농협은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                        sharedPreferenceGoogleLogin.setAccount(this@AccountSelectBankActivity, "nh.smart.banking")
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