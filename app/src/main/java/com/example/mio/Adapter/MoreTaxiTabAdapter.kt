package com.example.mio.Adapter
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Helper.SharedPref
import com.example.mio.Model.PostData
import com.example.mio.Model.SearchWordData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding
import com.example.mio.databinding.SearchWordLayoutBinding

class MoreTaxiTabAdapter : RecyclerView.Adapter<MoreTaxiTabAdapter.MoreTaxiViewHolder>(){

    private lateinit var binding : PostItemBinding
    //var searchWordData = ArrayList<SearchWordData>()
    var moreTaxiData = ArrayList<PostData>()
    var sharedPref : SharedPref? = null
    private lateinit var context : Context
    private var setKey = "setting_search_history"
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

        fun bind(moreData: PostData, position : Int) {
            this.position = position
            val s = context.getString(R.string.setText, moreData.postTargetDate, moreData.postTargetTime)
            postTitle.text = moreData.postTitle
            postDate.text = s
            postLocation.text = moreData.postLocation
            postParticipation.text = moreData.postParticipation.toString()
            postParticipantTotal.text = moreData.postParticipationTotal.toString()



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



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreTaxiTabAdapter.MoreTaxiViewHolder {
        context = parent.context
        sharedPref = SharedPref(context)
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MoreTaxiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoreTaxiViewHolder, position: Int) {
        holder.bind(moreTaxiData[holder.adapterPosition], position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, moreTaxiData[holder.adapterPosition].postID)
        }
        val content : PostData = moreTaxiData[position]
        //holder.searchWord_tv.text = content.searchWordText
    }

    override fun getItemCount(): Int {
        return moreTaxiData.size
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        moreTaxiData.removeAt(position)
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }
    private lateinit var itemClickListener: SearchWordAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: SearchWordAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}