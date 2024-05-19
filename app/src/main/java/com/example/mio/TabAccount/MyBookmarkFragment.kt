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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.Adapter.MyAccountPostAdapter
import com.example.mio.Model.BookMarkResponseData
import com.example.mio.Model.PostData
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.databinding.FragmentMyBookmarkBinding
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
 * Use the [MyBookmarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyBookmarkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentMyBookmarkBinding
    private var myAdapter : MyAccountPostAdapter? = null //간단한건 그냥 같이 사용 adpater
    private var myBookmarkAllData = ArrayList<PostData?>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    //로딩 즉 item의 끝이며 스크롤의 끝인지
    private var isLoading = false
    //데이터의 현재 페이지 수
    private var currentPage = 0
    //데이터의 전체 페이지 수
    private var totalPages = 0
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
        binding = FragmentMyBookmarkBinding.inflate(inflater, container, false)

        initMyRecyclerView()
        initScrollListener()
        initSwipeRefresh()

        myAdapter!!.setItemClickListener(object : MyAccountPostAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = myBookmarkAllData[position]
                    //dataPosition = position
                    val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp!!.user.profileImageUrl)
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        return binding.root
    }

    private fun initMyRecyclerView() {
        setMyBookmarkData()
        myAdapter = MyAccountPostAdapter()
        myAdapter!!.myPostItemData = myBookmarkAllData
        binding.bookmarkRv.adapter = myAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        binding.bookmarkRv.setHasFixedSize(true)
        binding.bookmarkRv.layoutManager = manager

    }

    private fun setMyBookmarkData() {
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

        //println(userId)

        CoroutineScope(Dispatchers.IO).launch {
            api.getBookmark().enqueue(object :
                Callback<List<BookMarkResponseData>> {
                override fun onResponse(call: Call<List<BookMarkResponseData>>, response: Response<List<BookMarkResponseData>>) {
                    if (response.isSuccessful) {
                        val responseData = response.body()

                        for (i in responseData!!) {
                            myBookmarkAllData.add(
                                PostData(
                                    i.user.studentId,
                                    i.post.postId,
                                    i.post.title,
                                    i.post.content,
                                    i.post.targetDate,
                                    i.post.targetTime,
                                    i.post.category.categoryName,
                                    i.post.location,
                                    //participantscount가 현재 참여하는 인원들
                                    i.post.participants!!.size,
                                    //numberOfPassengers은 총 탑승자 수
                                    i.post.numberOfPassengers,
                                    i.post.cost,
                                    i.post.verifyGoReturn,
                                    i.user,
                                    i.post.latitude,
                                    i.post.longitude
                                )
                            )
                        }
                        totalPages = myBookmarkAllData.size / 5
                        myAdapter!!.notifyDataSetChanged()
                        if (myBookmarkAllData.size > 0) {
                            binding.bookmarkPostNotDataLl.visibility = View.GONE
                            binding.bookmarkSwipe.visibility = View.VISIBLE
                            binding.bookmarkRv.visibility = View.VISIBLE
                        } else {
                            binding.bookmarkPostNotDataLl.visibility = View.VISIBLE
                            binding.bookmarkSwipe.visibility = View.GONE
                            binding.bookmarkRv.visibility = View.GONE
                        }
                    } else {
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<List<BookMarkResponseData>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun initSwipeRefresh() {
        binding.bookmarkSwipe.setOnRefreshListener {
            //새로고침 시 터치불가능하도록
            activity?.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 화면 터치 못하게 하기
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                setMyBookmarkData()
                myAdapter!!.myPostItemData = myBookmarkAllData
                //noticeBoardAdapter.recyclerView.startLayoutAnimation()
                binding.bookmarkSwipe.isRefreshing = false
                myAdapter!!.notifyDataSetChanged()
                activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
            //터치불가능 해제ss
            //activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun initScrollListener(){
        binding.bookmarkRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (currentPage < totalPages - 1) {
                    if(!isLoading){
                        if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == myBookmarkAllData.size - 1){
                            Log.e("true", "True")
                            getMoreItem()
                            isLoading =  true
                        }
                    }
                } else {
                    isLoading = false
                }
            }
        })
    }

    private fun getMoreItem() {
        //성공//
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

        val runnable = kotlinx.coroutines.Runnable {
            myBookmarkAllData.add(null)
            myAdapter?.notifyItemInserted(myBookmarkAllData.size - 1)
        }
        binding.bookmarkRv.post(runnable)
        //null을 감지 했으니
        //이 부분에 프로그래스바가 들어올거라 알림
        //mttBinding.moreTaxiTabRv.adapter!!.notifyItemInserted(moreCarpoolAllData.size-1)




        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(java.lang.Runnable {
            //null추가한 거 삭제
            myBookmarkAllData.removeAt(myBookmarkAllData.size - 1)
            myAdapter?.notifyItemRemoved(myBookmarkAllData.size)


            if (currentPage < totalPages - 1) {
                currentPage += 1
                CoroutineScope(Dispatchers.IO).launch {
                    api.getBookmark().enqueue(object :
                        Callback<List<BookMarkResponseData>> {
                        override fun onResponse(call: Call<List<BookMarkResponseData>>, response: Response<List<BookMarkResponseData>>) {
                            if (response.isSuccessful) {
                                val responseData = response.body()
                                for (i in responseData!!) {
                                    myBookmarkAllData.add(
                                        PostData(
                                            i.user.studentId,
                                            i.post.postId,
                                            i.post.title,
                                            i.post.content,
                                            i.post.targetDate,
                                            i.post.targetTime,
                                            i.post.category.categoryName,
                                            i.post.location,
                                            //participantscount가 현재 참여하는 인원들
                                            i.post.participants!!.size,
                                            //numberOfPassengers은 총 탑승자 수
                                            i.post.numberOfPassengers,
                                            i.post.cost,
                                            i.post.verifyGoReturn,
                                            i.user,
                                            i.post.latitude,
                                            i.post.longitude
                                        )
                                    )

                                    myAdapter!!.notifyDataSetChanged()
                                }

                                if (myBookmarkAllData.size > 0) {
                                    binding.bookmarkPostNotDataLl.visibility = View.GONE
                                    binding.bookmarkSwipe.visibility = View.VISIBLE
                                    binding.bookmarkRv.visibility = View.VISIBLE
                                } else {
                                    binding.bookmarkPostNotDataLl.visibility = View.VISIBLE
                                    binding.bookmarkSwipe.visibility = View.GONE
                                    binding.bookmarkRv.visibility = View.GONE
                                }
                            } else {
                                Log.d("f", response.code().toString())
                            }
                        }

                        override fun onFailure(call: Call<List<BookMarkResponseData>>, t: Throwable) {
                            Log.d("error", t.toString())
                        }
                    })
                }
            }

            println(myBookmarkAllData)
            isLoading = false
        }, 2000)
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                //val post = it.data?.getSerializableExtra("postData") as PostData
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            /*taxiAllData.add(post)
                            calendarTaxiAllData.add(post) //데이터 전부 들어감

                            //들어간 데이터를 key로 분류하여 저장하도록함
                            selectCalendarData[post.postTargetDate] = arrayListOf()
                            selectCalendarData[post.postTargetDate]!!.add(post)

                            println(selectCalendarData)*/
                            //setData()
                        }
                        //livemodel을 통해 저장
                        //sharedViewModel!!.setCalendarLiveData("add", selectCalendarData)
                        //noticeBoardAdapter!!.notifyDataSetChanged()
                    }
                    //edit
                    1 -> {
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
         * @return A new instance of fragment MyBookmarkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyBookmarkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}