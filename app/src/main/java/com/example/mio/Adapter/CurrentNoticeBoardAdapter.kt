package com.example.mio.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.CurrentPostItemBinding
import com.example.mio.databinding.PostItemBinding
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CurrentNoticeBoardAdapter : RecyclerView.Adapter<CurrentNoticeBoardAdapter.CurrentNoticeBoardViewHolder>(){
    private lateinit var binding : CurrentPostItemBinding
    var currentPostItemData = ArrayList<PostData>()
    private lateinit var context : Context

    private var identification = ""

    init {
        setHasStableIds(true)
    }

    inner class CurrentNoticeBoardViewHolder(private val binding : CurrentPostItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage
        var cPostDate = binding.currentPostDate
        var cPostLocation = binding.currentPostLocation

        fun bind(accountData: PostData, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID
            val s = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime)
            cPostDate.text = s
            cPostLocation.text = accountData.postLocation
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)


            val postDateTime = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime)
            println(postDateTime)

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {

                if(diffSeconds > -1){

                }
                if (diffSeconds > 0) {
                    println("curreenttnetnente")
                    binding.currentCompleteFl.visibility = View.VISIBLE
                    binding.currentCompleteTv.text = "카풀 완료"
                } else {
                    binding.currentCompleteFl.visibility = View.GONE
                }
                /*if (diffMinutes > 0) {
                    binding.commentRealtimeCheck.text = "${diffMinutes.toString()}분전"
                }
                if (diffHours > 0) {
                    binding.commentRealtimeCheck.text = "${diffHours.toString()}시간전"
                }
                if (diffDays > 0) {
                    binding.commentRealtimeCheck.text = "${diffDays.toString()}일전"
                }*/
            }
        } //bind
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentNoticeBoardViewHolder {
        context = parent.context
        binding = CurrentPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CurrentNoticeBoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder:CurrentNoticeBoardViewHolder, position: Int) {
        holder.bind(currentPostItemData[holder.adapterPosition], position)


        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context)!!

        if (identification == currentPostItemData[holder.adapterPosition].user.email) {
        }

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, currentPostItemData[holder.adapterPosition].postID)
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
        return currentPostItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        currentPostItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: CurrentNoticeBoardAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: CurrentNoticeBoardAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}