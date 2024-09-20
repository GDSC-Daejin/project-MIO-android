package com.example.mio.Model

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.mio.BuildConfig
import com.kakao.vectormap.KakaoMapSdk
import java.io.File

class FragSharedViewModel2 : Application() {
    lateinit var sharedViewModel: FragSharedViewModel
    private val kakaoMapInit = BuildConfig.kakao_native_app_key

    override fun onCreate() {
        super.onCreate()
        KakaoMapSdk.init(this, kakaoMapInit)
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        sharedViewModel = ViewModelProvider(ViewModelStore(), factory)[FragSharedViewModel::class.java]
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initializeWorkManager()
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
    }

    private fun initializeWorkManager() {
        WorkManager.initialize(
            this,
            Configuration.Builder()
                // 필요한 경우 추가 설정 가능
                .build()
        )
    }
}