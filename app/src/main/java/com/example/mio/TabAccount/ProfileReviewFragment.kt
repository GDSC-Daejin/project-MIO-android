package com.example.mio.TabAccount

import android.content.Intent
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
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.*
import com.example.mio.Adapter.ProfilePostAdapter
import com.example.mio.Adapter.ProfileReviewAdapter
import com.example.mio.Model.MyAccountReviewData
import com.example.mio.Model.PostData
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.Model.ReviewViewModel
import com.example.mio.databinding.FragmentProfilePostBinding
import com.example.mio.databinding.FragmentProfileReviewBinding
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
 * Use the [ProfileReviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileReviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var prBinding : FragmentProfileReviewBinding

    private var myAdapter : ProfileReviewAdapter? = null
    private var profileReviewAllData = ArrayList<MyAccountReviewData>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private lateinit var viewModel: ReviewViewModel
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
        prBinding = FragmentProfileReviewBinding.inflate(inflater, container, false)

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
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val profileUserId = saveSharedPreferenceGoogleLogin.getProfileUserId(activity)!!
        ///////////////////////////////////////////////////
        viewModel.setLoading(true)
        RetrofitServerConnect.create(requireActivity()).getMyMannersReceiveReview(profileUserId).enqueue(object : Callback<List<MyAccountReviewData>> {
            override fun onResponse(call: Call<List<MyAccountReviewData>>, response: Response<List<MyAccountReviewData>>) {
                if (response.isSuccessful) {
                    Log.e("review", response.body()?.toString()!!)
                    response.body()?.let {
                        profileReviewAllData.clear()
                        profileReviewAllData.addAll(it)
                        viewModel.setReviews(response.body() ?: emptyList())
                        viewModel.setLoading(false)

                    }
                    if (profileReviewAllData.isEmpty()) {
                        updateUI2(profileReviewAllData)
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

        if (reviews.isNotEmpty()) {
            Log.e("reviews", "isnotempty")
            prBinding.profileReviewNotDataLl.visibility = View.GONE
            prBinding.profileReviewSwipe.visibility = View.VISIBLE
            prBinding.profileReviewRv.visibility = View.VISIBLE
        } else {
            Log.e("reviews", "isempty")
            prBinding.profileReviewNotDataLl.visibility = View.VISIBLE
            prBinding.profileReviewSwipe.visibility = View.GONE
            prBinding.profileReviewRv.visibility = View.GONE
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ReviewViewModel::class.java]
        setProfileReviewData()
        // LiveData 관찰
        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            myAdapter?.updateData(reviews.toList())
            updateUI2(reviews)
            Log.e("observereview1", reviews.toString())
            /*CoroutineScope(Dispatchers.IO).launch {
                initNotificationPostData(notificationAllData)
            }*/

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