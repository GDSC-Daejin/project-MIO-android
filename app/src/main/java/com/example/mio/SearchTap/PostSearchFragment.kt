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
import com.example.mio.Model.SharedViewModel
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.FragmentPostSearchBinding
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    private fun testData() {
        searchAllData.add(PostData("2020", 2, "aaaa", "aaaa", "22", "22"))
        searchAllData.add(PostData("2020", 2, "bbb", "bbb", "22", "22"))
        searchAllData.add(PostData("2020", 2, "cc", "cc", "22", "22"))
        searchAllData.add(PostData("2020", 2, "csa", "csa", "22", "22"))
    }

    private fun initSearchRecyclerView() {
        testData()
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