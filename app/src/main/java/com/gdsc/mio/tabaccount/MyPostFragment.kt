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
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.mio.RetrofitServerConnect
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin
import com.gdsc.mio.adapter.MyAccountPostAdapter
import com.gdsc.mio.databinding.FragmentMyPostBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.PostData
import com.gdsc.mio.model.PostReadAllResponse
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
 * Use the [MyPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPostFragment : Fragment() { //첫번째 어카운트
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var pBinding : FragmentMyPostBinding

    private var myAdapter : MyAccountPostAdapter? = null
    private var myAccountPostAllData = ArrayList<PostData?>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)

    private var dataPosition = 0
    //로딩 즉 item의 끝이며 스크롤의 끝인지
    private var isLoading = false
    //데이터의 현재 페이지 수
    private var currentPage = 0
    //데이터의 전체 페이지 수
    private var totalPages = 0

    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    //private var token : String = ""
    private var getExpireDate = ""
    private var email = ""
    private var userId = ""


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
        pBinding = FragmentMyPostBinding.inflate(inflater, container, false)
        //token = saveSharedPreferenceGoogleLogin.getToken(requireActivity()).toString()
        getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(requireActivity()).toString()
        email = saveSharedPreferenceGoogleLogin.getUserEMAIL(requireActivity())!!.split("@").map { it }.first()
        userId = saveSharedPreferenceGoogleLogin.getUserId(requireActivity()).toString()
        initMyRecyclerView()
        initSwipeRefresh()
        initScrollListener()

        myAdapter!!.setItemClickListener(object : MyAccountPostAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = myAccountPostAllData[position]
                    dataPosition = position
                    val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp!!.user.profileImageUrl)
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        return pBinding.root
    }


    private fun setMyPostData() {
        RetrofitServerConnect.create(requireActivity()).getMyPostData(userId.toInt(),"createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                if (response.isSuccessful) {
                    totalPages = response.body()!!.totalPages
                    //데이터 청소
                    myAccountPostAllData.clear()

                    //데드라인 체크안함
                    for (i in response.body()!!.content.filter { it.isDeleteYN == "N" }.indices) {
                        val part = response.body()!!.content[i].participantsCount
                        val location = response.body()!!.content[i].location
                        val title = response.body()!!.content[i].title
                        val content = response.body()!!.content[i].content
                        val targetDate = response.body()!!.content[i].targetDate
                        val targetTime = response.body()!!.content[i].targetTime
                        val categoryName = response.body()!!.content[i].category.categoryName
                        val cost = response.body()!!.content[i].cost
                        val verifyGoReturn = response.body()!!.content[i].verifyGoReturn

                        //println(response!!.body()!!.content[i].user.studentId)
                        myAccountPostAllData.add(
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
                            )
                        )
                    }

                    myAdapter?.updateDataList(myAccountPostAllData)
                    if (myAccountPostAllData.size > 0) {
                        pBinding.accountPostNotDataLl.visibility = View.GONE
                        pBinding.accountSwipe.visibility = View.VISIBLE
                        pBinding.myAccountPostRv.visibility = View.VISIBLE
                    } else {
                        pBinding.accountPostNotDataLl.visibility = View.VISIBLE
                        pBinding.accountSwipe.visibility = View.GONE
                        pBinding.myAccountPostRv.visibility = View.GONE
                    }
                    // 새로 고침 완료 및 터치 가능하게 설정
                    pBinding.accountSwipe.isRefreshing = false
                    LoadingProgressDialogManager.hide()

                } else {
                    requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            LoadingProgressDialogManager.hide()
                            Toast.makeText(requireActivity(), "게시글 정보를 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                requireActivity().runOnUiThread {
                    if (isAdded && !requireActivity().isFinishing) {
                        LoadingProgressDialogManager.hide()
                        Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun initMyRecyclerView() {
        LoadingProgressDialogManager.show(requireContext())
        setMyPostData()
        myAdapter = MyAccountPostAdapter()
        //myAdapter!!.myPostItemData = myAccountPostAllData
        pBinding.myAccountPostRv.adapter = myAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        pBinding.myAccountPostRv.setHasFixedSize(true)
        pBinding.myAccountPostRv.layoutManager = manager

    }

    private fun initSwipeRefresh() {
        pBinding.accountSwipe.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // 데이터 새로 고침
            refreshData()

            /* // 새로 고침 완료 및 터치 가능하게 설정
             mttBinding.moreRefreshSwipeLayout.isRefreshing = false
             this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/

            // 스크롤 리스너 초기화
            initScrollListener()

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                pBinding.accountSwipe.isRefreshing = false
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 500)
        }
    }

    private fun refreshData() {
        isLoading = false
        currentPage = 0
        //moreCarpoolAllData.clear() // Clear existing data
        myAdapter?.updateDataList(emptyList()) // Notify adapter of data change

        // Fetch fresh data
        setMyPostData()
    }

    private fun initScrollListener() {
        pBinding.myAccountPostRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            myAccountPostAllData.add(null)
                            myAdapter?.notifyItemInserted(myAccountPostAllData.size - 1)
                        }
                        pBinding.myAccountPostRv.post(runnable)

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
            val loadingPosition = myAccountPostAllData.indexOf(null)
            if (loadingPosition != -1) {
                myAccountPostAllData.removeAt(loadingPosition)
                myAdapter?.notifyItemRemoved(loadingPosition)
            }

            // Fetch more data if necessary
            if (currentPage < totalPages - 1) {
                currentPage += 1
                RetrofitServerConnect.create(requireActivity()).getMyPostData(userId.toInt(), "createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
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

                                myAccountPostAllData.addAll(newItems)
                                myAdapter?.updateDataList(newItems)
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                if (isAdded && !requireActivity().isFinishing) {
                                    LoadingProgressDialogManager.hide()
                                    Toast.makeText(requireActivity(), "게시글 정보를 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        isLoading = false
                    }

                    override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                        isLoading = false
                        requireActivity().runOnUiThread {
                            if (isAdded && !requireActivity().isFinishing) {
                                LoadingProgressDialogManager.hide()
                                Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })

            }
        }, 2000)
    }

    /*private fun getMoreItem() {
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
        ////////////////////////////////////////////////////
        val runnable = kotlinx.coroutines.Runnable {
            myAccountPostAllData.add(null)
            myAdapter?.notifyItemInserted(myAccountPostAllData.size - 1)
        }
        pBinding.myAccountPostRv.post(runnable)
        //null을 감지 했으니
        //이 부분에 프로그래스바가 들어올거라 알림
        //mttBinding.moreTaxiTabRv.adapter!!.notifyItemInserted(moreCarpoolAllData.size-1)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(java.lang.Runnable {
            //null추가한 거 삭제
            myAccountPostAllData.removeAt(myAccountPostAllData.size - 1)
            myAdapter?.notifyItemRemoved(myAccountPostAllData.size)
            //data.clear()

            //page수가 totalpages 보다 작거나 같다면 데이터 더 가져오기 가능
            if (currentPage < totalPages - 1) {
                currentPage += 1
                CoroutineScope(Dispatchers.IO).launch {
                    api.getMyPostData(userId,"createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
                        override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                            if (response.isSuccessful) {

                                //println(response.body()!!.content)
                                *//*val start = SystemClock.elapsedRealtime()

                                // 함수 실행시간
                                val date = Date(start)
                                val mFormat = SimpleDateFormat("HH:mm:ss")
                                val time = mFormat.format(date)
                                println(start)
                                println(time)*//*
                                *//*val s : ArrayList<PostReadAllResponse> = ArrayList()
                                s.add(PostReadAllResponse())*//*

                                //데드라인 체크안함
                                for (i in response.body()!!.content.filter { it.isDeleteYN == "N" }.indices) {
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
                                    myAccountPostAllData.add(
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
                                }
                                Log.d("mypostfragment", myAccountPostAllData.toString())
                                myAdapter!!.notifyDataSetChanged()
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
         * @return A new instance of fragment MyPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}