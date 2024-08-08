package com.example.mio

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitServerConnect {
    fun create(context: Context): MioInterface {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context).toString()

        val interceptor = Interceptor { chain ->
            val newRequest: Request
            if (token != null && token != "") { // 토큰이 없지 않은 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    context.startActivity(intent)
                    return@Interceptor chain.proceed(newRequest)
                }
            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)


        return api
    }
}

/*
*           val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            val token = saveSharedPreferenceGoogleLogin.getToken(this).toString()
            val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(this).toString()

            //Authorization jwt토큰 로그인
            val interceptor = Interceptor { chain ->

                var newRequest: Request
                if (token != null && token != "") { // 토큰이 없는 경우
                    // Authorization 헤더에 토큰 추가
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                    val expireDate: Long = getExpireDate.toLong()
                    if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                        //refresh 들어갈 곳
                        newRequest =
                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                        return@Interceptor chain.proceed(newRequest)
                    }
                } else newRequest = chain.request()
                chain.proceed(newRequest)
            }
            val builder = OkHttpClient.Builder()
            builder.interceptors().add(interceptor)
            val client: OkHttpClient = builder.build()
            retrofit.client(client)
            val retrofit2: Retrofit = retrofit.build()
            val api = retrofit2.create(MioInterface::class.java)
* */