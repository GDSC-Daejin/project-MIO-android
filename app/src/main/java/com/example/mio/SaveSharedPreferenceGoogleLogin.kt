package com.example.mio

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


public class SaveSharedPreferenceGoogleLogin {
    private val PREF_USER_EMAIL = "email"
    private val acctoken = "token"
    private val expireDate = "expireDate"
    private val privateUserId = "userId"
    private val refreshTokenTag = "refreshToken"
    private val myAreaTag = "myArea"

    private val notificationCheck = "notificationCheck"

    private val privateProfileUserId = "profileUserId"
    private val privateUserAccountName = "privateUserAccountName"

    private val isGender = "geneder"
    private val isSchool = "school"
    private val isSmoke = "smoke"

    // SharedPreferences 키
    private val PREF_LAST_BOTTOM_SHEET_TIME = "last_bottom_sheet_time"

    //alarm key
    private val ALARM_SETTING = "alarm_setting"

    fun getAccount(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(privateUserAccountName, "")
    }


    fun setAccount(ctx: Context?, accountBank: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(privateUserAccountName, accountBank)
        editor.apply()
    }

    //알람 받을 건지 아닌지 get
    fun getSharedAlarm(ctx: Context?): Boolean {
        return getSharedPreferences(ctx).getBoolean(ALARM_SETTING, true)
    }
    //알람 set
    fun setSharedAlarm(ctx: Context?, check: Boolean) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putBoolean(ALARM_SETTING, check)
        editor.apply()
    }

    //알람의 데이터 수가 달라지는 거 확인용
    fun getSharedNotification(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(notificationCheck, "0")
    }
    //알람의 데이터 수를 저장
    fun setNotification(ctx: Context?, check: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(notificationCheck, check)
        editor.apply()
    }


    fun getSharedArea(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(myAreaTag, "")
    }

    // 필터정보저장
    fun setArea(ctx: Context?, area: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(myAreaTag, area)
        editor.apply()
    }

    fun getSharedGender(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(isGender, "")
    }

    // 필터정보저장
    fun setGender(ctx: Context?, gender: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(isGender, gender)
        editor.apply()
    }

    fun getSharedSchool(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(isSchool, "")
    }

    // 계정 정보 저장
    fun setSchool(ctx: Context?, school: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(isSchool, school)
        editor.apply()
    }
    fun getSharedSmoke(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(isSmoke, "")
    }

    // 계정 정보 저장
    fun setSmoke(ctx: Context?, smoke: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(isSmoke, smoke)
        editor.apply()
    }


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

    fun setRefreshToken(ctx: Context?, refreshToken: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(refreshTokenTag, refreshToken)
        editor.apply()
    }
    fun getRefreshToken(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(refreshTokenTag, "")
    }

    fun setExpireDate(ctx: Context?, expire: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(expireDate, expire!!)
        editor.apply()
    }
    fun getExpireDate(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(expireDate, "")
    }

    fun setUserId(ctx: Context?, userId: Int?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putInt(privateUserId, userId!!)
        editor.apply()
    }
    fun getUserId(ctx: Context?): Int? {
        return getSharedPreferences(ctx).getInt(privateUserId, 0)
    }

    fun setProfileUserId(ctx: Context?, userId: Int?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putInt(privateProfileUserId, userId!!)
        editor.apply()
    }
    fun getProfileUserId(ctx: Context?): Int? {
        return getSharedPreferences(ctx).getInt(privateProfileUserId, 0)
    }

    // 로그아웃
    fun clearUserEMAIL(ctx: Context?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.clear()
        editor.apply()
    }

    fun setLastBottomSheetTime(ctx: Context?, time: Long?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putLong(PREF_LAST_BOTTOM_SHEET_TIME, time!!)
        editor.apply()
    }

    fun getLastBottomSheetTime(ctx: Context?): Long {
        return getSharedPreferences(ctx).getLong(PREF_LAST_BOTTOM_SHEET_TIME, 0L)
    }


}