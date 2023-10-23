package com.example.mio.TabAccount

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.MyReviewAdapter
import com.example.mio.Adapter.MyReviewWriteableAdapter
import com.example.mio.BuildConfig
import com.example.mio.MioInterface
import com.example.mio.Model.MyAccountReviewData
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
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
    private var reviewWriteableReadAllData = ArrayList<MyAccountReviewData>()

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

        return wBinding.root
    }

    private fun setReadReviewData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(activity).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(activity).toString()
        val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(activity)!!.substring(0 until 8)
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
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
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
            api.getMyMannersReceiveReview(userId).enqueue(object :
                Callback<List<MyAccountReviewData>> {
                override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                    if (response.isSuccessful) {

                        //데이터 청소
                        reviewWriteableReadAllData.clear()

                        for (i in response.body()!!.indices) {
                            reviewWriteableReadAllData.add(
                                MyAccountReviewData(
                                    response.body()!![i].id,
                                    response.body()!![i].manner,
                                    response.body()!![i].content,
                                    response.body()!![i].getUserId,
                                    response.body()!![i].postUserId,
                                    response.body()!![i].createDate,
                                )
                            )
                            wAdapter!!.notifyDataSetChanged()
                        }
                        if (reviewWriteableReadAllData.size > 0) {
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

                override fun onFailure(call: Call<List<MyAccountReviewData>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun initRecyclerview() {
        setReadReviewData()
        wAdapter = MyReviewWriteableAdapter()
        wAdapter!!.myReviewWriteableData = reviewWriteableReadAllData

        wBinding.writeablReviewPostRv.adapter = wAdapter
        wBinding.writeablReviewPostRv.setHasFixedSize(true)
        wBinding.writeablReviewPostRv.layoutManager = manager
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