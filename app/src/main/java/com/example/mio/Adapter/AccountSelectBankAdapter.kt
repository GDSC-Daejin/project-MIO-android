package com.example.mio.Adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.mio.Model.BankItemData
import com.example.mio.R
import com.example.mio.databinding.BankItemBinding

class AccountSelectBankAdapter  : RecyclerView.Adapter<AccountSelectBankAdapter.BankViewHolder>() {
    private lateinit var binding : BankItemBinding
    private lateinit var context : Context
    var itemData = ArrayList<BankItemData>()

    inner class BankViewHolder(private val binding : BankItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bankData: BankItemData, position : Int) {
            Glide.with(context)
                .load(bankData.image)
                .error(R.drawable.top_icon_vector)
                .circleCrop()
                .override(90,90)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("Glide", "Image load failed: ${e?.message}")
                        println(e?.message.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("Glide", "Image load Ready: {$isFirstResource}")
                        return false
                    }
                })
                .into(binding.bankItemIv)

            binding.bankItemTv.text = bankData.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        context = parent.context
        binding = BankItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        holder.bind(itemData[holder.adapterPosition], position)
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, itemData[holder.adapterPosition].name.toString())
        }
    }

    override fun getItemCount(): Int {
        return itemData.size
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: String)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}