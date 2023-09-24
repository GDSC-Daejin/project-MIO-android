package com.example.mio.Navigation

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        aBinding.accountViewpager.adapter = AccountTabAdapter(requireActivity())

        TabLayoutMediator(aBinding.accountCategoryTabLayout, aBinding.accountViewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()


        return aBinding.root
    }

    private fun initSetAccountData() {
        //여기서 기본설정들 다 넣기

        saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(activity)!!.toString()

        aBinding.accountUserId.text = email

        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getAccountData(email).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        //나중에 response.body()!!.mannerCount 다시 체크하기 Todo
                        println("ss")
                        saveSharedPreferenceGoogleLogin.setUserId(activity, response.body()!!.id)
                        myAccountData = response.body()

                        if (response.body()!!.mannerCount == null) {
                            println("mn")
                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(aBinding.accountGradePb, "progress", 0, 20)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 1500

                                // 애니메이션 시작
                                animator.start()
                            }
                        } else {
                            println("mmc")
                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(aBinding.accountGradePb, "progress", 0, response.body()!!.mannerCount)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 2000

                                // 애니메이션 시작
                                animator.start()
                            }
                        }


                    } else {
                        println("ff")
                        println("e" + response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.d("error","error $t")
                }
            })
        }

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