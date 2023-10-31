package com.example.mio.Helper

import android.content.Context
import android.content.SharedPreferences
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.NotificationData
import com.example.mio.Model.SearchWordData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException

private const val PREFS_NAME = "MyAppPrefs"
private const val KEY_BEFORE_NOTIFICATION_DATA = "beforeNotificationData"
class SharedPref(context: Context) {
    var mySharedPref: SharedPreferences //
    var storeSharedPref : SharedPreferences
    init {
        mySharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE)
        storeSharedPref = context.getSharedPreferences("store_data", Context.MODE_PRIVATE)
    }

    //만약 너무 커져서 용량이 부족해지거나 어떤 문제가 생기면 파일시스템의 내부저장소 저장을 이용하기기
   /*fun setNotify(context: Context, key: String, values: ArrayList<AddAlarmResponseData>) {
        //val prefs: SharedPreferences = storeSharedPref
        val editor = storeSharedPref.edit()
        val data : JSONArray = JSONArray()

        for (i in 0 until values.size) {
            data.put(values[i].post)
            data.put(values[i].id)
            data.put(values[i].content)
            data.put(values[i].userEntity)
            data.put(values[i].createDate)
        }
        if (values.isNotEmpty()) {
            editor.putString(key, data.toString())
        } else {
            editor.putString(key, null)
        }
        editor.apply()
    }

    fun getNotify(context: Context, key: String) : MutableList<String> {
        //val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val json = storeSharedPref.getString(key, null)
        val historyArr : ArrayList<String> = ArrayList()
        if (json != null) {
            try {
                val data : JSONArray = JSONArray(json)
                for (i in 0 until data.length()) {
                    val s = data.optString(i)
                    historyArr.add(s)
                }
            } catch (e : JSONException) {
                e.printStackTrace()
            }
        }
        return historyArr
    }*/


    // 함수: beforeNotificationAllData를 SharedPreferences에 저장
    fun setNotify(context: Context, data: List<AddAlarmResponseData>) {
        // SharedPreferences 초기화
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Gson을 사용하여 데이터를 JSON 문자열로 직렬화
        val gson = Gson()
        val dataAsString = gson.toJson(data)

        // 데이터 저장
        val editor = prefs.edit()
        editor.putString(KEY_BEFORE_NOTIFICATION_DATA, dataAsString)
        editor.apply()
    }

    // 함수: SharedPreferences에서 beforeNotificationAllData 로드
    fun getNotify(context: Context): List<AddAlarmResponseData> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // SharedPreferences에서 저장된 JSON 문자열을 읽어와서 역직렬화
        val gson = Gson()
        val dataAsString = prefs.getString(KEY_BEFORE_NOTIFICATION_DATA, null)
        if (dataAsString != null) {
            val type = object : TypeToken<List<AddAlarmResponseData>>() {}.type
            return gson.fromJson(dataAsString, type)
        }

        // 저장된 데이터가 없으면 빈 리스트 반환
        return emptyList()
    }

    fun setSearchData(searchText : String) {
        val editor = mySharedPref.edit()
        editor.putString("search", searchText)
        editor.apply()
    }

    fun getSearchData() : String {
        return mySharedPref.getString("search", "").toString()
    }
////////////////////////
    fun setNightModeState(state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean("NightMode", state!!)
        editor.apply()
    }

    fun loadNightModeState(): Boolean {
        return mySharedPref.getBoolean("NightMode", false)
    }

    fun setSmallModeState(state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean("SmallMode", state!!)
        editor.apply()
    }

    fun loadSmallModeState(): Boolean {
        return mySharedPref.getBoolean("SmallMode", false)
    }

    fun setMiddleModeState(state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean("MiddleMode", state!!)
        editor.apply()
    }

    fun loadMiddleModeState(): Boolean {
        return mySharedPref.getBoolean("MiddleMode", false)
    }

    fun setLargeModeState(state: Boolean?) {
        val editor = mySharedPref.edit()
        editor.putBoolean("LargeMode", state!!)
        editor.apply()
    }

    fun loadLargeModeState(): Boolean {
        return mySharedPref.getBoolean("LargeMode", false)
    }

    fun setSearchHistory(context: Context, key: String, values: ArrayList<SearchWordData>) {
        //val prefs: SharedPreferences = storeSharedPref
        val editor = storeSharedPref.edit()
        val data : JSONArray = JSONArray()

        for (i in 0 until values.size) {
            data.put(values[i]!!.searchWordText)
        }
        if (values.isNotEmpty()) {
            editor.putString(key, data.toString())
        } else {
            editor.putString(key, null)
        }
        editor.apply()
    }

    fun getSearchHistory(context: Context, key: String) : ArrayList<String> {
        //val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val json = storeSharedPref.getString(key, null)
        val historyArr : ArrayList<String> = ArrayList()
        if (json != null) {
            try {
                val data : JSONArray = JSONArray(json)
                for (i in 0 until data.length()) {
                    val s = data.optString(i)
                    historyArr.add(s)
                }
            } catch (e : JSONException) {
                e.printStackTrace()
            }
        }

        return historyArr
    }
}