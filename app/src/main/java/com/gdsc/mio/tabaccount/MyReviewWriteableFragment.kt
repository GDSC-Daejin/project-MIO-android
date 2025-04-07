package com.gdsc.mio.tabaccount

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.mio.*
import com.gdsc.mio.adapter.MyReviewWriteableAdapter
import com.gdsc.mio.model.*
import com.gdsc.mio.databinding.FragmentMyReviewWriteableBinding
import com.gdsc.mio.loading.LoadingProgressDialogManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyReviewWriteableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyReviewWriteableFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var wBinding:FragmentMyReviewWriteableBinding
    private var wAdapter : MyReviewWriteableAdapter? = null
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var reviewWriteableReadAllData = ArrayList<PostData?>()
    private var reviewPassengersData = HashMap<String, ArrayList<ParticipationData>>()

    //로딩 즉 item의 끝이며 스크롤의 끝인지
    private var isLoading = false
    //데이터의 현재 페이지 수
    private var currentPage = 0
    //데이터의 전체 페이지 수
    private var totalPages = 0

    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var identification = ""
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
        wBinding = FragmentMyReviewWriteableBinding.inflate(inflater, container, false)

        initSwipeRefresh()
        initScrollListener()
        initRecyclerview()

        return wBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(requireActivity()).toString()


        wAdapter?.setItemClickListener(object : MyReviewWriteableAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: Int) {
                if (!wBinding.writeableSwipe.isRefreshing) { // 새로고침 중일 때는 클릭을 막음
                    val temp = reviewWriteableReadAllData[position]
                    //내가 손님일때
                    if (identification != temp?.user?.email) {
                       /* Log.e("identification", temp?.user?.email.toString())
                        Log.e("identification", temp.toString())*/
                        val intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                            putExtra("type", "PASSENGER")
                            putExtra("postDriver", temp!!.user)
                            putExtra("Data", temp)
                        }
                        requestActivity.launch(intent)
                    } else { //내가 작성자(운전자)일때
                       /* Log.e("identification driver", temp?.user?.email.toString())
                        Log.e("identification driver", temp.toString())*/
                        val intent = Intent(activity, PassengersReviewActivity::class.java).apply {
                            putExtra("type",  "DRIVER")
                            putExtra("postPassengers", reviewPassengersData[reviewWriteableReadAllData[position]?.postCreateDate])
                            putExtra("Data", temp)
                        }
                        requestActivity.launch(intent)
                    }
                }
            }
        })
    }


    private fun setReadReviewData() {
        RetrofitServerConnect.create(requireActivity()).getMyMannersWriteableReview("createDate,desc",0, 5).enqueue(object :
            Callback<PostReadAllResponse> {
            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                if (response.isSuccessful) {
                    //데이터 청소
                    reviewWriteableReadAllData.clear()
                    totalPages = response.body()?.totalPages ?: 0
                    val responseData = response.body()

                    //데드라인 체크안함
                    if (responseData != null) {
                        for (i in responseData.content.filter { it.isDeleteYN == "N" }.indices) {
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
                            reviewWriteableReadAllData.add(
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
                            if (responseData.content[i].participants?.isNotEmpty() == true /*&& identification == responseData.content[i].user.email*/) {
                                reviewPassengersData[responseData.content[i].createDate] = responseData.content[i].participants!!
                            }
                        }
                        //reviewWriteableReadAllData.sortByDescending {item -> item?.postCreateDate}
                        //wAdapter?.updateDataList(reviewWriteableReadAllData.toList().sortedByDescending { it?.postCreateDate })
                        reviewWriteableReadAllData.sortByDescending { it?.postTargetDate }
                        wAdapter?.updateDataList(reviewWriteableReadAllData.toList())
                    }
                    if (reviewWriteableReadAllData.isNotEmpty()) {
                        wBinding.writeableReviewPostNotDataLl.visibility = View.GONE
                        wBinding.writeablReviewPostRv.visibility = View.VISIBLE
                    } else {
                        wBinding.writeableReviewPostNotDataLl.visibility = View.VISIBLE
                        wBinding.writeablReviewPostRv.visibility = View.GONE
                    }

                } else {
                    requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            Toast.makeText(requireActivity(), "후기 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
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

    private fun initRecyclerview() {
        setReadReviewData()
        wAdapter = MyReviewWriteableAdapter()
        //wAdapter!!.myReviewWriteableData = reviewWriteableReadAllData

        wBinding.writeablReviewPostRv.adapter = wAdapter
        wBinding.writeablReviewPostRv.setHasFixedSize(true)
        wBinding.writeablReviewPostRv.layoutManager = manager
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        setReadReviewData()
                    }

                    1 -> {
                        refreshData()
                    }
                }
            }
        }
    }

    private fun initSwipeRefresh() {
        wBinding.writeableSwipe.setOnRefreshListener {
            refreshData()
            // 스크롤 리스너 초기화
            initScrollListener()

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                wBinding.writeableSwipe.isRefreshing = false
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1000)
        }
    }

    private fun refreshData() {
        isLoading = false
        currentPage = 0
        //moreCarpoolAllData.clear() // Clear existing data
        wAdapter?.updateDataList(emptyList()) // Notify adapter of data change
        // Fetch fresh data
        setReadReviewData()
    }

    private fun initScrollListener() {
        wBinding.writeablReviewPostRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            reviewWriteableReadAllData.add(null)
                            wAdapter?.notifyItemInserted(reviewWriteableReadAllData.size - 1)
                        }
                        wBinding.writeablReviewPostRv.post(runnable)

                        // Load more items
                        getMoreItem()
                    }
                }
            }
        })
    }

    private fun getMoreItem() {
        LoadingProgressDialogManager.show(requireContext())
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // Remove the loading item placeholder
            val loadingPosition = reviewWriteableReadAllData.indexOf(null)
            if (loadingPosition != -1) {
                reviewWriteableReadAllData.removeAt(loadingPosition)
                wAdapter?.notifyItemRemoved(loadingPosition)
            }

            if (currentPage < totalPages - 1) {
                currentPage += 1
                RetrofitServerConnect.create(requireActivity()).getMyMannersWriteableReview("createDate,desc", currentPage, 5).enqueue(object : Callback<PostReadAllResponse> {
                    override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                        if (response.isSuccessful) {
                            LoadingProgressDialogManager.hide()
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
                                reviewWriteableReadAllData.addAll(newItems)
                                wAdapter?.updateDataList(reviewWriteableReadAllData)
                                wAdapter?.notifyDataSetChanged()
                            }
                            reviewWriteableReadAllData.sortByDescending { it?.postTargetDate }
                        } else {
                            requireActivity().runOnUiThread {
                                if (isAdded && !requireActivity().isFinishing) {
                                    Toast.makeText(requireActivity(), "후기 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        isLoading = false
                    }

                    override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                        isLoading = false
                        requireActivity().runOnUiThread {
                            if (isAdded && !requireActivity().isFinishing) {
                                Toast.makeText(requireActivity(), "연결에 실패했습니다. ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
        }, 2000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyReviewWriteableFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyReviewWriteableFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}