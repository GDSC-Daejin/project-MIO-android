package com.example.mio.TabAccount

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.ProfilePostAdapter
import com.example.mio.Adapter.ProfileReviewAdapter
import com.example.mio.Model.MyAccountReviewData
import com.example.mio.Model.PostData
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.databinding.FragmentProfilePostBinding
import com.example.mio.databinding.FragmentProfileReviewBinding
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
 * Use the [ProfileReviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileReviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var prBinding : FragmentProfileReviewBinding

    private var myAdapter : ProfileReviewAdapter? = null
    private var profileReviewAllData = ArrayList<MyAccountReviewData>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)

    private var dataPosition = 0

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
        prBinding = FragmentProfileReviewBinding.inflate(inflater, container, false)

        initSwipeRefresh()
        initMyRecyclerView()

        return prBinding.root
    }

    private fun initSwipeRefresh() {
        prBinding.profileReviewSwipe.setOnRefreshListener {
            //새로고침 시 터치불가능하도록
            activity?.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 화면 터치 못하게 하기
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                setProfileReviewData()
                myAdapter!!.profileReviewItemData = profileReviewAllData
                //noticeBoardAdapter.recyclerView.startLayoutAnimation()
                prBinding.profileReviewSwipe.isRefreshing = false
                myAdapter!!.notifyDataSetChanged()
                activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
            //터치불가능 해제ss
            //activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun initMyRecyclerView() {
        setProfileReviewData()
        myAdapter = ProfileReviewAdapter()
        myAdapter!!.profileReviewItemData = profileReviewAllData
        prBinding.profileReviewRv.adapter = myAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        prBinding.profileReviewRv.setHasFixedSize(true)
        prBinding.profileReviewRv.layoutManager = manager
    }

    private fun setProfileReviewData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(activity).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(activity).toString()
        val profileUserId = saveSharedPreferenceGoogleLogin.getProfileUserId(activity)!!

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
        ///////////////////////////////////////////////////

        CoroutineScope(Dispatchers.IO).launch {
            api.getMyMannersReceiveReview(profileUserId).enqueue(object : Callback<List<MyAccountReviewData>> {
                override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                    if (response.isSuccessful) {

                        //데이터 청소
                        profileReviewAllData.clear()

                        for (i in response.body()!!.indices) {
                            profileReviewAllData.add(MyAccountReviewData(
                                response.body()!![i].id,
                                response.body()!![i].manner,
                                response.body()!![i].content,
                                response.body()!![i].getUserId,
                                response.body()!![i].postUserId,
                                response.body()!![i].createDate,
                            ))
                            myAdapter!!.notifyDataSetChanged()
                        }
                        if (profileReviewAllData.size > 0) {
                            prBinding.profileReviewNotDataLl.visibility = View.GONE
                            prBinding.profileReviewSwipe.visibility = View.VISIBLE
                            prBinding.profileReviewRv.visibility = View.VISIBLE
                        } else {
                            prBinding.profileReviewNotDataLl.visibility = View.VISIBLE
                            prBinding.profileReviewSwipe.visibility = View.GONE
                            prBinding.profileReviewRv.visibility = View.GONE
                        }

                    } else {
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<List<MyAccountReviewData>>, t: Throwable) {
                    Log.d("error", t.toString())
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
         * @return A new instance of fragment ProfileReviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}