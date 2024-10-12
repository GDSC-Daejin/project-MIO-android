package com.example.mio.adapter

import com.example.mio.diffutil.ProfileReviewDiffUtilCallback
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.model.MyAccountReviewData
import com.example.mio.R
import com.example.mio.databinding.ReviewItemBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ProfileReviewAdapter : ListAdapter<MyAccountReviewData, ProfileReviewAdapter.MyReviewViewHolder>(ProfileReviewDiffUtilCallback){
    private lateinit var binding : ReviewItemBinding
    //private var myReviewData = ArrayList<MyAccountReviewData>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class MyReviewViewHolder(private val binding : ReviewItemBinding ) : RecyclerView.ViewHolder(binding.root) {
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
        binding = ReviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        holder.bind(currentList[holder.adapterPosition], position)

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
        submitList(newData.toMutableList())
    }

}