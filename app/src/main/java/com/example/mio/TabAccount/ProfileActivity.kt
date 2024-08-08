package com.example.mio.TabAccount

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.mio.Adapter.AccountTabAdapter
import com.example.mio.Adapter.ProfileTabAdapter
import com.example.mio.Model.User
import com.example.mio.R
import com.example.mio.RetrofitServerConnect
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.ActivityProflieBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//남의 프로필 보는 activity

class ProfileActivity : AppCompatActivity() {
    private lateinit var pBinding : ActivityProflieBinding
    private val tabTextList = listOf("게시글", "후기")
    private var profileId = 0
    private var profileData : User? = null
    private var userGrade : String? = null
    private var mannerCount : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pBinding = ActivityProflieBinding.inflate(layoutInflater)

        setProfile()

        setContentView(pBinding.root)

        pBinding.profileViewpager.adapter = ProfileTabAdapter(this)

        TabLayoutMediator(pBinding.profileCategoryTabLayout, pBinding.profileViewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()

        pBinding.backArrow.setOnClickListener {
            this.finish()
        }
    }

    private fun setProfile() {
        profileId =  intent.getIntExtra("studentId", 0)
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(this@ProfileActivity).getUserProfileData(profileId).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {

                    if (response.isSuccessful) {

                        println("scssucsucsucs")

                        profileData = response.body()

                        userGrade = try {
                            response.body()!!.grade
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            "F"
                        }

                        mannerCount = try {
                            response.body()!!.mannerCount
                        } catch (e : java.lang.NullPointerException) {
                            Log.d("null", e.toString())
                            0
                        }


                        println(profileData)

                        saveSharedPreferenceGoogleLogin.setProfileUserId(this@ProfileActivity, profileId)

                        pBinding.profileTopTv.text = "${profileData!!.studentId}님의 프로필"
                        pBinding.profileUserGrade.text = "${profileData!!.studentId}님의 현재 등급은 $userGrade 입니다"

                        if (userGrade != null) {
                            println("mn")

                            pBinding.profileUserGrade.text = "${profileData!!.studentId}님의 현재 등급은 $userGrade 입니다"

                            if (userGrade != null) {
                                val word = userGrade!!
                                val start: Int = pBinding.profileUserGrade.text.indexOf(word)
                                val end = start + word.length
                                val spannableString = SpannableString(pBinding.profileUserGrade.text) //객체 생성
                                //등급 글자의 색변경
                                spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                pBinding.profileUserGrade.text = spannableString
                            } else {

                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(pBinding.profileGradePb, "progress", 0, mannerCount!!)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 1500

                                // 애니메이션 시작
                                animator.start()
                            }
                        } else {
                            println("mmc")
                            pBinding.profileUserGrade.text = "${profileData!!.studentId}님의 현재 등급은 F 입니다"

                            val word = "F"
                            val start: Int = pBinding.profileUserGrade.text.indexOf(word)
                            val end = start + word.length
                            val spannableString = SpannableString(pBinding.profileUserGrade.text) //객체 생성
                            //등급 글자의 색변경
                            spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            pBinding.profileUserGrade.text = spannableString

                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(pBinding.profileGradePb, "progress", 0, 0)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 2000

                                // 애니메이션 시작
                                animator.start()
                            }
                        }

                    } else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }

    }
}