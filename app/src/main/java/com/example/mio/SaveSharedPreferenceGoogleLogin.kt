package com.example.mio

import android.content.Context
import android.content.SharedPreferences
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import androidx.preference.PreferenceManager


public class SaveSharedPreferenceGoogleLogin {
    private val PREF_USER_EMAIL = "email"
    private val acctoken = "token"
    private val expireDate = "expireDate"
    fun getSharedPreferences(ctx: Context?): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ctx!!)
    }

    // 계정 정보 저장
    fun setUserEMAIL(ctx: Context?, userName: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(PREF_USER_EMAIL, userName)
        editor.apply()
    }
    // 저장된 정보 가져오기
    fun getUserEMAIL(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(PREF_USER_EMAIL, "")
    }


    fun setToken(ctx: Context?, token: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(acctoken, token)
        editor.apply()
    }
    fun getToken(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(acctoken, "")
    }

    fun setExpireDate(ctx: Context?, expire: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(expireDate, expire!!)
        editor.apply()
    }
    fun getExpireDate(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(expireDate, "")
    }

    // 로그아웃
    fun clearUserEMAIL(ctx: Context?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.clear()
        editor.apply()
    }

}