package com.example.mio.tabaccount

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mio.R
import com.example.mio.adapter.ProfileTabAdapter
import com.example.mio.model.User
import com.example.mio.RetrofitServerConnect
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
        setContentView(pBinding.root)

        profileId =  intent.getIntExtra("studentId", 0)
        setProfile()

        pBinding.profileViewpager.adapter = ProfileTabAdapter(this)

        TabLayoutMediator(pBinding.profileCategoryTabLayout, pBinding.profileViewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()

        pBinding.backArrow.setOnClickListener {
            this.finish()
        }


    }

    private fun setProfile() {

        RetrofitServerConnect.create(this@ProfileActivity).getUserProfileData(profileId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {

                if (response.isSuccessful) {

                    profileData = response.body()

                    if (profileData?.name != "(알 수 없음)") {
                        userGrade = try {
                            response.body()!!.grade
                        } catch (e : java.lang.NullPointerException) {
                            "B"
                        }

                        mannerCount = try {
                            response.body()!!.mannerCount
                        } catch (e : java.lang.NullPointerException) {
                            0
                        }

                        pBinding.profileTopTv.text = getString(R.string.setUserDescribe, profileData?.studentId)//"${profileData!!.studentId}님의 프로필"
                        pBinding.profileUserGrade.text = getString(R.string.setUserGrade2, profileData?.studentId, "$userGrade")//"${profileData!!.studentId}님의 현재 등급은 $userGrade 입니다"

                        if (userGrade != null) {
                            pBinding.profileUserGrade.text = getString(R.string.setUserGrade2, profileData?.studentId, "$userGrade")

                            if (userGrade != null) {
                                val word = userGrade!!
                                val start: Int = pBinding.profileUserGrade.text.indexOf(word)
                                val end = start + word.length
                                val spannableString = SpannableString(pBinding.profileUserGrade.text) //객체 생성
                                //등급 글자의 색변경
                                spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                pBinding.profileUserGrade.text = spannableString
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(pBinding.profileGradePb, "progress", 0, mannerCount!!)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 1500

                                // 애니메이션 시작
                                animator.start()
                            }
                        } else {
                            pBinding.profileUserGrade.text = getString(R.string.setUserGrade2, profileData?.studentId, "$userGrade")

                            val word = "B"
                            val start: Int = pBinding.profileUserGrade.text.indexOf(word)
                            val end = start + word.length
                            val spannableString = SpannableString(pBinding.profileUserGrade.text) //객체 생성
                            //등급 글자의 색변경
                            spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0046CC")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            pBinding.profileUserGrade.text = spannableString

                            CoroutineScope(Dispatchers.Main).launch {
                                val animator = ObjectAnimator.ofInt(pBinding.profileGradePb, "progress", 0, mannerCount!!)

                                // 애니메이션 지속 시간 설정 (예: 2초)
                                animator.duration = 1500

                                // 애니메이션 시작
                                animator.start()
                            }
                        }
                    } else { //탈퇴한 사용자
                        Toast.makeText(this@ProfileActivity, "탈퇴한 사용자입니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "사용자 정보를 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("error", t.toString())
                Toast.makeText(this@ProfileActivity, "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }
}