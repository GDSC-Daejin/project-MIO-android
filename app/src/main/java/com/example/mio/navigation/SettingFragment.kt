package com.example.mio.navigation

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mio.OpenSourceManagementActivity
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.FragmentSettingBinding
import com.example.mio.sse.SSEForegroundService
import com.example.mio.viewmodel.SharedViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

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

    private lateinit var sharedViewModel : SharedViewModel

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

        binding.enableFeature.isChecked = sharedPreference.getSharedAlarm(requireActivity())

        binding.enableFeature.setOnCheckedChangeListener { _, check ->
            sharedPreference.setSharedAlarm(requireActivity(), check)
            if (sharedPreference.getSharedAlarm(requireActivity())) {
                requestIgnoreBatteryOptimization()
                /*Log.e("switch", "start service")
                //foreground실행행
                serviceIntent =
                    Intent(requireActivity(), SSEForegroundService::class.java) // MyBackgroundService 를 실행하는 인텐트 생성


                //절전사용금지앱
                val pm = requireActivity().applicationContext.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
                var isWhiteListing = false
                isWhiteListing = pm.isIgnoringBatteryOptimizations(requireActivity().applicationContext.packageName)
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
                }*/
            }
        }

        binding.policyIntent.setOnClickListener {
            val url = "https://github.com/MIO-Privacy-Policy-for-Android/MIO_Privacy_Policy_for_Android"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
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

    private fun requestIgnoreBatteryOptimization() {//절전사용금지앱
        val pm = requireActivity().applicationContext.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        val isWhiteListing: Boolean = pm.isIgnoringBatteryOptimizations(requireActivity().applicationContext.packageName)
        if (!isWhiteListing) {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("배터리 최적화 제외 요청")
                setMessage("정상적인 알림을 수신하기 위해 배터리 사용량 최적화 목록에서 제외해야 합니다. 제외하시겠습니까?")
                setPositiveButton("권한 허용") { _, _ ->
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:" + requireActivity().applicationContext.packageName)
                    sharedPreference.setSharedAlarm(requireActivity(), true)
                    sseStartCheck()
                    startActivity(intent)
                }
                setNegativeButton("취소") { dialog, _ ->
                    // 배터리 최적화 제외 권한이 거부되었습니다.
                    Toast.makeText(requireActivity(), "배터리 최적화 제외가 거부되었습니다", Toast.LENGTH_SHORT).show()
                    sharedPreference.setSharedAlarm(requireActivity(), false)
                    dialog.dismiss()
                }
                create()
                show()
            }
        }
    }
    private fun sseStartCheck() {
        //foreground실행행
        serviceIntent =
            Intent(requireActivity(), SSEForegroundService::class.java) // MyBackgroundService 를 실행하는 인텐트 생성

        if (!foregroundServiceRunning()) { // 이미 작동중인 동일한 서비스가 없다면 실행
            serviceIntent =
                Intent(requireActivity(), SSEForegroundService::class.java) // MyBackgroundService 를 실행하는 인텐트 생성
            // 빌드 버전코드 "O" 보다 높은 버전일 경우
            requireActivity().startService(serviceIntent) // 서비스 인텐트를 전달한 서비스 시작 메서드 실행
        } else {
            serviceIntent = SSEForegroundService().serviceIntent
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    private fun foregroundServiceRunning(): Boolean {
        val activityManager =
            requireActivity().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager // 액티비티 매니져를 통해 작동중인 서비스 가져오기

        for (service in activityManager.runningAppProcesses) { // 작동중인 서비스수 만큼 반복
            if (SSEForegroundService::class.java.name == service.processName) { // 비교한 서비스의 이름이 MyForgroundService 와 같다면
                return true // true 반환
            }
        }
        return false // 기본은 false 로 설정
    }

    override fun onStart() {
        super.onStart()
        view?.post {
            val currentActivity = activity ?: return@post
            if (isAdded) {
                currentActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        isEnabled = false
                        sharedViewModel.setNotificationType("알림")
                        currentActivity.supportFragmentManager.popBackStack()
                    }
                })
            }
        }
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