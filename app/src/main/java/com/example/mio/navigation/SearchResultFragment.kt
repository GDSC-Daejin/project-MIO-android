package com.example.mio.navigation

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.adapter.SearchResultAdapter
import com.example.mio.model.FragSharedViewModel
import com.example.mio.model.LocationReadAllResponse
import com.example.mio.R
import com.example.mio.RetrofitServerConnect
import com.example.mio.databinding.FragmentSearchResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultFragment : Fragment() { //안씀

    private var srbinding: FragmentSearchResultBinding? = null
    private lateinit var sradapter: SearchResultAdapter
    val sharedViewModel: FragSharedViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        srbinding = FragmentSearchResultBinding.inflate(inflater, container, false)

        //getPostsByLocation(37.870684661337016, 127.15612168310325)

        return srbinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FragmentLifeCycle", "onViewCreatedRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");

        val layoutManager = LinearLayoutManager(context)
        srbinding?.rvSearchList?.layoutManager = layoutManager

        sradapter = SearchResultAdapter(emptyList())

        // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        sradapter.setOnItemClickListener(object: SearchResultAdapter.OnItemClickListener {
            override fun onItemClicked(location: LocationReadAllResponse) {
                sharedViewModel.selectedLocation.value = location
                Log.d("SharedViewModel", "Location set to ViewModel: $location")
                print("SharedViewModel_selectedLocation_222222222222222222222222222222222222222222")

                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_content, SearchFragment())
                //transaction.addToBackStack(null)
                transaction.commit()

            }
        })

        srbinding?.rvSearchList?.adapter = sradapter

        // EditText에 리스너 추가
        srbinding?.etSearchField2?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterAndHighlightText(s.toString())
                // 여기 테스트 하고 잘되면 다른거 포함 함수로 변경
                if (s?.isNotEmpty() == true) {
                    srbinding?.rvSearchList?.visibility = View.VISIBLE
                    srbinding?.textView2?.visibility = View.GONE
                    srbinding?.textView3?.visibility = View.GONE
                    srbinding?.btnClear?.visibility = View.VISIBLE
                } else {
                    srbinding?.rvSearchList?.visibility = View.GONE
                    srbinding?.textView2?.visibility = View.VISIBLE
                    srbinding?.textView3?.visibility = View.VISIBLE
                    srbinding?.btnClear?.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 줄바꿈을 허용하지 않는 InputFilter 설정
        srbinding?.etSearchField2?.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            if (source == "\n") {  // 줄바꿈 문자 체크
                return@InputFilter ""
            }
            null
        })

        // 'x' 버튼을 누르면 EditText의 텍스트를 지우는 동작
        srbinding?.btnClear?.setOnClickListener {
            srbinding?.etSearchField2?.text?.clear()
        }

        // back_arrow 버튼 클릭 리스너 설정
        srbinding?.backArrow?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 뒤로가기 동작 핸들링
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    private fun filterAndHighlightText(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(requireContext()).getLocationPostData(query).enqueue(object :
                Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        println("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")
                        response.body()?.let { items ->
                            CoroutineScope(Dispatchers.Main).launch {
                                sradapter.updateData(items, query)

                                // 검색 결과에 따른 textView4와 textView5의 가시성 설정
                                if (items.isEmpty()) {  // 검색 결과가 없는 경우
                                    srbinding?.textView4?.visibility = View.VISIBLE
                                    srbinding?.textView5?.visibility = View.VISIBLE
                                }
                                else {  // 검색 결과가 있는 경우
                                    srbinding?.textView4?.visibility = View.GONE
                                    srbinding?.textView5?.visibility = View.GONE
                                }
                            }
                        }
                    }
                    else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
/*            val filteredList = sradapter.currentList.filter { it.location.contains(query, true) }

            withContext(Dispatchers.Main) {
                sradapter.updateData(filteredList, query)
            }*/
        }
    }
/*
    private fun getPostsByLocation(latitude: Double, longitude: Double) {
        val call = RetrofitServerConnect.service
        CoroutineScope(Dispatchers.IO).launch {
            call.getLocationPostData(latitude, longitude).enqueue(object :
                Callback<List<LocationReadAllResponse>> {
                override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                    if (response.isSuccessful) {
                        println("dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd")
                        response.body()?.let { items ->
                            CoroutineScope(Dispatchers.Main).launch {
                                sradapter.updateData(items)
                            }
                        }
                    }
                    else {
                        println("faafa")
                        Log.d("comment", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                    Log.d("error", t.toString())
                }
            })
        }

    }*/

    override fun onStart() {
        super.onStart()
        Log.d("searchResultFragment", "start")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        srbinding = null
    }

    companion object {
        fun newInstance() = SearchResultFragment()
    }
}