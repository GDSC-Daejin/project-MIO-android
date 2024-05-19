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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyAccountParticipationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
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

            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)

            val postDateTime = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime)
            println(postDateTime)
            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate) //현재시간
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(postDateTime) //카풀이 출발하는시간
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!) //현재시간 - 카풀시간
            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {
                //TODO 이거 여기 api 잘보고 데이터 값 확인하고 수정하기
                /*if (diffMinutes < 0) {
                    if (accountData.postVerifyGoReturn && accountData.) {
                        binding.postStatus.setImageResource(R.drawable.reservation_carpool_complete)
                        binding.postStatus.visibility = View.VISIBLE
                    } else {
                        //3번?
                        binding.postStatus.setImageResource(R.drawable.reservation_complete)
                        binding.postStatus.visibility = View.VISIBLE
                    }
                }*/

                if(diffSeconds > -1){

                }

                if (diffSeconds > 0) {

                }

                if (diffMinutes > 0) {

                }

                if (diffHours > 0) {

                }

                //여기 잘 생각해서 다시 수정하기 TODO
                if (diffDays > 0) {
                    println("진입완료")
                    if (accountData.postVerifyGoReturn) {
                        binding.postStatus.setImageResource(R.drawable.reservation_carpool_complete)
                        binding.postStatus.visibility = View.VISIBLE
                    } else {
                        binding.postStatus.setImageResource(R.drawable.reservation_complete)
                        binding.postStatus.visibility = View.VISIBLE
                    }
                } else {
                    println("???????")
                    binding.postStatus.visibility = View.VISIBLE
                }

                if (diffDays < 0) {

                }
            }
        }
    }
    inner class LoadingViewHolder(var loadingBinding: RvLoadingBinding) : RecyclerView.ViewHolder(loadingBinding.root) {
        val processBar : ProgressBar = loadingBinding.loadingPb
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == TAG_ITEM) {
            AccountViewHolder(binding)
        } else {
            val binding2 = RvLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding2)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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
    private lateinit var itemClickListener: MyAccountParticipationAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: MyAccountParticipationAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}