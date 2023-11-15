package com.example.mio.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding
import java.lang.ref.WeakReference


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
    private lateinit var itemClickListener: NoticeBoardAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: NoticeBoardAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}