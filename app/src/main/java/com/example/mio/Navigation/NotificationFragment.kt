package com.example.mio.Navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.MyAccountPostAdapter
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Adapter.NotificationAdapter
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.*
import com.example.mio.NoticeBoard.NoticeBoardReadActivity
import com.example.mio.databinding.FragmentNotificationBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
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
    private var notificationPostAllData : ArrayList<AlarmPost> = ArrayList()

    private var dataPosition = 0

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
        setHasOptionsMenu(true)

        initNotificationRV()

        /*data = arguments?.getSerializable("notification") as NotificationData
        Log.d("data" , "$data")*/

        nfBinding.notNotificationLl.setOnClickListener {
            println(notificationAllData)
            println(data)
            println(title)
        }


        nAdapter!!.setItemClickListener(object : NotificationAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = notificationPostAllData[position]
                    dataPosition = position
                    //여기 구현 완료하기 클릭 시 해당 read로 이동해야함
                    val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp!!.user.profileImageUrl)
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
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
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
                                response.body()!![i].post,
                                response.body()!![i].userEntity
                            ))

                            notificationPostAllData.add(AlarmPost(
                                response.body()!![i].post.id,
                                response.body()!![i].post.title,
                                response.body()!![i].post.content,
                                response.body()!![i].post.createDate,
                                response.body()!![i].post.targetDate,
                                response.body()!![i].post.targetTime,
                                response.body()!![i].post.verifyGoReturn,
                                response.body()!![i].post.numberOfPassengers,
                                response.body()!![i].post.viewCount,
                                response.body()!![i].post.verifyFinish,
                                response.body()!![i].post.latitude,
                                response.body()!![i].post.longitude,
                                response.body()!![i].post.bookMarkCount,
                                response.body()!![i].post.participantsCount,
                                response.body()!![i].post.location,
                                response.body()!![i].post.cost,
                                response.body()!![i].post.category,
                                response.body()!![i].post.commentList,
                                response.body()!![i].post.user,
                                response.body()!![i].post.participants
                            ))
                        }

                        nAdapter.notifyDataSetChanged()

                        if (notificationAllData.isEmpty()) {
                            nfBinding.notNotificationLl.visibility = View.VISIBLE
                            nfBinding.notificationRV.visibility = View.GONE
                        } else {
                            nfBinding.notNotificationLl.visibility = View.GONE
                            nfBinding.notificationRV.visibility = View.VISIBLE
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
    }



    private fun initNotificationRV() {
        setNotificationData()
        nAdapter = NotificationAdapter()
        /*if (notificationAllData.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
        }*/

        nAdapter!!.notificationItemData = notificationAllData
        nfBinding.notificationRV.adapter = nAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nfBinding.notificationRV.setHasFixedSize(true)
        nfBinding.notificationRV.layoutManager = manager

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