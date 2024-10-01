package com.example.mio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.R
import com.example.mio.model.LocationReadAllResponse
import com.example.mio.databinding.PostItem2Binding

class NearbyPostAdapter(private val onItemClick: (LocationReadAllResponse) -> Unit) : ListAdapter<LocationReadAllResponse, NearbyPostAdapter.ViewHolder>(NearbyPostDiffUtil){

    //private var posts: List<LocationReadAllResponse> = listOf()
    private var context : Context? = null

    inner class ViewHolder(val binding: PostItem2Binding) : RecyclerView.ViewHolder(binding.root) {
        val postTitle = binding.postTitle
        val postDate = binding.postDate
        val postLocation= binding.postLocation
        val postMoney = binding.postCost
        val postParticipantsCount = binding.postParticipation
        val postParticipantsTotal = binding.postParticipationTotal

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(currentList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = PostItem2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = currentList[position]
        holder.postTitle.text = post.title
        holder.postDate.text = context?.getString(R.string.setDateText2, post.targetDate, post.targetTime)//post.targetDate + " " + post.targetTime
        holder.postLocation.text = post.location
        holder.postMoney.text = post.cost.toString()
        holder.postParticipantsCount.text = post.participantsCount.toString()
        holder.postParticipantsTotal.text = post.numberOfPassengers.toString()
    }

    override fun getItemCount(): Int = currentList.size

    fun setData(newPosts: List<LocationReadAllResponse>) {
        val updateData = newPosts.filter { it.isDeleteYN != "Y" && it.postType == "BEFORE_DEADLINE" }
        submitList(updateData.toList())
    }
}

object NearbyPostDiffUtil : DiffUtil.ItemCallback<LocationReadAllResponse>() {
    override fun areItemsTheSame(
        oldItem: LocationReadAllResponse,
        newItem: LocationReadAllResponse
    ): Boolean {
        return oldItem.postId == newItem.postId
    }

    override fun areContentsTheSame(
        oldItem: LocationReadAllResponse,
        newItem: LocationReadAllResponse
    ): Boolean {
        return oldItem == newItem
    }
}