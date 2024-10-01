package com.example.mio.tabaccount

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.adapter.MyReviewAdapter
import com.example.mio.model.*
import com.example.mio.databinding.FragmentMyReviewReadBinding
import com.example.mio.viewmodel.MyReviewReadViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyReviewReadFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyReviewReadFragment : Fragment() { //내가 받은 리뷰 보는 곳
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mrBinding : FragmentMyReviewReadBinding
    private var reviewReadAllData = ArrayList<MyAccountReviewData>()
    private var reviewAdapter : MyReviewAdapter? = null
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private lateinit var viewModel: MyReviewReadViewModel
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
        mrBinding = FragmentMyReviewReadBinding.inflate(inflater, container, false)

        initSwipeRefresh()
        initRecyclerview()

        return mrBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[MyReviewReadViewModel::class.java]
        setReadReviewData()
        // LiveData 관찰
        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            reviewAdapter?.submitList(reviews.toList().sortedByDescending { it.createDate })
            updateUI2(reviews)
            Log.e("myreviewread", reviews.toString())
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

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                Log.e("error observe", errorMessage)
            }
        }

    }

    private fun updateUI2(reviews: List<MyAccountReviewData>) {
        viewModel.setLoading(false)
        if (loadingDialog != null && loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
            loadingDialog = null
        }

        if (reviews.isNotEmpty()) {
            mrBinding.readReviewPostNotDataLl.visibility = View.GONE
            mrBinding.readReviewPostRv.visibility = View.VISIBLE
        } else {
            mrBinding.readReviewPostNotDataLl.visibility = View.VISIBLE
            mrBinding.readReviewPostRv.visibility = View.GONE
        }
    }
    private fun setReadReviewData() {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val userId = saveSharedPreferenceGoogleLogin.getUserId(activity)!!
        viewModel.setLoading(true)
        RetrofitServerConnect.create(requireActivity()).getMyMannersReceiveReview(userId).enqueue(object :
            Callback<List<MyAccountReviewData>> {
            override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                if (response.isSuccessful) {
                    viewModel.setLoading(false)
                    response.body()?.let {
                        reviewReadAllData.clear()
                        reviewReadAllData.addAll(it)
                        viewModel.setReviews(it)
                    }
                    if (reviewReadAllData.isEmpty()) {
                        updateUI2(reviewReadAllData)
                    }

                } else {
                    requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            Toast.makeText(requireActivity(), "사용자 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<MyAccountReviewData>>, t: Throwable) {
                requireActivity().runOnUiThread {
                    if (isAdded && !requireActivity().isFinishing) {
                        Toast.makeText(requireActivity(), "사용자 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun initSwipeRefresh() {
        mrBinding.reviewSwipe.setOnRefreshListener {
            // 데이터 새로 고침
            refreshData()

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                mrBinding.reviewSwipe.isRefreshing = false
            }, 1000)
        }
    }

    private fun refreshData() {
        setReadReviewData()
    }

    private fun initRecyclerview() {
        //setReadReviewData()
        reviewAdapter = MyReviewAdapter()
        //reviewAdapter!!.myReviewData = reviewReadAllData

        mrBinding.readReviewPostRv.adapter = reviewAdapter
        mrBinding.readReviewPostRv.setHasFixedSize(true)
        mrBinding.readReviewPostRv.layoutManager = manager
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyReviewReadFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyReviewReadFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}