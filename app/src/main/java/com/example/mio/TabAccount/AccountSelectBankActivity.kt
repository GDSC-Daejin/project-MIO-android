package com.example.mio.TabAccount

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mio.Adapter.AccountSelectBankAdapter
import com.example.mio.Model.BankItemData
import com.example.mio.R
import com.example.mio.databinding.ActivityAccountSelectBankBinding

class AccountSelectBankActivity : AppCompatActivity() {
    private val  binding by lazy {
        ActivityAccountSelectBankBinding.inflate(layoutInflater)
    }
    private val layoutManager = GridLayoutManager(this@AccountSelectBankActivity, 2)
    private lateinit var adapter : AccountSelectBankAdapter
    private var bankData = ArrayList<BankItemData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initRecyclerView()

        adapter.setItemClickListener(object : AccountSelectBankAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: String) {

            }
        })
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