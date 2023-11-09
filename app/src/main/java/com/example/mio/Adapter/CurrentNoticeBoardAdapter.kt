package com.example.mio.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.Model.PostData
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.CurrentPostItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CurrentNoticeBoardAdapter : RecyclerView.Adapter<CurrentNoticeBoardAdapter.CurrentNoticeBoardViewHolder>(){
    private lateinit var binding : CurrentPostItemBinding
    var currentPostItemData = ArrayList<PostData>()
    private var hashMapCurrentPostItemData = HashMap<Int, PostStatus>()
    private lateinit var context : Context

    private var identification = ""
    enum class PostStatus {
        Passenger, //손님이면서 카풀완료
        Driver, //운전자이면서 카풀완료
        Neither //둘 다 아니고 그냥 자기가 예약한 게시글 보고 싶음
    }

    init {
        setHasStableIds(true)

        for (i in currentPostItemData.indices) {
            hashMapCurrentPostItemData[i] = PostStatus.Neither
        }
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
            val s = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime) //10-34.5.67.8.910 , 8-5

            //요일 구하기
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val dayDate = dateFormat.parse(accountData.postTargetDate)
            val cal = Calendar.getInstance()
            if (dayDate != null) {
                cal.time = dayDate
            }
            val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }


            val year = accountData.postTargetDate.substring(2..3)
            val month = accountData.postTargetDate.substring(5..6)
            val date1 = accountData.postTargetDate.substring(8..9)
            val hour = accountData.postTargetTime.substring(0..1)
            val minute = accountData.postTargetTime.substring(3..4)

            cPostDate.text = "${year}.${month}.${date1} ($dayOfWeek) ${hour}:${minute}"
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
                    binding.currentCompleteFl.visibility = View.VISIBLE
                    binding.currentCompleteTv.text = "카풀 완료"
                    if (identification == currentPostItemData[position].user.email) {
                        hashMapCurrentPostItemData[position] = PostStatus.Driver
                    } else {
                        hashMapCurrentPostItemData[position] = PostStatus.Passenger
                    }
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

        //본인이 작성자(운전자) 이면서 카풀이 완료
        if (identification == currentPostItemData[holder.adapterPosition].user.email && hashMapCurrentPostItemData[holder.adapterPosition] == PostStatus.Driver) {
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, currentPostItemData[holder.adapterPosition].postID, PostStatus.Driver)
            }
        } else if (hashMapCurrentPostItemData[holder.adapterPosition] == PostStatus.Passenger) { //본인은 운전자가 아니고 손님
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, currentPostItemData[holder.adapterPosition].postID,  PostStatus.Passenger)
            }
        } else { //그냥 자기 게시글 확인
            holder.itemView.setOnClickListener {
                itemClickListener.onClick(it, holder.adapterPosition, currentPostItemData[holder.adapterPosition].postID, PostStatus.Neither)
            }
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
        fun onClick(view: View, position: Int, itemId: Int, status : PostStatus?)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: CurrentNoticeBoardAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: CurrentNoticeBoardAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}