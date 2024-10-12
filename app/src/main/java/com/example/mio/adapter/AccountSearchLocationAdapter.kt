package com.example.mio.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.model.AddressData
import com.example.mio.databinding.ListLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountSearchLocationAdapter(private var items: List<AddressData>) : RecyclerView.Adapter<AccountSearchLocationAdapter.AccountSearchViewHolder>() {
    private var highlightText: String = ""
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