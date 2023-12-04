package com.example.mio

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val parentContext = context
    private val sharedPreferences = context.getSharedPreferences("TokenData", Context.MODE_PRIVATE)
    private val sg = SaveSharedPreferenceGoogleLogin()
    private val sp = sg.getToken(context).toString()
    private val expiredException = sg.getExpireDate(context)?.toString()


    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = sharedPreferences.getString("accessToken", "") ?: ""
        println("idToken" + accessToken)
        println("test" + sp)
        println("expired"+expiredException?.toLong())
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $sp")
            .build()
        val response = chain.proceed(request)
        //Error opening kernel wakelock stats for: wakeup34: Permission denied 오류 수정
        if (expiredException?.toLong()!! <= System.currentTimeMillis()) {
            /*Toast.makeText(parentContext, "사용자 정보가 유효하지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()*/
            Log.e("REFRESH ERROR", "EXPIRED")
            /*val intent = Intent(parentContext, LoginActivity::class.java)
            parentContext.startActivity(intent)*/
        }
        return response
    }
}