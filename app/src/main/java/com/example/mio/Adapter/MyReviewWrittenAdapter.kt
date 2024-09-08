package com.example.mio.Adapter
import com.example.mio.diffutil.ProfileReviewDiffUtilCallback
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.AddAlarmResponseData
import com.example.mio.Model.CommentData
import com.example.mio.Model.MyAccountReviewData
import com.example.mio.R
import com.example.mio.databinding.MyReviewItemBinding
import com.example.mio.databinding.ReviewItemTeBinding
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class MyReviewWrittenAdapter : ListAdapter<MyAccountReviewData, MyReviewWrittenAdapter.MyReviewViewHolder>(WrittenDiffUtil){
    private lateinit var binding : MyReviewItemBinding
    //private var myReviewData = ArrayList<MyAccountReviewData>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class MyReviewViewHolder(private val binding : MyReviewItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage
        private var reviewSatisfactionIcon = binding.reviewSatisfaction
        private var reviewCommonlyIcon = binding.reviewCommonly
        private var reviewDissatisfaction = binding.reviewDissatisfaction
        private var reviewContent = binding.reviewContentTv
        private var reviewCreateDate = binding.reviewCreateDate

        fun bind(reviewData: MyAccountReviewData, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID
            when (reviewData.manner) {
                "GOOD" -> {
                    reviewSatisfactionIcon.setImageResource(R.drawable.review_satisfaction_update_icon)
                }
                "BAD" -> {
                    reviewDissatisfaction.setImageResource(R.drawable.review_dissatisfaction_update_icon)
                }
                else -> {
                    reviewCommonlyIcon.setImageResource(R.drawable.review_commonly_update_icon)
                }
            }

            reviewContent.text = reviewData.content

            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            val localDateTime = LocalDateTime.parse(reviewData.createDate, inputFormatter)
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            reviewCreateDate.text = localDateTime.format(outputFormatter)


            //accountProfile.setImageURI() = pillData.pillTakeTime
            //val listener = itemClickListener?.get()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewViewHolder {
        context = parent.context
        binding = MyReviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        holder.bind(currentList[position], position)

        /*holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, myReviewData[holder.adapterPosition].id)
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
        return currentList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        currentList.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }


    /*fun updateDataList(newItems: List<MyAccountReviewData>) {
        val diff = DiffUtil.calculateDiff(ProfileReviewDiffUtilCallback(myReviewData, newItems))
        myReviewData.clear()
        myReviewData.addAll(newItems)

        diff.dispatchUpdatesTo(this)
    }*/

    /*fun updateNotifications(newData: List<AddAlarmResponseData>) {
        val oldSize = notificationItemData.size
        val newSize = newData.size
        val diff = DiffUtil.calculateDiff(NotificationDiffUtilCallback(notificationItemData, newData))

        notificationItemData.clear()
        notificationItemData.addAll(newData)
        diff.dispatchUpdatesTo(this)
    }*/
    fun updateData(newData: List<MyAccountReviewData>) {
        Log.d("NotificationAdapter", "Previous data: ${currentList}") // currentList는 현재 어댑터의 데이터
        Log.d("NotificationAdapter", "New data: $newData")
        //Log.d("NotificationAdapter", "data: $notificationItemData")
        submitList(newData.toList())
    }

}

object WrittenDiffUtil : DiffUtil.ItemCallback<MyAccountReviewData>() {

    override fun areItemsTheSame(oldItem: MyAccountReviewData, newItem: MyAccountReviewData): Boolean {
        val result = oldItem.id == newItem.id // Assuming 'id' is unique for each notification
        Log.d("CommentData", "areItemsTheSame: Comparing oldItem.id = ${oldItem.id} with newItem.id = ${newItem.id}, result: $result")
        return result
    }

    override fun areContentsTheSame(oldItem: MyAccountReviewData, newItem: MyAccountReviewData): Boolean {
        val result = oldItem == newItem // This checks if all fields are the same
        Log.d("CommentData", "areContentsTheSame: Comparing oldItem = $oldItem with newItem = $newItem, result: $result")
        return result
    }
}