package com.gdsc.mio.tabaccount

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.mio.RetrofitServerConnect
import com.gdsc.mio.adapter.MyAccountPostAdapter
import com.gdsc.mio.databinding.FragmentMyBookmarkBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.BookMarkResponseData
import com.gdsc.mio.model.Content
import com.gdsc.mio.model.PostData
import com.gdsc.mio.noticeboard.NoticeBoardReadActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        binding = FragmentMyBookmarkBinding.inflate(inflater, container, false)
        initMyRecyclerView()
        //initScrollListener()
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
        LoadingProgressDialogManager.show(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            setMyBookmarkData()
        }

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
        val thisData : ArrayList<BookMarkResponseData> = ArrayList()
        RetrofitServerConnect.create(requireActivity()).getBookmark().enqueue(object : Callback<List<BookMarkResponseData>> {
            override fun onResponse(call: Call<List<BookMarkResponseData>>, response: Response<List<BookMarkResponseData>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    //데이터 청소
                    if (responseData?.isNotEmpty() == true) {
                        responseData.let {
                            thisData.clear()
                            thisData.addAll(it)
                        }
                        //updateUI()
                        CoroutineScope(Dispatchers.IO).launch {
                            initBookMarkPostData(thisData)
                        }
                    } else {
                        LoadingProgressDialogManager.hide()
                        if (myBookmarkAllData.isNotEmpty()) {
                            binding.bookmarkPostNotDataLl.visibility = View.GONE
                            binding.bookmarkSwipe.visibility = View.VISIBLE
                            binding.bookmarkRv.visibility = View.VISIBLE
                        } else {
                            binding.bookmarkPostNotDataLl.visibility = View.VISIBLE
                            binding.bookmarkSwipe.visibility = View.GONE
                            binding.bookmarkRv.visibility = View.GONE
                        }
                    }
                } else {
                    LoadingProgressDialogManager.hide()
                    Toast.makeText(requireActivity(), "북마크 정보를 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BookMarkResponseData>>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initBookMarkPostData(bookMarkList : List<BookMarkResponseData>?) {
        var shouldBreak = false
        if (bookMarkList?.isNotEmpty() == true) {
            for (i in bookMarkList) {
                if (shouldBreak) break
                RetrofitServerConnect.create(requireActivity()).getPostIdDetailSearch(i.postId).enqueue(object : Callback<Content> {
                    override fun onResponse(call: Call<Content>, response: Response<Content>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            if (responseData != null) {
                                if (responseData.isDeleteYN == "N") {
                                    responseData.let {
                                        myBookmarkAllData.add(
                                            PostData(
                                                responseData.user.studentId,
                                                responseData.postId,
                                                responseData.title,
                                                responseData.content,
                                                responseData.createDate,
                                                responseData.targetDate,
                                                responseData.targetTime,
                                                responseData.category.categoryName,
                                                responseData.location,
                                                responseData.participantsCount,
                                                responseData.numberOfPassengers,
                                                responseData.cost,
                                                responseData.verifyGoReturn,
                                                responseData.user,
                                                responseData.latitude,
                                                responseData.longitude
                                            ))
                                    }
                                    updateUI()
                                }
                            }
                        } else {
                            LoadingProgressDialogManager.hide()
                            shouldBreak = true
                        }
                    }

                    override fun onFailure(call: Call<Content>, t: Throwable) {
                        shouldBreak = true
                    }
                })
            }
            CoroutineScope(Dispatchers.Main).launch {
                updateUI()
            }
        }
    }

    private fun updateUI() {
        myAdapter!!.notifyDataSetChanged()

        if (myBookmarkAllData.isNotEmpty()) {
            binding.bookmarkPostNotDataLl.visibility = View.GONE
            binding.bookmarkSwipe.visibility = View.VISIBLE
            binding.bookmarkRv.visibility = View.VISIBLE
        } else {
            binding.bookmarkPostNotDataLl.visibility = View.VISIBLE
            binding.bookmarkSwipe.visibility = View.GONE
            binding.bookmarkRv.visibility = View.GONE
        }
        LoadingProgressDialogManager.hide()
    }

    private fun initSwipeRefresh() {
        binding.bookmarkSwipe.setOnRefreshListener {
            //새로고침 시 터치불가능하도록
            activity?.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 화면 터치 못하게 하기
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                myBookmarkAllData.clear()
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

//    private fun initScrollListener(){
//        binding.bookmarkRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                if (currentPage < totalPages - 1) {
//                    if(!isLoading){
//                        if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == myBookmarkAllData.size - 1){
//                            Log.e("true", "True")
//                            getMoreItem()
//                            isLoading =  true
//                        }
//                    }
//                } else {
//                    isLoading = false
//                }
//            }
//        })
//    }

   /* private fun getMoreItem() {
        //성공//
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
                    *//*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
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
                if (currentPage < totalPages - 1) {
                    currentPage += 1

                    CoroutineScope(Dispatchers.IO).launch {
                        val start = currentPage * 5
                        val end = minOf(start + 5, myBookmarkAllData.size)
                        val nextData = myBookmarkAllData.subList(start, end)

                        withContext(Dispatchers.Main) {
                            myAdapter!!.addMoreData(nextData)
                        }
                    }
                }
            }

            println(myBookmarkAllData)
            isLoading = false
        }, 2000)
    }*/

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

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