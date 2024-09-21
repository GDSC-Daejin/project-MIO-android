package com.example.mio.adapter
import com.example.mio.diffutil.ReviewWriteableDiffUtilCallback
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.model.PostData
import com.example.mio.databinding.*


class MyReviewWriteableAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private lateinit var binding : MyReviewWriteableItemBinding
    var myReviewWriteableData = ArrayList<PostData?>()
    private lateinit var context : Context

    companion object {
        //item을 표시할 때
        private const val TAG_ITEM = 0
        //loading을 표시할 때
        private const val TAG_LOADING = 1
    }

    init {
        setHasStableIds(true)
    }

    inner class MyReviewWriteableViewHolder(private val binding : MyReviewWriteableItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage

        private var reviewTitle = binding.reviewWriteableTitle
        private var reviewCompleteCreateDate = binding.reviewWriteableCompleteCreateDate


        fun bind(reviewData: PostData, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID

            //reviewTitle.text = reviewData.
            reviewCompleteCreateDate.text = reviewData.postTargetDate
            reviewTitle.text = reviewData.postTitle


            //accountProfile.setImageURI() = pillData.pillTakeTime
            //val listener = itemClickListener?.get()
        }
    }
    inner class LoadingViewHolder(var loadingBinding: RvLoadingBinding) : RecyclerView.ViewHolder(loadingBinding.root) {
        val processBar : ProgressBar = loadingBinding.loadingPb
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        binding = MyReviewWriteableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == TAG_ITEM) {
            MyReviewWriteableViewHolder(binding)
        } else {
            val binding2 = RvLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding2)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyReviewWriteableViewHolder) {
            holder.bind(myReviewWriteableData[position]!!, position)

            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, position, myReviewWriteableData[position]!!.postID)
            }
        }
    }

    override fun getItemCount(): Int {
        return myReviewWriteableData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (myReviewWriteableData[position] != null) {
            TAG_ITEM
        } else {
            TAG_LOADING
        }
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        myReviewWriteableData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: MyReviewWriteableAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: MyReviewWriteableAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun updateDataList(newItems: List<PostData?>) {
        // Create a new DiffUtil.Callback instance
        val diffCallback = ReviewWriteableDiffUtilCallback(myReviewWriteableData, newItems)

        // Calculate the diff
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        myReviewWriteableData.clear()
        myReviewWriteableData.addAll(newItems.sortedByDescending { it?.postCreateDate })

        diffResult.dispatchUpdatesTo(this)
    }

}