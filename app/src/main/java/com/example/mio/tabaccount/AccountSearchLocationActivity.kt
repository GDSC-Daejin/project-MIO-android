package com.example.mio.tabaccount

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mio.adapter.AccountSearchLocationAdapter
import com.example.mio.adapter.RecentSearchAdapter
import com.example.mio.BuildConfig
import com.example.mio.helper.SharedPrefManager
import com.example.mio.helper.SharedPrefManager.convertAccountLocationToJSON
import com.example.mio.KakaoAPI
import com.example.mio.model.*
import com.example.mio.databinding.ActivityAccountSearchLocationBinding
import com.example.mio.viewmodel.FragSharedViewModel
import com.example.mio.viewmodel.FragSharedViewModel2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountSearchLocationActivity : AppCompatActivity() {
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        private const val API_KEY = BuildConfig.map_api_key
    }

    private lateinit var binding : ActivityAccountSearchLocationBinding
    //private lateinit var adapter :
    private var searchWord : String? = null
    private lateinit var adapter : AccountSearchLocationAdapter
    private lateinit var recentSearchAdapter: RecentSearchAdapter
    private val sharedViewModel: FragSharedViewModel by lazy {
        (application as FragSharedViewModel2).sharedViewModel
    }
    private val layoutManager = LinearLayoutManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSearchLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvSearchList.layoutManager = layoutManager

        adapter = AccountSearchLocationAdapter(emptyList())
        initRecyclerView(emptyList())
        // 최근 검색 리스트의 설정
        recentSearchAdapter = RecentSearchAdapter(emptyList()).apply {
            setOnItemClickListener(object : RecentSearchAdapter.OnItemClickListener {
                override fun onItemClicked(location: LocationReadAllResponse) {
                    sharedViewModel.selectedAccountLocation.value = location

                    if (location.location.split(" ").size < 2) {
                        val locationJson = convertAccountLocationToJSON(location)
                        SharedPrefManager.saveAccountLocationRecentSearch(this@AccountSearchLocationActivity, locationJson)

                        val intent = Intent(this@AccountSearchLocationActivity, AccountSettingActivity::class.java).apply {
                            putExtra("flag", 4)
                            putExtra("locationData2", location.location.split(" ")[2])
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {

                        Toast.makeText(this@AccountSearchLocationActivity, "동으로 검색해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onItemRemove(location: LocationReadAllResponse) {
                    // 선택된 위치를 SharedPref에서 제거합니다.
                    val locationJson = convertAccountLocationToJSON(location)
                    SharedPrefManager.removeAccountLocationRecentSearch(this@AccountSearchLocationActivity, locationJson)

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
                    binding.textView3.visibility = View.VISIBLE
                    binding.textView3.text = "검색된 내용"
                    binding.btnClear.visibility = View.VISIBLE
                } else {
                    binding.rvSearchList.visibility = View.GONE
                    binding.rvRecentSearchList.visibility = View.VISIBLE
                    binding.textView2.visibility = View.VISIBLE
                    binding.textView3.visibility = View.VISIBLE
                    binding.textView3.text = "최근 검색어"
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
                if (searchWord != null) {
                    //검색한 텍스트의 모든 공백을 제거 ex) 노원 문화의 거리 -> 노원문화의거리
                    val searchText = searchWord.toString().replace("\\s".toRegex(), "")
                    searchKeyword(searchText)
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

        adapter.setOnItemClickListener(object : AccountSearchLocationAdapter.OnItemClickListener{
            override fun onItemClicked(location: AddressData?) {
                if (location != null) {
                    val setL = location.address_name //+ "," + location.place_name
                    sharedViewModel.selectedAccountLocation.value = LocationReadAllResponse(
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "",
                        null,
                        false,
                        -1,
                        null,
                        -1,
                        false,
                        null,
                        location.x.toDouble(),
                        location.y.toDouble(),
                        -1,
                        -1,
                        setL,
                        -1,
                        "N",
                        "BEFORE_DEADLINE"
                    )
                    val locationJson = convertAccountLocationToJSON(LocationReadAllResponse(
                        -1,
                        "",
                        "",
                        "",
                        "",
                        "",
                        null,
                        false,
                        -1,
                        null,
                        -1,
                        false,
                        null,
                        location.x.toDouble(),
                        location.y.toDouble(),
                        -1,
                        -1,
                        setL,
                        -1,
                        "N",
                        "BEFORE_DEADLINE"
                    ))

                    if (SharedPrefManager.isAccountLocationInRecentSearch(this@AccountSearchLocationActivity, locationJson)) {
                        SharedPrefManager.removeAccountLocationRecentSearch(this@AccountSearchLocationActivity, locationJson)
                    }

                    // 최근 검색어 저장
                    SharedPrefManager.saveAccountLocationRecentSearch(this@AccountSearchLocationActivity, locationJson)

                    if (!SharedPrefManager.isAccountLocationInRecentSearch(this@AccountSearchLocationActivity, locationJson)) {
                        SharedPrefManager.saveAccountLocationRecentSearch(this@AccountSearchLocationActivity, locationJson)
                    }

                    val intent = Intent(this@AccountSearchLocationActivity, AccountSettingActivity::class.java).apply {
                        putExtra("flag", 3)
                        putExtra("locationData", location)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    private fun initRecyclerView(items: List<AddressData>) {
        adapter = AccountSearchLocationAdapter(items)
        binding.rvSearchList.adapter = adapter
        binding.rvSearchList.setHasFixedSize(true)
        binding.rvSearchList.layoutManager = layoutManager
       /* noticeBoardAdapter = NoticeBoardAdapter()
        //noticeBoardAdapter!!.postItemData = data
        nbBinding.noticeBoardRV.adapter = noticeBoardAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        nbBinding.noticeBoardRV.setHasFixedSize(true)
        nbBinding.noticeBoardRV.layoutManager = manager*/
    }


    private fun searchKeyword(keyword: String) {
      val retrofit = Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(GsonConverterFactory.create())
          .build()
      val api = retrofit.create(KakaoAPI::class.java)
      val call = api.getAddressSearch(API_KEY, keyword)

      call.enqueue(object: Callback<ResultSearchAddress> {
          override fun onResponse(call: Call<ResultSearchAddress>, response: Response<ResultSearchAddress>) {
              if (response.isSuccessful) {
                  val responseData = response.body()?.documents
                  if (responseData?.isEmpty() == true) {
                      binding.textView4.visibility = View.VISIBLE
                      binding.textView5.visibility = View.VISIBLE
                      binding.rvSearchList.visibility = View.GONE
                  }
                  else {
                      if (responseData != null) {
                          response.body()?.documents.let {
                              if (it != null) {
                                  adapter.updateData(it, keyword)
                              }
                          }
                      }
                    }
                } else {
                    Toast.makeText(this@AccountSearchLocationActivity, "검색 오류가 발생했습니다. 다시 시도해주세요 ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultSearchAddress>, t: Throwable) {
                Log.w("LocalSearch", "통신 실패: ${t.message}")
                Toast.makeText(this@AccountSearchLocationActivity, "검색 오류가 발생했습니다. 다시 시도해주세요 ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadRecentSearch() {
        try {
            // Ensure the return type is List<String> or modify accordingly
            val recentSearchListJson: List<String> = SharedPrefManager.loadAccountLocationRecentSearch(this) ?: listOf()

            val recentSearchList = recentSearchListJson.map {
                // Ensure `it` is a String type or modify accordingly
                SharedPrefManager.convertJSONToAccountLocation(it)
            }
            recentSearchAdapter.updateData(recentSearchList) // 여기에서 최근 검색 데이터를 업데이트

            if (recentSearchList.isEmpty()) {
                binding.textView4.visibility = View.VISIBLE
                binding.textView2.visibility = View.VISIBLE
                binding.textView3.visibility = View.VISIBLE
            } else {
                binding.textView4.visibility = View.GONE
                binding.textView2.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Handle exception
            Toast.makeText(this@AccountSearchLocationActivity, "Error loading recent searches: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

}