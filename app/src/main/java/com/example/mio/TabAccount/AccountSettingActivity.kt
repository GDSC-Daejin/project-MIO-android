package com.example.mio.TabAccount

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mio.MainActivity
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.TabCategory.TaxiTabFragment
import com.example.mio.databinding.ActivityAccountSettingBinding

class AccountSettingActivity : AppCompatActivity() {
    private var aBinding : ActivityAccountSettingBinding? = null
    private var type : String? = null
    private var email = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        aBinding = ActivityAccountSettingBinding.inflate(layoutInflater)
        type = intent.getStringExtra("type")

        if (type.equals("ACCOUNT")) {
            email = intent.getStringExtra("accountData").toString()

        }

        aBinding!!.asAccountLl.setOnClickListener {
            //dialog로 수정 Todo
        }

        aBinding!!.completeTv.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("type", "ACCOUNT")
                putExtra("location", aBinding!!.asLocationTv.text)
                putExtra("account", aBinding!!.asAccountTv.text)
                putExtra("flag", 5)
            }
            setResult(RESULT_OK, intent)
            finish()
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
}