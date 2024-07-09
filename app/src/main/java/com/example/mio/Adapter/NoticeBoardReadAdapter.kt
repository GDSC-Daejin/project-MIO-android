package com.example.mio.Adapter
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.CommentData
import com.example.mio.R
import com.example.mio.ReadSettingBottomSheetFragment
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.CommentItemLayoutBinding
import org.w3c.dom.Comment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NoticeBoardReadAdapter() : RecyclerView.Adapter<NoticeBoardReadAdapter.NoticeBoardReadViewHolder>(){
    private lateinit var binding : CommentItemLayoutBinding
    var commentItemData = mutableListOf<CommentData?>()
    var supportFragment : FragmentManager? = null
    private lateinit var context : Context
    private var manager : LinearLayoutManager? = null

    //게시글용
    private var getBottomSheetData = ""
    var getWriter = ""


    private var replyCommentAdapter : ReplyCommentAdapter? = null
    var replyCommentItemData = ArrayList<CommentData?>()

    //게시글 확인
    private var identification = ""


    inner class NoticeBoardReadViewHolder(private val binding : CommentItemLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var commentContent = binding.commentContent
        var commentRealTimeCheck = binding.commentRealtimeCheck
        /*var commentDetail = binding.commentDetailIv*/
        var commentUserId = binding.commentUserId

        fun bind(comment : CommentData, position : Int) {
            //대댓글 adapter 세팅
            replyCommentAdapter = ReplyCommentAdapter()
            manager = LinearLayoutManager(context)
            binding.reCommentRv.adapter = replyCommentAdapter
            binding.reCommentRv.setHasFixedSize(true)
            binding.reCommentRv.layoutManager = manager
            replyCommentAdapter!!.setReplyCommentData(comment.childComments) // 댓글의 답변 댓글 리스트 설정


            this.position = position
            if (identification == getWriter) { //게시글 작성자와 댓글 쓴 사람 아이디가 같으면
                binding.commentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_blue_4))
                commentUserId.text = comment.user.studentId.toString()
            } else {
                binding.commentUserId.setTextColor(ContextCompat.getColor(context ,R.color.mio_gray_7))
                commentUserId.text = comment.user.studentId.toString()
            }

            commentContent.text = comment.content
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
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!.split("@").map { it }.first()

        return NoticeBoardReadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardReadAdapter.NoticeBoardReadViewHolder, position: Int) {


        holder.bind(commentItemData[holder.adapterPosition]!!, holder.adapterPosition)

        replyCommentAdapter?.setItemClickListener(object : ReplyCommentAdapter.ItemClickListener {
            /*override fun onClick(view: View, position: Int, itemId: Int) {
                println(itemId)
            }*/

            //수정,삭제
            override fun onLongClick(view: View, position: Int, itemId: Int) {
                //위에 요거는 itemId가 10인 commentItemData의 childComment를 찾고 아래가 부모댓글찾는거
                //즉 이거는 클릭한 대댓글의 모든 정보
                val temp = commentItemData.asSequence().flatMap { it?.childComments?.asSequence()!! }.find { it.commentId == itemId }

                //any 함수는 하나라도 만족하는지 체크합니다.
                //이거는 클릭한 대댓글의 부모 댓글의 모든 정보
                val parentComment = commentItemData.find { comment ->
                    comment?.childComments?.any { it.commentId == itemId }!! } //부모 댓글 찾기

                if (identification == temp!!.user.studentId) {
                    //수정용
                    val bottomSheet = ReadSettingBottomSheetFragment()
                    bottomSheet.show(supportFragment!!, bottomSheet.tag)
                    bottomSheet.apply {
                        setCallback(object : ReadSettingBottomSheetFragment.OnSendFromBottomSheetDialog{
                            override fun sendValue(value: String) {
                                Log.d("test adapter", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                                getBottomSheetData = value
                                //댓글 부분 고치기 Todo
                                when(value) {
                                    "수정" -> {
                                        Log.d("adpater Read Test", "${temp}")
                                        Log.d("adpater Read Test", "${parentComment}")
                                        commentClickListener.onReplyClicked("수정", parentComment?.commentId, temp)
                                    }

                                    "삭제" -> {
                                        commentClickListener.onReplyClicked("삭제", temp.commentId, temp)
                                    }
                                }
                            }
                        })
                    }
                }
            }
        })
        //댓글 팝업메뉴
        /*binding.commentDetailIv.setOnClickListener {
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
        }*/
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

    interface CommentClickListener {
        fun onReplyClicked(status : String? , commentId: Int?, commentData : CommentData?)
    }


    private lateinit var itemClickListener: ItemClickListener

    private lateinit var commentClickListener: CommentClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setCommentClickListener(commentClickListener: CommentClickListener) {
        this.commentClickListener = commentClickListener
    }


}