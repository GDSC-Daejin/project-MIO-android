package com.gdsc.mio.helper

import android.content.Context
import android.content.SharedPreferences

/*private const val PREFS_NAME = "MyAppPrefs"
private const val KEY_BEFORE_NOTIFICATION_DATA = "beforeNotificationData"*/
class SharedPref(context: Context) {
    private var mySharedPref: SharedPreferences //
    private var storeSharedPref : SharedPreferences

    private val version = "VERSION"
    init {
        mySharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE)
        storeSharedPref = context.getSharedPreferences("store_data", Context.MODE_PRIVATE)
    }

    fun getMinSupportedVersion(): String? {
        return mySharedPref.getString(version, "1.0.0")
    }


    fun setMinSupportedVersion(newVersion: String?) {
        val editor = mySharedPref.edit()
        editor.putString(version, newVersion)
        editor.apply()
    }

    /*fun getSearchData() : String {
        return mySharedPref.getString("search", "").toString()
    }*/
////////////////////////
    /*fun setNightModeState(state: Boolean?) {
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
    }*/
}