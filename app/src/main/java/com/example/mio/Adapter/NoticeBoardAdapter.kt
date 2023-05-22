package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.PostData
import com.example.mio.databinding.PostItemBinding


class NoticeBoardAdapter : RecyclerView.Adapter<NoticeBoardAdapter.NoticeBoardViewHolder>(){
    private lateinit var binding : PostItemBinding
    var postItemData = mutableListOf<PostData?>()
    private lateinit var context : Context

    inner class NoticeBoardViewHolder(private val binding : PostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var accountId = binding.accountId
        var accountProfile = binding.accountImage
        fun bind(accountData: PostData, position : Int) {
            this.position = position
            accountId.text = accountData.accountID
            //accountProfile.setImageURI() = pillData.pillTakeTime

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, postItemData[layoutPosition]!!.postID)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardViewHolder {
        context = parent.context
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeBoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardViewHolder, position: Int) {
        holder.bind(postItemData[position]!!, position)
        /*binding.homeRemoveIv.setOnClickListener {
            val builder : AlertDialog.Builder = AlertDialog.Builder(context)
            val ad : AlertDialog = builder.create()
            var deleteData = pillItemData[holder.adapterPosition]!!.pillName
            builder.setTitle(deleteData)
            builder.setMessage("정말로 삭제하시겠습니까?")
            builder.setNegativeButton("예",
                DialogInterface.OnClickListener { dialog, which ->
                    ad.dismiss()
                    //temp = listData[holder.adapterPosition]!!
                    //extraditeData()
                    //testData.add(temp)
                    //deleteServerData = tempServerData[holder.adapterPosition]!!.api_id
                    removeData(holder.adapterPosition)
                    //removeServerData(deleteServerData!!)
                    //println(deleteServerData)
                })

            builder.setPositiveButton("아니오",
                DialogInterface.OnClickListener { dialog, which ->
                    ad.dismiss()
                })
            builder.show()
        }*/
    }

    override fun getItemCount(): Int {
        return postItemData.size
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        postItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}