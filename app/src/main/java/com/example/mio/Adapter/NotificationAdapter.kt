package com.example.mio.Adapter
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.CommentData
import com.example.mio.Model.NotificationData
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.NotificationItemBinding
import com.example.mio.databinding.PostItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>(){
    private lateinit var binding : NotificationItemBinding
    var notificationItemData = ArrayList<AddAlarmResponseData>()
    private lateinit var context : Context
    var sharedPref : SharedPref? = null
    private var setKey = "setting_history"
    private var manager : LinearLayoutManager? = null


    //알람의 내용
    private var notificationContentAdapter : NotificationContentAdapter? = null
    var notificationContentItemData = ArrayList<AddAlarmResponseData?>()
    init {
        setHasStableIds(true)
    }

    inner class NotificationViewHolder(private val binding : NotificationItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var notificationContentText = binding.notificationContentTv
        var notificationTitleText = binding.notificationItemTitleTv

        fun bind(notification : AddAlarmResponseData, position : Int) {
            this.position = position

            val tempText = context.getString(R.string.setText,notification.post.user.studentId, notification.post.category.toString())
            notificationContentText.text = notification.content
            notificationTitleText.text = tempText

            //accountProfile.setImageURI() = pillData.pillTakeTime
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = context.getString(R.string.setText, notification!!.createDate.substring(0 .. 9), notification!!.createDate.substring(11 .. 18))

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
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


            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, notificationItemData[layoutPosition].id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        context = parent.context
        sharedPref = this.context?.let { SharedPref(it) }
        binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notificationItemData[holder.adapterPosition], holder.adapterPosition)
        /*binding.deleteNotify.setOnClickListener {
            val builder : AlertDialog.Builder = AlertDialog.Builder(context)
            val ad : AlertDialog = builder.create()
            var deleteData = notificationItemData[holder.adapterPosition]!!.applyDate
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

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}