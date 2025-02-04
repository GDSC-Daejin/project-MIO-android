package com.gdsc.mio.navigation

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdsc.mio.PassengersReviewActivity
import com.gdsc.mio.R
import com.gdsc.mio.RetrofitServerConnect
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin
import com.gdsc.mio.adapter.NotificationAdapter
import com.gdsc.mio.databinding.FragmentNotificationBinding
import com.gdsc.mio.helper.SharedPref
import com.gdsc.mio.loading.LoadingProgressDialogManager
import com.gdsc.mio.model.AddAlarmResponseData
import com.gdsc.mio.model.NotificationData
import com.gdsc.mio.noticeboard.NoticeBoardReadActivity
import com.gdsc.mio.viewmodel.NotificationViewModel
import com.gdsc.mio.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private var sharedPref : SharedPref? = null
    //notification data
    //private var notificationAllData : ArrayList<AddAlarmResponseData> = ArrayList()
    private var sharedViewModel: SharedViewModel? = null
    var data: NotificationData? = null
    var title : String? = null

    //알람에서 받아온 PostData를 따로 저장
    //private var notificationPostAllData : ArrayList<PostData?> = ArrayList()
    //private var notificationPostParticipationAllData : ArrayList<Pair<Int, ArrayList<ParticipationData>?>> = ArrayList()
    private var dataPosition = 0

    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var identification : String? = null
    //private var hashMapCurrentPostItemData = HashMap<Int, NotificationAdapter.NotificationStatus>()
    //private var reviewWrittenAllData = ArrayList<MyAccountReviewData>()
    private lateinit var viewModel: NotificationViewModel
    //private lateinit var reviewViewModel: ReviewViewModel
    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

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

        /*nfBinding.notNotificationLl.setOnClickListener {
            println(notificationAllData)
            println(data)
            println(title)
        }*/


        nAdapter.setItemClickListener(object : NotificationAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int?, status : NotificationAdapter.NotificationStatus) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = itemId?.let { id -> //전체 알람에서 알람id를 담은 postid를 찾음
                        viewModel.notifications.value?.find { it.id == id }?.postId
                    }

                    val contentPost = temp?.let { postId -> //위 postid를 토대로 post데이터에 있는지 없는지 찾음
                        viewModel.notificationsPostData.value.find { it.postID == postId }
                    }

                    if (contentPost == null) {
                        withContext(context = Dispatchers.Main) {
                            Toast.makeText(requireContext(), "게시글이 삭제되었거나 종료되었습니다", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val content =  viewModel.notifications.value?.find { it.id == itemId }?.content
                    val statusSet = when {
                        identification == contentPost.user.email && content?.contains("탑승자") == true -> {
                            NotificationAdapter.NotificationStatus.Driver
                        }
                        content?.contains("님과") == true -> {
                            NotificationAdapter.NotificationStatus.Passenger
                        }
                        else -> {
                            NotificationAdapter.NotificationStatus.Neither
                        }
                    }

                    val temp2 = temp.let { postId ->
                        viewModel.notificationsParticipationData.value.find { it.first == postId }?.second
                    }
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
                    viewModel.setLoading(false)
                    alertDialog.dismiss()
                }

                dialogRightBtn.setOnClickListener {
                    viewModel.setLoading(true)
                    RetrofitServerConnect.create(requireContext()).deleteMyAlarm(itemId).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            viewModel.setLoading(false) // 로딩 상태 해제
                            if (response.isSuccessful) {
                                viewModel.deleteNotification(itemId)
                                alertDialog.dismiss()
                            } else {
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
        LoadingProgressDialogManager.show(requireContext())
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
                    val responseData = response.body()
                    //notificationAllData.clear()
                    viewModel.setLoading(false)
                    if (response.isSuccessful) {
                        viewModel.setNotifications(responseData ?: emptyList())
                        if (responseData != null) {
                            updateUI2(responseData)
                        }

                    } else {
                        viewModel.setError("Failed to load notifications")
                    }
                } else {
                    viewModel.setError("Failed to load notifications")

                }
            }

            override fun onFailure(call: Call<List<AddAlarmResponseData>>, t: Throwable) {
                viewModel.setLoading(false)
                viewModel.setError("Error: ${t.message}")
            }
        })
    }
    private fun updateUI2(notifications: List<AddAlarmResponseData>) {
        viewModel.setLoading(false)
        LoadingProgressDialogManager.hide()
        if (notifications.isEmpty()) {
            nfBinding.notNotificationLl.visibility = View.VISIBLE
            nfBinding.notificationSwipe.visibility = View.GONE
        } else {
            nfBinding.notNotificationLl.visibility = View.GONE
            nfBinding.notificationSwipe.visibility = View.VISIBLE
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
        //setNotificationData()
        viewModel.fetchNotificationData(requireActivity())

       // setReadReviewData()
        // LiveData 관찰
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            nAdapter.updateNotifications(notifications.sortedByDescending { it.createDate }.toList())
            updateUI2(notifications)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                LoadingProgressDialogManager.show(requireContext())
            } else {
                LoadingProgressDialogManager.hide()
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
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()//Log.e("error observe", errorMessage)
            }
        }

    }


    override fun onStart() {
        super.onStart()
        view?.post {
            val currentActivity = activity ?: return@post

            if (isAdded) {
                currentActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        isEnabled = false
                        sharedViewModel?.setNotificationType("알림")
                        currentActivity.supportFragmentManager.popBackStack()
                    }
                })
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