package com.gdsc.mio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gdsc.mio.databinding.ActivityOpenSourceManagementBinding

class OpenSourceManagementActivity : AppCompatActivity() {
    private lateinit var binding : ActivityOpenSourceManagementBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenSourceManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrow.setOnClickListener {
            this.finish()
        }
    }
}