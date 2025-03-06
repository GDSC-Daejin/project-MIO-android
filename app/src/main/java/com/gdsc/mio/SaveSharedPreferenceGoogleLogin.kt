package com.gdsc.mio

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.gdsc.mio.util.AESUtil
import javax.crypto.SecretKey


class SaveSharedPreferenceGoogleLogin {
    private val prefUserEmail = "email"
    private val acctoken = "token"
    private val expireDate = "expireDate"
    private val privateUserId = "userId"
    //private val refreshTokenTag = "refreshToken"
    private val myAreaTag = "myArea"

    private val notificationCheck = "notificationCheck"

    private val privateProfileUserId = "profileUserId"
    private val privateUserAccountName = "privateUserAccountName"
    private val accountDeletionDate = "accountDeletionDate"

    /*private val isGender = "geneder"
    private val isSchool = "school"
    private val isSmoke = "smoke"*/

    // SharedPreferences 키
    private val prefLastBottomSheetTime = "last_bottom_sheet_time"

    //alarm key
    private val alarmSetting = "alarm_setting"


    fun clearUserData(ctx: Context?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.clear()
        editor.apply()
    }

    fun getDeleteAccountDate(ctx: Context?): Long {
        return getSharedPreferences(ctx).getLong(accountDeletionDate, System.currentTimeMillis())
    }
    //알람 set
    fun setDeleteAccountDate(ctx: Context?, deleteDate: Long) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putLong(accountDeletionDate, deleteDate)
        editor.apply()
    }

    fun setAccount(ctx: Context?, accountBank: String?, secretKey: SecretKey) { //은행
        val encryptedAccountBank = AESUtil.encryptAES(secretKey, accountBank ?: "")
        val formatEncrypted = "${encryptedAccountBank.first},${encryptedAccountBank.second}" // ',' 구분자로 변경
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(privateUserAccountName, formatEncrypted) // 암호화된 값을 저장
        editor.apply()
    }

    fun getAccount(ctx: Context?, secretKey: SecretKey): String? {
        val encryptedAccountBank = getSharedPreferences(ctx).getString(privateUserAccountName, "")
        return if (encryptedAccountBank.isNullOrEmpty()) {
            null
        } else {
            // ','로 암호화된 텍스트와 IV를 구분하여 가져옴
            val splitEncrypted = encryptedAccountBank.split(",")
            AESUtil.decryptAES(secretKey, splitEncrypted[0], splitEncrypted[1]) // 복호화하여 원래 값을 반환
        }
    }

    //알람 받을 건지 아닌지 get
    fun getSharedAlarm(ctx: Context?): Boolean {
        return getSharedPreferences(ctx).getBoolean(alarmSetting, true)
    }
    //알람 set
    fun setSharedAlarm(ctx: Context?, check: Boolean) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putBoolean(alarmSetting, check)
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


    fun getSharedArea(ctx: Context?, secretKey: SecretKey): String {
        val encryptedArea =  getSharedPreferences(ctx).getString(myAreaTag, "")
        return if (encryptedArea.isNullOrEmpty()) {
            ""
        } else {
            // ','로 암호화된 텍스트와 IV를 구분하여 가져옴
            val splitEncrypted = encryptedArea.split(",")
            if (splitEncrypted.size > 1) {
                AESUtil.decryptAES(secretKey, splitEncrypted[0], splitEncrypted[1]) // 복호화하여 원래 값을 반환
            } else {
                splitEncrypted.first().toString()
            }
        }
    }

    // 필터정보저장
    fun setArea(ctx: Context?, area: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(myAreaTag, area)
        editor.apply()
    }

    /*fun getSharedGender(ctx: Context?): String? {
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
    }*/


    private fun getSharedPreferences(ctx: Context?): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ctx!!)
    }

    // 계정 정보 저장
    fun setUserEMAIL(ctx: Context?, userName: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(prefUserEmail, userName)
        editor.apply()
    }
    // 저장된 정보 가져오기
    fun getUserEMAIL(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(prefUserEmail, "")
    }


    fun setToken(ctx: Context?, token: String?, secretKey : SecretKey) {
        val encryptedTk = AESUtil.encryptAES(secretKey, token ?: "")
        val formatEncrypted = "${encryptedTk.first},${encryptedTk.second}" // ',' 구분자로 변경
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(acctoken, formatEncrypted)
        editor.apply()
    }
    fun getToken(ctx: Context?, secretKey: SecretKey): String? {
        val encryptedTk = getSharedPreferences(ctx).getString(acctoken, "")
        return if (encryptedTk.isNullOrEmpty()) {
            null
        } else {
            // ','로 암호화된 텍스트와 IV를 구분하여 가져옴
            val splitEncrypted = encryptedTk.split(",")
            AESUtil.decryptAES(secretKey, splitEncrypted[0], splitEncrypted[1]) // 복호화하여 원래 값을 반환
        }
    }

    /*fun setRefreshToken(ctx: Context?, refreshToken: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(refreshTokenTag, refreshToken)
        editor.apply()
    }
    fun getRefreshToken(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(refreshTokenTag, "")
    }*/

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
    fun getUserId(ctx: Context?): Int {
        return getSharedPreferences(ctx).getInt(privateUserId, -1)
    }

    fun setProfileUserId(ctx: Context?, userId: Int?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putInt(privateProfileUserId, userId!!)
        editor.apply()
    }
    fun getProfileUserId(ctx: Context?): Int {
        return getSharedPreferences(ctx).getInt(privateProfileUserId, 0)
    }

    fun setLastBottomSheetTime(ctx: Context?, time: Long?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putLong(prefLastBottomSheetTime, time!!)
        editor.apply()
    }

    fun getLastBottomSheetTime(ctx: Context?): Long {
        return getSharedPreferences(ctx).getLong(prefLastBottomSheetTime, 0L)
    }


}