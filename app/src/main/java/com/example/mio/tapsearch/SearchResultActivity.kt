package com.example.mio.tapsearch

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.adapter.RecentSearchAdapter
import com.example.mio.adapter.SearchResultAdapter
import com.example.mio.BuildConfig
import com.example.mio.helper.SharedPrefManager
import com.example.mio.helper.SharedPrefManager.convertLocationToJSON
import com.example.mio.model.*
import com.example.mio.RetrofitServerConnect
import com.example.mio.databinding.ActivitySearchResultBinding
import com.example.mio.viewmodel.FragSharedViewModel
import com.example.mio.viewmodel.FragSharedViewModel2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchResultActivity : AppCompatActivity() { //검색창

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        private const val API_KEY = BuildConfig.map_api_key
    }

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var adapter: SearchResultAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private var searchWord : String? = null

    val sharedViewModel: FragSharedViewModel by lazy {
        (application as FragSharedViewModel2).sharedViewModel
    }

    //검색창에서 이동된건지, 계정설정에서 이동된 건지 타입 확인
    private var type : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = intent.getStringExtra("type") //account 받음

        val layoutManager = LinearLayoutManager(this)
        binding.rvSearchList.layoutManager = layoutManager

        setupAdapter(emptyList())

        // 최근 검색 리스트의 설정
        recentSearchAdapter = RecentSearchAdapter(emptyList()).apply {
            setOnItemClickListener(object : RecentSearchAdapter.OnItemClickListener {
                override fun onItemClicked(location: LocationReadAllResponse) {
                    sharedViewModel.selectedLocation.value = location
                    Log.e("searchresultintent", "click recent")
                    Log.e("searchresultintent", location.location)
                    val locationJson = convertLocationToJSON(location)
                    Log.e("searchresultintent", locationJson)
                    SharedPrefManager.saveRecentSearch(this@SearchResultActivity, location)
                    moveToSearchFragment(location)

                    val resultIntent = Intent().apply {
                        putExtra("flag", 103) // 필요한 결과 값을 설정
                        putExtra("location", location)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish() // Activity 종료
                }
                override fun onItemRemove(location: LocationReadAllResponse) {
                    // 선택된 위치를 SharedPref에서 제거합니다.
                    val locationJson = SharedPrefManager.convertLocationToJSON(location)
                    SharedPrefManager.removeRecentSearch(this@SearchResultActivity, location.location)

                    // 최근 검색어 목록을 다시 로드하여 UI를 업데이트 합니다.
                    loadRecentSearch()
                }
            })
        }
        binding.rvRecentSearchList.adapter = recentSearchAdapter
        binding.rvRecentSearchList.layoutManager = LinearLayoutManager(this)

        loadRecentSearch()

        binding.rvSearchList.adapter = adapter

        binding.etSearchField2.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //filterAndHighlightText(s.toString())
                if (s?.isNotEmpty() == true) {
                    searchWord = s.toString()
                    binding.rvSearchList.visibility = View.VISIBLE
                    binding.rvRecentSearchList.visibility = View.GONE
                    binding.textView2.visibility = View.GONE
                    binding.textView3.visibility = View.GONE
                    binding.btnClear.visibility = View.VISIBLE
                } else {
                    binding.rvSearchList.visibility = View.GONE
                    binding.rvRecentSearchList.visibility = View.VISIBLE
                    binding.textView2.visibility = View.VISIBLE
                    binding.textView3.visibility = View.VISIBLE
                    binding.btnClear.visibility = View.INVISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.etSearchField2.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 완료 키가 눌렸을 때 수행할 작업을 여기에 작성합니다.
                // 예를 들어, 키보드를 숨기거나 입력을 처리할 수 있습니다.
                // true를 반환하여 이벤트를 소비하고 더 이상 처리하지 않도록 합니다.
                if (type != "account") {
                    if (searchWord != null) {
                        filterAndHighlightText(searchWord.toString())
                    }
                }
                true
            } else {
                // 다른 키 입력 이벤트에 대한 기본 동작을 유지합니다.
                false
            }
        }

        binding.etSearchField2.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source == "\n") {
                return@InputFilter ""
            }
            null
        })

        binding.btnClear.setOnClickListener {
            binding.etSearchField2.text?.clear()
            binding.textView4.visibility = View.GONE
            binding.textView5.visibility = View.GONE
        }

        binding.backArrow.setOnClickListener {
            finish() // 액티비티 종료
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // 액티비티 종료
            }
        })
    }



    private fun filterAndHighlightText(query: String) {
        RetrofitServerConnect.create(this@SearchResultActivity).getLocationPostData(query).enqueue(object :
            Callback<List<LocationReadAllResponse>> {
            override fun onResponse(call: Call<List<LocationReadAllResponse>>, response: Response<List<LocationReadAllResponse>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    //val responseDataFilter = ArrayList<LocationReadAllResponse>()
                    Log.d("searchResultActivity filterhigh", responseData.toString())
                    if (responseData.isNullOrEmpty()) {
                        binding.textView4.visibility = View.VISIBLE
                        binding.textView5.visibility = View.VISIBLE
                        binding.rvSearchList.visibility = View.GONE
                    }
                    else {
                        adapter.updateData(responseData, query)
                        binding.textView4.visibility = View.GONE
                        binding.textView5.visibility = View.GONE
                        binding.rvSearchList.visibility = View.VISIBLE
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


    private fun moveToSearchFragment(location: LocationReadAllResponse) {
        sharedViewModel.selectedLocation.value = location
        /*val locationJson = SharedPrefManager.convertLocationToJSON(location)

        if (SharedPrefManager.isLocationInRecentSearch(this@SearchResultActivity, locationJson)) {
            SharedPrefManager.removeRecentSearch(this@SearchResultActivity, locationJson)
        }

        // 최근 검색어 저장
        SharedPrefManager.saveRecentSearch(this@SearchResultActivity, locationJson)

        if (!SharedPrefManager.isLocationInRecentSearch(this@SearchResultActivity, locationJson)) {
            SharedPrefManager.saveRecentSearch(this@SearchResultActivity, locationJson)
        }*/
    }


    private fun setupAdapter(items: List<LocationReadAllResponse>) {
        adapter = SearchResultAdapter(items).apply {
            setOnItemClickListener(object : SearchResultAdapter.OnItemClickListener {
                override fun onItemClicked(location: LocationReadAllResponse) {
                    moveToSearchFragment(location)
                    Log.e("searchresultintent", "back searchfragment")
                    Log.e("searchresultintent", location.toString())
                    val locationJson = convertLocationToJSON(location)
                    SharedPrefManager.saveRecentSearch(this@SearchResultActivity, location)
                    val resultIntent = Intent().apply {
                        putExtra("flag", 103) // 필요한 결과 값을 설정
                        putExtra("location", location)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            })
        }
        binding.rvSearchList.adapter = adapter
    }

    private fun loadRecentSearch() {
        try {
            // Ensure the return type is List<String> or modify accordingly
            val recentSearchListJson: List<LocationReadAllResponse> = SharedPrefManager.loadRecentSearch(this) ?: listOf()

            /*val recentSearchList = recentSearchListJson.map {
                // Ensure `it` is a String type or modify accordingly
                SharedPrefManager.convertJSONToLocation(it)
            }*/
            //setupAdapter(recentSearchList)
            recentSearchAdapter.updateData(recentSearchListJson) // 여기에서 최근 검색 데이터를 업데이트

            if (recentSearchListJson.isEmpty()) {
                //binding.textView4.visibility = View.VISIBLE
                binding.textView2.visibility = View.VISIBLE
                binding.textView3.visibility = View.VISIBLE
            } else {
                binding.textView4.visibility = View.GONE
                binding.textView2.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Handle exception
            Log.e("SearchResultActivity", "Error loading recent searches: ${e.localizedMessage}")
        }
    }

    override fun onStart() {
        super.onStart()
        //Log.e("searchresultintent", "click start")
    }

/*    private fun getAllPosts(): List<PostReadAllResponse> {
        var postList = listOf<LocationReadAllResponse>()
        val call = RetrofitServerConnect.service
        call.getServerPostData().enqueue(object : Callback<PostReadAllResponse> {
            override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                if (response.isSuccessful) {
                    postList = response.body() ?: listOf()
                }
            }

            override fun onFailure(call: Call<List<LocationReadAllResponse>>, t: Throwable) {
                Log.d("API Error", t.message ?: "Error fetching posts")
            }
        })
        return postList
    }*/
}