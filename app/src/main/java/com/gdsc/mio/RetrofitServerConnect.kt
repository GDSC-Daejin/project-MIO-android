package com.gdsc.mio

import android.content.Context
import android.util.Log
import com.gdsc.mio.model.LoginResponsesData
import com.gdsc.mio.model.RefreshTokenRequest
import com.gdsc.mio.util.AESKeyStoreUtil
import kotlinx.coroutines.runBlocking
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
            if (expireDate <= System.currentTimeMillis() && expireDate != 0L) {

                // RefreshToken을 사용하여 새로운 accessToken 요청
                val refreshToken = saveSharedPreferenceGoogleLogin.getRefreshToken(context, secretKey)
                val newLoginResponsesData = runBlocking {
                    // Refresh Token을 사용하여 새로운 Access Token 받아오는 비동기 호출
                    refreshToken?.let {
                        refreshLoginWithRefreshToken(it, context)
                    }
                }

                if (newLoginResponsesData != null) {
                    // 새로운 값 저장
                    saveSharedPreferenceGoogleLogin.setToken(context, newLoginResponsesData.accessToken, secretKey)
                    saveSharedPreferenceGoogleLogin.setExpireDate(context, newLoginResponsesData.accessTokenExpiresIn)
                    saveSharedPreferenceGoogleLogin.setRefreshToken(context, newLoginResponsesData.refreshToken, secretKey)
                    // 새로운 Access Token으로 Authorization 헤더 갱신
                    newRequestBuilder.addHeader("Authorization", "Bearer ${newLoginResponsesData.accessToken}")
                } else {
                    // RefreshToken 실패 시
                    throw IOException("Failed to refresh the token")
                }
            } else {
                // 토큰이 유효하면 Authorization 헤더에 포함
                if (token.isNotEmpty()) {
                    newRequestBuilder.addHeader("Authorization", "Bearer $token")
                }
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
}

private suspend fun refreshLoginWithRefreshToken(refreshToken: String, context: Context): LoginResponsesData? {
    return try {
        val response = RetrofitServerConnect.create(context)
            .refreshLogin(RefreshTokenRequest(refreshToken))

        if (response.isSuccessful) {
            LoginResponsesData(
                grantType = "",
                accessToken = response.body()?.accessToken ?: "",
                refreshToken = response.body()?.refreshToken ?: "",
                accessTokenExpiresIn = response.body()?.accessTokenExpiresIn ?: 0L
            )
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
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