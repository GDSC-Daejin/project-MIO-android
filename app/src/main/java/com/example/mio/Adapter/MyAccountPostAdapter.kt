package com.example.mio.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding
import com.example.mio.databinding.RvLoadingBinding
import kotlinx.coroutines.NonDisposableHandle.parent
import java.lang.ref.WeakReference


class MyAccountPostAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var binding : PostItemBinding
    var myPostItemData = ArrayList<PostData?>()
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

    inner class AccountViewHolder(private val binding : PostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage
        var postTitle = binding.postTitle
        var postDate = binding.postDate
        var postLocation = binding.postLocation
        var postParticipation = binding.postParticipation
        var postParticipantTotal = binding.postParticipationTotal
        var postCost = binding.postCost

        fun bind(accountData: PostData, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID
            val s = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime)
            postTitle.text = accountData.postTitle
            postDate.text = s
            postLocation.text = accountData.postLocation
            postParticipation.text = accountData.postParticipation.toString()
            postParticipantTotal.text = accountData.postParticipationTotal.toString()
            postCost.text = context.getString(R.string.setCost, accountData.postCost.toString())

            //accountProfile.setImageURI() = pillData.pillTakeTime
            //val listener = itemClickListener?.get()
        }
    }
    inner class LoadingViewHolder(var loadingBinding: RvLoadingBinding) : RecyclerView.ViewHolder(loadingBinding.root) {
        val processBar : ProgressBar = loadingBinding.loadingPb
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return if (viewType == TAG_ITEM) {
            return AccountViewHolder(binding)
        } else {
            val binding2 = RvLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding2)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //holder.bind(myPostItemData[holder.adapterPosition]!!, position)

        if (holder is AccountViewHolder) {
            holder.bind(myPostItemData[holder.adapterPosition]!!, position)

            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, myPostItemData[holder.adapterPosition]!!.postID)
            }
            //val content : PostData = myPostItemData[position]!!
            //holder.searchWord_tv.text = content.searchWordText
        }
    }

    override fun getItemCount(): Int {
        return myPostItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (myPostItemData[position] != null) {
            TAG_ITEM
        } else {
            TAG_LOADING
        }
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        myPostItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: MyAccountPostAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: MyAccountPostAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
    fun addMoreData(newData: List<PostData?>) {
        val startPosition = myPostItemData.size
        myPostItemData.addAll(newData)
        notifyItemRangeInserted(startPosition, newData.size)
    }
}