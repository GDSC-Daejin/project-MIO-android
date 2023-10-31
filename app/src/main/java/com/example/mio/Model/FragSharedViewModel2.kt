package com.example.mio.Model

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

class FragSharedViewModel2 : Application() {
    lateinit var sharedViewModel: FragSharedViewModel

    override fun onCreate() {
        super.onCreate()
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(this)
        sharedViewModel = ViewModelProvider(ViewModelStore(), factory)[FragSharedViewModel::class.java]
    }
}