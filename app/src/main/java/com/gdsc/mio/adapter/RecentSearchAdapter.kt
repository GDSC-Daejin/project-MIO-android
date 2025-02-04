package com.gdsc.mio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.mio.model.LocationReadAllResponse
import com.gdsc.mio.databinding.SearchListLayoutBinding

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
                binding.tvListName.text = item.location.split("/").first()
                binding.tvListRoad.text = item.location.split("/").last().toString()
            }
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