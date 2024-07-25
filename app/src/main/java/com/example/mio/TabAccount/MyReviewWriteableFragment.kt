package com.example.mio.TabAccount

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.CurrentNoticeBoardAdapter
import com.example.mio.Adapter.MyReviewAdapter
import com.example.mio.Adapter.MyReviewWriteableAdapter
import com.example.mio.Model.*
import com.example.mio.databinding.FragmentMyReviewWriteableBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyReviewWriteableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyReviewWriteableFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var wBinding:FragmentMyReviewWriteableBinding
    private var wAdapter : MyReviewWriteableAdapter? = null
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var reviewWriteableReadAllData = ArrayList<PostData>()
    private var reviewPassengersData = ArrayList<Participants>()

    private var identification = ""
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
    ): View? {
        wBinding = FragmentMyReviewWriteableBinding.inflate(inflater, container, false)

        initRecyclerview()

        wAdapter?.setItemClickListener(object : MyReviewWriteableAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: Int) {
                val temp = reviewWriteableReadAllData[position]
                //내가 손님일때
                if (identification != reviewWriteableReadAllData[position].user.email) {
                    val intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                        putExtra("type",  CurrentNoticeBoardAdapter.PostStatus.Passenger)
                        putExtra("postDriver", temp.user)
                    }
                    requestActivity.launch(intent)

                } else { //내가 작성자(운전자)일때
                    val intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                        putExtra("type",  CurrentNoticeBoardAdapter.PostStatus.Driver)
                        putExtra("postPassengers", reviewPassengersData)
                    }
                    requestActivity.launch(intent)
                }
            }
        })
        return wBinding.root
    }

    private fun setReadReviewData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(activity).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(activity).toString()


        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    startActivity(intent)
                    requireActivity().finish()
                    return@Interceptor chain.proceed(newRequest)
                }
            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////////////////////////////////////////////////////

        //여기 나중에 데이터 바뀌면 체크 TODO
        CoroutineScope(Dispatchers.IO).launch {
            api.getMyMannersWriteableReview("createDate,desc",0, 5).enqueue(object :
                Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        //데이터 청소
                        reviewWriteableReadAllData.clear()

                        for (i in response.body()!!.content.indices) {
                            //탑승자 null체크
                            var part = 0
                            var location = ""
                            var title = ""
                            var content = ""
                            var targetDate = ""
                            var targetTime = ""
                            var categoryName = ""
                            var cost = 0
                            var verifyGoReturn = false

                            if (response.isSuccessful) {
                                part = try {
                                    response.body()!!.content[i].participantsCount
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                location = try {
                                    response.body()!!.content[i].location.isEmpty()
                                    response.body()!!.content[i].location
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "수락산역 3번 출구"
                                }
                                title = try {
                                    response.body()!!.content[i].title.isEmpty()
                                    response.body()!!.content[i].title
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                content = try {
                                    response.body()!!.content[i].content.isEmpty()
                                    response.body()!!.content[i].content
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetDate = try {
                                    response.body()!!.content[i].targetDate.isEmpty()
                                    response.body()!!.content[i].targetDate
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetTime = try {
                                    response.body()!!.content[i].targetTime.isEmpty()
                                    response.body()!!.content[i].targetTime
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                categoryName = try {
                                    response.body()!!.content[i].category.categoryName.isEmpty()
                                    response.body()!!.content[i].category.categoryName
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                cost = try {
                                    response.body()!!.content[i].cost
                                    response.body()!!.content[i].cost
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                verifyGoReturn = try {
                                    response.body()!!.content[i].verifyGoReturn
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    false
                                }
                            }

                            //println(response!!.body()!!.content[i].user.studentId)
                            reviewWriteableReadAllData.add(
                                PostData(
                                    response.body()!!.content[i].user.studentId,
                                    response.body()!!.content[i].postId,
                                    title,
                                    content,
                                    response.body()!!.content[i].createDate,
                                    targetDate,
                                    targetTime,
                                    categoryName,
                                    location,
                                    //participantscount가 현재 참여하는 인원들
                                    part,
                                    //numberOfPassengers은 총 탑승자 수
                                    response.body()!!.content[i].numberOfPassengers,
                                    cost,
                                    verifyGoReturn,
                                    response.body()!!.content[i].user,
                                    response.body()!!.content[i].latitude,
                                    response.body()!!.content[i].longitude
                                ))

                            wAdapter!!.notifyDataSetChanged()
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            response.body()?.content?.forEach { content ->
                                content.participants?.forEach { participants ->
                                    reviewPassengersData.add(Participants(
                                        participants.id,
                                        participants.email,
                                        participants.studentId,
                                        participants.profileImageUrl,
                                        participants.name,
                                        participants.accountNumber,
                                        participants.gender,
                                        participants.verifySmoker,
                                        participants.roleType,
                                        participants.status,
                                        participants.mannerCount,
                                        participants.grade,
                                    ))
                                }
                            }
                        }


                        if (reviewWriteableReadAllData.isNotEmpty()) {
                            wBinding.writeableReviewPostNotDataLl.visibility = View.GONE
                            wBinding.writeablReviewSwipe.visibility = View.VISIBLE
                            wBinding.writeablReviewPostRv.visibility = View.VISIBLE
                        } else {
                            wBinding.writeableReviewPostNotDataLl.visibility = View.VISIBLE
                            wBinding.writeablReviewSwipe.visibility = View.GONE
                            wBinding.writeablReviewPostRv.visibility = View.GONE
                        }

                    } else {
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun initRecyclerview() {
        val s = SaveSharedPreferenceGoogleLogin()
        identification = s.getUserEMAIL(requireActivity()).toString()
        setReadReviewData()
        wAdapter = MyReviewWriteableAdapter()
        wAdapter!!.myReviewWriteableData = reviewWriteableReadAllData

        wBinding.writeablReviewPostRv.adapter = wAdapter
        wBinding.writeablReviewPostRv.setHasFixedSize(true)
        wBinding.writeablReviewPostRv.layoutManager = manager
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        setReadReviewData()
                    }
                }
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
         * @return A new instance of fragment MyReviewWriteableFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyReviewWriteableFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}