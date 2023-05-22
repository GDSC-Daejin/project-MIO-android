package com.example.mio

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


public class SaveSharedPreferenceGoogleLogin {
    private val PREF_USER_EMAIL = "email"
    public fun getSharedPreferences(ctx: Context?): SharedPreferences {
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

    // 로그아웃
    fun clearUserEMAIL(ctx: Context?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.clear()
        editor.apply()
    }

}