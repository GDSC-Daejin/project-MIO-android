package com.example.mio.Navigation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.example.mio.Helper.NotificationHelper
import com.example.mio.OpenSourceManagementActivity
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class SettingsFragment : PreferenceFragmentCompat() {
    private var alarmSettingPreference : Preference? = null
    private var openSourceLicensePreference : Preference? = null
    private var openSourceLicensePreference2 : Preference? = null

    private var sharedPreference = SaveSharedPreferenceGoogleLogin()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // rootkey가 null이라면
        if (rootKey == null) {
            // Preference 객체들 초기화
            alarmSettingPreference = findPreference("alarmReceive")
            openSourceLicensePreference = findPreference("openSource_license")
            openSourceLicensePreference2 = findPreference("openSource_license2")

            if (sharedPreference.getSharedAlarm(requireActivity())) {
                alarmSettingPreference?.setDefaultValue(sharedPreference.getSharedAlarm(requireActivity()))
            } else {
                alarmSettingPreference?.setDefaultValue(false)
            }
        }


        alarmSettingPreference!!.setOnPreferenceChangeListener { preference, newValue ->
            var check = false
            if (newValue as Boolean) {
                check = newValue
            }
            sharedPreference.setSharedAlarm(requireActivity(), check)
            if (check) {
                Toast.makeText(requireActivity(), "${check}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireActivity(), "${check}", Toast.LENGTH_SHORT).show()
            }

            return@setOnPreferenceChangeListener true
        }

        openSourceLicensePreference!!.setOnPreferenceClickListener {
            val intent = Intent(requireActivity(), OssLicensesMenuActivity::class.java)

            startActivity(intent)

            OssLicensesMenuActivity.setActivityTitle("오픈소스 라이선스")

            return@setOnPreferenceClickListener true
        }

        openSourceLicensePreference2!!.setOnPreferenceClickListener {
            val intent = Intent(requireActivity(), OpenSourceManagementActivity::class.java)
            startActivity(intent)

            return@setOnPreferenceClickListener true
        }


    }

    override fun onResume() {
        super.onResume()

        // SharedPreferences에서 SwitchPreferenceCompat 값을 가져옵니다.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isAlarmEnabled = sharedPreferences.getBoolean("alarmReceive", true)

        // isAlarmEnabled를 사용하여 해당 스위치 설정에 대한 작업을 수행합니다.
        if (isAlarmEnabled) {
            println("switch changed $isAlarmEnabled")
            val deleteAlarm = NotificationHelper(requireActivity())
            deleteAlarm.getManager()
        } else {
            // 스위치가 비활성화되어 있을 때 수행할 작업 channelID
            println("switch changed $isAlarmEnabled")
            val deleteAlarm = NotificationHelper(requireActivity())
            deleteAlarm.deleteChannel()
        }
    }
}