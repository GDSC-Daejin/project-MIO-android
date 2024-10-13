package com.example.mio.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.helper.SharedPref
import com.example.mio.model.PostData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding
import com.example.mio.databinding.RvLoadingBinding
import com.example.mio.diffutil.ReviewWriteableDiffUtilCallback
import com.example.mio.model.MyAccountReviewData


class MoreTaxiTabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(

){


    companion object {
        //item을 표시할 때
        private const val TAG_ITEM = 0
        //loading을 표시할 때
        private const val TAG_LOADING = 1
    }

    private lateinit var binding : PostItemBinding
    //var searchWordData = ArrayList<SearchWordData>()
    var moreTaxiData = ArrayList<PostData?>()
    private var sharedPref : SharedPref? = null
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class MoreTaxiViewHolder(private val binding : PostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var postTitle = binding.postTitle
        private var postDate = binding.postDate
        var postLocation = binding.postLocation
        var postParticipation = binding.postParticipation
        private var postParticipantTotal = binding.postParticipationTotal
        var postCost = binding.postCost

        fun bind(moreData: PostData, position : Int) {
            this.position = position
            val s = context.getString(R.string.setText, moreData.postTargetDate, moreData.postTargetTime)
            postTitle.text = moreData.postTitle
            postDate.text = s
            postLocation.text = if (moreData.postLocation.split("/").last().isEmpty()) {
                moreData.postLocation.split("/").first()
            } else {
                moreData.postLocation.split("/").last().toString()
            }
            postParticipation.text = moreData.postParticipation.toString()
            postParticipantTotal.text = moreData.postParticipationTotal.toString()
            postCost.text = context.getString(R.string.setCost, moreData.postCost.toString())



            itemView.setOnClickListener {
                itemClickListener.onClick(it, adapterPosition, moreData.postID)
            }
        }
    }
    inner class LoadingViewHolder(private var loadingBinding: RvLoadingBinding) : RecyclerView.ViewHolder(loadingBinding.root) {
        val processBar : ProgressBar = loadingBinding.loadingPb
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        sharedPref = SharedPref(context)
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return if (viewType == TAG_ITEM) {
            MoreTaxiViewHolder(binding)
        } else {
            val binding2 = RvLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding2)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder , position: Int) {

        if (holder is MoreTaxiViewHolder) {
            holder.bind(moreTaxiData[position]!!, position)
        } /*else {

        }*/


    }

    override fun getItemCount(): Int {
        return moreTaxiData.size
    }


    override fun getItemId(position: Int): Long {
        return moreTaxiData[position]?.postID?.toLong() ?: position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (moreTaxiData[position] != null) {
            TAG_ITEM
        } else {
            TAG_LOADING
        }
    }

    //데이터 Handle 함수
    /*fun removeData(position: Int) {
        moreTaxiData.removeAt(position)
        notifyItemRemoved(position)
    }*/

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }
    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    // Adapter의 데이터 리스트를 업데이트하는 메서드
    fun updateDataList(newItems: List<PostData?>) {
        val diffCallback = ReviewWriteableDiffUtilCallback(moreTaxiData, newItems)

        // Calculate the diff
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // 원본 데이터를 업데이트
        moreTaxiData.clear()
        moreTaxiData.addAll(newItems)
        // Dispatch the updates to the adapter
        diffResult.dispatchUpdatesTo(this)
    }

    /*fun updateSortDataList(sortType: String) {
        // 정렬 방식에 따른 데이터 정렬
        val sortedItems = when (sortType) {
            "가격순" -> originalData.sortedBy { it?.postCost }
            "날짜순" -> originalData.sortedByDescending { it?.postCreateDate }
            // 추가적인 정렬 방식이 있으면 추가 가능
            else -> originalData
        }

        val diffCallback = ReviewWriteableDiffUtilCallback(sortedData, sortedItems)

        // Calculate the diff
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // sortedData를 업데이트
        sortedData.clear()
        sortedData.addAll(sortedItems)

        // Dispatch the updates to the adapter
        diffResult.dispatchUpdatesTo(this)
    }*/
}