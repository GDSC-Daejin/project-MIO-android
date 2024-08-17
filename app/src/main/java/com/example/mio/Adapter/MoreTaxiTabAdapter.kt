package com.example.mio.Adapter
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.PostData
import com.example.mio.Model.SearchWordData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding
import com.example.mio.databinding.RvLoadingBinding
import com.example.mio.databinding.SearchWordLayoutBinding

class MoreTaxiTabAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    companion object {
        //item을 표시할 때
        private const val TAG_ITEM = 0
        //loading을 표시할 때
        private const val TAG_LOADING = 1
    }

    private lateinit var binding : PostItemBinding
    //var searchWordData = ArrayList<SearchWordData>()
    var moreTaxiData = ArrayList<PostData?>()
    var sharedPref : SharedPref? = null
    private lateinit var context : Context
    var isRemove = false //삭제 체크용

    init {
        setHasStableIds(true)
    }
    inner class MoreTaxiViewHolder(private val binding : PostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        var postTitle = binding.postTitle
        var postDate = binding.postDate
        var postLocation = binding.postLocation
        var postParticipation = binding.postParticipation
        var postParticipantTotal = binding.postParticipationTotal
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



            /*binding.searchwordRemoveIv.setOnClickListener {
                val builder : AlertDialog.Builder = AlertDialog.Builder(context)
                val ad : AlertDialog = builder.create()
                var deleteData = moreTaxiData[this.layoutPosition]!!
                builder.setTitle(deleteData)
                builder.setMessage("정말로 삭제하시겠습니까?")

                builder.setNegativeButton("예",
                    DialogInterface.OnClickListener { dialog, which ->
                        ad.dismiss()
                        removeData(this.layoutPosition)
                    })

                builder.setPositiveButton("아니오",
                    DialogInterface.OnClickListener { dialog, which ->
                        ad.dismiss()
                    })
                builder.show()
            }*/
        }
    }
    inner class LoadingViewHolder(var loadingBinding: RvLoadingBinding) : RecyclerView.ViewHolder(loadingBinding.root) {
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
            holder.bind(moreTaxiData[holder.adapterPosition]!!, position)

            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, moreTaxiData[holder.adapterPosition]!!.postID)
            }
            //val content : PostData = moreTaxiData[position]!!
            //holder.searchWord_tv.text = content.searchWordText
        } /*else {

        }*/


    }

    override fun getItemCount(): Int {
        return moreTaxiData.size
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return if (moreTaxiData[position] != null) {
            TAG_ITEM
        } else {
            TAG_LOADING
        }
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        moreTaxiData.removeAt(position)
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }
    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
    fun addMoreData(newData: List<PostData?>) {
        val startPosition = moreTaxiData.size
        moreTaxiData.addAll(newData)
        notifyItemRangeInserted(startPosition, newData.size)
    }

    fun deleteLoading(){
        moreTaxiData.removeAt(moreTaxiData.lastIndex)
    }
}