package com.example.mio.tabaccount

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.adapter.ProfileReviewAdapter
import com.example.mio.model.MyAccountReviewData
import com.example.mio.viewmodel.ReviewViewModel
import com.example.mio.databinding.FragmentProfileReviewBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileReviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileReviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var prBinding : FragmentProfileReviewBinding

    private var myAdapter : ProfileReviewAdapter? = null
    //private var profileReviewAllData = ArrayList<MyAccountReviewData>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private lateinit var viewModel: ReviewViewModel
    //로딩
    private var loadingDialog : LoadingProgressDialog? = null
    private val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
    private var profileUserId : Int? = null

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
        prBinding = FragmentProfileReviewBinding.inflate(inflater, container, false)
        profileUserId = saveSharedPreferenceGoogleLogin.getProfileUserId(requireActivity())
        //initSwipeRefresh()
        initMyRecyclerView()

        return prBinding.root
    }


    private fun initMyRecyclerView() {
       // setProfileReviewData()
        myAdapter = ProfileReviewAdapter()
        //myAdapter!!.profileReviewItemData = profileReviewAllData
        prBinding.profileReviewRv.adapter = myAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        prBinding.profileReviewRv.setHasFixedSize(true)
        prBinding.profileReviewRv.layoutManager = manager
    }

    private fun setProfileReviewData() {

        ///////////////////////////////////////////////////
        viewModel.setLoading(true)
        profileUserId?.let {
            RetrofitServerConnect.create(requireActivity()).getMyMannersReceiveReview(it).enqueue(object : Callback<List<MyAccountReviewData>> {
                override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                    if (response.isSuccessful) {
                        Log.e("reviewProfile", response.body()?.toString()!!)
                        viewModel.setLoading(false)
                        response.body()?.let { profileReview ->
                            viewModel.setReviews(profileReview)
                        }
                    } else {
                        Log.e("f", response.code().toString())
                        requireActivity().runOnUiThread {
                            if (isAdded && !requireActivity().isFinishing) {
                                Toast.makeText(requireActivity(), "후기 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<MyAccountReviewData>>, t: Throwable) {
                    requireActivity().runOnUiThread {
                        if (isAdded && !requireActivity().isFinishing) {
                            Toast.makeText(requireActivity(), "후기 정보를 가져오는데 실패했습니다. 다시 시도해주세요 ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

    }

    private fun updateUI2(reviews: List<MyAccountReviewData>) {
        viewModel.setLoading(false)
        if (loadingDialog != null && loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
            loadingDialog = null
        }

        if (reviews.isNotEmpty()) {
            prBinding.profileReviewNotDataLl.visibility = View.GONE
            //prBinding.profileReviewSwipe.visibility = View.VISIBLE
            prBinding.profileReviewRv.visibility = View.VISIBLE
        } else {
            prBinding.profileReviewNotDataLl.visibility = View.VISIBLE
            //prBinding.profileReviewSwipe.visibility = View.GONE
            prBinding.profileReviewRv.visibility = View.GONE
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]
        setProfileReviewData()
        // LiveData 관찰
        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            myAdapter?.submitList(reviews.toList())
            updateUI2(reviews)
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
                requireActivity().runOnUiThread {
                    if (isAdded && !requireActivity().isFinishing) {
                        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
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
         * @return A new instance of fragment ProfileReviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}