package com.example.mio.navigation

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mio.MainActivity
import com.example.mio.OpenSourceManagementActivity
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.FragmentSettingBinding
import com.example.mio.sse.SSEForegroundService
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentSettingBinding
    private var sharedPreference = SaveSharedPreferenceGoogleLogin()
    private var serviceIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        binding.enableFeature.setOnCheckedChangeListener { compoundButton, check ->
            sharedPreference.setSharedAlarm(requireActivity(), check)
            Log.e("switch", sharedPreference.getSharedAlarm(requireActivity()).toString())
            Log.e("switch", check.toString())
            if (sharedPreference.getSharedAlarm(requireActivity())) {
                Log.e("switch", "start service")
                //foreground실행행
                serviceIntent =
                    Intent(requireActivity(), SSEForegroundService::class.java) // MyBackgroundService 를 실행하는 인텐트 생성


                //절전사용금지앱
                val pm = requireActivity().applicationContext.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
                var isWhiteListing = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isWhiteListing = pm.isIgnoringBatteryOptimizations(requireActivity().applicationContext.packageName)
                }
                if (!isWhiteListing) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:" + requireActivity().applicationContext.packageName)
                    startActivity(intent)
                }

                if (!foregroundServiceRunning()) { // 이미 작동중인 동일한 서비스가 없다면 실행
                    serviceIntent =
                        Intent(requireActivity(), SSEForegroundService::class.java) // MyBackgroundService 를 실행하는 인텐트 생성
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 빌드 버전코드 "O" 보다 높은 버전일 경우
                        requireActivity().startService(serviceIntent) // 서비스 인텐트를 전달한 서비스 시작 메서드 실행
                    }
                } else {
                    serviceIntent = SSEForegroundService().serviceIntent
                }
            }
        }


        binding.openSourceLicense.setOnClickListener {
            val intent = Intent(requireActivity(), OssLicensesMenuActivity::class.java)

            startActivity(intent)

            OssLicensesMenuActivity.setActivityTitle("오픈소스 라이선스")
        }

        binding.openSourceLicense2.setOnClickListener {
            val intent = Intent(requireActivity(), OpenSourceManagementActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().let { currentActivity ->
            // 뒤로가기 콜백 설정
            currentActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // 현재 액티비티가 MainActivity인지 확인
                    if (currentActivity is MainActivity) {
                        // SettingFragment를 종료할지 HomeFragment로 이동할지 결정하는 로직
                        val fragmentManager = currentActivity.supportFragmentManager
                        val homeFragment = fragmentManager.findFragmentByTag(HomeFragment::class.java.simpleName)

                        if (homeFragment == null) {
                            // HomeFragment가 존재하지 않으면 HomeFragment로 이동
                            currentActivity.changeFragment(HomeFragment())
                            currentActivity.toolbarType = "기본"
                            currentActivity.setToolbarView("기본")
                            currentActivity.isClicked = false
                            currentActivity.isSettingClicked = false
                            currentActivity.mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home
                        } else {
                            // HomeFragment가 이미 존재하면 SettingFragment 종료
                            fragmentManager.popBackStack() // 현재 프래그먼트를 스택에서 제거
                        }
                    }
                }
            })
        }
    }

    private fun foregroundServiceRunning(): Boolean {
        val activityManager =
            requireActivity().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager // 액티비티 매니져를 통해 작동중인 서비스 가져오기

        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) { // 작동중인 서비스수 만큼 반복
            if (SSEForegroundService::class.java.name == service.service.className) { // 비교한 서비스의 이름이 MyForgroundService 와 같다면
                return true // true 반환
            }
        }
        return false // 기본은 false 로 설정
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}