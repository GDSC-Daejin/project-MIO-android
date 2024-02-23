package com.example.mio.Navigation

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mio.Adapter.AccountTabAdapter
import com.example.mio.Adapter.CategoryTabAdapter
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.Model.User
import com.example.mio.R
import com.example.mio.RetrofitServerConnect
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.TabAccount.AccountReviewActivity
import com.example.mio.TabAccount.AccountSettingActivity
import com.example.mio.databinding.FragmentAccountBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Objects

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
    private val tabTextList = listOf("게시글", "예약", "스크랩")
    private var saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var email = ""
    private var myAccountData : User? = null
    private var userGrade = ""

    private var gender : Boolean? = null //false 남, true 여
    private var accountNumber : String? = null
    private var verifySmoker : Boolean? = null //false 비흡, true 흡
    private var mannerCount = 0
    private var grade : String? = null
    private var activityLocation : String? = null

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

        initSetAccountData()

        aBinding.accountSettingIv.setOnClickListener {
            val intent = Intent(activity, AccountSettingActivity::class.java).apply {
                putExtra("type", "ACCOUNT")
                putExtra("accountData", email.substring(0 until 8)) //20201530 숫자만
            }
            startActivity(intent)
        }

        aBinding.accountReviewBtn.setOnClickListener {
            val intent = Intent(activity, AccountReviewActivity::class.java).apply {
                putExtra("type", "REVIEW")
                putExtra("userId", myAccountData!!.id) //4 숫자만
            }
            startActivity(intent)
        }

        aBinding.accountBank.setOnClickListener {
            createClipData(myAccountData!!.accountNumber)
        }

        aBinding.accountViewpager.adapter = AccountTabAdapter(requireActivity())

        TabLayoutMediator(aBinding.accountCategoryTabLayout, aBinding.accountViewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()


        return aBinding.root
    }

    private fun initSetAccountData() {
        //여기서 기본설정들 다 넣기

        saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(activity).toString()
        aBinding.accountUserId.text = email.substring(0..7).toString()

        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getAccountData(email).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        gender = try {
                            response.body()!!.gender
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }

                        accountNumber = try {
                            response.body()!!.accountNumber
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }

                        verifySmoker = try {
                            response.body()!!.verifySmoker
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }

                        mannerCount = try {
                            response.body()!!.mannerCount
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            0
                        }

                        grade = try {
                            response.body()!!.grade
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            "F"
                        }

                        activityLocation = try {
                            response.body()!!.activityLocation
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            null
                        }


                        //나중에 response.body()!!.mannerCount 다시 체크하기  TODO
                        println("ss")
                        saveSharedPreferenceGoogleLogin.setUserId(activity, response.body()!!.id)
                        myAccountData = response.body()

                        if (grade != null) {
                            println("mn")

                            aBinding.accountGradeTv.text = "${myAccountData!!.studentId}님의 현재 등급은 $grade 입니다"

                            if (grade != null) {
                                val word = grade!!
                                val start: Int = aBinding.accountGradeTv.text.indexOf(word)
                                val end = start + word.length
                                val spannableString = SpannableString(aBinding.accountGradeTv.text) //객체 생성
                                //등급 글자의 색변경
                                spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                aBinding.accountGradeTv.text = spannableString
                            } else {

                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(aBinding.accountGradePb, "progress", 0, mannerCount)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 1500

                                // 애니메이션 시작
                                animator.start()
                            }
                        } else {
                            println("mmc")
                            aBinding.accountGradeTv.text = "${myAccountData!!.studentId}님의 현재 등급은 F 입니다"

                            val word = "F"
                            val start: Int = aBinding.accountGradeTv.text.indexOf(word)
                            val end = start + word.length
                            val spannableString = SpannableString(aBinding.accountGradeTv.text) //객체 생성
                            //등급 글자의 색변경
                            spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            aBinding.accountGradeTv.text = spannableString

                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(aBinding.accountGradePb, "progress", 0, 0)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 2000

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
                            aBinding.accountAddress.text = activityLocation + "/"
                            aBinding.accountAddress.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_7))
                        } else {
                            aBinding.accountAddress.text = "설정에서 개인정보를 입력해주세요"
                            aBinding.accountAddress.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_6))
                        }

                        if (accountNumber != null) {
                            aBinding.accountBank.text = accountNumber
                            aBinding.accountAddress.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_gray_7))
                        } else {
                            aBinding.accountBank.text = ""
                        }


                    } else {
                        println("ff")
                        aBinding.accountGender.text = "기본 세팅"
                        aBinding.accountSmokingStatus.text = "기본 세팅"
                        aBinding.accountBank.text = "기본 세팅"
                        aBinding.accountAddress.text = "기본 세팅"

                        aBinding.accountGradeTv.text = "${myAccountData!!.studentId}님의 현재 등급은 F 입니다"


                        Log.d("add", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.d("error","error $t")
                }
            })
        }

    }

    //클립보드에 복사하기
    private fun createClipData(message : String) {
        val clipManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("message", message)

        clipManager.setPrimaryClip(clipData)

        Toast.makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

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