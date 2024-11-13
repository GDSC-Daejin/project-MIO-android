package com.example.mio.tabaccount

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.RetrofitServerConnect
import com.example.mio.adapter.MyAccountParticipationAdapter
import com.example.mio.databinding.FragmentMyParticipationBinding
import com.example.mio.loading.LoadingProgressDialogManager
import com.example.mio.model.Content
import com.example.mio.model.ParticipationData
import com.example.mio.model.PostData
import com.example.mio.noticeboard.NoticeBoardReadActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyParticipationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyParticipationFragment : Fragment() { //두번쨰
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var mpBinding : FragmentMyParticipationBinding

    private var myAdapter : MyAccountParticipationAdapter? = null
    //자신이 신청한 예약의 게시글 정보를 순서대로 담은 데이터 변수
    private var myParticipationAllData = ArrayList<PostData?>()
    //자신이 신청한 예약의 정보(waiting, approval등)을 순서대로 담은 데이터변수
    private var myParticipationApprovalOrRejectAllData = HashMap<String?,String?>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)

    private var dataPosition = 0

    //로딩 즉 item의 끝이며 스크롤의 끝인지
    //private var isLoading = false
    //데이터의 현재 페이지 수
    //private var currentPage = 0
    //데이터의 전체 페이지 수
    //private var totalPages = 0

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
        mpBinding = FragmentMyParticipationBinding.inflate(inflater, container, false)

        initMyParticipationRecyclerView()
        initSwipeRefresh()
        //initScrollListener()

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
        LoadingProgressDialogManager.show(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            setMyParticipationData()
        }

        myAdapter = MyAccountParticipationAdapter()
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
                //noticeBoardAdapter.recyclerView.startLayoutAnimation()
                mpBinding.participationAccountSwipe.isRefreshing = false
                activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
            //터치불가능 해제ss
            //activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
    private fun setMyParticipationData() {
        RetrofitServerConnect.create(requireActivity()).getMyParticipantsData().enqueue(object : Callback<List<ParticipationData>> {
            override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    //totalPages = responseData?.size?.toInt()?.div(5) ?: 0
                    myParticipationAllData.clear()

                    if (responseData.isNullOrEmpty()) {
                        LoadingProgressDialogManager.hide()
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
                        //Log.d("Notification Fragment Data", "Received data: $myParticipationApprovalOrRejectAllData")
                        CoroutineScope(Dispatchers.IO).launch {
                            setPostUserData(postList = responseData.filter { it.isDeleteYN != "Y" })
                        }
                    }

                    /*myAdapter!!.notifyDataSetChanged()
                    updateUI()*/


                } else {
                    LoadingProgressDialogManager.hide()
                    if (myParticipationAllData.size > 0) {
                        mpBinding.accountParticipationNotDataLl.visibility = View.GONE
                        mpBinding.participationAccountSwipe.visibility = View.VISIBLE
                        mpBinding.participationPostRv.visibility = View.VISIBLE
                    } else {
                        mpBinding.accountParticipationNotDataLl.visibility = View.VISIBLE
                        mpBinding.participationAccountSwipe.visibility = View.GONE
                        mpBinding.participationPostRv.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                LoadingProgressDialogManager.hide()
                if (myParticipationAllData.size > 0) {
                    mpBinding.accountParticipationNotDataLl.visibility = View.GONE
                    mpBinding.participationAccountSwipe.visibility = View.VISIBLE
                    mpBinding.participationPostRv.visibility = View.VISIBLE
                } else {
                    mpBinding.accountParticipationNotDataLl.visibility = View.VISIBLE
                    mpBinding.participationAccountSwipe.visibility = View.GONE
                    mpBinding.participationPostRv.visibility = View.GONE
                }
            }
        })
    }

    private fun updateUI() {
        LoadingProgressDialogManager.hide()
        myAdapter?.updateDataList(myParticipationAllData)
        if (myParticipationAllData.size > 0) {
            mpBinding.accountParticipationNotDataLl.visibility = View.GONE
            mpBinding.participationAccountSwipe.visibility = View.VISIBLE
            mpBinding.participationPostRv.visibility = View.VISIBLE
        } else {
            mpBinding.accountParticipationNotDataLl.visibility = View.VISIBLE
            mpBinding.participationAccountSwipe.visibility = View.GONE
            mpBinding.participationPostRv.visibility = View.GONE
        }
    }

    //게시글 승인 예약 인원정보
    private fun setPostUserData(postList: List<ParticipationData>?) {
        if (postList?.isNotEmpty() == true) {
            for (i in postList.indices) {
                RetrofitServerConnect.create(requireActivity()).getPostIdDetailSearch(postId = postList[i].postId).enqueue(object : Callback<Content> {
                    override fun onResponse(call: Call<Content>, response: Response<Content>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            if (responseData != null) {
                                if (responseData.isDeleteYN == "N") {
                                    responseData.let {
                                        myParticipationAllData.add(PostData(
                                            it.user.studentId,
                                            it.postId,
                                            it.title,
                                            it.content,
                                            it.createDate,
                                            it.targetDate,
                                            it.targetTime,
                                            it.category.categoryName,
                                            it.location,
                                            //participantscount가 현재 참여하는 인원들
                                            it.participantsCount,
                                            //numberOfPassengers은 총 탑승자 수
                                            it.numberOfPassengers,
                                            it.cost,
                                            it.verifyGoReturn,
                                            it.user,
                                            it.latitude,
                                            it.longitude
                                        ))

                                        /*val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                                        val sortedTargets = nearPostAllData.sortedByDescending {
                                            // 시간과 날짜를 하나로 결합하여 내림차순으로 정렬
                                            LocalDate.parse(it.targetDate, dateFormatter).atTime(LocalTime.parse(it.targetTime, timeFormatter))
                                        }

                                        adapter.setData(sortedTargets)
                                        * */
                                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                                        myParticipationApprovalOrRejectAllData[LocalDate.parse(it.targetDate, dateFormatter).atTime(
                                            LocalTime.parse(it.targetTime, timeFormatter)).toString()] = postList[i].approvalOrReject
                                        val sortedTargets = myParticipationAllData.sortedByDescending { sortPostData ->
                                            // 시간과 날짜를 하나로 결합하여 내림차순으로 정렬
                                            LocalDate.parse(sortPostData?.postTargetDate, dateFormatter).atTime(LocalTime.parse(sortPostData?.postTargetTime, timeFormatter))
                                        }
                                        myParticipationAllData.clear()
                                        myParticipationAllData.addAll(sortedTargets)
                                    }
                                    updateUI()
                                }
                            } else {
                                updateUI()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Content>, t: Throwable) {
                        updateUI()
                    }
                })
            }
            CoroutineScope(Dispatchers.Main).launch {
                updateUI()
            }
        }
    }



    /*private fun initScrollListener(){
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
    }*/

    /*private fun getMoreItem() {
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
                api.getMyParticipantsData().enqueue(object : Callback<List<ParticipationData>> {
                    override fun onResponse(call: Call<List<ParticipationData>>, response: Response<List<ParticipationData>>) {
                        if (response.isSuccessful) {
                            //데이터 청소
                            Log.d("mypartic more", response.code().toString())
                            Log.d("mypartic more", response.body().toString())
                            val responseData = response.body()
                            totalPages = responseData?.size?.toInt()?.div(5) ?: 0
                            myParticipationAllData.clear()


                            if (responseData != null) {
                                for (i in responseData) {
                                    myParticipationApprovalOrRejectAllData.add(
                                        i.approvalOrReject
                                    )
                                }
                            }
                            Log.d("more Fragment Data", "Received data: $myParticipationApprovalOrRejectAllData")
                            CoroutineScope(Dispatchers.IO).launch {
                                setPostUserData(postList = responseData)
                            }

                        } else {
                            Log.e("f", response.code().toString())
                            Log.e("f", response.errorBody()?.string()!!)
                        }
                    }

                    override fun onFailure(call: Call<List<ParticipationData>>, t: Throwable) {
                        Log.e("error", t.toString())
                    }
                })
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