package com.example.mio.Adapter
import android.app.AlertDialog
import android.app.Notification
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.*
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.NotificationItemBinding
import com.example.mio.databinding.PostItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>(){
    enum class NotificationStatus {
        Passenger, //손님이면서 카풀완료
        Driver, //운전자이면서 카풀완료
        Neither //둘 다 아니고 그냥 자기가 예약한 게시글 보고 싶음
    }


    private lateinit var binding : NotificationItemBinding
    var notificationItemData = ArrayList<AddAlarmResponseData>()
    private lateinit var context : Context
    var sharedPref : SharedPref? = null

    private var identification = ""
    //var notificationContentItemData = ArrayList<PostData?>()
    init {
        setHasStableIds(true)
       /* for (i in notificationContentItemData.indices) {
            hashMapCurrentPostItemData[i] = NotificationStatus.Neither
        }
        Log.e("xcxccxcxcx", notificationContentItemData.toString())
        Log.e("sdsfsdfsdfs", notificationItemData.toString())*/
    }

    inner class NotificationViewHolder(private val binding : NotificationItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null

        fun bind(notification : AddAlarmResponseData, position : Int) {
            this.position = position
            //accountProfile.setImageURI() = pillData.pillTakeTime
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = notification.createDate.replace("T", " ").split(".")[0] ?: ""

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime) //위 두개는 알림이 온 시간체크용용


            binding.notificationContentTv.text = notification.content

            val temp = if (notification.content.contains("후기")) {
                "후기 알림"
            } else if (notification.content.contains("신청") && notification.content.contains("승인")) {
                "승인 알림"
            } else if (notification.content.contains("신청")) {
                "신청 알림"
            } else if (notification.content.contains("취소")) {
                "취소 알림"
            } else if (notification.content.contains("거절")) {
                "거절 알림"
            } else if (notification.content.contains("댓글")){
                "댓글 알림"
            } else {
                "기타 알림"
            }

            binding.notificationItemTitleTv.text = temp

            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {

                if(diffSeconds > -1){
                    binding.notificationTime.text = "방금전"
                }
                if (diffSeconds > 0) {
                    binding.notificationTime.text = "${diffSeconds.toString()}초전"
                }
                if (diffMinutes > 0) {
                    binding.notificationTime.text = "${diffMinutes.toString()}분전"
                }
                if (diffHours > 0) {
                    binding.notificationTime.text = "${diffHours.toString()}시간전"
                }
                if (diffDays > 0) {
                    binding.notificationTime.text = "${diffDays.toString()}일전"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        context = parent.context
        sharedPref = SharedPref(context)
        binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notificationItemData[position], position)

        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!

        binding.root.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, notificationItemData[holder.adapterPosition].id, NotificationStatus.Neither)
        }

        binding.root.setOnLongClickListener {
            itemClickListener.onLongClick(it, holder.adapterPosition, notificationItemData[holder.adapterPosition].id, notificationItemData[holder.adapterPosition].postId)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return notificationItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        notificationItemData.removeAt(position)
        //sharedPref!!.setNotify(context, setKey, notificationItemData)
        notifyItemRemoved(position)
    }

    fun updateData(newItems: List<AddAlarmResponseData>) {
        notificationItemData.clear()
        notificationItemData.addAll(newItems)
        Log.d("updateData", newItems.toString())
        Log.d("updateData", notificationItemData.toString())
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onClick(view: View,position: Int, itemId: Int?,status : NotificationStatus )
        fun onLongClick(view: View, position: Int, itemId: Int, postId : Int?) //position -> 리사이클러뷰 위치, itemId -> 알람 id값, postId -> postdata찾기위한 값인디 없을수도
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}