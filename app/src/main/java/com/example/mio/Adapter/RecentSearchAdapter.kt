package com.example.mio.Adapter

import android.graphics.Color
import android.location.Geocoder
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.databinding.SearchListLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentSearchAdapter(private var items: List<LocationReadAllResponse>) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClicked(location: LocationReadAllResponse)
        fun onItemRemove(location: LocationReadAllResponse)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class ViewHolder(val binding: SearchListLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private val geocoder = Geocoder(binding.root.context)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClicked(items[position])
                }
            }
            binding.cancelButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemRemove(items[position])
                }
            }
        }

/*        fun bind(item: LocationReadAllResponse) {
            binding.tvListName.text = item.location
            // 이 어댑터에서는 최근 검색어만 보여주기 때문에, 도로명 주소는 보여주지 않아도 됩니다.
            // 그러나 필요하다면 아래 코드의 주석을 해제하여 사용하실 수 있습니다.
            //binding.tvListRoad.text = item.roadName
            binding.cancelButton.visibility = View.VISIBLE
        }*/

        fun bind(item: LocationReadAllResponse) {
            if (item.location.isNotEmpty()) {
                Log.e("recentSearch", item.location)
                binding.tvListName.text = item.location.split("/").first()
                binding.tvListRoad.text = item.location.split("/").last().toString()
            }
            /*CoroutineScope(Dispatchers.IO).launch {
                val s = getAddressFromLatLng(item.latitude, item.longitude)
                Log.e("getAddressFromLatLng", s)
            }*/
        }

        private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val adminArea = address.adminArea ?: ""
                val subAdminArea = address.subAdminArea ?: ""
                val locality = address.locality ?: ""
                val subLocality = address.subLocality ?: ""
                val thoroughfare = address.thoroughfare ?: ""
                val featureName = address.featureName ?: ""
                return "$adminArea $subAdminArea $locality $subLocality $thoroughfare $featureName".trim()
            }
            return "Unknown Address"
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SearchListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[holder.adapterPosition])
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<LocationReadAllResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}