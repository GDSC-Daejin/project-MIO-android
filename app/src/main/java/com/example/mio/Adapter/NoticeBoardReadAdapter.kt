package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.CommentData
import com.example.mio.R
import com.example.mio.databinding.CommentItemLayoutBinding


class NoticeBoardReadAdapter : RecyclerView.Adapter<NoticeBoardReadAdapter.NoticeBoardReadViewHolder>(){
    private lateinit var binding : CommentItemLayoutBinding
    var commentItemData = mutableListOf<CommentData?>()
    private lateinit var context : Context
    private var manager : LinearLayoutManager? = null


    private var replyCommentAdapter : ReplyCommentAdapter? = null
    var replyCommentItemData = ArrayList<CommentData?>()


    inner class NoticeBoardReadViewHolder(private val binding : CommentItemLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var commentContent = binding.commentContent
        var commentRealTimeCheck = binding.commentRealtimeCheck
        var commentDetail = binding.commentDetailIv
        var commentUserId = binding.commentUserId

        fun bind(comment : CommentData, position : Int) {
            this.position = position
            commentUserId.text = comment.user.studentId.toString()
            commentContent.text = comment.content
            commentDetail
            commentRealTimeCheck.text = comment.createDate

            //accountProfile.setImageURI() = pillData.pillTakeTime

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, commentItemData[layoutPosition]!!.commentId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardReadAdapter.NoticeBoardReadViewHolder {
        context = parent.context
        binding = CommentItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        replyCommentAdapter = ReplyCommentAdapter()
        manager = LinearLayoutManager(context)


        return NoticeBoardReadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardReadAdapter.NoticeBoardReadViewHolder, position: Int) {
        binding.reCommentRv.adapter = replyCommentAdapter
        binding.reCommentRv.setHasFixedSize(true)
        binding.reCommentRv.layoutManager = manager

        holder.bind(commentItemData[position]!!, position)

        binding.commentDetailIv.setOnClickListener {
            val popUpMenu = PopupMenu(context, binding.commentDetailIv)
            popUpMenu.menuInflater.inflate(R.menu.comment_option_menu, popUpMenu.menu)
            popUpMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.comment_menu_write -> {
                        Toast.makeText(context, "대댓글쓰기", Toast.LENGTH_SHORT).show()
                    }
                    R.id.comment_menu_edit -> {
                        Toast.makeText(context, "수정", Toast.LENGTH_SHORT).show()
                    }
                    R.id.comment_menu_delete -> {
                        Toast.makeText(context, "삭제", Toast.LENGTH_SHORT).show()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popUpMenu.show()
        }
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