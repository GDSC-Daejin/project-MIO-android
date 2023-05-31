package com.example.mio

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitServerConnect {
    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service = retrofit.create(MioInterface::class.java)
}