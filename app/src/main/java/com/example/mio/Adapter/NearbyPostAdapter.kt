package com.example.mio.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.R
import com.example.mio.databinding.PostItem2Binding

class NearbyPostAdapter(private val onItemClick: (LocationReadAllResponse) -> Unit) :
    RecyclerView.Adapter<NearbyPostAdapter.ViewHolder>() {

    private var posts: List<LocationReadAllResponse> = listOf()

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
                    onItemClick(posts[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostItem2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.postTitle.text = post.title
        holder.postDate.text = post.targetDate + " " + post.targetTime  // postDate 형식에 따라 적절하게 변환 필요
        holder.postLocation.text = post.location
        holder.postMoney.text = post.cost.toString()
        holder.postParticipantsCount.text = post.participantsCount.toString()
        holder.postParticipantsTotal.text = post.numberOfPassengers.toString()
    }

    override fun getItemCount(): Int = posts.size

    fun setData(newPosts: List<LocationReadAllResponse>) {
        this.posts = newPosts.filter { it.isDeleteYN != "Y" && it.postType == "BEFORE_DEADLINE" }
        notifyDataSetChanged()
    }
}