package com.example.mio.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.model.CommentData
import com.example.mio.viewmodel.CommentsViewModel
import com.example.mio.R
import com.example.mio.databinding.ReplyCommentsItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.*


class ReplyCommentAdapter(private val commentsViewModel: CommentsViewModel) :
    ListAdapter<CommentData, ReplyCommentAdapter.ReplyCommentViewHolder>(ReplyDiffUtilCallback) {

    private lateinit var context: Context
    var getWriter: String? = ""
    private val viewModel2 = commentsViewModel
    inner class ReplyCommentViewHolder(private val binding: ReplyCommentsItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                itemClickListener.onLongClick(it, layoutPosition, currentList[layoutPosition]!!.commentId, currentList[layoutPosition])
                true
            }
        }

        val commentUserId = binding.reCommentUserId
        val commentContent = binding.reCommentContent
        fun bind(comment: CommentData) {
            if (comment.user.studentId == getWriter) {
                commentUserId.setTextColor(ContextCompat.getColor(context, R.color.mio_blue_4))
                commentUserId.text =  comment.user.studentId
            } else {
                commentUserId.setTextColor(ContextCompat.getColor(context, R.color.mio_gray_11))
                commentUserId.text =  comment.user.studentId
            }

            if (comment.content == "삭제된 댓글입니다.") {
                commentContent.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_8))
                commentContent.text = comment.content
            } else {
                commentContent.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_11))
                commentContent.text = comment.content
            }


            /*binding.reCommentUserId.text = comment.user.studentId.toString()
            binding.reCommentContent.text = comment.content*/

            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)

            val postDateTime = context.getString(R.string.setText, comment.createDate.substring(0..9), comment.createDate.substring(11..18))
            val nowFormat = sdf.parse(currentDate)
            val beforeFormat = sdf.parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time ?: 0)
            val diffSeconds = diffMilliseconds?.div(1000) ?: 0
            val diffMinutes = diffMilliseconds?.div(60 * 1000) ?: 0
            val diffHours = diffMilliseconds?.div(60 * 60 * 1000) ?: 0
            val diffDays = diffMilliseconds?.div(24 * 60 * 60 * 1000) ?: 0

            binding.reCommentRealtimeCheck.text = when {
                diffSeconds < 60 -> "방금전"
                diffMinutes < 60 -> "${diffMinutes}분전"
                diffHours < 24 -> "${diffHours}시간전"
                else -> "${diffDays}일전"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyCommentViewHolder {
        context = parent.context
        val binding = ReplyCommentsItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyCommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyCommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
        if (currentList[position].user.studentId == getWriter) {
            holder.commentUserId.apply {
                setTextColor(ContextCompat.getColor(context ,R.color.mio_blue_4))
            }
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Remove the data at a specific position
    fun removeData(position: Int) {
        val updatedList = currentList.toMutableList()
        if (position in updatedList.indices) {
            updatedList.removeAt(position)
            submitList(updatedList)
        }
    }

    // Update comments list
    fun updateChildComments(newChildComments: List<CommentData>) {
        submitList(newChildComments)
        Log.e("ReplyCommentItemData", "$currentList")
    }

    interface ItemClickListener {
        fun onLongClick(view: View, position: Int, itemId: Int, comment : CommentData?)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}

object ReplyDiffUtilCallback : DiffUtil.ItemCallback<CommentData>() {
    override fun areItemsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
        return oldItem.commentId == newItem.commentId
    }

    override fun areContentsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
        return oldItem == newItem
    }
}