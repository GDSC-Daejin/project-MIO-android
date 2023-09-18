package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.CommentData
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.CommentItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NoticeBoardReadAdapter : RecyclerView.Adapter<NoticeBoardReadAdapter.NoticeBoardReadViewHolder>(){
    private lateinit var binding : CommentItemLayoutBinding
    var commentItemData = mutableListOf<CommentData?>()
    private lateinit var context : Context
    private var manager : LinearLayoutManager? = null


    private var replyCommentAdapter : ReplyCommentAdapter? = null
    var replyCommentItemData = ArrayList<CommentData?>()

    //본인 확인
    private var identification = ""


    inner class NoticeBoardReadViewHolder(private val binding : CommentItemLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var commentContent = binding.commentContent
        var commentRealTimeCheck = binding.commentRealtimeCheck
        var commentDetail = binding.commentDetailIv
        var commentUserId = binding.commentUserId

        fun bind(comment : CommentData, position : Int) {
            //대댓글 adapter 세팅
            replyCommentAdapter = ReplyCommentAdapter()
            manager = LinearLayoutManager(context)
            binding.reCommentRv.adapter = replyCommentAdapter
            binding.reCommentRv.setHasFixedSize(true)
            binding.reCommentRv.layoutManager = manager
            replyCommentAdapter!!.setReplyCommentData(comment.childComments) // 댓글의 답변 댓글 리스트 설정

            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!.substring(0..7)

            this.position = position
            if (identification == comment.user.studentId) {
                binding.commentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_blue_4))
                commentUserId.text = comment.user.studentId.toString()
            } else {
                commentUserId.text = comment.user.studentId.toString()
            }

            commentContent.text = comment.content
            commentDetail
            //commentRealTimeCheck.text = comment.createDate

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

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, commentItemData[layoutPosition]!!.commentId)
                println(postDateTime)
                println(comment.createDate)
                println(identification)
            }

            binding.root.setOnLongClickListener {
                itemClickListener.onLongClick(it, layoutPosition, commentItemData[layoutPosition]!!.commentId)
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardReadAdapter.NoticeBoardReadViewHolder {
        context = parent.context
        binding = CommentItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)


        return NoticeBoardReadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardReadAdapter.NoticeBoardReadViewHolder, position: Int) {


        holder.bind(commentItemData[holder.adapterPosition]!!, holder.adapterPosition)

        //댓글 팝업메뉴
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
        fun onLongClick(view: View, position: Int, itemId: Int)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}