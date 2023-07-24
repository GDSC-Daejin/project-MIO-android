package com.example.mio.SearchTap

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.CalendarAdapter
import com.example.mio.Adapter.NoticeBoardAdapter
import com.example.mio.Adapter.SearchAdapter
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.PostData
import com.example.mio.Model.PostReadAllResponse
import com.example.mio.Model.SharedViewModel
import com.example.mio.R
import com.example.mio.RetrofitServerConnect
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.FragmentPostSearchBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostSearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostSearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var psBinding : FragmentPostSearchBinding? = null

    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private var sAdapter : SearchAdapter? = null

    private var searchAllData : ArrayList<PostData> = ArrayList()

    private var sharedPref : SharedPref? = null

    private var isSearch = false
    //검색 테스트
    private var filterData : ArrayList<PostData> = ArrayList()

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
        psBinding = FragmentPostSearchBinding.inflate(inflater, container, false)
        sharedPref = this.context?.let { SharedPref(it) }

        initSearchRecyclerView()

        psBinding!!.testbtn.setOnClickListener {
            val se = sharedPref!!.getSearchData().toString()
            Log.d("search", se)
        }

        return psBinding!!.root
    }

    private fun setSearchData() {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getServerPostData().enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        //println(response.body()!!.content)
                        /*val start = SystemClock.elapsedRealtime()

                        // 함수 실행시간
                        val date = Date(start)
                        val mFormat = SimpleDateFormat("HH:mm:ss")
                        val time = mFormat.format(date)
                        println(start)
                        println(time)*/
                        /*val s : ArrayList<PostReadAllResponse> = ArrayList()
                        s.add(PostReadAllResponse())*/


                        for (i in response.body()!!.content.indices) {
                            //탑승자 null체크
                            var part = 0
                            var location = ""
                            var title = ""
                            var content = ""
                            var targetDate = ""
                            var targetTime = ""
                            var categoryName = ""
                            var cost = 0
                            if (response.isSuccessful) {
                                part = try {
                                    response.body()!!.content[i].participants.isEmpty()
                                    response.body()!!.content[i].participants.size
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                                location = try {
                                    response.body()!!.content[i].location.isEmpty()
                                    response.body()!!.content[i].location
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "수락산역 3번 출구"
                                }
                                title = try {
                                    response.body()!!.content[i].title.isEmpty()
                                    response.body()!!.content[i].title
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                content = try {
                                    response.body()!!.content[i].content.isEmpty()
                                    response.body()!!.content[i].content
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetDate = try {
                                    response.body()!!.content[i].targetDate.isEmpty()
                                    response.body()!!.content[i].targetDate
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                targetTime = try {
                                    response.body()!!.content[i].targetTime.isEmpty()
                                    response.body()!!.content[i].targetTime
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                categoryName = try {
                                    response.body()!!.content[i].category.categoryName.isEmpty()
                                    response.body()!!.content[i].category.categoryName
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    "null"
                                }
                                cost = try {
                                    response.body()!!.content[i].cost
                                    response.body()!!.content[i].cost
                                } catch (e : java.lang.NullPointerException) {
                                    Log.d("null", e.toString())
                                    0
                                }
                            }

                            //println(response!!.body()!!.content[i].user.studentId)
                            searchAllData.add(PostData(
                                response.body()!!.content[i].user.studentId,
                                response.body()!!.content[i].postId,
                                title,
                                content,
                                targetDate,
                                targetTime,
                                categoryName,
                                location,
                                //participantscount가 현재 참여하는 인원들
                                part,
                                //numberOfPassengers은 총 탑승자 수
                                response.body()!!.content[i].numberOfPassengers,
                                cost
                            ))
                            sAdapter!!.notifyDataSetChanged()
                        }
                    } else {
                        Log.d("f", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }
    }

    private fun initSearchRecyclerView() {
        setSearchData()
        sAdapter = SearchAdapter()

        psBinding!!.postSearchRv.adapter = sAdapter
        //psBinding .adapter = noticeBoardAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        psBinding!!.postSearchRv.setHasFixedSize(true)
        psBinding!!.postSearchRv.layoutManager = manager

        if (sharedPref!!.getSearchData().isNotEmpty()) {
            val se = sharedPref!!.getSearchData().toString()
            isSearch = true
            //sAdapter!!.filter.filter(se)
            filterData.clear()

            val filterString = sharedPref!!.getSearchData().toString().lowercase(Locale.getDefault()).trim {it < ' '}
            println("fs" + filterString)
            /*for (item in searchData) {
                if (item!!.content!!.lowercase(Locale.getDefault()).contains(filterString)) {
                    filterData.add(item)
                }
            }*/
            for (searchItem in searchAllData) {
                if (searchItem!!.postContent!!.lowercase(Locale.getDefault()).contains(filterString)) {
                    //println("tem - $tem")
                    filterData.add(searchItem)
                }
            }
            /*filterData = searchData.filter {
                it!!.content == searchQuery
            } as MutableList<TodoListData?>*/
            if (filterData.isEmpty()) {
                //검색했을 때 없을 경우 여기서 뷰 처리
                /*homeBinding.searchNotTv.visibility = View.VISIBLE
                todoAdapter!!.listData = filterData*/
                println("nonone")
            } else {
                /*homeBinding.searchNotTv.visibility = View.GONE
                todoAdapter!!.listData = filterData*/
                println("ysss")
            }
        }
        println("search"+filterData)
        sAdapter!!.searchData = filterData
        sAdapter!!.notifyDataSetChanged()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PostSearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PostSearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}