package com.example.mio.Navigation

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
                    val temp = notificationPostAllData[position]
                    val temp2 = notificationPostParticipationAllData.find { it.first == temp!!.postID }?.second
                    var intent : Intent? = null
                    dataPosition = position
                    //여기 구현 완료하기 클릭 시 해당 read로 이동해야함
                    when(status) {
                        NotificationAdapter.NotificationStatus.Passenger -> {//내가 손님으로 카풀이 완료되었을 떄
                            intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                                putExtra("type", "PASSENGER")
                                putExtra("postDriver", temp?.user)
                                putExtra("Data", temp)
                            }
                        }

                        NotificationAdapter.NotificationStatus.Driver -> { //내가 운전자로 카풀이 완료되었을 떄
                            if (temp2 != null) {
                                intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                                    putExtra("type", "DRIVER")
                                    putExtra("postPassengers", temp2)
                                    putExtra("Data", temp)
                                }
                            } else {
                                Toast.makeText(requireContext(), "탑승자가 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        NotificationAdapter.NotificationStatus.Neither -> {
                            intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                                putExtra("type", "READ")
                                putExtra("postItem", temp)
                                if (temp != null) {
                                    putExtra("uri", temp.user.profileImageUrl)
                                }
                            }
                        }
                        else -> {

                        }
                    }
                    requestActivity.launch(intent)
                }
            }
        })

        return nfBinding.root
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
        CoroutineScope(Dispatchers.IO).launch {
            api.getMyAlarm().enqueue(object : Callback<List<AddAlarmResponseData>> {
                override fun onResponse(call: Call<List<AddAlarmResponseData>>, response: Response<List<AddAlarmResponseData>>) {
                    if (response.isSuccessful) {
                        println("scssucsucsucs")

                        notificationAllData.clear()

                        for (i in response.body()!!.indices) {
                            notificationAllData.add(AddAlarmResponseData(
                                response.body()!![i].id,
                                response.body()!![i].createDate,
                                response.body()!![i].content,
                                response.body()!![i].postId,
                                response.body()!![i].userId
                            ))
                        }


                        Log.d("Notification Fragment Data", notificationAllData.toString())
                        initNotificationPostData()
                        nAdapter.notifyDataSetChanged()
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

        setNotificationData()
        nAdapter = NotificationAdapter()
        /*if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }*/

        nAdapter.notificationItemData = notificationAllData
        nAdapter.notificationContentItemData = notificationPostAllData
        nfBinding.notificationRV.adapter = nAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nfBinding.notificationRV.setHasFixedSize(true)
        nfBinding.notificationRV.layoutManager = manager
    }

    private fun initNotificationPostData() {
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
        /////////////////////////////////////////////////

        for (i in notificationAllData) {
            api.getPostIdDetailSearch(i.postId).enqueue(object : Callback<Content> {
                override fun onResponse(call: Call<Content>, response: Response<Content>) {
                    if (response.isSuccessful) {
                        response.body().let {
                            notificationPostAllData.add(PostData(
                                it!!.user.studentId,
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

                            if (it.participants != null) {
                                notificationPostParticipationAllData.add(Pair(it.postId,it.participants))
                            }
                        }
                        if (loadingDialog != null && loadingDialog!!.isShowing) {
                            loadingDialog?.dismiss()
                            loadingDialog = null // 다이얼로그 인스턴스 참조 해제
                        }
                    } else {
                        Log.e("fail notififrag", response.code().toString())
                        Log.e("fail notififrag", response.errorBody().toString())
                    }
                }

                override fun onFailure(call: Call<Content>, t: Throwable) {
                    Log.e("Failure notification", t.toString())
                }
            })
        }
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