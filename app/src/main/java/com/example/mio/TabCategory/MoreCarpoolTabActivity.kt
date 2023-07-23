package com.example.mio.TabCategory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mio.databinding.ActivityMoreCarpoolBinding

class MoreCarpoolTabActivity : AppCompatActivity() {
    private lateinit var mctBinding : ActivityMoreCarpoolBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mctBinding = ActivityMoreCarpoolBinding.inflate(layoutInflater)
        setContentView(mctBinding.root)
    }
}