package com.gdsc.mio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.mio.R
import com.gdsc.mio.model.*
import com.gdsc.mio.SaveSharedPreferenceGoogleLogin
import com.gdsc.mio.databinding.NotificationItemBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter : ListAdapter<AddAlarmResponseData, NotificationAdapter.NotificationViewHolder>(NotificationDiffUtil){
    enum class NotificationStatus {
        Passenger, //손님이면서 카풀완료
        Driver, //운전자이면서 카풀완료
        Neither //둘 다 아니고 그냥 자기가 예약한 게시글 보고 싶음
    }


    private lateinit var binding : NotificationItemBinding
    //private var notificationItemData: List<AddAlarmResponseData> = ArrayList()
    //private var notificationItemData = kotlin.collections.ArrayList<AddAlarmResponseData>()
    private lateinit var context : Context
    //private var sharedPref : SharedPref? = null

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


            val postDateTime = notification.createDate.replace("T", " ").split(".")[0]

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime) //위 두개는 알림이 온 시간체크용용


            binding.notificationContentTv.text = notification.content

            binding.notificationItemTitleTv.text = when {
                notification.content.contains("후기") -> "후기 알림"
                notification.content.contains("신청") && notification.content.contains("승인") -> "승인 알림"
                notification.content.contains("신청") -> "신청 알림"
                notification.content.contains("취소") -> "취소 알림"
                notification.content.contains("거절") -> "거절 알림"
                notification.content.contains("댓글") -> "댓글 알림"
                else -> "기타 알림"
            }

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
                    binding.notificationTime.text = context.getString(R.string.setTimeTextSeconds, "$diffSeconds")
                }
                if (diffMinutes > 0) {
                    binding.notificationTime.text = context.getString(R.string.setTimeTextMinutes, "$diffMinutes")
                }
                if (diffHours > 0) {
                    binding.notificationTime.text = context.getString(R.string.setTimeTextHours, "$diffHours")
                }
                if (diffDays > 0) {
                    binding.notificationTime.text = context.getString(R.string.setTimeTextDays, "$diffDays")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        context = parent.context
        //sharedPref = SharedPref(context)
        binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(currentList[position], position)
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!

        holder.itemView.setOnClickListener {
            if (itemClickListener == null) {
                Toast.makeText(context, "데이터를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            itemClickListener?.onClick(it, holder.adapterPosition, currentList[position].id, NotificationStatus.Neither)
        }

        holder.itemView.setOnLongClickListener {
            if (itemClickListener == null) {
                Toast.makeText(context, "데이터를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener false
            }
            itemClickListener?.onLongClick(it, holder.adapterPosition, currentList[position].id, currentList[position].postId)
            true
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
//    fun removeData(position: Int) {
//        notificationItemData.removeAt(position)
//        //sharedPref!!.setNotify(context, setKey, notificationItemData)
//        notifyItemRemoved(position)
//    }

    /*fun updateData(newItems: List<AddAlarmResponseData>) {
        notificationItemData.clear()
        notificationItemData.addAll(newItems)
        Log.d("updateData", newItems.toString())
        Log.d("updateData", notificationItemData.toString())
        notifyDataSetChanged()
    }*/

    interface ItemClickListener {
        fun onClick(view: View,position: Int, itemId: Int?,status : NotificationStatus )
        fun onLongClick(view: View, position: Int, itemId: Int, postId : Int?) //position -> 리사이클러뷰 위치, itemId -> 알람 id값, postId -> postdata찾기위한 값인디 없을수도
    }

    private var itemClickListener: ItemClickListener? = null

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    /*fun updateNotifications(newData: List<AddAlarmResponseData>) {
        val oldSize = notificationItemData.size
        val newSize = newData.size
        val diff = DiffUtil.calculateDiff(NotificationDiffUtilCallback(notificationItemData, newData))

        notificationItemData.clear()
        notificationItemData.addAll(newData)
        diff.dispatchUpdatesTo(this)
    }*/

    fun updateNotifications(newData: List<AddAlarmResponseData>) {
        submitList(newData.toList())
        //notifyDataSetChanged()
    }
}

object NotificationDiffUtil : DiffUtil.ItemCallback<AddAlarmResponseData>() {

    override fun areItemsTheSame(
        oldItem: AddAlarmResponseData,
        newItem: AddAlarmResponseData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: AddAlarmResponseData,
        newItem: AddAlarmResponseData
    ): Boolean {
        return oldItem == newItem
    }
}