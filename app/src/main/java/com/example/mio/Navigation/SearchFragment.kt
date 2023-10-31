package com.example.mio.Navigation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.Adapter.SearchTabAdapter
import com.example.mio.Adapter.SearchWordAdapter
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.SearchWordData
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.FragmentSearchBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var sBinding : FragmentSearchBinding? = null
    private val tabTextList = listOf("게시물", "위치")
    private var sharedPref : SharedPref? = null
    private var swAdapter : SearchWordAdapter? = null
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)

    //context를 따로 저장하기 위함
    private var sContext : Context? = null

    //검색어 저장용 키
    private var setKey = "setting_search_history"

    //검색어 저장 데이터
    private var searchWordList : ArrayList<SearchWordData> = ArrayList()
    private var searchPos = 0
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
        sBinding = FragmentSearchBinding.inflate(inflater, container, false)
        sharedPref = this.context?.let { SharedPref(it) }
        sContext = this.context

        initRecentSearchRecyclerview()

        getHistory()


        val searchViewTextListener: SearchView.OnQueryTextListener =
            object : SearchView.OnQueryTextListener {
                //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
                override fun onQueryTextSubmit(s: String): Boolean {
                    if (s.isEmpty()) {
                        //최근 검색어 뷰 보여주기 또는 검색어가 비었다고 알려주기기
                        Toast.makeText(requireActivity(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
                   } else {
                        sBinding!!.searchRV.visibility = View.GONE
                        sBinding!!.searchViewpager.visibility = View.VISIBLE
                        sBinding!!.searchTabLayout.visibility = View.VISIBLE

                        searchWordList.add(SearchWordData(searchPos, s))
                        searchPos += 1
                        //검색어 저장해서 다른 fragment에 넘길 준비
                        sharedPref!!.setSearchData(s)
                        //최근 검색어 배열 저장
                        sContext?.let { sharedPref!!.setSearchHistory(it, setKey, searchWordList) }

                        //뷰페이저 생성
                        sBinding!!.searchViewpager.adapter = SearchTabAdapter(requireActivity())
                        TabLayoutMediator(sBinding!!.searchTabLayout, sBinding!!.searchViewpager) { tab, pos ->
                            tab.text = tabTextList[pos]
                        }.attach()
                    }
                    Log.d("this", "SearchVies Text is changed : $s")
                    return false
                }

                //텍스트 입력/수정시에 호출
                override fun onQueryTextChange(s: String): Boolean {
                    if (s.isEmpty()) {
                        sBinding!!.searchRV.visibility = View.VISIBLE
                        sBinding!!.searchViewpager.visibility = View.GONE
                        sBinding!!.searchTabLayout.visibility = View.GONE
                    }
                    return false
                }
            }
        sBinding!!.searchEt.setOnQueryTextListener(searchViewTextListener)

        return sBinding!!.root
    }
    private fun initRecentSearchRecyclerview() {
        swAdapter = SearchWordAdapter()
        swAdapter!!.searchWordData = searchWordList
        sBinding!!.searchRV.adapter = swAdapter
        sBinding!!.searchRV.layoutManager = LinearLayoutManager(sContext)
        sBinding!!.searchRV.setHasFixedSize(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sBinding = null
    }


    private fun getHistory() {
        val historyData = sContext?.let { sharedPref!!.getSearchHistory(it, setKey) }
        if (historyData!!.isNotEmpty()) {
            searchWordList.clear()
            //searchWordList.addAll(historyData)
            for (i in historyData.indices) {
                searchWordList.add(SearchWordData(i,historyData[i]))
                println(historyData[i])
                println(searchWordList)
                swAdapter!!.notifyDataSetChanged()
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
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}