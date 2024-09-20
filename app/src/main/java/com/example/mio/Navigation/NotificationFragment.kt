package com.example.mio.Navigation

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.NotificationAdapter
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.*
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.databinding.FragmentNotificationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.CountDownLatch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [NotificationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var nfBinding : FragmentNotificationBinding
    private lateinit var nAdapter : NotificationAdapter
    var sharedPref : SharedPref? = null
    //notification data
    private var notificationAllData : ArrayList<AddAlarmResponseData> = ArrayList()
    private var sharedViewModel: SharedViewModel? = null
    var data: NotificationData? = null
    var title : String? = null

    //알람에서 받아온 PostData를 따로 저장
    private var notificationPostAllData : ArrayList<PostData?> = ArrayList()
    private var notificationPostParticipationAllData : ArrayList<Pair<Int, ArrayList<ParticipationData>?>> = ArrayList()
    private var dataPosition = 0
    //로딩
    private var loadingDialog : LoadingProgressDialog? = null
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var identification : String? = null
    //private var hashMapCurrentPostItemData = HashMap<Int, NotificationAdapter.NotificationStatus>()
    //private var reviewWrittenAllData = ArrayList<MyAccountReviewData>()
    private lateinit var viewModel: NotificationViewModel
    //private lateinit var reviewViewModel: ReviewViewModel

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
        //activity?.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nfBinding = FragmentNotificationBinding.inflate(inflater, container, false)
        sharedPref = SharedPref(requireContext())
        identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!

        if (isAdded) {
            // 프래그먼트가 아직 액티비티에 연결되어 있는 경우에만 작업 수행
            requireActivity().runOnUiThread {
                // 뒤로가기 동작 핸들링
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // MainActivity의 changeFragment 메서드를 호출하여 HomeFragment로 전환
                        (activity as MainActivity).changeFragment(HomeFragment())

                        // 툴바도 "기본"으로 변경
                        (activity as MainActivity).toolbarType = "기본"
                        (activity as MainActivity).setToolbarView("기본")

                        // 뒤로가기 플래그 설정 초기화
                        (activity as MainActivity).isClicked = false
                        (activity as MainActivity).isSettingClicked = false

                        // 네비게이션 바의 선택된 항목을 "Home"으로 설정
                        (activity as MainActivity).mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home
                    }
                })
            }
        }

        if (arguments != null) {
            title = requireArguments().getString("title")
        }
        initNotificationRV()

        /*data = arguments?.getSerializable("notification") as NotificationData
        Log.d("data" , "$data")*/

        nfBinding.notNotificationLl.setOnClickListener {
            println(notificationAllData)
            println(data)
            println(title)
        }


        nAdapter.setItemClickListener(object : NotificationAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int?, status : NotificationAdapter.NotificationStatus) {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("notifi", "$itemId") //알람 id
                    val temp = itemId?.let { id -> //전체 알람에서 알람id를 담은 postid를 찾음
                        notificationAllData.find { it.id == id }?.postId
                    }
                    Log.e("notifi", "$temp")

                    val contentPost = temp?.let { postId -> //위 postid를 토대로 post데이터에 있는지 없는지 찾음
                        notificationPostAllData.find { it?.postID == postId }
                    }

                    if (contentPost == null) {
                        Log.e("notification", "notification")
                        withContext(context = Dispatchers.Main) {
                            Toast.makeText(requireContext(), "게시글이 삭제되었거나 종료되었습니다", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val content = notificationAllData.find { it.id == itemId }?.content
                    val statusSet = when {
                        identification == contentPost.user.email && content?.contains("탑승자") == true -> {
                            Log.e("statusSEt", "${identification}")
                            Log.e("statusSEt", "${notificationAllData.find { it.id == itemId }?.content}")
                            NotificationAdapter.NotificationStatus.Driver
                        }
                        content?.contains("님과") == true -> {
                            Log.e("statusSEt", "${notificationAllData.find { it.id == itemId }?.content}")
                            NotificationAdapter.NotificationStatus.Passenger
                        }
                        else -> {
                            Log.e("statusSet", "Neither status set")
                            NotificationAdapter.NotificationStatus.Neither
                        }
                    }

                    Log.e("statusSet", statusSet.toString())
                    Log.e("statusSet", temp.toString())
                    Log.e("statusSet", contentPost.toString())

                    val temp2 = temp.let { postId ->
                        notificationPostParticipationAllData.find { it.first == postId }?.second
                    }
                    Log.e("statusSet temp2", temp2.toString())
                    var intent : Intent? = null
                    dataPosition = position

                    // Handle different statuses
                    when(statusSet) {
                        NotificationAdapter.NotificationStatus.Passenger -> { //손님 카풀종료
                            intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                                putExtra("type", "PASSENGER")
                                putExtra("postDriver", contentPost.user)
                                putExtra("Data", contentPost)
                            }
                        }

                        NotificationAdapter.NotificationStatus.Driver -> {//운전자 카풀종료
                            if (temp2?.isNotEmpty() == true) {
                                intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                                    putExtra("type", "DRIVER")
                                    putExtra("postPassengers", temp2)
                                    putExtra("Data", contentPost)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "탑승자가 없습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        NotificationAdapter.NotificationStatus.Neither -> {
                            intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                putExtra("type", "READ")
                                putExtra("postItem", contentPost)
                            }
                        }
                    }

                    intent?.let {
                        requestActivity.launch(it)
                    }
                }
            }

            //position -> 리사이클러뷰 위치, itemId -> 알람 id값, postId -> postdata찾기위한 값인디 없을수도
            override fun onLongClick(view: View, position: Int, itemId: Int, postId: Int?) {
                //사용할 곳
                val layoutInflater = LayoutInflater.from(context)
                val dialogView = layoutInflater.inflate(R.layout.dialog_layout, null)
                val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                    .setView(dialogView)
                    .create()
                val dialogContent = dialogView.findViewById<TextView>(R.id.dialog_tv)
                val dialogLeftBtn = dialogView.findViewById<View>(R.id.dialog_left_btn)
                val dialogRightBtn =  dialogView.findViewById<View>(R.id.dialog_right_btn)

                dialogContent.text = "정말로 삭제하시겠습니까?"
                //아니오
                dialogLeftBtn.setOnClickListener {
                    alertDialog.dismiss()
                }

                dialogRightBtn.setOnClickListener {
                    viewModel.setLoading(true)
                    /////////interceptor
                    /*val SERVER_URL = BuildConfig.server_URL
                    val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                    //Authorization jwt토큰 로그인
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
                    val builder = OkHttpClient.Builder()
                    builder.interceptors().add(interceptor)
                    val client: OkHttpClient = builder.build()
                    retrofit.client(client)
                    val retrofit2: Retrofit = retrofit.build()
                    val api = retrofit2.create(MioInterface::class.java)*/
                    /////////
                    RetrofitServerConnect.create(requireContext()).deleteMyAlarm(itemId).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            viewModel.setLoading(false) // 로딩 상태 해제
                            if (response.isSuccessful) {
                                Log.d("check deleteAlarm", response.code().toString())
                                viewModel.deleteNotification(itemId)
                                alertDialog.dismiss()
                            } else {
                                Log.e("comment", response.errorBody()?.string() ?: "Unknown error")
                                viewModel.setError("Failed to delete notification: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            viewModel.setLoading(false) // 로딩 상태 해제
                            viewModel.setError("Error deleting notification: ${t.message}")
                        }
                    })
                }
                alertDialog.show()
            }
        })

        return nfBinding.root
    }


    private fun initNotificationRV() {
        //로딩창 실행
        loadingDialog = LoadingProgressDialog(requireActivity())
        loadingDialog?.setCancelable(false)
        //loadingDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //로딩창
        loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog?.window?.attributes?.windowAnimations = R.style.FullScreenDialog // 위에서 정의한 스타일을 적용
        loadingDialog?.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        loadingDialog?.show()
        /*CoroutineScope(Dispatchers.IO).launch {
            setNotificationData()
        }*/
        nAdapter = NotificationAdapter()
        nfBinding.notificationRV.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = nAdapter
        }
    }

    private fun setNotificationData() {
        viewModel.setLoading(true)
        RetrofitServerConnect.create(requireActivity()).getMyAlarm().enqueue(object : Callback<List<AddAlarmResponseData>> {
            override fun onResponse(call: Call<List<AddAlarmResponseData>>, response: Response<List<AddAlarmResponseData>>) {
                if (response.isSuccessful) {
                    println("scssucsucsucs")
                    val responseData = response.body()
                    Log.d("taxi", response.code().toString())
                    notificationAllData.clear()
                    viewModel.setLoading(false)
                    if (response.isSuccessful) {
                        viewModel.setNotifications(response.body() ?: emptyList())
                        if (responseData != null) {
                            notificationAllData.addAll(responseData)
                            if (response.body().isNullOrEmpty()) {
                                updateUI2(responseData)
                            }
                        }

                    } else {
                        viewModel.setError("Failed to load notifications")
                    }
                    /*synchronized(notificationAllData) {
                        notificationAllData.clear()
                        if (responseData != null) {
                            viewModel.setLoading(false)
                            notificationAllData.addAll(responseData)
                        }
                        viewModel.setNotifications(response.body() ?: emptyList())
                    }*/
                    //nAdapter.notifyDataSetChanged()
                    //updateUI()
                    /*CoroutineScope(Dispatchers.IO).launch {
                        initNotificationPostData(notificationAllData)
                    }*/
                } else {
                    println("faafa")
                    viewModel.setError("Failed to load notifications")
                    Log.d("comment", response.errorBody()?.string()!!)
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<List<AddAlarmResponseData>>, t: Throwable) {
                Log.d("error", t.toString())
                viewModel.setLoading(false)
                viewModel.setError("Error: ${t.message}")
            }
        })
    }


    private fun initNotificationPostData(alarmList: List<AddAlarmResponseData>?) {

        Log.e("alarmList", alarmList.toString())
        //var latch : CountDownLatch? = null
        alarmList?.let { list ->
            val latch = CountDownLatch(list.size)
            for (i in list.indices) {
                RetrofitServerConnect.create(requireActivity()).getPostIdDetailSearch(list[i].postId).enqueue(object : Callback<Content> {
                    override fun onResponse(call: Call<Content>, response: Response<Content>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            if (responseData != null && responseData.isDeleteYN == "N") {
                                notificationPostAllData.add(
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
                                    )
                                )
                                if (responseData.participants != null) {
                                    notificationPostParticipationAllData.add(Pair(responseData.postId, responseData.participants))
                                }
                            }
                        }
                        latch.countDown()
                    }

                    override fun onFailure(call: Call<Content>, t: Throwable) {
                        Log.e("Failure notification", t.toString())
                        latch.countDown()
                    }
                })
            }
            latch.await()
            // UI 업데이트는 메인 스레드에서
            CoroutineScope(Dispatchers.Main).launch {
                updateUI()
            }
        }
    }


    //나중에 리뷰막을곳
    /*private fun setReadReviewData() {
        val userId = saveSharedPreferenceGoogleLogin.getUserId(activity)!!
        reviewViewModel.setLoading(true)
        RetrofitServerConnect.create(requireActivity()).getMyMannersSendReview(userId).enqueue(object :
            Callback<List<MyAccountReviewData>> {
            override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                if (response.isSuccessful) {

                    //데이터 청소
                    response.body()?.let {
                        reviewViewModel.setLoading(false)
                        reviewViewModel.setReviews(response.body() ?: emptyList())
                        reviewWrittenAllData.clear()
                        reviewWrittenAllData.addAll(it)

                        Log.e("written", it.toString())
                    }
                    *//*if (reviewWrittenAllData.isEmpty()) {
                        updateUI2(reviewWrittenAllData)
                    }*//*
                } else {
                    Log.d("f", response.code().toString())
                }
            }

            override fun onFailure(call: Call<List<MyAccountReviewData>>, t: Throwable) {
                Log.d("error", t.toString())
            }
        })
    }*/

    private fun updateUI() {
        Log.e("updateui", "in ui")
        if (loadingDialog != null && loadingDialog?.isShowing == true)  {
            loadingDialog?.dismiss()
            loadingDialog = null
            viewModel.setLoading(false)
        }

        // Update adapter with new notification data
        //nAdapter.updateNotifications(notificationAllData)

        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
            nfBinding.notificationSwipe.visibility = View.GONE
            Log.e("notifi", "isempty")
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
            nfBinding.notificationSwipe.visibility = View.VISIBLE
            Log.e("notifi", "isnotempty")
        }

        val dataSize = notificationAllData.size.toString().ifEmpty {
            "0"
        }
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        saveSharedPreferenceGoogleLogin.setNotification(requireActivity(), dataSize)
    }


    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {

                    }
                }
            }
        }
    }
    private fun updateUI2(notifications: List<AddAlarmResponseData>) {
        viewModel.setLoading(false)
        if (loadingDialog != null && loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
            loadingDialog = null
        }
        if (notifications.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
            nfBinding.notificationSwipe.visibility = View.GONE
            Log.e("notifi", "isempty2")
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
            nfBinding.notificationSwipe.visibility = View.VISIBLE
            Log.e("notifi", "isnotempty2")
        }

        val dataSize = notifications.size.toString().ifEmpty { "0" }
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        saveSharedPreferenceGoogleLogin.setNotification(requireActivity(), dataSize)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[NotificationViewModel::class.java]
        //reviewViewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]
        setNotificationData()
       // setReadReviewData()
        // LiveData 관찰
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            nAdapter.updateNotifications(notifications.sortedByDescending { it.createDate }.toList())
            updateUI2(notifications)
            Log.e("observeNoti1", notifications.toString())
            Log.e("sortedByDescending", "${notifications.sortedByDescending { it.createDate }.toList()}")
            /*CoroutineScope(Dispatchers.IO).launch {
                initNotificationPostData(notificationAllData)
            }*/
            CoroutineScope(Dispatchers.IO).launch {
                initNotificationPostData(notifications.sortedByDescending { it.createDate }.toList())
            }
        }

        /*reviewViewModel.reviews.observe(viewLifecycleOwner) {

        }*/

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // 로딩 다이얼로그를 생성하고 표시
                if (loadingDialog == null) {
                    loadingDialog = LoadingProgressDialog(requireActivity())
                    loadingDialog?.setCancelable(false)
                    loadingDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    loadingDialog?.window?.attributes?.windowAnimations = R.style.FullScreenDialog
                    loadingDialog?.window!!.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    loadingDialog?.show()
                }
            } else {
                // 로딩 다이얼로그를 해제
                if (loadingDialog != null && loadingDialog?.isShowing == true)  {
                    loadingDialog?.dismiss()
                    loadingDialog = null
                }

            }
        }

        nfBinding.notificationSwipe.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            setNotificationData()
            //viewModel.refreshNotifications(requireContext())
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                nfBinding.notificationSwipe.isRefreshing = false
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 500)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Log.e("error observe", errorMessage)
            }
        }

    }


    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}