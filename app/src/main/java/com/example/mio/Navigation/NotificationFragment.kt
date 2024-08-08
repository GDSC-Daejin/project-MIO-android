package com.example.mio.Navigation

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
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
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

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
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    var sharedPref : SharedPref? = null
    private var setKey = "setting_history"
    //notification data
    private var notificationAllData : ArrayList<AddAlarmResponseData> = ArrayList()
    private var sharedViewModel: SharedViewModel? = null
    var data: NotificationData? = null
    var title : String? = null

    //알람에서 받아온 PostData를 따로 저장
    private var notificationPostAllData : ArrayList<PostData?> = ArrayList()
    private var notificationPostParticipationAllData : ArrayList<Pair<Int, ArrayList<Participants>?>> = ArrayList()
    private var dataPosition = 0
    //로딩
    private var loadingDialog : LoadingProgressDialog? = null
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var identification : String? = null
    //private var hashMapCurrentPostItemData = HashMap<Int, NotificationAdapter.NotificationStatus>()

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
                    val temp = notificationAllData.find { it.id == itemId }?.postId
                    val contentPost = notificationPostAllData.find { it?.postID == temp } //선택한 알람의 포스터
                    //본인이 작성자(운전자) 이면서 카풀이 완료
                    // 카풀 종료시 운전자에게 후기 작성하라는 알림 발송
                    val statusSet = if (identification == contentPost?.user?.email &&  notificationAllData.find { it.id == itemId }?.content?.contains("탑승자") == true) {
                        NotificationAdapter.NotificationStatus.Driver
                    } else if (notificationAllData.find { it.id == itemId }?.content?.contains("님과") == true) { //본인은 운전자가 아니고 손님
                        NotificationAdapter.NotificationStatus.Passenger
                    } else { //그냥 자기 게시글 확인
                        NotificationAdapter.NotificationStatus.Neither
                    }

                    //손님 리스트
                    val temp2 = notificationPostParticipationAllData.find { it.first == temp }?.second
                    var intent : Intent? = null
                    dataPosition = position
                    //여기 구현 완료하기 클릭 시 해당 read로 이동해야함
                    when(statusSet) {
                        NotificationAdapter.NotificationStatus.Passenger -> {//내가 손님으로 카풀이 완료되었을 떄
                            intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                                putExtra("type", "PASSENGER")
                                putExtra("postDriver", contentPost?.user)
                                putExtra("Data", contentPost)
                            }
                        }

                        NotificationAdapter.NotificationStatus.Driver -> { //내가 운전자로 카풀이 완료되었을 떄
                            if (temp2 != null) {
                                intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                                    putExtra("type", "DRIVER")
                                    putExtra("postPassengers", temp2)
                                    putExtra("Data", contentPost)
                                }
                            } else {
                                Toast.makeText(requireContext(), "탑승자가 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        NotificationAdapter.NotificationStatus.Neither -> {
                            intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                putExtra("type", "READ")
                                putExtra("postItem", contentPost)
                            }
                        }
                        else -> {

                        }
                    }
                    requestActivity.launch(intent)
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
                    val now = System.currentTimeMillis()
                    val date = Date(now)
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                    val currentDate = sdf.format(date)
                    val formatter = DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                    val result: Instant = Instant.from(formatter.parse(currentDate))

                    val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
                    val token = saveSharedPreferenceGoogleLogin.getToken(requireActivity()).toString()
                    val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(requireActivity()).toString()

                    /////////interceptor
                    val SERVER_URL = BuildConfig.server_URL
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
                    val builder = OkHttpClient.Builder()
                    builder.interceptors().add(interceptor)
                    val client: OkHttpClient = builder.build()
                    retrofit.client(client)
                    val retrofit2: Retrofit = retrofit.build()
                    val api = retrofit2.create(MioInterface::class.java)
                    /////////
                    api.deleteMyAlarm(itemId).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Log.d("check deleteAlarm", response.code().toString())
                            } else {
                                println("faafa")
                                println(response.code())
                                Log.e("comment", response.errorBody()?.string()!!)
                                Log.e("message", call.request().toString())
                                response.errorBody().toString()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.d("error", t.toString())
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
        CoroutineScope(Dispatchers.IO).launch {
            setNotificationData()
        }
        nAdapter = NotificationAdapter()
        nAdapter.notificationItemData = notificationAllData
        //nAdapter.notificationContentItemData = notificationPostAllData
        nfBinding.notificationRV.adapter = nAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nfBinding.notificationRV.setHasFixedSize(true)
        nfBinding.notificationRV.layoutManager = manager


        /*if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }*/
    }

    private fun setNotificationData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(requireActivity()).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(requireActivity()).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
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
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////////
        api.getMyAlarm().enqueue(object : Callback<List<AddAlarmResponseData>> {
            override fun onResponse(call: Call<List<AddAlarmResponseData>>, response: Response<List<AddAlarmResponseData>>) {
                if (response.isSuccessful) {
                    println("scssucsucsucs")
                    val responseData = response.body()
                    Log.d("taxi", response.code().toString())
                    //데이터 청소
                    notificationAllData.clear()

                    if (responseData != null) {
                        for (i in responseData) {
                            notificationAllData.add(
                                AddAlarmResponseData(
                                    i.id,
                                    i.createDate,
                                    i.content,
                                    i.postId,
                                    i.userId
                                )
                            )
                        }
                    }
                    Log.d("Notification Fragment Data", "Received data: $notificationAllData")
                    nAdapter.notifyDataSetChanged()
                    updateUI()
                    CoroutineScope(Dispatchers.IO).launch {
                        initNotificationPostData(notificationAllData)
                    }
                } else {
                    println("faafa")
                    Log.d("comment", response.errorBody()?.string()!!)
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<List<AddAlarmResponseData>>, t: Throwable) {
                Log.d("error", t.toString())
            }
        })
    }


    private fun initNotificationPostData(alarmList: ArrayList<AddAlarmResponseData>?) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(requireActivity()).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(requireActivity()).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token.isNotEmpty()) { // 토큰이 있는 경우
                newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    requireActivity().finish()
                    return@Interceptor chain.proceed(newRequest)
                }
            } else {
                newRequest = chain.request()
            }
            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit2 = retrofit.newBuilder().client(client).build()
        val api = retrofit2.create(MioInterface::class.java)
        /////////////////////////////////////////////////

        var shouldBreak = false
        Log.e("alarmList", alarmList.toString())
        var latch : CountDownLatch? = null
        if (alarmList?.isNotEmpty() == true) {
            for (i in alarmList.indices) {
                //요청의 수와 동일한 초기 카운트를 가진 CountDownLatch를 생성합니다.
                latch = CountDownLatch(alarmList.size)
                if (shouldBreak) break
                Log.e("alarm", alarmList[i].toString())
                api.getPostIdDetailSearch(alarmList[i].postId).enqueue(object : Callback<Content> {
                    override fun onResponse(call: Call<Content>, response: Response<Content>) {
                        if (response.isSuccessful) {
                            val responseData = response.body()
                            Log.e("indexout check", response.body().toString())
                            if (responseData != null) {
                                if (responseData.isDeleteYN == "N" && responseData.postType == "BEFORE_DEADLINE") {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        response.body()?.let {
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
                                            Log.e("indexout check post", notificationPostAllData.toString())
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e("fail notififrag", response.code().toString())
                            Log.e("fail notififrag", response.errorBody()?.string()!!)
                            shouldBreak = true
                        }
                        latch.countDown() // 요청 완료 시 감소
                    }

                    override fun onFailure(call: Call<Content>, t: Throwable) {
                        Log.e("Failure notification", t.toString())
                        shouldBreak = true
                        latch.countDown() // 요청 완료 시 감소
                    }
                })
            }
        }
        // UI 업데이트는 메인 스레드에서
        // 비동기 요청이 모두 완료될 때까지 기다림
        //요청 완료 대기: 모든 요청이 완료될 때까지 latch.await()로 대기합니다. 모든 요청이 완료되면 UI를 업데이트합니다.
        /*Thread {
            latch?.await()
            requireActivity().runOnUiThread {

            }
        }.start()*/

        /*// UI 업데이트는 메인 스레드에서
        requireActivity().runOnUiThread {

            nAdapter.notifyDataSetChanged()

            if (notificationAllData.isEmpty()) {
                nfBinding.notNotificationLl.visibility = View.VISIBLE
                nfBinding.nestedScrollView.visibility = View.GONE
            } else {
                val dataSize = notificationAllData.size.toString().ifEmpty {
                    "0"
                }

                saveSharedPreferenceGoogleLogin.setNotification(requireActivity(), dataSize)
                nfBinding.notNotificationLl.visibility = View.GONE
                nfBinding.nestedScrollView.visibility = View.VISIBLE
            }
        }*/
    }

    private fun updateUI() {
        Log.e("updateui", "in ui")
        loadingDialog?.dismiss()
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog?.dismiss()
            loadingDialog = null // 다이얼로그 인스턴스 참조 해제
        }
        nAdapter.notifyDataSetChanged()
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
            nfBinding.nestedScrollView.visibility = View.GONE
        } else {
            val dataSize = notificationAllData.size.toString().ifEmpty {
                "0"
            }
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            saveSharedPreferenceGoogleLogin.setNotification(requireActivity(), dataSize)
            nfBinding.notNotificationLl.visibility = View.GONE
            nfBinding.nestedScrollView.visibility = View.VISIBLE
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

    }

    override fun onStart() {
        super.onStart()
        println("start")
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        println("resume")
        //getHistory()
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }
    }


    override fun onPause() {
        super.onPause()
        println("pause")
        if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }
    }



    /*private fun getHistory() {
        val historyData = sharedPref!!.getNotify(requireContext(), setKey)

        if (historyData.isNotEmpty()) {
            //notificationAllData.clear()
            //searchWordList.addAll(historyData)
            for (i in historyData.indices) {
                //notificationAllData.add(NotificationData(i,"test", PostData("20201530@daejin.ac.kr", 0, "test", "test", "test", "test", "a","10" ,1, 4, 3000, true, User), true, historyData[i]))
                println(historyData[i])
                println(notificationAllData)
            }
            nAdapter!!.notifyDataSetChanged()
        } else {

        }


    }*/


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