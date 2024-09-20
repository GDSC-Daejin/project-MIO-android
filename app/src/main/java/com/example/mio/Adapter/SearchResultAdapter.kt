package com.example.mio.Adapter

import android.graphics.Color
import android.location.Geocoder
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.databinding.ListLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResultAdapter(private var items: List<LocationReadAllResponse>) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    private var highlightText: String = ""

    // 아이템 클릭을 위한 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClicked(location: LocationReadAllResponse)
    }

    private var onItemClickListener: OnItemClickListener? = null

    // 강조할 텍스트를 함께 전달
    fun updateData(newItems: List<LocationReadAllResponse>, highlightText: String) {
        items = newItems.filter { it.isDeleteYN != "Y" && it.postType == "BEFORE_DEADLINE" }
        this.highlightText = highlightText
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class ViewHolder(val binding: ListLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private val geocoder = Geocoder(binding.root.context)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClicked(items[position])
                }
            }
        }

        fun bind(item: LocationReadAllResponse, highlightText: String) {
            CoroutineScope(Dispatchers.IO).launch {
                //val address = getAddressFromLatLng(item.latitude, item.longitude)
                withContext(Dispatchers.Main) {
                    if (highlightText.isNotEmpty() && item.location.contains(highlightText, true)) {
                        // 강조해야 하는 텍스트가 있고, 아이템의 location에 포함되어 있다면
                        val spannable = SpannableStringBuilder(item.location)
                        val startIdx = item.location.indexOf(highlightText, ignoreCase = true)
                        val endIdx = startIdx + highlightText.length

                        // 강조할 텍스트의 위치에 파란색으로 강조를 추가
                        spannable.setSpan(
                            ForegroundColorSpan(Color.BLUE),
                            startIdx, endIdx,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.tvListName.text = spannable
                    } else {
                        binding.tvListName.text = item.location
                    }

                    binding.tvListRoad.text = item.title
                }
            }
        }

        /*private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                Log.d("GeocoderResult", address.toString())

                val adminArea = address.adminArea ?: ""
                val subAdminArea = address.subAdminArea ?: ""
                val locality = address.locality ?: ""
                val subLocality = address.subLocality ?: ""
                val thoroughfare = address.thoroughfare ?: ""
                val featureName = address.featureName ?: ""

                val detailedAddress = "$adminArea $subAdminArea $locality $subLocality $thoroughfare $featureName".trim()
                return detailedAddress
            }

            // 주소 정보가 없을 경우 "Unknown Address" 반환
            return "Unknown Address"
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], highlightText)
    }

    override fun getItemCount() = items.size
}
