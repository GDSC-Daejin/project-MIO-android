package com.gdsc.mio.navigation

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.gdsc.mio.*
import com.gdsc.mio.databinding.FragmentSettingBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.User
import com.gdsc.mio.sse.SSEForegroundService
import com.gdsc.mio.viewmodel.SharedViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    @RequiresApi(Build.VERSION_CODES.R)
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
            }
        }

        binding.policyIntent.setOnClickListener {
            val url = "https://github.com/MIO-Privacy-Policy-for-Android/MIO_Privacy_Policy"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        binding.termsIntent.setOnClickListener {
            val url = "https://github.com/MIO-Privacy-Policy-for-Android/MIO_Terms_Of_Service"
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

        binding.accountCancellation.setOnClickListener {
            LoadingProgressDialogManager.show(requireContext())
            val layoutInflater = LayoutInflater.from(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.account_cancellation_dialog_layout, null)
            val alertDialog = android.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(dialogView)
                .create()
            val dialogCancelBtn = dialogView.findViewById<View>(R.id.dialog_left_btn)
            val dialogAcceptBtn =  dialogView.findViewById<View>(R.id.dialog_right_btn)

            val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val rect = windowManager.currentWindowMetrics.bounds

            val window = alertDialog.window
            val x = (rect.width() * 0.8f).toInt()
            val y = (rect.height() * 0.4f).toInt()

            window?.setLayout(x, y)

            // 다이얼로그 창 크기 조정
            /*alertDialog.window?.let { window ->
                val layoutParams = window.attributes
                layoutParams.width = (resources.displayMetrics.widthPixels * 1f).toInt() // 80%의 너비로 설정
                window.attributes = layoutParams
            }*/

            dialogCancelBtn.setOnClickListener {
                LoadingProgressDialogManager.hide()
                alertDialog.dismiss()
            }


            dialogAcceptBtn.setOnClickListener {
                RetrofitServerConnect.create(requireContext()).deleteAccountData(sharedPreference.getUserId(requireContext())).enqueue(object :
                    Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            LoadingProgressDialogManager.hide()
                            Toast.makeText(requireContext(), "MIO를 이용해 주셔서 감사합니다.", Toast.LENGTH_SHORT).show()

                            // SharedPreference에서 사용자 정보를 삭제하고 로그아웃 처리
                            sharedPreference.clearUserData(requireContext())

                            sharedPreference.setUserId(requireContext(), -1)
                            sharedPreference.setDeleteAccountDate(requireContext(), System.currentTimeMillis())

                            // 로그인 화면으로 이동 및 모든 스택 제거
                            val loginIntent = Intent(requireContext(), LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(loginIntent)
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(requireContext(), "탈퇴 요청에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                })
                alertDialog.dismiss()
            }
            alertDialog.setOnDismissListener {
                // 다이얼로그가 종료된 후 처리할 동작
                LoadingProgressDialogManager.hide()
            }

            alertDialog.show()
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

    private fun isIgnoringBatteryOptimization(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = requireActivity().applicationContext.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(requireActivity().applicationContext.packageName)
        } else {
            // M 이하 버전은 배터리 최적화 기능이 없으므로 항상 true
            true
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // TIRAMISU 미만은 권한이 필요하지 않음 (자동 허용됨)
            true
        }
    }

    override fun onResume() {
        super.onResume()

        // 알림 권한 확인
        if (!isNotificationPermissionGranted()) {
            sharedPreference.setSharedAlarm(requireContext(), false)
        }

        // 배터리 최적화 제외 확인
        if (!isIgnoringBatteryOptimization()) {
            sharedPreference.setSharedAlarm(requireContext(), false)
        }

        val isAlarm = sharedPreference.getSharedAlarm(requireContext())

        if (isAlarm) {
            sseStartCheck()
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