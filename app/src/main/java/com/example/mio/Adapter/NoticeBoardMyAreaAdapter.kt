package com.example.mio.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.LocationReadAllResponse
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.databinding.PostItemBinding
import java.lang.ref.WeakReference


class NoticeBoardMyAreaAdapter : RecyclerView.Adapter<NoticeBoardMyAreaAdapter.NoticeBoardMyAreaViewHolder>(){
    private lateinit var binding : PostItemBinding
    var postAreaItemData : List<LocationReadAllResponse>? = null
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class NoticeBoardMyAreaViewHolder(private val binding : PostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage
        var postTitle = binding.postTitle
        var postDate = binding.postDate
        var postLocation = binding.postLocation
        var postParticipation = binding.postParticipation
        var postParticipantTotal = binding.postParticipationTotal
        var postCost = binding.postCost

        fun bind(areaData: LocationReadAllResponse, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID
            val s = context.getString(R.string.setText, areaData.targetDate, areaData.targetTime)
            postTitle.text = areaData.title
            postDate.text = s
            postLocation.text = areaData.location
            postParticipation.text = areaData.participants?.size.toString()
            postParticipantTotal.text = areaData.numberOfPassengers.toString()
            postCost.text = context.getString(R.string.setCost, areaData.cost.toString())

            //accountProfile.setImageURI() = pillData.pillTakeTime
            //val listener = itemClickListener?.get()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeBoardMyAreaViewHolder {
        context = parent.context
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoticeBoardMyAreaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeBoardMyAreaViewHolder, position: Int) {
        holder.bind(postAreaItemData!![holder.adapterPosition], position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, postAreaItemData!![holder.adapterPosition].postId)
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
        return postAreaItemData?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    /*fun removeData(position: Int) {
        postAreaItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }*/

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: NoticeBoardMyAreaAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: NoticeBoardMyAreaAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}