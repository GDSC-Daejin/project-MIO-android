package com.gdsc.mio.sse

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferenceChangeObserver(
    private val context: Context,
    private val onAlarmSettingChanged: (Boolean) -> Unit
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun register() {
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    fun unregister() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "alarm_setting") {
            val isAlarmOn = sharedPreferences?.getBoolean("alarm_setting", true)
            onAlarmSettingChanged(isAlarmOn!!)
        }
    }
}
