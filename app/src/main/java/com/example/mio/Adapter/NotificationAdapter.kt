package com.example.mio.Adapter
import android.app.AlertDialog
import android.app.Notification
import android.content.Context
import android.content.DialogInterface
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
    private var hashMapCurrentPostItemData = HashMap<Int,NotificationStatus>()


    //알람의 내용
    private var notificationContentAdapter : NotificationContentAdapter? = null
    var notificationContentItemData = ArrayList<AlarmPost?>()
    init {
        setHasStableIds(true)
        for (i in notificationContentItemData.indices) {
            hashMapCurrentPostItemData[i] = NotificationStatus.Neither
        }
    }

    inner class NotificationViewHolder(private val binding : NotificationItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var notificationContentText = binding.notificationContentTv
        var notificationTitleText = binding.notificationItemTitleTv

        fun bind(notification : AddAlarmResponseData, position : Int) {
            this.position = position
            //여기 카풀신청, 예약, 댓글 시 받은 알림의 content를 사용하여 알림만들기 Todo
            val category = if (notification.post.category.categoryName == "carpool") {
                "카풀"
            } else {
                "택시"
            }



            //accountProfile.setImageURI() = pillData.pillTakeTime
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = context.getString(R.string.setText, notification.createDate.substring(0 .. 9), notification.createDate.substring(11 .. 18))

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime) //위 두개는 알림이 온 시간체크용용

            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(notification.post.targetDate) //이건 게시글과의 차이를 계산해 카풀종료 알림인지 확인하기위함
            val diff = nowFormat?.time?.minus(format?.time!!)

            if (notification.content.substring(0..1) == "신청") {
                notificationTitleText.text = context.getString(R.string.setNotificationTitleText, notification.content.substring(2..9), category)
                notificationContentText.text = notification.content.removeRange(0..9)
            } else if (notification.content.substring(0..1) == "예약") {
                notificationTitleText.text = context.getString(R.string.setReservationNotificationText, notification.content.substring(2..9), category)
                notificationContentText.text = notification.post.title
            } else if (notification.content.substring(0..1) == "취소") {
                notificationTitleText.text = context.getString(R.string.setReservationNotificationText, notification.content.substring(2..9), category)
                notificationContentText.text = notification.post.title
            } else if (notification.content.substring(0..1) == "댓글") {
                notificationTitleText.text = context.getString(R.string.setCommentNotificationText, notification.content.substring(2..9))
                notificationContentText.text = notification.content.removeRange(0..9)
            } else if (diff?.div((60 * 1000))!! > 0) {
                if (identification == notificationContentItemData[position]?.user?.email) {

                    val endText = context.getString(R.string.setEndNotificationText, notification.content.substring(0..7), category )

                    hashMapCurrentPostItemData[position] = NotificationStatus.Driver
                    //driver이면서 게시글이 종료되었을 때는 후기니까 알람때보낸 content로
                    notificationTitleText.text = endText //님과의 카풀(택시)은/는 어떠셨나요?
                    notificationContentText.text = notification.content //2202020님이 후기를 기다리고 있어요
                } else {
                    val endText = context.getString(R.string.setEndNotificationText, notification.content.substring(0..7), category )

                    hashMapCurrentPostItemData[position] = NotificationStatus.Passenger
                    notificationTitleText.text = endText //님과의 카풀(택시)은/는 어떠셨나요?
                    notificationContentText.text = notification.content //2202020님이 후기를 기다리고 있어요
                }
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
        holder.bind(notificationItemData[holder.adapterPosition], holder.adapterPosition)

        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!

        //본인이 작성자(운전자) 이면서 카풀이 완료
        if (identification == notificationItemData[holder.adapterPosition].post.user.email && hashMapCurrentPostItemData[holder.adapterPosition] == NotificationStatus.Driver) {
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, notificationContentItemData[holder.adapterPosition]?.id, NotificationStatus.Driver)
            }
        } else if (hashMapCurrentPostItemData[holder.adapterPosition] == NotificationStatus.Passenger) { //본인은 운전자가 아니고 손님
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, notificationContentItemData[holder.adapterPosition]?.id, NotificationStatus.Passenger)
            }
        } else { //그냥 자기 게시글 확인
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, notificationContentItemData[holder.adapterPosition]?.id, NotificationStatus.Neither)
            }
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

    interface ItemClickListener {
        fun onClick(view: View,position: Int, itemId: Int?,status : NotificationStatus )
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}