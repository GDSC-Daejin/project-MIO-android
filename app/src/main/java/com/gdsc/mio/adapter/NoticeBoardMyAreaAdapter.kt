package com.gdsc.mio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gdsc.mio.model.Content
import com.gdsc.mio.R
import com.gdsc.mio.databinding.PostItemBinding


class NoticeBoardMyAreaAdapter : RecyclerView.Adapter<NoticeBoardMyAreaAdapter.NoticeBoardMyAreaViewHolder>(){
    private lateinit var binding : PostItemBinding
    var postAreaItemData = ArrayList<Content?>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class NoticeBoardMyAreaViewHolder(private val binding : PostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage
        var postTitle = binding.postTitle
        private var postDate = binding.postDate
        var postLocation = binding.postLocation
        var postParticipation = binding.postParticipation
        private var postParticipantTotal = binding.postParticipationTotal
        var postCost = binding.postCost

        fun bind(areaData: Content, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID
            val s = context.getString(R.string.setText, areaData.targetDate, areaData.targetTime)
            postTitle.text = areaData.title
            postDate.text = s
            postLocation.text = areaData.location
            postParticipation.text = areaData.participantsCount.toString()
            postParticipantTotal.text = areaData.numberOfPassengers.toString()
            postCost.text = context.getString(R.string.setCost, areaData.cost.toString())

            //accountProfile.setImageURI() = pillData.pillTakeTime
            //val listener = itemClickListener?.get()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardMyAreaViewHolder {
        context = parent.context
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeBoardMyAreaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardMyAreaViewHolder, position: Int) {
        holder.bind(postAreaItemData[position]!!, position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, postAreaItemData[holder.adapterPosition]!!.postId)
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
        return postAreaItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    /*fun removeData(position: Int) {
        postAreaItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }*/

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}