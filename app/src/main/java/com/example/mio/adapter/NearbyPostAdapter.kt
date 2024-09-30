package com.example.mio.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.model.LocationReadAllResponse
import com.example.mio.databinding.PostItem2Binding

class NearbyPostAdapter(private val onItemClick: (LocationReadAllResponse) -> Unit) : ListAdapter<LocationReadAllResponse, NearbyPostAdapter.ViewHolder>(NearbyPostDiffUtil){

    //private var posts: List<LocationReadAllResponse> = listOf()

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
        val binding = PostItem2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = currentList[position]
        holder.postTitle.text = post.title
        holder.postDate.text = post.targetDate + " " + post.targetTime  // postDate 형식에 따라 적절하게 변환 필요
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
    override fun areItemsTheSame(oldItem: LocationReadAllResponse, newItem: LocationReadAllResponse): Boolean {
        val result = oldItem.postId == newItem.postId // Assuming 'id' is unique for each notification
        return result
    }

    override fun areContentsTheSame(oldItem: LocationReadAllResponse, newItem: LocationReadAllResponse): Boolean {
        val result = oldItem == newItem // This checks if all fields are the same
        Log.d("CommentData", "areContentsTheSame: Comparing oldItem = $oldItem with newItem = $newItem, result: $result")
        return result
    }
}