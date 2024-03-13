package com.example.mio.TabAccount

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mio.Adapter.AccountSelectBankAdapter
import com.example.mio.Model.BankItemData
import com.example.mio.Model.SharedViewModel
import com.example.mio.R
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
    private var currentPage = 0
    private lateinit var myViewModel : SharedViewModel

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
                if (editable.isEmpty()) {
                    userAccountNumber = editable.toString()
                    myViewModel.postCheckComplete(complete = true)
                    /*if (editable.isEmpty()) {
                        nbrBinding.readSendComment.visibility = View.GONE
                        nbrBinding.readEditSendComment.visibility = View.GONE
                    } else if (getBottomSheetCommentData != "수정"){
                        nbrBinding.readSendComment.visibility = View.VISIBLE
                        nbrBinding.readEditSendComment.visibility = View.GONE
                    } else {
                        nbrBinding.readSendComment.visibility = View.GONE
                        nbrBinding.readEditSendComment.visibility = View.VISIBLE
                    }*/
                } else {
                    userAccountNumber = editable.toString()
                    myViewModel.postCheckComplete(complete = false)
                }
            }
        })

        adapter.setItemClickListener(object : AccountSelectBankAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: String) {
                when (itemId) {
                    "카카오뱅크" -> {
                        userAccountNumber += " 카카오뱅크"
                        binding.accountVf.showNext()
                        currentPage += 1
                    }

                    "토스뱅크" -> {
                        userAccountNumber += " 토스뱅크"
                        binding.accountVf.showNext()
                        currentPage += 1
                    }

                    "국민은행" -> {
                        userAccountNumber += " 국민은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                    }

                    "하나은행" -> {
                        userAccountNumber += " 하나은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                    }

                    "신한은행" -> {
                        userAccountNumber += " 신한은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                    }

                    "기업은행" -> {
                        userAccountNumber += " 기업은행"
                        binding.accountVf.showNext()
                        currentPage += 1
                    }
                }
            }
        })

        binding.backArrow.setOnClickListener {
            if (currentPage > 0) {
                binding.accountVf.showPrevious()
            } else {
                this@AccountSelectBankActivity.finish()
            }
        }

        binding.accountSelectBankBtn.setOnClickListener {
            val intent = Intent(this@AccountSelectBankActivity, AccountSettingActivity::class.java).apply {
                putExtra("flag", 2)
                putExtra("AccountNumber", userAccountNumber)
            }
            setResult(RESULT_OK, intent)
            finish()
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
        bankData.add(BankItemData(R.drawable.kakao_bank_icon, "카카오뱅크"))
        bankData.add(BankItemData(R.drawable.toss_icon, "토스뱅크"))
        bankData.add(BankItemData(R.drawable.kb_icon, "국민은행"))
        bankData.add(BankItemData(R.drawable.hana_icon, "하나은행"))
        bankData.add(BankItemData(R.drawable.shinhan_icon, "신한은행"))
        bankData.add(BankItemData(R.drawable.ibk_icon, "기업은행"))
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