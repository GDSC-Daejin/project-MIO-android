package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.CommentData
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.CommentItemLayoutBinding
import com.example.mio.databinding.ReplyCommentsItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ReplyCommentAdapter : RecyclerView.Adapter<ReplyCommentAdapter.ReplyCommentViewHolder>(){
    private lateinit var binding : ReplyCommentsItemLayoutBinding
    var replyCommentItemData = ArrayList<CommentData?>()
    private lateinit var context : Context
    private var identification = ""

    inner class ReplyCommentViewHolder(private val binding : ReplyCommentsItemLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var reCommentContent = binding.reCommentContent
        private var reCommentRealTimeCheck = binding.reCommentRealtimeCheck
       /* private var reCommentDetail = binding.reCommentDetailIv*/
        private var reCommentUserId = binding.reCommentUserId

        fun bind(comment : CommentData, position : Int) {
            this.position = position
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!.substring(0..7)

            if (identification == comment.user.studentId) {
                binding.reCommentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_blue_4))
                reCommentUserId.text = comment.user.studentId.toString()
            } else {
                reCommentUserId.text = comment.user.studentId.toString()
            }
            //reCommentUserId.text = comment.user.studentId.toString()
            reCommentContent.text = comment.content


            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = context.getString(R.string.setText, comment!!.createDate.substring(0 .. 9), comment!!.createDate.substring(11 .. 18))

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {

                if(diffSeconds > -1){
                    reCommentRealTimeCheck.text = "방금전"
                }
                if (diffSeconds > 0) {
                    reCommentRealTimeCheck.text = "${diffSeconds.toString()}초전"
                }
                if (diffMinutes > 0) {
                    reCommentRealTimeCheck.text = "${diffMinutes.toString()}분전"
                }
                if (diffHours > 0) {
                    reCommentRealTimeCheck.text = "${diffHours.toString()}시간전"
                }
                if (diffDays > 0) {
                    reCommentRealTimeCheck.text = "${diffDays.toString()}일전"
                }
            }

            //accountProfile.setImageURI() = pillData.pillTakeTime

            /*binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, replyCommentItemData[layoutPosition]!!.commentId)
                println(postDateTime)
            }*/

            binding.root.setOnLongClickListener {
                itemClickListener.onLongClick(it, layoutPosition, replyCommentItemData[layoutPosition]!!.commentId)
                return@setOnLongClickListener true
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

    //대댓글 데이터 관리
    fun setReplyCommentData(replyComments: List<CommentData>) {
        replyCommentItemData.clear()
        replyCommentItemData.addAll(replyComments)
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        //fun onClick(view: View, position: Int, itemId: Int)

        fun onLongClick(view: View, position: Int, itemId: Int)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}