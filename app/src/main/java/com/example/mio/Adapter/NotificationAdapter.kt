package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.NotificationData
import com.example.mio.Model.PostData
import com.example.mio.databinding.NotificationItemBinding
import com.example.mio.databinding.PostItemBinding


class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>(){
    private lateinit var binding : NotificationItemBinding
    var notificationItemData = ArrayList<NotificationData>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class NotificationViewHolder(private val binding : NotificationItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var notificationContentText = binding.notificationItemContentTv
        var notificationDateText = binding.notificationItemDateTv
        fun bind(notification : NotificationData, position : Int) {
            this.position = position
            notificationContentText.text = notification.notificationContentText.postContent
            notificationDateText.text = notification.applyDate

            //accountProfile.setImageURI() = pillData.pillTakeTime

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, layoutPosition, notificationItemData[layoutPosition].notificationPos)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        context = parent.context
        binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notificationItemData[holder.adapterPosition], holder.adapterPosition)
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
        return notificationItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        notificationItemData.removeAt(position)
        //temp = null
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