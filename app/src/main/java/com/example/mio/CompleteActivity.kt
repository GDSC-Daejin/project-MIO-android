package com.example.mio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.mio.databinding.ActivityCompleteBinding

class CompleteActivity : AppCompatActivity() {
    private val cBinding by lazy {
        ActivityCompleteBinding.inflate(layoutInflater)
    }

    private var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cBinding.root)

        type = intent.getStringExtra("type") as String

        if (type == "PASSENGER") {


        } else if (type == "DRIVER") {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.topMargin = 150
            cBinding.completeEntireLl.layoutParams = lp
        }

        cBinding.closeScreen.setOnClickListener {
            this.finish()
        }
    }
}