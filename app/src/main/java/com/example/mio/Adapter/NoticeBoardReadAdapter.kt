package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.CommentData
import com.example.mio.databinding.CommentItemBinding


class NoticeBoardReadAdapter : RecyclerView.Adapter<NoticeBoardReadAdapter.NoticeBoardReadViewHolder>(){
    private lateinit var binding : CommentItemBinding
    var commentItemData = mutableListOf<CommentData?>()
    private lateinit var context : Context

    inner class NoticeBoardReadViewHolder(private val binding : CommentItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var commentAccountId = binding.userEmail
        //var accountProfile = binding.accountImage
        var commenetContent = binding.postItemContentText
        fun bind(comment : CommentData, position : Int) {
            this.position = position
            commentAccountId.text = comment.commentAccountID
            commenetContent.text = comment.commentContent
            //accountProfile.setImageURI() = pillData.pillTakeTime

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, commentItemData[layoutPosition]!!.commentPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardReadAdapter.NoticeBoardReadViewHolder {
        context = parent.context
        binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeBoardReadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardReadAdapter.NoticeBoardReadViewHolder, position: Int) {
        holder.bind(commentItemData[position]!!, position)
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
        return commentItemData.size
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        commentItemData.removeAt(position)
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