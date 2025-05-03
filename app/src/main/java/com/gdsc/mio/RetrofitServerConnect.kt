package com.gdsc.mio

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.gdsc.mio.util.AESKeyStoreUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import javax.crypto.SecretKey

object RetrofitServerConnect {
    fun create(context: Context): MioInterface {
        val secretKey: SecretKey by lazy {
            AESKeyStoreUtil.getOrCreateAESKey()
        }
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context,secretKey).toString()
        val expireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context)

        val interceptor = Interceptor { chain ->
            val newRequestBuilder = chain.request().newBuilder()

            if ((expireDate <= System.currentTimeMillis() && expireDate != 0L)) {
                saveSharedPreferenceGoogleLogin.setAppManager(context, false)

                if (!saveSharedPreferenceGoogleLogin.getAppManager(context)) {
                    saveSharedPreferenceGoogleLogin.setAppManager(context, true)
                    redirectToLogin(context)
                }
                throw IOException("Token expired")
            }

            if (token.isNotEmpty()) {
                newRequestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(newRequestBuilder.build())
        }

        val serverUrl =  BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()

        return retrofit2.create(MioInterface::class.java)
    }


    private fun redirectToLogin(context: Context) {
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(context, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
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