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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.Adapter.CurrentNoticeBoardAdapter
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

import kotlin.collections.ArrayList

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
    private var reviewWriteableReadAllData = ArrayList<PostData?>()
    private var reviewPassengersData = ArrayList<ParticipationData>()

    //로딩 즉 item의 끝이며 스크롤의 끝인지
    private var isLoading = false
    //데이터의 현재 페이지 수
    private var currentPage = 0
    //데이터의 전체 페이지 수
    private var totalPages = 0


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

        //initSwipeRefresh()
        initScrollListener()
        initRecyclerview()

        wAdapter?.setItemClickListener(object : MyReviewWriteableAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: Int) {
                val temp = reviewWriteableReadAllData[position]
                //내가 손님일때
                Log.e("wWriteable", reviewPassengersData.toString())
                if (identification != reviewWriteableReadAllData[position]!!.user.email) {
                    val intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                        putExtra("type",  CurrentNoticeBoardAdapter.PostStatus.Passenger)
                        putExtra("postDriver", temp!!.user)
                    }
                    requestActivity.launch(intent)

                } else { //내가 작성자(운전자)일때
                    //val s = reviewPassengersData.find { it.postId == itemId }
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


        /*val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    *//*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                    Log.e("reviewWriteabl", "reviewWriteabl1")
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    Toast.makeText(requireActivity(), "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
        val api = retrofit2.create(MioInterface::class.java)*/
        /////////////////////////////////////////////////////

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(requireActivity()).getMyMannersWriteableReview("createDate,desc",0, 5).enqueue(object :
                Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        //데이터 청소
                        reviewWriteableReadAllData.clear()
                        totalPages = response.body()?.totalPages ?: 0
                        val responseData = response.body()
                        //데드라인 체크안함
                        if (responseData != null) {
                            for (i in responseData.content.filter { it.isDeleteYN == "N" }.indices) {
                                val part = response.body()!!.content[i].participantsCount ?: 0
                                val location = response.body()!!.content[i].location ?: "수락산역 3번 출구"
                                val title = response.body()!!.content[i].title ?: "null"
                                val content = response.body()!!.content[i].content ?: "null"
                                val targetDate = response.body()!!.content[i].targetDate ?: "null"
                                val targetTime = response.body()!!.content[i].targetTime ?: "null"
                                val categoryName = response.body()!!.content[i].category.categoryName ?: "null"
                                val cost = response.body()!!.content[i].cost ?: 0
                                val verifyGoReturn = response.body()!!.content[i].verifyGoReturn ?: false

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

                                if (responseData.content[i].participants?.isNotEmpty() == true /*&& identification == responseData.content[i].user.email*/) {
                                    reviewPassengersData.addAll(responseData.content[i].participants!!)
                                }
                            }
                            wAdapter?.updateDataList(reviewWriteableReadAllData)
                        }
                       /* CoroutineScope(Dispatchers.IO).launch {
                            response.body()?.content?.forEach { content ->
                                content.participants?.forEach { participationData  ->

                                }
                            }
                        }*/
                        if (reviewWriteableReadAllData.isNotEmpty()) {
                            wBinding.writeableReviewPostNotDataLl.visibility = View.GONE
                            wBinding.writeablReviewPostRv.visibility = View.VISIBLE
                        } else {
                            wBinding.writeableReviewPostNotDataLl.visibility = View.VISIBLE
                            wBinding.writeablReviewPostRv.visibility = View.GONE
                        }
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

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
        //wAdapter!!.myReviewWriteableData = reviewWriteableReadAllData

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


    /*private fun initSwipeRefresh() {
        wBinding.writeablReviewSwipe.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // 데이터 새로 고침
            refreshData()

            *//* // 새로 고침 완료 및 터치 가능하게 설정
             mttBinding.moreRefreshSwipeLayout.isRefreshing = false
             this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*//*

            // 스크롤 리스너 초기화
            initScrollListener()
        }
    }*/

    private fun refreshData() {
        isLoading = false
        currentPage = 0
        //moreCarpoolAllData.clear() // Clear existing data
        wAdapter?.notifyDataSetChanged() // Notify adapter of data change

        // Fetch fresh data
        setReadReviewData()
    }

    private fun initScrollListener() {
        wBinding.writeablReviewPostRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val lastVisibleItemPosition = layoutManager?.findLastCompletelyVisibleItemPosition() ?: -1
                val itemTotalCount = recyclerView.adapter?.itemCount ?: 0

                // 스크롤이 끝에 도달했는지 확인하고 isLoading 상태 확인
                if (lastVisibleItemPosition >= itemTotalCount - 1 && !isLoading) {
                    if (currentPage < totalPages - 1) {
                        isLoading = true // Set isLoading to true to prevent multiple calls

                        // Add a placeholder for the loading item
                        val runnable = kotlinx.coroutines.Runnable {
                            reviewWriteableReadAllData.add(null)
                            wAdapter?.notifyItemInserted(reviewWriteableReadAllData.size - 1)
                        }
                        wBinding.writeablReviewPostRv.post(runnable)

                        // Load more items
                        getMoreItem()
                    }
                }
            }
        })
    }

    private fun getMoreItem() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // Remove the loading item placeholder
            val loadingPosition = reviewWriteableReadAllData.indexOf(null)
            if (loadingPosition != -1) {
                reviewWriteableReadAllData.removeAt(loadingPosition)
                wAdapter?.notifyItemRemoved(loadingPosition)
            }

            // Fetch more data if necessary
            if (currentPage < totalPages - 1) {
                currentPage += 1
                RetrofitServerConnect.create(requireActivity()).getMyMannersWriteableReview("createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
                    override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            responseData?.let {
                                val newItems = it.content.filter { item ->
                                    item.isDeleteYN == "N"
                                }.map { item ->
                                    PostData(
                                        item.user.studentId,
                                        item.postId,
                                        item.title,
                                        item.content,
                                        item.createDate,
                                        item.targetDate,
                                        item.targetTime,
                                        item.category.categoryName,
                                        item.location,
                                        item.participantsCount,
                                        item.numberOfPassengers,
                                        item.cost,
                                        item.verifyGoReturn,
                                        item.user,
                                        item.latitude,
                                        item.longitude
                                    )
                                }

                                reviewWriteableReadAllData.addAll(newItems)
                                wAdapter?.notifyDataSetChanged()
                            }
                        } else {
                            Log.d("Error", "Response code: ${response.code()}")
                        }
                        isLoading = false
                    }

                    override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                        Log.d("Error", "Failure: ${t.message}")
                        isLoading = false
                    }
                })

            }
        }, 2000)
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