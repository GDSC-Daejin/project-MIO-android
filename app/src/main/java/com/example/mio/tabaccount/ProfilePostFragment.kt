package com.example.mio.tabaccount

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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.adapter.ProfilePostAdapter
import com.example.mio.bottomsheetfragment.AnotherBottomSheetFragment
import com.example.mio.model.PostData
import com.example.mio.model.PostReadAllResponse
import com.example.mio.viewmodel.SharedViewModel
import com.example.mio.noticeboard.NoticeBoardReadActivity
import com.example.mio.databinding.FragmentProfilePostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfilePostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfilePostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var ppBinding : FragmentProfilePostBinding
    private var myAdapter : ProfilePostAdapter? = null
    private var profilePostAllData = ArrayList<PostData?>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var getBottomSheetData = ""
    private var dataPosition = 0
    private lateinit var myViewModel : SharedViewModel
    private var profileUserId : Int? = null

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
    ): View {
        ppBinding = FragmentProfilePostBinding.inflate(inflater, container, false)

        profileUserId = SaveSharedPreferenceGoogleLogin().getProfileUserId(requireActivity())

        initMyRecyclerView()
        initSwipeRefresh()
        initScrollListener()

        myAdapter!!.setItemClickListener(object : ProfilePostAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val temp = profilePostAllData[position]
                    dataPosition = position
                    val intent = Intent(activity, NoticeBoardReadActivity::class.java).apply {
                        putExtra("type", "READ")
                        putExtra("postItem", temp)
                        putExtra("uri", temp!!.user.profileImageUrl)
                    }
                    startActivity(intent)
                }
            }
        })

        ppBinding.profileSearchFilterBtn.setOnClickListener {
            val bottomSheet = AnotherBottomSheetFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : AnotherBottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        getBottomSheetData = value
                        myViewModel.postCheckSearchFilter(getBottomSheetData)
                    }
                })
            }
        }



        return ppBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        myViewModel.checkSearchFilter.observe(requireActivity()) {
            when(getBottomSheetData) {
                "최신 순" -> {
                    ppBinding.profileSearchFilterTv.text = "최신 순"
                    ppBinding.profileSearchFilterTv.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_blue_4))
                    profilePostAllData.sortByDescending { it?.postCreateDate }
                    myAdapter?.updateDataList(profilePostAllData.sortedByDescending { it?.postCreateDate })
                }
                "마감 임박 순" -> {
                    ppBinding.profileSearchFilterTv.text = "마감 임박 순"
                    ppBinding.profileSearchFilterTv.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_blue_4))

                    // 날짜 및 시간 형식 지정
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                    // 정렬 로직
                    val sortedTargets = profilePostAllData.sortedWith { t1, t2 ->
                        // 날짜 비교
                        val dateComparison = LocalDate.parse(t1?.postTargetDate, dateFormatter)
                            .compareTo(LocalDate.parse(t2?.postTargetDate, dateFormatter))

                        // 날짜가 같으면 시간 비교
                        if (dateComparison == 0) {
                            LocalTime.parse(t1?.postTargetTime, timeFormatter)

                                .compareTo(LocalTime.parse(t2?.postTargetTime, timeFormatter))
                        } else {
                            dateComparison
                        }
                    }
                    profilePostAllData.clear()
                    profilePostAllData.addAll(sortedTargets)
                    myAdapter?.updateDataList(sortedTargets)
                }
                "낮은 가격 순" -> {
                    ppBinding.profileSearchFilterTv.text = "낮은 가격 순"
                    ppBinding.profileSearchFilterTv.setTextColor(ContextCompat.getColor(requireActivity() ,R.color.mio_blue_4))
                    profilePostAllData.sortBy { it?.postCost }
                    myAdapter?.updateDataList(profilePostAllData.sortedBy { it?.postCost })
                }
            }

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                ppBinding.profileSwipe.isRefreshing = false
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1500)
        }
    }

    private fun initSwipeRefresh() {
        ppBinding.profileSwipe.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // 데이터 새로 고침
            refreshData()

            /* // 새로 고침 완료 및 터치 가능하게 설정
             mttBinding.moreRefreshSwipeLayout.isRefreshing = false
             this.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)*/

            // 스크롤 리스너 초기화
            initScrollListener()
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
        ppBinding.profilePostRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            profilePostAllData.add(null)
                            myAdapter?.notifyItemInserted(profilePostAllData.size - 1)
                        }
                        ppBinding.profilePostRv.post(runnable)

                        // Load more items
                        getMoreItem()
                    }
                }
            }
        })
    }

    private fun setMyPostData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val profileUserId = saveSharedPreferenceGoogleLogin.getProfileUserId(requireActivity())

        //deadLine 안씀
        RetrofitServerConnect.create(requireActivity()).getMyPostData(profileUserId,"createDate,desc", 0, 5).enqueue(object : Callback<PostReadAllResponse> {
            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                if (response.isSuccessful) {

                    //데이터 청소
                    profilePostAllData.clear()

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

                        profilePostAllData.add(
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

                    myAdapter?.updateDataList(profilePostAllData)

                    if (getBottomSheetData.isNotEmpty()) {
                        myViewModel.postCheckSearchFilter(getBottomSheetData)
                    } else {
                        // 새로 고침 완료 후 터치 활성화
                        ppBinding.profileSwipe.isRefreshing = false
                        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }

                    if (profilePostAllData.size > 0) {
                        ppBinding.profilePostNotDataLl.visibility = View.GONE
                        ppBinding.profileSwipe.visibility = View.VISIBLE
                        ppBinding.profilePostRv.visibility = View.VISIBLE
                    } else {
                        ppBinding.profilePostNotDataLl.visibility = View.VISIBLE
                        ppBinding.profileSwipe.visibility = View.GONE
                        ppBinding.profilePostRv.visibility = View.GONE
                    }

                } else {
                    requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            Toast.makeText(requireActivity(), "사용자 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                requireActivity().runOnUiThread {
                    if (isAdded && !requireActivity().isFinishing) {
                        Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getMoreItem() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // Remove the loading item placeholder
            val loadingPosition = profilePostAllData.indexOf(null)
            if (loadingPosition != -1) {
                profilePostAllData.removeAt(loadingPosition)
                myAdapter?.notifyItemRemoved(loadingPosition)
            }

            // Fetch more data if necessary
            if (currentPage < totalPages - 1) {
                currentPage += 1
                profileUserId?.let {
                    RetrofitServerConnect.create(requireActivity()).getMyPostData(it, "createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
                        override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                            if (response.isSuccessful) {
                                val responseData = response.body()
                                responseData?.let { data ->
                                    val newItems = data.content.filter { item ->
                                        item.isDeleteYN == "N" && item.postType == "BEFORE_DEADLINE"
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

                                    profilePostAllData.addAll(newItems)
                                    myAdapter?.updateDataList(profilePostAllData)
                                }
                                //profilePostAllData.sortByDescending { sortData -> sortData?.postCreateDate }
                            } else {
                                requireActivity().runOnUiThread {
                                    if (isAdded && !requireActivity().isFinishing) {
                                        Toast.makeText(requireActivity(), "게시글 정보를 가져오는데 실패했습니다. ${response.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            isLoading = false
                        }

                        override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                            requireActivity().runOnUiThread {
                                if (isAdded && !requireActivity().isFinishing) {
                                    Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            isLoading = false
                        }
                    })
                }

            }
        }, 2000)
    }

    private fun initMyRecyclerView() {
        setMyPostData()
        myAdapter = ProfilePostAdapter()
        //myAdapter!!.profilePostItemData = profilePostAllData
        ppBinding.profilePostRv.adapter = myAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        ppBinding.profilePostRv.setHasFixedSize(true)
        ppBinding.profilePostRv.layoutManager = manager
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfliePostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfilePostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}