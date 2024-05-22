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
import androidx.core.view.ViewCompat.canScrollVertically
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.Adapter.MyAccountParticipationAdapter
import com.example.mio.Adapter.MyAccountPostAdapter
import com.example.mio.Model.*
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.databinding.FragmentMyParticipationBinding
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
 * Use the [MyParticipationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyParticipationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var mpBinding : FragmentMyParticipationBinding

    private var myAdapter : MyAccountParticipationAdapter? = null
    //자신이 신청한 예약의 게시글 정보를 순서대로 담은 데이터 변수
    private var myParticipationAllData = ArrayList<PostData?>()
    //자신이 신청한 예약의 정보(waiting, approval등)을 순서대로 담은 데이터변수
    private var myParticipationApprovalOrRejectAllData = ArrayList<String?>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)

    private var dataPosition = 0

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
        mpBinding = FragmentMyParticipationBinding.inflate(inflater, container, false)

        initMyParticipationRecyclerView()
        initSwipeRefresh()
        initScrollListener()

        myAdapter!!.setItemClickListener(object : MyAccountParticipationAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = myParticipationAllData[position]
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

        return mpBinding.root
    }
    private fun initMyParticipationRecyclerView() {
        setMyParticipationData()
        myAdapter = MyAccountParticipationAdapter()
        myAdapter!!.myPostItemData = myParticipationAllData
        myAdapter!!.myPostReservationCheck = myParticipationApprovalOrRejectAllData
        mpBinding.participationPostRv.adapter = myAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        mpBinding.participationPostRv.setHasFixedSize(true)
        mpBinding.participationPostRv.layoutManager = manager
    }
    private fun initSwipeRefresh() {
        mpBinding.participationAccountSwipe.setOnRefreshListener {
            //새로고침 시 터치불가능하도록
            activity?.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) // 화면 터치 못하게 하기
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                setMyParticipationData()
                myAdapter!!.myPostItemData = myParticipationAllData
                //noticeBoardAdapter.recyclerView.startLayoutAnimation()
                mpBinding.participationAccountSwipe.isRefreshing = false
                myAdapter!!.notifyDataSetChanged()
                activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
            //터치불가능 해제ss
            //activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
    private fun setMyParticipationData() {
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
            api.getMyParticipantsData(0, 5).enqueue(object : Callback<List<ParticipationData>> {
                override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                    if (response.isSuccessful) {
                        //데이터 청소
                        println("가져오기 참가 성공")
                        totalPages = response.body()!!.size / 5
                        myParticipationAllData.clear()

                        for (i in response.body()!!.indices) {
                            //println(response!!.body()!!.content[i].user.studentId)
                            myParticipationApprovalOrRejectAllData.add(response.body()!![i].approvalOrReject)
                            CoroutineScope(Dispatchers.IO).launch {
                                setPostUserData(postId = response.body()!![i].postId)
                            }
                        }
                        myAdapter!!.notifyDataSetChanged()

                        if (myParticipationAllData.size > 0) {
                            mpBinding.accountParticipationNotDataLl.visibility = View.GONE
                            mpBinding.participationAccountSwipe.visibility = View.VISIBLE
                            mpBinding.participationPostRv.visibility = View.VISIBLE
                        } else {
                            mpBinding.accountParticipationNotDataLl.visibility = View.VISIBLE
                            mpBinding.participationAccountSwipe.visibility = View.GONE
                            mpBinding.participationPostRv.visibility = View.GONE
                        }
                        Log.d("mypartipationFragment", myParticipationAllData.toString())
                    } else {
                        Log.e("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                    Log.e("error", t.toString())
                }
            })
        }
    }
    //게시글 승인 예약 인원정보
    private fun setPostUserData(postId : Int) {
        Log.e("participationFragment PostId Test", "진입완료")
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(requireActivity()).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(requireActivity()).toString()
        /*val email = saveSharedPreferenceGoogleLogin.getUserEMAIL(this)!!.substring(0 until 8)
        val profileUserId = saveSharedPreferenceGoogleLogin.getProfileUserId(this)!!*/

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
        api.getPostIdDetailSearch(postId).enqueue(object : Callback<Content> {
            override fun onResponse(call: Call<Content>, response: Response<Content>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    responseData.let {
                        myParticipationAllData.add(PostData(
                                response.body()!!.user.studentId,
                                response.body()!!.postId,
                                response.body()!!.title,
                                response.body()!!.content,
                                response.body()!!.targetDate,
                                response.body()!!.targetTime,
                                response.body()!!.category.categoryName,
                                response.body()!!.location,
                                //participantscount가 현재 참여하는 인원들
                                response.body()!!.participantsCount,
                                //numberOfPassengers은 총 탑승자 수
                                response.body()!!.numberOfPassengers,
                                response.body()!!.cost,
                                response.body()!!.verifyGoReturn,
                                response.body()!!.user,
                                response.body()!!.latitude,
                                response.body()!!.longitude
                        ))
                    }


                } else {
                    Log.e("MyParticipationFragment", response.code().toString())
                }
            }

            override fun onFailure(call: Call<Content>, t: Throwable) {
                Log.e("MyParticipationFail", t.message.toString())
            }

        })
    }



    private fun initScrollListener(){
        mpBinding.participationPostRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                //val layoutm = pBinding.myAccountPostRv.layoutManager as LinearLayoutManager
                //화면에 보이는 마지막 아이템의 position
                // 어댑터에 등록된 아이템의 총 개수 -1
                //데이터의 마지막이 아이템의 화면에 뿌려졌는지
                if (currentPage < totalPages - 1) {
                    if(!isLoading){
                        if ((recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition() == myParticipationAllData.size - 1){
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
        ////////////////////////////////////////////////////

        val runnable = kotlinx.coroutines.Runnable {
            myParticipationAllData.add(null)
            myAdapter?.notifyItemInserted(myParticipationAllData.size - 1)
        }
        mpBinding.participationPostRv.post(runnable)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            //null추가한 거 삭제
            myParticipationAllData.removeAt(myParticipationAllData.size - 1)
            myAdapter?.notifyItemRemoved(myParticipationAllData.size)

            //data.clear()

            //page수가 totalpages 보다 작거나 같다면 데이터 더 가져오기 가능
            if (currentPage < totalPages - 1) {
                currentPage += 1
                CoroutineScope(Dispatchers.IO).launch {
                    api.getMyParticipantsData(currentPage, 5).enqueue(object : Callback<List<ParticipationData>> {
                        override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                            if (response.isSuccessful) {
                                //데이터 청소
                                println("가져오기 참가 성공")
                                totalPages = response.body()!!.size / 5
                                //myParticipationAllData.clear()

                                for (i in response.body()!!.indices) {
                                    //println(response!!.body()!!.content[i].user.studentId)
                                    myParticipationApprovalOrRejectAllData.add(response.body()!![i].approvalOrReject)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        setPostUserData(postId = response.body()!![i].postId)
                                    }
                                }
                                myAdapter!!.notifyDataSetChanged()

                                if (myParticipationAllData.size > 0) {
                                    mpBinding.accountParticipationNotDataLl.visibility = View.GONE
                                    mpBinding.participationAccountSwipe.visibility = View.VISIBLE
                                    mpBinding.participationPostRv.visibility = View.VISIBLE
                                } else {
                                    mpBinding.accountParticipationNotDataLl.visibility = View.VISIBLE
                                    mpBinding.participationAccountSwipe.visibility = View.GONE
                                    mpBinding.participationPostRv.visibility = View.GONE
                                }

                            } else {
                                Log.d("f", response.code().toString())
                            }
                        }

                        override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                            Log.d("error", t.toString())
                        }
                    })
                }
            }

            //println(moreTaxiAllData)
            isLoading = false
        }, 2000)
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val post = it.data?.getSerializableExtra("postData") as PostData
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
         * @return A new instance of fragment MyParticipationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyParticipationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}