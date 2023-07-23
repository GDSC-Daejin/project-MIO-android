package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.CommentData
import com.example.mio.databinding.CommentItemLayoutBinding
import com.example.mio.databinding.ReplyCommentsItemLayoutBinding


class ReplyCommentAdapter : RecyclerView.Adapter<ReplyCommentAdapter.ReplyCommentViewHolder>(){
    private lateinit var binding : ReplyCommentsItemLayoutBinding
    var replyCommentItemData = mutableListOf<CommentData?>()
    private lateinit var context : Context

    inner class ReplyCommentViewHolder(private val binding : ReplyCommentsItemLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var reCommentContent = binding.reCommentContent
        private var reCommentRealTimeCheck = binding.reCommentRealtimeCheck
        private var reCommentDetail = binding.reCommentDetailIv
        private var reCommentUserId = binding.reCommentUserId

        fun bind(comment : CommentData, position : Int) {
            this.position = position
            reCommentUserId.text = comment.user.id.toString()
            reCommentContent.text = comment.content
            reCommentDetail
            reCommentRealTimeCheck.text = comment.createDate

            //accountProfile.setImageURI() = pillData.pillTakeTime

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, replyCommentItemData[layoutPosition]!!.commentId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyCommentAdapter.ReplyCommentViewHolder {
        context = parent.context
        binding = ReplyCommentsItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyCommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyCommentAdapter.ReplyCommentViewHolder, position: Int) {
        holder.bind(replyCommentItemData[position]!!, position)
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
        return replyCommentItemData.size
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        replyCommentItemData.removeAt(position)
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