package com.example.mio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mio.databinding.ActivityOpenSourceManagementBinding

class OpenSourceManagementActivity : AppCompatActivity() {
    private lateinit var oBinding : ActivityOpenSourceManagementBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        oBinding = ActivityOpenSourceManagementBinding.inflate(layoutInflater)

        setContentView(oBinding.root)
    }
}