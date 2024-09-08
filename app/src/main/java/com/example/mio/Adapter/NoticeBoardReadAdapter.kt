package com.example.mio.Adapter
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NoticeBoardReadAdapter(commentsViewModel: CommentsViewModel): ListAdapter<CommentData, NoticeBoardReadAdapter.NoticeBoardReadViewHolder>(ReadDiffUtil){
    private lateinit var binding : CommentItemLayoutBinding
    private var parentComments: List<CommentData> = listOf()
    var supportFragment : FragmentManager? = null
    private lateinit var context : Context
    private var manager : LinearLayoutManager? = null
    private val viewPool = RecyclerView.RecycledViewPool()
    //게시글용
    private var getBottomSheetData = ""
    var getWriter = ""
    private var viewModel = commentsViewModel


    //private var replyCommentAdapter : ReplyCommentAdapter? = null
    var replyCommentItemData = ArrayList<CommentData?>()
    private var childCommentsMap: Map<Int, List<CommentData>> = emptyMap()

    //게시글 확인
    private var identification = ""


    /*init {
        // ViewModel의 childCommentsMap을 관찰하여 업데이트
        commentsViewModel.childCommentsMap.observeForever { newChildCommentsMap ->
            Log.d("NoticeBoardReadAdapter", "Child comments updated: $newChildCommentsMap")
            childCommentsMap = newChildCommentsMap

            setChildComments(newChildCommentsMap)
        }
    }*/

    inner class NoticeBoardReadViewHolder(private val binding : CommentItemLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var commentContent = binding.commentContent
        var commentRealTimeCheck = binding.commentRealtimeCheck
        /*var commentDetail = binding.commentDetailIv*/
        var commentUserId = binding.commentUserId
        val replyCommentAdapter = ReplyCommentAdapter(viewModel)
        var replyRv = binding.reCommentRv

        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!.split("@").map { it }.first()

        private fun updateChildComments(childComments: List<CommentData>) {
            replyCommentAdapter.updateChildComments(childComments.toMutableList())
        }

        /*private fun updateNotify(replyComments : kotlin.collections.List<CommentData>) {
            replyCommentAdapter.setReplyCommentData(replyComments)
        }*/
        init {
            replyCommentAdapter.getWriter = getWriter

            binding.reCommentRv.apply {
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = replyCommentAdapter
                setHasFixedSize(true)
            }

            // Set click listeners if necessary
            binding.root.setOnClickListener {
                itemClickListener?.onClick(it, layoutPosition, parentComments[layoutPosition]!!.commentId)
            }

            binding.root.setOnLongClickListener {
                itemClickListener?.onLongClick(it, layoutPosition, parentComments[layoutPosition]!!.commentId)
                true
            }
        }

        fun bind(comment : CommentData, childComments: List<CommentData>, position : Int) {
            //대댓글 adapter 세팅
            /*binding.parentComment = parentComment*/
            // Setup child RecyclerView
            // 업데이트된 자식 댓글을 처리
            updateChildComments(childComments)

            this.position = position
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


            //commentRealTimeCheck.text = comment.createDate

            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = context.getString(R.string.setText, comment.createDate.substring(0 .. 9), comment.createDate.substring(11 .. 18))

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {

                if(diffSeconds > -1){
                    binding.commentRealtimeCheck.text = "방금전"
                }
                if (diffSeconds > 0) {
                    binding.commentRealtimeCheck.text = "${diffSeconds.toString()}초전"
                }
                if (diffMinutes > 0) {
                    binding.commentRealtimeCheck.text = "${diffMinutes.toString()}분전"
                }
                if (diffHours > 0) {
                    binding.commentRealtimeCheck.text = "${diffHours.toString()}시간전"
                }
                if (diffDays > 0) {
                    binding.commentRealtimeCheck.text = "${diffDays.toString()}일전"
                }
            }

            //accountProfile.setImageURI() = pillData.pillTakeTime

           /* binding.root.setOnClickListener {
                itemClickListener?.onClick(it, layoutPosition, currentList[layoutPosition]!!.commentId)
                println(postDateTime)
                println(comment.createDate)
                println(identification)
            }

            binding.root.setOnLongClickListener {
                itemClickListener?.onLongClick(it, layoutPosition, currentList[layoutPosition]!!.commentId)
                return@setOnLongClickListener true
            }*/

            replyCommentAdapter.setItemClickListener(object : ReplyCommentAdapter.ItemClickListener {
                //수정,삭제
                override fun onLongClick(view: View, position: Int, itemId: Int, comment: CommentData?) {
                    Log.e("noticeboardadapter", "onlongclick")
                    //위에 요거는 itemId가 10인 commentItemData의 childComment를 찾고 아래가 부모댓글찾는거
                    //즉 이거는 클릭한 대댓글의 모든 정보
                    val temp = comment

                    //any 함수는 하나라도 만족하는지 체크합니다.
                    //이거는 클릭한 대댓글의 부모 댓글의 모든 정보
                   /* val parentComment = parentComments.find { comment ->
                        comment?.childComments?.any { it.commentId == itemId }!! } //부모 댓글 찾기*/

                    if (identification == temp!!.user.studentId && temp.content != "삭제된 댓글입니다.") {
                        //수정용
                        val bottomSheet = ReadSettingBottomSheetFragment()
                        bottomSheet.show(supportFragment!!, bottomSheet.tag)
                        bottomSheet.apply {
                            setCallback(object : ReadSettingBottomSheetFragment.OnSendFromBottomSheetDialog{
                                override fun sendValue(value: String) {
                                    Log.d("child comment adapter", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                                    getBottomSheetData = value
                                    when(value) {
                                        "수정" -> {
                                            Log.d("adpater Read Test", "${temp}")
                                            itemClickListener?.onReplyClicked("수정", temp.commentId, temp)
                                        }

                                        "삭제" -> {
                                            itemClickListener?.onReplyClicked("삭제", temp.commentId, temp)
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardReadAdapter.NoticeBoardReadViewHolder {
        context = parent.context
        binding = CommentItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        manager = LinearLayoutManager(context)
        return NoticeBoardReadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardReadViewHolder, position: Int) {
        val parentComment = parentComments[position]
        val childComments = childCommentsMap[parentComment.commentId] ?: emptyList()
        holder.bind(parentComment, childComments, position)
        //holder.replyRv.requestLayout()

        if (parentComments[position].user.studentId == getWriter) {
            holder.commentUserId.apply {
                setTextColor(ContextCompat.getColor(context ,R.color.mio_blue_4))
            }
        }
    }


    override fun getItemCount(): Int {
        return parentComments.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    //데이터 Handle 함수
    /*fun removeData(position: Int) {
        parentComments.re(position)
        //temp = null
        notifyItemRemoved(position)
    }*/

    // Method to update parent comments
    fun updateParentComment(parentComments: List<CommentData>) {
        this.parentComments = parentComments
        //notifyDataSetChanged() // 전체 데이터 변경 사항 알림
        Log.e("updateParentComment", "$parentComments")
        submitList(parentComments.toList())//notifyDataSetChanged()
        //submitList(parentComments.toList())
    }

    // Method to update child comments
    fun updateChildComments(childCommentsMap: Map<Int, List<CommentData>>) {
        this.childCommentsMap = childCommentsMap.toMutableMap()
        Log.e("updateChildComments", "${viewModel.childCommentsMap.value}")
        notifyDataSetChanged() // 전체 데이터 변경 사항 알림
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
        fun onLongClick(view: View, position: Int, itemId: Int)

        fun onReplyClicked(status : String? , commentId: Int?, commentData : CommentData?)
    }


    private var itemClickListener: ItemClickListener? = null

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}

object ReadDiffUtil : DiffUtil.ItemCallback<CommentData>() {

    override fun areItemsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
        val result = oldItem.commentId == newItem.commentId // Assuming 'id' is unique for each notification
        Log.d("ReadDiffUtil", "areItemsTheSame: Comparing oldItem.id = ${oldItem.commentId} with newItem.id = ${newItem.commentId}, result: $result")
        return result
    }

    override fun areContentsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
        val result = oldItem == newItem // This checks if all fields are the same
        Log.d("ReadDiffUtil", "areContentsTheSame: Comparing oldItem = $oldItem with newItem = $newItem, result: $result")
        return result
    }
}