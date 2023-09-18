package com.example.mio.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.MyAccountReviewData
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding
import com.example.mio.databinding.ReviewItemBinding
import kotlinx.coroutines.NonDisposableHandle.parent
import java.lang.ref.WeakReference


class ProfileReviewAdapter : RecyclerView.Adapter<ProfileReviewAdapter.ProfileReviewViewHolder>(){
    private lateinit var binding : ReviewItemBinding
    var profileReviewItemData = ArrayList<MyAccountReviewData>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class ProfileReviewViewHolder(private val binding : ReviewItemBinding ) : RecyclerView.ViewHolder(binding.root) {
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
                "good" -> {
                    reviewSatisfactionIcon.setImageResource(R.drawable.review_satisfaction_update_icon)
                }
                "bad" -> {
                    reviewDissatisfaction.setImageResource(R.drawable.review_dissatisfaction_update_icon)
                }
                else -> {
                    reviewCommonlyIcon.setImageResource(R.drawable.review_commonly_update_icon)
                }
            }

            reviewContent.text = reviewData.content
            reviewCreateDate.text = reviewData.createDate


            //accountProfile.setImageURI() = pillData.pillTakeTime
            //val listener = itemClickListener?.get()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileReviewViewHolder {
        context = parent.context
        binding = ReviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileReviewViewHolder, position: Int) {
        holder.bind(profileReviewItemData[holder.adapterPosition], position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, profileReviewItemData[holder.adapterPosition].id)
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
        return profileReviewItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        profileReviewItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: ProfileReviewAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: ProfileReviewAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}