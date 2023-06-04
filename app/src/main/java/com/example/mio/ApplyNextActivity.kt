package com.example.mio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mio.databinding.ActivityApplyNextBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApplyNextActivity : AppCompatActivity() {
    private lateinit var anaBinding : ActivityApplyNextBinding
    private var isPosEnd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        anaBinding = ActivityApplyNextBinding.inflate(layoutInflater)

        anaBinding.applyBottomBtn.setOnClickListener {
            if (isPosEnd) {
                CoroutineScope(Dispatchers.Main).launch {
                    anaBinding.applyBottomBtn.text = "완료"
                }
                val intent = Intent(this@ApplyNextActivity, MainActivity::class.java).apply {
                }
                setResult(8, intent)
                finish()
            } else {
                anaBinding.applyViewflipper.showNext()
                CoroutineScope(Dispatchers.IO).launch {
                    isPosEnd = !isPosEnd
                }
            }
        }

        //뒤로가기
        anaBinding.applyBackArrow.setOnClickListener {
            val intent = Intent(this@ApplyNextActivity, MainActivity::class.java).apply {

            }
            setResult(8, intent)
            finish()
        }

        setContentView(anaBinding.root)

    }
}