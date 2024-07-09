package com.example.mio.TabAccount

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.MyReviewAdapter
import com.example.mio.Model.MyAccountReviewData
import com.example.mio.databinding.FragmentMyReivewWrittenBinding
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
 * Use the [MyReviewWrittenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyReviewWrittenFragment : Fragment() { //내가 쓴 리뷰 보는 곳
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rwBinding : FragmentMyReivewWrittenBinding
    private var rwAdapter : MyReviewAdapter? = null
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var reviewWrittenAllData = ArrayList<MyAccountReviewData>()

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
        rwBinding = FragmentMyReivewWrittenBinding.inflate(inflater, container, false)


        initRecyclerview()

        return rwBinding.root
    }

    private fun setReadReviewData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(activity).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(activity).toString()
        val userId = saveSharedPreferenceGoogleLogin.getUserId(activity)!!

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
            api.getMyMannersSendReview(userId).enqueue(object :
                Callback<List<MyAccountReviewData>> {
                override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                    if (response.isSuccessful) {

                        //데이터 청소
                        reviewWrittenAllData.clear()

                        for (i in response.body()!!.indices) {
                            reviewWrittenAllData.add(
                                MyAccountReviewData(
                                    response.body()!![i].id,
                                    response.body()!![i].manner,
                                    response.body()!![i].content,
                                    response.body()!![i].getUserId,
                                    response.body()!![i].postUserId,
                                    response.body()!![i].createDate,
                                )
                            )
                            rwAdapter!!.notifyDataSetChanged()
                        }
                        if (reviewWrittenAllData.size > 0) {
                            rwBinding.writtenReviewPostNotDataLl.visibility = View.GONE
                            rwBinding.writtenReviewSwipe.visibility = View.VISIBLE
                            rwBinding.writtenReviewPostRv.visibility = View.VISIBLE
                        } else {
                            rwBinding.writtenReviewPostNotDataLl.visibility = View.VISIBLE
                            rwBinding.writtenReviewSwipe.visibility = View.GONE
                            rwBinding.writtenReviewPostRv.visibility = View.GONE
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

    private fun initRecyclerview() {
        setReadReviewData()
        rwAdapter = MyReviewAdapter()
        rwAdapter!!.myReviewData = reviewWrittenAllData

        rwBinding.writtenReviewPostRv.adapter = rwAdapter
        rwBinding.writtenReviewPostRv.setHasFixedSize(true)
        rwBinding.writtenReviewPostRv.layoutManager = manager
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyReivewWrittenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyReviewWrittenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}