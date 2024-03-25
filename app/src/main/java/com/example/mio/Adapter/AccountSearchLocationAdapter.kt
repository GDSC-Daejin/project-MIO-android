package com.example.mio.Adapter

import android.content.Context
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.BuildConfig
import com.example.mio.KakaoApiService
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.Model.Place
import com.example.mio.NoticeBoard.NoticeBoardEditActivity
import com.example.mio.PlaceDocument
import com.example.mio.SearchResult
import com.example.mio.databinding.ListLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class AccountSearchLocationAdapter(private var items: List<Place>) : RecyclerView.Adapter<AccountSearchLocationAdapter.AccountSearchViewHolder>() {
    private var highlightText: String = ""
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        private const val API_KEY = BuildConfig.map_api_key
    }
    // 아이템 클릭을 위한 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClicked(location: Place?)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var context : Context? = null
    // 강조할 텍스트를 함께 전달
    fun updateData(newItems: List<Place>, highlightText: String) {
        items = newItems
        this.highlightText = highlightText
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class AccountSearchViewHolder(val binding: ListLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private val geocoder = Geocoder(binding.root.context)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClicked(items[position])
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun bind(item: Place, highlightText: String) {
            CoroutineScope(Dispatchers.IO).launch {
                //val address = getAddressFromLatLng(item.x.toDouble(), item.y.toDouble())
                val address = item.place_name
                withContext(Dispatchers.Main) {
                    if (highlightText.isNotEmpty() && item.place_name.contains(highlightText, true)) {
                        // 강조해야 하는 텍스트가 있고, 아이템의 location에 포함되어 있다면
                        val spannable = SpannableStringBuilder(item.place_name)
                        val startIdx = item.place_name.indexOf(highlightText, ignoreCase = true)
                        val endIdx = startIdx + highlightText.length

                        // 강조할 텍스트의 위치에 파란색으로 강조를 추가
                        spannable.setSpan(
                            ForegroundColorSpan(Color.BLUE),
                            startIdx, endIdx,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.tvListName.text = spannable
                    } else {
                        binding.tvListName.text = item.place_name
                    }

                    binding.tvListRoad.text = item.address_name
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private fun getAddressFromLatLng(latitude: Double, longitude: Double) {
            var address: Address? = null
            //val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val geocodeListener = @RequiresApi(33) object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    // 주소 리스트를 가지고 할 것을 적어주면 됩니다.
                    address = addresses.firstOrNull()
                    address?.let {
                        val adminArea = it.adminArea ?: ""
                        val subAdminArea = it.subAdminArea ?: ""
                        val locality = it.locality ?: ""
                        val subLocality = it.subLocality ?: ""
                        val thoroughfare = it.thoroughfare ?: ""
                        val featureName = it.featureName ?: ""

                        val detailedAddress =
                            "$adminArea $subAdminArea $locality $subLocality $thoroughfare $featureName".trim()

                        //mBinding.placeName.text =
                        //mBinding.placeRoad.text = "현재 마커 주소 : " + detailedAddress // 텍스트뷰에 주소 정보 표시
                        //getAddressFromCoordinates(longitude, latitude)
                        /* val latitude = latitude
                         val longitude = longitude
                         val apiKey = API_KEY*/

                        /*getAddressFromCoordinates(latitude, longitude, apiKey) { address ->
                            if (address != null) {
                                *//*val address = address[0].*//*
                                //val name = address.split(" ").map { it.toString() }

                                println("주소: $address") //buildingName포함
                            } else {
                                println("주소를 가져올 수 없습니다.")
                            }
                        }*/
                        // 도로명 주소 검색

                        if (addresses.isNotEmpty()) {
                            address = addresses[0]
                            val roadAddress = address?.getAddressLine(0) ?: ""
                            Log.d("Address", "도로명 주소: $roadAddress")
                            Log.d("detail", "$detailedAddress")

                            // Kakao Map API를 호출하여 해당 주소 주변의 빌딩 정보 검색
                            searchNearbyBuildings(detailedAddress,latitude, longitude) { buildingNames ->
                                buildingNames?.let {
                                    Log.d("BuildingNames", "빌딩 이름들: $buildingNames")
                                    // 여기에서 UI 업데이트 또는 다른 작업을 수행할 수 있습니다.
                                }
                            }
                        } else {
                            Log.e("Address", "주소를 가져올 수 없습니다.")
                            //Toast.makeText(this, "주소를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }

                        getAddressFromCoordinates(latitude, longitude,
                            API_KEY
                        ) { addressCoordinates ->
                            addressCoordinates.let {
                                Log.d("test", addressCoordinates.toString())
                            }
                        }

                        Log.d("NoticeEdit Map Test", detailedAddress)
                        Log.d("NoticeEdit Map Test", "${address?.featureName}") //빌딩이름?
                        Log.d("NoticeEdit Map Test", "$address")
                        Log.d("NoticeEdit Map Test", address?.getAddressLine(0).toString())
                        Log.d("NoticeEdit Map Test", addresses.toString())
                        Log.d("NoticeEdit Map Test", longitude.toString())
                        Log.d("NoticeEdit Map Test", latitude.toString())
                    }
                }

                override fun onError(errorMessage: String?) {
                    address = null
                    Toast.makeText(
                        context,
                        "주소가 발견되지 않았습니다.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            geocoder.getFromLocation(latitude, longitude, 10, geocodeListener)
        }
    }

    // Kakao Map API를 호출하여 주변 빌딩 정보를 검색하는 함수
    private fun searchNearbyBuildings(query : String, latitude: Double, longitude: Double, callback: (List<PlaceDocument>?) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(KakaoApiService::class.java)
        val apiKey = API_KEY // 본인의 Kakao API 키 입력
        val call = service.searchAddress(query, longitude, latitude, radius = 100, sort = "accuracy", apiKey = apiKey)

        call.enqueue(object : Callback<SearchResult> {
            override fun onResponse(call: Call<SearchResult>, response: Response<SearchResult>) {
                if (response.isSuccessful) {
                    val result = response.body()?.documents?.toList()
                    //val buildingNames = result?.documents?.map { it.road_address.building_name!! }
                    callback(result)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SearchResult>, t: Throwable) {
                callback(null)
            }
        })
    }

    private fun getAddressFromCoordinates(latitude: Double, longitude: Double, apiKey: String, callback: (String?) -> Unit) {
        val url = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=$longitude&y=$latitude"
        val httpClient = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", apiKey)
            .build()

        httpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                val address = parseAddressFromResponse(responseBody)
                Log.d("getAddressFromCoordinates", address.toString())
                callback(address)
            }
        })
    }
    private fun parseAddressFromResponse(responseBody: String?): String? {
        if (responseBody.isNullOrBlank()) return null

        val json = JSONObject(responseBody)
        val documents = json.getJSONArray("documents")

        if (documents.length() > 0) {
            val firstDocument = documents.getJSONObject(0)
            //Log.d("Document", documents.toString())
            val address = firstDocument.getJSONObject("address")
            val addressName = address.getString("address_name")
            // road_address 필드가 있는지 확인하고 처리합니다.
            val roadAddress = if (!firstDocument.isNull("road_address")) {
                firstDocument.getJSONObject("road_address")
            } else {
                JSONObject() // "road_address" 필드가 없을 경우 빈 JSONObject 생성
            }
            // roadAddress가 null이 아니라면 해당 필드를 가져옵니다.
            val buildName = if (!roadAddress.isNull("building_name")) {
                roadAddress?.getString("building_name")
            } else {
                null
            }


            return "$addressName $buildName"
        }

        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountSearchLocationAdapter.AccountSearchViewHolder {
        val binding = ListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return AccountSearchViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onBindViewHolder(holder: AccountSearchViewHolder, position: Int) {
        holder.bind(items[position], highlightText)
    }

    override fun getItemCount() = items.size
}