package com.example.mio.Adapter
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.CommentData
import com.example.mio.Model.NotificationData
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.NotificationItemBinding
import com.example.mio.databinding.NotificationItemRvLayoutBinding
import com.example.mio.databinding.PostItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotificationContentAdapter : RecyclerView.Adapter<NotificationContentAdapter.NotificationContentViewHolder>(){
    private lateinit var binding : NotificationItemRvLayoutBinding
    var notificationItemContentData = ArrayList<AddAlarmResponseData>()
    private lateinit var context : Context
    var sharedPref : SharedPref? = null
    private var setKey = "setting_history"

    init {
        setHasStableIds(true)
    }

    inner class NotificationContentViewHolder(private val binding : NotificationItemRvLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var notificationItemContentText = binding.notificationItemTv
        //var notificationItemDate = binding.notificationItemDateTv

        fun bind(notificationContentData : AddAlarmResponseData, position : Int) {
            this.position = position
            //val tempText = context.getString(R.string.setText,notification.notificationContentText.accountID, notification.notificationContentText.postCategory.toString())
            notificationItemContentText.text = notificationContentData.content

            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = context.getString(R.string.setText, notificationContentData!!.createDate.substring(0 .. 9), notificationContentData!!.createDate.substring(11 .. 18))

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {

                if(diffSeconds > -1){
                    binding.notificationItemDateTv.text = "방금전"
                }
                if (diffSeconds > 0) {
                    binding.notificationItemDateTv.text = "${diffSeconds.toString()}초전"
                }
                if (diffMinutes > 0) {
                    binding.notificationItemDateTv.text = "${diffMinutes.toString()}분전"
                }
                if (diffHours > 0) {
                    binding.notificationItemDateTv.text = "${diffHours.toString()}시간전"
                }
                if (diffDays > 0) {
                    binding.notificationItemDateTv.text = "${diffDays.toString()}일전"
                }
            }



            //accountProfile.setImageURI() = pillData.pillTakeTime

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, notificationItemContentData[layoutPosition].id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationContentAdapter.NotificationContentViewHolder {
        context = parent.context
        sharedPref = this.context?.let { SharedPref(it) }
        binding = NotificationItemRvLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationContentViewHolder, position: Int) {
        holder.bind(notificationItemContentData[holder.adapterPosition], holder.adapterPosition)
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
        return notificationItemContentData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setNotificationContentData(notificationContents: List<AddAlarmResponseData>) {
        notificationItemContentData.clear()
        notificationItemContentData.addAll(notificationContents)
        notifyDataSetChanged()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        notificationItemContentData.removeAt(position)
        //sharedPref!!.setNotify(context, setKey, notificationItemContentData)
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