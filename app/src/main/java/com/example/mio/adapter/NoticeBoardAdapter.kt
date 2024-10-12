package com.example.mio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.model.PostData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding

class NoticeBoardAdapter : RecyclerView.Adapter<NoticeBoardAdapter.NoticeBoardViewHolder>(){
    private lateinit var binding : PostItemBinding
    var postItemData = ArrayList<PostData>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class NoticeBoardViewHolder(private val binding : PostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage
        var postTitle = binding.postTitle
        private var postDate = binding.postDate
        var postLocation = binding.postLocation
        var postParticipation = binding.postParticipation
        private var postParticipantTotal = binding.postParticipationTotal
        var postCost = binding.postCost

        fun bind(accountData: PostData, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID
            val s = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime)
            postTitle.text = accountData.postTitle
            postDate.text = s
           /* postLocation.text = if (accountData.postLocation.split(" ").last().toString() == " ") {
                accountData.postLocation.split(" ").dropLast(1).joinToString(" ")//마지막 빼고 세팅 ex) 경기 선단로 100011 - 1 무슨빌딩 -> 경기 선단로 100011 - 1
            } else {
                accountData.postLocation.split(" ").last().toString()
            }*/
            postLocation.text = accountData.postLocation
            postParticipation.text = accountData.postParticipation.toString()
            postParticipantTotal.text = accountData.postParticipationTotal.toString()
            postCost.text = context.getString(R.string.setCost, accountData.postCost.toString())

            //accountProfile.setImageURI() = pillData.pillTakeTime
            //val listener = itemClickListener?.get()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardViewHolder {
        context = parent.context
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeBoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardViewHolder, position: Int) {
        holder.bind(postItemData[holder.adapterPosition], position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, postItemData[holder.adapterPosition].postID)
        }
    }

    override fun getItemCount(): Int {
        return postItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        postItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

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