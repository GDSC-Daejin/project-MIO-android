package com.example.mio.tabaccount

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.adapter.MyReviewWrittenAdapter
import com.example.mio.model.MyAccountReviewData
import com.example.mio.model.ReviewWrittenViewModel
import com.example.mio.databinding.FragmentMyReivewWrittenBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyReviewWrittenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyReviewWrittenFragment : Fragment() { //내가 쓴 리뷰 보는 곳
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var rwBinding : FragmentMyReivewWrittenBinding
    private var rwAdapter : MyReviewWrittenAdapter? = null
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var reviewWrittenAllData = ArrayList<MyAccountReviewData>()
    private lateinit var viewModel: ReviewWrittenViewModel
    private var loadingDialog : LoadingProgressDialog? = null

    private var isLoading = false

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
        rwBinding = FragmentMyReivewWrittenBinding.inflate(inflater, container, false)


        initSwipeRefresh()
        initRecyclerview()

        return rwBinding.root
    }

    private fun setReadReviewData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val userId = saveSharedPreferenceGoogleLogin.getUserId(activity)!!
        viewModel.setLoading(true)
        /////////////////////////////////////////////////////
        RetrofitServerConnect.create(requireActivity()).getMyMannersSendReview(userId).enqueue(object :
            Callback<List<MyAccountReviewData>> {
            override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                if (response.isSuccessful) {
                    viewModel.setLoading(false)
                    //데이터 청소
                    response.body()?.let {
                        viewModel.setReviews(it)
                        reviewWrittenAllData.clear()
                        reviewWrittenAllData.addAll(it)
                    }
                    if (reviewWrittenAllData.isEmpty()) {
                        updateUI2(reviewWrittenAllData)
                    }
                } else {
                    Log.d("f", response.code().toString())
                }
            }

            override fun onFailure(call: Call<List<MyAccountReviewData>>, t: Throwable) {
                Log.d("error", t.toString())
            }
        })
    }
    private fun updateUI2(reviews: List<MyAccountReviewData>) {
        viewModel.setLoading(false)
        if (loadingDialog != null && loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
            loadingDialog = null
        }

        Log.e("updateUI2 written", "suc")

        if (reviews.isNotEmpty()) {
            rwBinding.writtenReviewPostNotDataLl.visibility = View.GONE
            //rwBinding.writtenReviewSwipe.visibility = View.VISIBLE
            rwBinding.writtenReviewPostRv.visibility = View.VISIBLE
        } else {
            rwBinding.writtenReviewPostNotDataLl.visibility = View.VISIBLE
            //rwBinding.writtenReviewSwipe.visibility = View.GONE
            rwBinding.writtenReviewPostRv.visibility = View.GONE
        }
    }


    private fun initSwipeRefresh() {
        rwBinding.writtenSwipe.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        isLoading = false
        setReadReviewData()
        rwBinding.writtenSwipe.isRefreshing = false
    }


    private fun initRecyclerview() {
        //setReadReviewData()
        rwAdapter = MyReviewWrittenAdapter()
        //rwAdapter!!.myReviewData = reviewWrittenAllData

        rwBinding.writtenReviewPostRv.adapter = rwAdapter
        rwBinding.writtenReviewPostRv.setHasFixedSize(true)
        rwBinding.writtenReviewPostRv.layoutManager = manager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ReviewWrittenViewModel::class.java]
        setReadReviewData()
        // LiveData 관찰
        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            rwAdapter?.submitList(reviews.toList().sortedByDescending { it.createDate })
            updateUI2(reviews)
            Log.e("myreviewwritten", reviews.toString())
        }

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

        /*nfBinding.notificationSwipe.setOnRefreshListener {
            // 화면 터치 불가능하도록 설정
            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            setNotificationData()
            //viewModel.refreshNotifications(requireContext())
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                nfBinding.notificationSwipe.isRefreshing = false
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }, 1500)
        }*/

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Log.e("error observe", errorMessage)
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
         * @return A new instance of fragment MyReivewWrittenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyReviewWrittenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}