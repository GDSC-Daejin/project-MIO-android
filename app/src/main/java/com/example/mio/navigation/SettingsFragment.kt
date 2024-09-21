package com.example.mio.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.mio.OpenSourceManagementActivity
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var switchPreferenceCompat: SwitchPreferenceCompat
    private var openSourceLicensePreference : Preference? = null
    private var openSourceLicensePreference2 : Preference? = null

    private var sharedPreference = SaveSharedPreferenceGoogleLogin()
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Preference 객체들 초기화
        openSourceLicensePreference = findPreference("openSource_license")
        openSourceLicensePreference2 = findPreference("openSource_license2")
        switchPreferenceCompat = findPreference("enable_feature")!!

        // 사용자 정의 레이아웃에서 `Switch`를 설정합니다.
        /*val customSwitch = requireActivity().findViewById<Switch>(R.id.yourSwitchCompat)

        customSwitch?.setOnCheckedChangeListener { _, isChecked ->
            // `SwitchPreferenceCompat`의 상태를 업데이트합니다.
            switchPreferenceCompat.isChecked = isChecked
            Toast.makeText(requireContext(), "Feature Enabled switch: $isChecked", Toast.LENGTH_SHORT).show()
            sharedPreference.setSharedAlarm(requireActivity(), isChecked)

            Log.e("switch", sharedPreference.getSharedAlarm(requireActivity()).toString())
        }*/
        switchPreferenceCompat = findPreference("enable_feature")!!
        val customSwitch = requireActivity().findViewById<SwitchCompat>(R.id.yourSwitchCompat)

        // 스위치 상태 동기화
        customSwitch.isChecked = switchPreferenceCompat.isChecked
        customSwitch.setOnCheckedChangeListener { _, isChecked ->
            // `SwitchPreferenceCompat`의 상태를 업데이트합니다.
            switchPreferenceCompat.isChecked = isChecked
            Toast.makeText(requireContext(), "Feature Enabled switch: $isChecked", Toast.LENGTH_SHORT).show()
            sharedPreference.setSharedAlarm(requireActivity(), isChecked)
            Log.e("switch", sharedPreference.getSharedAlarm(requireActivity()).toString())
        }

        // 스위치 상태 변경 리스너를 설정합니다.
        switchPreferenceCompat.setOnPreferenceChangeListener { _, newValue ->
            val isEnabled = newValue as Boolean
            // 스위치 상태가 변경될 때 수행할 작업
            Toast.makeText(requireContext(), "Feature Enabled: $isEnabled", Toast.LENGTH_SHORT).show()
            customSwitch?.isChecked = isEnabled
            sharedPreference.setSharedAlarm(requireActivity(), isEnabled)

            Log.e("switch", sharedPreference.getSharedAlarm(requireActivity()).toString())
            true
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
    }
}