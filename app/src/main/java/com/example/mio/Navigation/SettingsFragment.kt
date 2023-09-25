package com.example.mio.Navigation

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.mio.R

class SettingsFragment : PreferenceFragmentCompat() {
    private var alarmSettingPreference : Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // rootkey가 null이라면
        if (rootKey == null) {
            // Preference 객체들 초기화
            alarmSettingPreference = findPreference("alarmReceive")
        }

        alarmSettingPreference!!.setOnPreferenceChangeListener { preference, newValue ->
            var check = false
            if (newValue as Boolean) {
                check = newValue
            }

            if (check) {
                Toast.makeText(requireActivity(), "${check}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireActivity(), "${check}", Toast.LENGTH_SHORT).show()
            }

            return@setOnPreferenceChangeListener true
        }
    }

    override fun onResume() {
        super.onResume()

        // SharedPreferences에서 SwitchPreferenceCompat 값을 가져옵니다.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isAlarmEnabled = sharedPreferences.getBoolean("alarmReceive", true)

        // isAlarmEnabled를 사용하여 해당 스위치 설정에 대한 작업을 수행합니다.
        if (isAlarmEnabled) {
            // 스위치가 활성화되어 있을 때 수행할 작업
        } else {
            // 스위치가 비활성화되어 있을 때 수행할 작업
        }
    }
}