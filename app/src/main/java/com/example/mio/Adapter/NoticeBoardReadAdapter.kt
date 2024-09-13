package com.example.mio.Adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.CommentData
import com.example.mio.Model.CommentsViewModel
import com.example.mio.R
import com.example.mio.ReadSettingBottomSheetFragment
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.CommentItemLayoutBinding
import com.example.mio.databinding.ReplyCommentsItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NoticeBoardReadAdapter(private val commentsViewModel: CommentsViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val parentComments = mutableListOf<CommentData>()
    private val childCommentsMap = mutableMapOf<Int, List<CommentData>>()
    private val flatCommentList = mutableListOf<CommentData>()
    var supportFragment : FragmentManager? = null
    var getWriter = ""
    private lateinit var context: Context

    companion object {
        private const val PARENT_COMMENT = 0
        private const val CHILD_COMMENT = 1
    }

    init {
        // Initialize with empty lists
        updateComments(emptyList())
    }

    fun updateComments(newParentComments: List<CommentData>) {
        parentComments.clear()
        parentComments.addAll(newParentComments)
        flattenComments()
    }

    fun updateChildComments(newChildCommentsMap: Map<Int, List<CommentData>>) {
        childCommentsMap.clear()
        childCommentsMap.putAll(newChildCommentsMap)
        flattenComments()
    }

    private fun flattenComments() {
        flatCommentList.clear()
        parentComments.forEach { parentComment ->
            flatCommentList.add(parentComment)
            flatCommentList.addAll(childCommentsMap[parentComment.commentId] ?: emptyList())
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when (viewType) {
            PARENT_COMMENT -> {
                val binding = CommentItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ParentCommentViewHolder(binding)
            }
            CHILD_COMMENT -> {
                val binding = ReplyCommentsItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChildCommentViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val comment = flatCommentList[position]
        when (holder) {
            is ParentCommentViewHolder -> holder.bind(comment)
            is ChildCommentViewHolder -> holder.bind(comment)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val comment = flatCommentList[position]
        return if (comment.isParent == true) PARENT_COMMENT else CHILD_COMMENT
    }

    override fun getItemCount(): Int {
        return flatCommentList.size
    }

    inner class ParentCommentViewHolder(private val binding: CommentItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentData) {
            // Bind parent comment data here
            with(binding) {
                //commentUserId.text = comment.user.studentId
                if (comment.user.studentId == getWriter) { //게시글 작성자와 댓글 쓴 사람 아이디가 같으면
                    commentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_blue_4))
                    commentUserId.text = comment.user.studentId.toString()
                } else {
                    commentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_7))
                    commentUserId.text = comment.user.studentId.toString()
                }

                if (comment.content == "삭제된 댓글입니다.") {
                    commentContent.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_8))
                    commentContent.text = comment.content
                } else {
                    commentContent.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_11))
                    commentContent.text = comment.content
                }

                //commentContent.text = comment.content
                // Handle date formatting and color changes
                commentRealtimeCheck.text = formatDate(comment.createDate)
                root.setOnClickListener {
                    itemClickListener?.onClick(it, layoutPosition, comment.commentId)
                }
                root.setOnLongClickListener {
                    itemClickListener?.onLongClick(it, layoutPosition, comment.commentId)
                    true
                }
            }
        }
    }

    inner class ChildCommentViewHolder(private val binding: ReplyCommentsItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentData) {
            // Bind child comment data here
            with(binding) {
               /* reCommentUserId.text = comment.user.studentId
                reCommentContent.text = comment.content*/

                if (comment.user.studentId == getWriter) { //게시글 작성자와 댓글 쓴 사람 아이디가 같으면
                    reCommentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_blue_4))
                    reCommentUserId.text = comment.user.studentId.toString()
                } else {
                    reCommentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_7))
                    reCommentUserId.text = comment.user.studentId.toString()
                }

                if (comment.content == "삭제된 댓글입니다.") {
                    reCommentContent.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_8))
                    reCommentContent.text = comment.content
                } else {
                    reCommentContent.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_11))
                    reCommentContent.text = comment.content
                }
                // Handle date formatting
                reCommentRealtimeCheck.text = formatDate(comment.createDate)
                itemView.setOnLongClickListener {
                    childItemClickListener?.onLongClick(it, layoutPosition, comment.commentId, comment)
                    true
                }
            }
        }
    }

    private fun formatDate(createDate: String): String {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val currentDate = sdf.format(Date(now))
        val postDateTime = context.getString(R.string.setText, createDate.substring(0..9), createDate.substring(11..18))
        val nowFormat = sdf.parse(currentDate)
        val beforeFormat = sdf.parse(postDateTime)
        val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time ?: 0) ?: 0
        val diffSeconds = diffMilliseconds / 1000
        val diffMinutes = diffMilliseconds / (60 * 1000)
        val diffHours = diffMilliseconds / (60 * 60 * 1000)
        val diffDays = diffMilliseconds / (24 * 60 * 60 * 1000)

        return when {
            diffSeconds < 60 -> "방금전"
            diffMinutes < 60 -> "${diffMinutes}분전"
            diffHours < 24 -> "${diffHours}시간전"
            else -> "${diffDays}일전"
        }
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
        fun onLongClick(view: View, position: Int, itemId: Int)
        fun onReplyClicked(status: String?, commentId: Int?, commentData: CommentData?)
    }

    interface ChildClickListener {
        fun onLongClick(view: View, position: Int, itemId: Int, comment: CommentData?)
    }

    private var itemClickListener: ItemClickListener? = null
    private var childItemClickListener: ChildClickListener? = null

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setChildItemClickListener(childItemClickListener: ChildClickListener) {
        this.childItemClickListener = childItemClickListener
    }
}