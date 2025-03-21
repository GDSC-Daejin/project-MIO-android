package com.gdsc.mio.navigation

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gdsc.mio.model.User
import com.gdsc.mio.R
import com.gdsc.mio.RetrofitServerConnect
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin
import com.gdsc.mio.adapter.AccountTabAdapter
import com.gdsc.mio.tabaccount.AccountReviewActivity
import com.gdsc.mio.tabaccount.AccountSettingActivity
import com.gdsc.mio.databinding.FragmentAccountBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.util.AESKeyStoreUtil
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.crypto.SecretKey

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var aBinding : FragmentAccountBinding
    private val tabTextList = listOf("게시글", "예약", "북마크")
    private var saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var email = ""
    private var myAccountData : User? = null

    private var gender : Boolean? = null //false 남, true 여
    private var accountNumber : String? = null
    private var verifySmoker : Boolean? = null //false 비흡, true 흡
    private var mannerCount = 0
    private var grade : String? = null
    private var activityLocation : String? = null

    //체크용
    private var isPolicyAllow : Boolean? = null

    private val secretKey: SecretKey by lazy {
        AESKeyStoreUtil.getOrCreateAESKey()
    }

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
        aBinding = FragmentAccountBinding.inflate(inflater, container, false)
        saveSettingData()
        initSetAccountData()

        aBinding.accountSettingIv.setOnClickListener {
            if (isPolicyAllow == true) {
                val intent = Intent(activity, AccountSettingActivity::class.java).apply {
                    putExtra("type", "ACCOUNT")
                    putExtra("accountData", email) //20201530 숫자만
                }
                requestActivity.launch(intent)
            } else {
                Toast.makeText(requireContext(), "개인정보처리방침에 동의해주세요.", Toast.LENGTH_SHORT).show()
                //saveSettingData()
            }
        }

        aBinding.accountReviewBtn.setOnClickListener {
            val intent = Intent(activity, AccountReviewActivity::class.java).apply {
                putExtra("type", "REVIEW")
                putExtra("userId", myAccountData!!.id) //4 숫자만
            }
            startActivity(intent)
        }

        aBinding.accountViewpager.adapter = AccountTabAdapter(requireActivity())

        TabLayoutMediator(aBinding.accountCategoryTabLayout, aBinding.accountViewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()

        return aBinding.root
    }

    private fun saveSettingData() {
        val sharedPref = requireActivity().getSharedPreferences("privacyPolicySettingCheck", Context.MODE_PRIVATE)
        isPolicyAllow = sharedPref.getBoolean("isPolicyAllow", false)
    }

    /*private fun initPersonalInformationConsent() {
        val sharedPref = requireActivity().getSharedPreferences("privacyPolicySettingCheck", Context.MODE_PRIVATE)
        //isPolicyAllow = sharedPref.getBoolean("isPolicyAllow", false)
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.privacy_policy_dialog_layout, null)
        val alertDialog = android.app.AlertDialog.Builder(context, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()
        val dialogContent = dialogView.findViewById<TextView>(R.id.message_text)
        val dialogLeftBtn = dialogView.findViewById<View>(R.id.dialog_left_btn)
        val dialogRightBtn =  dialogView.findViewById<View>(R.id.dialog_right_btn)

        dialogContent.setOnClickListener {
            val url = "https://sites.google.com/daejin.ac.kr/mio/%ED%99%88"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

        dialogLeftBtn.setOnClickListener {
            Toast.makeText(requireContext(), "서비스 이용이 제한될 수 있습니다.", Toast.LENGTH_SHORT).show()
            with(sharedPref.edit()) {
                putBoolean("isPolicyAllow", false)
                apply() // 비동기적으로 데이터를 저장
            }
            isPolicyAllow = false
            alertDialog.dismiss()
        }

        dialogRightBtn.setOnClickListener {
            RetrofitServerConnect.create(requireContext()).postUserAcceptPolicy(AccountStatus("APPROVED")).enqueue(object : retrofit2.Callback<User> {
                override fun onResponse(call: Call<User>, response: retrofit2.Response<User>) {
                    if (response.isSuccessful) {
                        Log.e("loginPolicy", response.code().toString())
                        Toast.makeText(requireContext(), "승인이 완료되었습니다. ${response.code()}}", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("loginPolicy", response.code().toString())
                        Log.e("loginPolicy", response.errorBody()?.string()!!)
                        Toast.makeText(requireContext(), "승인 확인 데이터 전송에 실패하였습니다. ${response.code()}}", Toast.LENGTH_SHORT).show()
                        if (loadingDialog != null && loadingDialog!!.isShowing) {
                            loadingDialog?.dismiss()
                            loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.e("loginPolicy", t.message.toString())
                    if (loadingDialog != null && loadingDialog!!.isShowing) {
                        loadingDialog?.dismiss()
                        loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                    }
                    Toast.makeText(requireContext(), "예상치 못한 오류가 발생했습니다. ${t.message}}", Toast.LENGTH_SHORT).show()
                }
            })

            with(sharedPref.edit()) {
                putBoolean("isPolicyAllow", true)
                apply() // 비동기적으로 데이터를 저장
            }
            isPolicyAllow = true
            alertDialog.dismiss()
        }
        alertDialog.show()
    }*/

    private fun initSetAccountData() {
        //여기서 기본설정들 다 넣기

        saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(activity).toString()
        aBinding.accountUserId.text = email.split("@").map { it }.first()

        LoadingProgressDialogManager.show(requireContext())

        RetrofitServerConnect.create(requireContext()).getAccountData(email).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    gender = try {
                        response.body()!!.gender
                    } catch (e : java.lang.NullPointerException) {
                        null
                    }

                    accountNumber = try {
                        response.body()!!.accountNumber
                    } catch (e : java.lang.NullPointerException) {
                        null
                    }

                    verifySmoker = try {
                        response.body()!!.verifySmoker
                    } catch (e : java.lang.NullPointerException) {
                        null
                    }

                    mannerCount = try {
                        response.body()?.mannerCount!!.toInt()
                    } catch (e : java.lang.NullPointerException) {
                        0
                    }

                    grade = try {
                        response.body()!!.grade
                    } catch (e : java.lang.NullPointerException) {
                        "F"
                    }

                    activityLocation = try {
                        response.body()!!.activityLocation
                    } catch (e : java.lang.NullPointerException) {
                        null
                    }

                    saveSharedPreferenceGoogleLogin.setUserId(requireActivity(), response.body()!!.id)
                    myAccountData = response.body()

                    if (grade != null) {
                        aBinding.accountGradeTv.text = requireActivity().getString(R.string.setUserGrade2, myAccountData!!.studentId, grade)//"${myAccountData!!.studentId}님의 현재 등급은 $grade 입니다"

                        val word = grade!!
                        val start: Int = aBinding.accountGradeTv.text.indexOf(word)
                        val end = start + word.length
                        val spannableString = SpannableString(aBinding.accountGradeTv.text) //객체 생성
                        //등급 글자의 색변경
                        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        aBinding.accountGradeTv.text = spannableString

                        CoroutineScope(Dispatchers.Main).launch {
                            val animator = ObjectAnimator.ofInt(aBinding.accountGradePb, "progress", 0, mannerCount)

                            // 애니메이션 지속 시간 설정 (예: 2초)
                            animator.duration = 1000

                            // 애니메이션 시작
                            animator.start()
                        }
                    } else {
                        aBinding.accountGradeTv.text = requireActivity().getString(R.string.setUserGrade, myAccountData!!.studentId)//"${myAccountData!!.studentId}님의 현재 등급은 B 입니다"

                        val word = "B"
                        val start: Int = aBinding.accountGradeTv.text.indexOf(word)
                        val end = start + word.length
                        val spannableString = SpannableString(aBinding.accountGradeTv.text) //객체 생성
                        //등급 글자의 색변경
                        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        aBinding.accountGradeTv.text = spannableString

                        CoroutineScope(Dispatchers.Main).launch {
                            val animator = ObjectAnimator.ofInt(aBinding.accountGradePb, "progress", 0, mannerCount)

                            // 애니메이션 지속 시간 설정 (예: 2초)
                            animator.duration = 1000

                            // 애니메이션 시작
                            animator.start()
                        }
                    }

                    if (gender != null) {
                        aBinding.accountGender.text = if (gender == true) {
                            "여성"
                        } else {
                            "남성"
                        }
                        aBinding.accountGender.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_blue_4))

                    } else {
                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity() , R.color.mio_gray_4))
                        aBinding.accountGender.text = "성별"
                        aBinding.accountGender.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_7))
                        aBinding.accountGender.backgroundTintList = colorStateList
                    }

                    if (verifySmoker != null) {
                        aBinding.accountSmokingStatus.text = if (verifySmoker == true) {
                            "흡연자"
                        } else {
                            "비흡연자"
                        }
                        aBinding.accountSmokingStatus.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_blue_4))
                    } else {
                        val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity() , R.color.mio_gray_4))
                        aBinding.accountSmokingStatus.text = "흡연여부"
                        aBinding.accountSmokingStatus.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_7))
                        aBinding.accountSmokingStatus.backgroundTintList = colorStateList
                    }

                    if (activityLocation != null) {
                        aBinding.accountAddress.text = saveSharedPreferenceGoogleLogin.getSharedArea(requireActivity(), secretKey = secretKey)
                        aBinding.accountAddress.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_7))
                        saveSharedPreferenceGoogleLogin.setArea(requireActivity(), activityLocation)
                    } else {
                        aBinding.accountAddress.text = "설정에서 개인정보를 입력해주세요"
                        aBinding.accountAddress.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_6))
                    }

                    if (accountNumber != null) {
                        aBinding.accountBank.text = "********"//accountNumber
                        aBinding.accountAddress.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_7))
                    } else {
                        aBinding.accountBank.text = ""
                    }

                    LoadingProgressDialogManager.hide()
                } else {
                    aBinding.accountGender.text = "기본 세팅"
                    aBinding.accountSmokingStatus.text = "기본 세팅"
                    aBinding.accountBank.text = "기본 세팅"
                    aBinding.accountAddress.text = "기본 세팅"

                    aBinding.accountGradeTv.text = requireActivity().getString(R.string.setUserGrade,
                        myAccountData!!.studentId
                    )//"${myAccountData!!.studentId}님의 현재 등급은 B 입니다"
                    LoadingProgressDialogManager.hide()

                    requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            Toast.makeText(requireActivity(), "계정 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                requireActivity().runOnUiThread {
                    if (isAdded && !requireActivity().isFinishing) {
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(requireActivity(), "계정 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                when (it.data?.getIntExtra("flag", -1)) {
                    //add
                    6 -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            initSetAccountData()
                        }
                        //finish()
                    }
                }
            }
        }
    }

    //클립보드에 복사하기
    /*private fun createClipData(message : String) {
        val clipManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("message", message)

        clipManager.setPrimaryClip(clipData)

        Toast.makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT).show()
    }
*/
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}