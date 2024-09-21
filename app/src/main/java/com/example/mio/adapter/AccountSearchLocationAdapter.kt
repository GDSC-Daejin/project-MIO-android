package com.example.mio.adapter

import android.content.Context
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.BuildConfig
import com.example.mio.model.AddressData
import com.example.mio.databinding.ListLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class AccountSearchLocationAdapter(private var items: List<AddressData>) : RecyclerView.Adapter<AccountSearchLocationAdapter.AccountSearchViewHolder>() {
    private var highlightText: String = ""
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        private const val API_KEY = BuildConfig.map_api_key
    }
    // 아이템 클릭을 위한 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClicked(location: AddressData?)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var context : Context? = null
    // 강조할 텍스트를 함께 전달
    fun updateData(newItems: List<AddressData>, highlightText: String) {
        items = newItems.take(1)
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
        fun bind(item: AddressData, highlightText: String) {
            CoroutineScope(Dispatchers.IO).launch {
                //val address = getAddressFromLatLng(item.x.toDouble(), item.y.toDouble())
                withContext(Dispatchers.Main) {
                    if (highlightText.isNotEmpty() && item.address_name.contains(highlightText, true)) {
                        // 강조해야 하는 텍스트가 있고, 아이템의 location에 포함되어 있다면
                        val spannable = SpannableStringBuilder(item.address_name)
                        val startIdx = item.address_name.indexOf(highlightText, ignoreCase = true)
                        val endIdx = startIdx + highlightText.length

                        // 강조할 텍스트의 위치에 파란색으로 강조를 추가
                        spannable.setSpan(
                            ForegroundColorSpan(Color.BLUE),
                            startIdx, endIdx,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.tvListName.text = spannable
                    } else {
                        binding.tvListName.text = item.address_name
                    }

                    binding.tvListRoad.text = item.address_name
                }
            }
        }
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