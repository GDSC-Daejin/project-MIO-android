import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.R
import com.example.mio.SaveSharedPreferenceGoogleLogin
import com.example.mio.databinding.CurrentPostItemBinding
import com.example.mio.diffutil.CurrentReservationDiffUtilCallback
import com.example.mio.diffutil.ReviewWriteableDiffUtilCallback
import com.example.mio.model.PostData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CurrentCarpoolAdapter : RecyclerView.Adapter<CurrentCarpoolAdapter.CurrentViewHolder>() {
    private lateinit var binding: CurrentPostItemBinding
    private var currentPostItemData = ArrayList<PostData?>()
    private var hashMapCurrentPostItemData = HashMap<Int, PostStatus>()
    private lateinit var context: Context

    private var identification = ""

    enum class PostStatus {
        Passenger,
        Driver,
        Neither
    }

    init {
        setHasStableIds(true)
    }

    inner class CurrentViewHolder(private val binding: CurrentPostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(accountData: PostData, position: Int) {
            // 날짜 및 시간 포맷팅
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val dayDate = dateFormat.parse(accountData.postTargetDate)
            val cal = Calendar.getInstance().apply {
                time = dayDate ?: Date() // null 체크
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

            // 사용자 이메일 가져오기
            val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
            identification = saveSharedPreferenceGoogleLogin.getUserEMAIL(context) ?: ""

            // 날짜 및 시간 포맷팅
            val year = accountData.postTargetDate.substring(2..3)
            val month = accountData.postTargetDate.substring(5..6)
            val date1 = accountData.postTargetDate.substring(8..9)
            val hour = accountData.postTargetTime.substring(0..1)
            val minute = accountData.postTargetTime.substring(3..4)
            binding.currentPostDate.text = "${year}.${month}.${date1} ($dayOfWeek) ${hour}:${minute}"

            // 위치 정보 설정
            val location = accountData.postLocation.split(" ")
            binding.currentPostLocation.text = if (location.last() == " ") {
                location.dropLast(1).joinToString(" ")
            } else {
                location.last()
            }

            // 현재 시간과 비교하여 상태 업데이트
            updatePostCompletionStatus(accountData, position)
        }

        private fun updatePostCompletionStatus(accountData: PostData, position: Int) {
            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)
            val postDateTime = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime)

            // 시간 차이 계산
            val nowFormat = sdf.parse(currentDate)
            val beforeFormat = sdf.parse(postDateTime)
            val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time ?: 0) ?: 0
            val diffSeconds = diffMilliseconds / 1000

            if (diffSeconds > 0) {
                binding.currentCompleteFl.visibility = View.VISIBLE
                binding.currentCompleteTv.text = "운행 종료"

                // 운전자가 아니라면 손님으로 설정
                if (identification == currentPostItemData[position]?.user?.email) {
                    hashMapCurrentPostItemData[position] = PostStatus.Driver
                } else {
                    hashMapCurrentPostItemData[position] = PostStatus.Passenger
                }
            } else {
                binding.currentCompleteFl.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentViewHolder {
        context = parent.context
        binding = CurrentPostItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CurrentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrentViewHolder, position: Int) {
        currentPostItemData[position]?.let { postData ->
            holder.bind(postData, position)
            setupClickListener(holder, position)
        }
    }

    private fun setupClickListener(holder: CurrentViewHolder, position: Int) {
        val status = hashMapCurrentPostItemData[position]
        val itemId = currentPostItemData[position]?.postID ?: -1 // 안전한 아이디 할당

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position, itemId, status)
        }
    }

    override fun getItemCount(): Int = currentPostItemData.size

    override fun getItemId(position: Int): Long = position.toLong()

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int, status: PostStatus?)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun updateDataList(newItems: List<PostData?>) {
        Log.e("updateCar", "New data items: $newItems")

        // Log currentPostItemData before update
        Log.e("updateCar", "Current data items before update: $currentPostItemData")

        if (currentPostItemData.isEmpty()) {
            Log.d("updateCar", "Initial data set, using notifyDataSetChanged()")
            currentPostItemData.clear()
            currentPostItemData.addAll(newItems)

            // Update the HashMap based on newItems
            hashMapCurrentPostItemData.clear() // 해시맵 초기화
            newItems.forEachIndexed { index, postData ->
                hashMapCurrentPostItemData[index] = PostStatus.Neither // 기본 상태 설정
                Log.d("updateCar", "HashMap updated: index=$index, status=Neither")
            }

            // Use notifyDataSetChanged for the first update
            notifyDataSetChanged()
        } else {
            // Create a new DiffUtil.Callback instance
            val diffCallback = CurrentReservationDiffUtilCallback(currentPostItemData, newItems)

            // Calculate the diff
            Log.d("updateCar", "Calculating DiffUtil...")
            val diffResult = DiffUtil.calculateDiff(diffCallback)

            // Clear and update the list only once
            Log.d("updateCar", "Clearing current data list and adding new items...")
            currentPostItemData.clear()
            currentPostItemData.addAll(newItems)

            // Log currentPostItemData after update
            Log.e("updateCar", "Current data items after update: $currentPostItemData")

            // Update the HashMap based on newItems
            Log.d("updateCar", "Updating hashMapCurrentPostItemData...")
            hashMapCurrentPostItemData.clear() // 해시맵 초기화
            newItems.forEachIndexed { index, postData ->
                hashMapCurrentPostItemData[index] = PostStatus.Neither // 기본 상태 설정
                Log.d("updateCar", "HashMap updated: index=$index, status=Neither")
            }

            // Log before dispatching updates to adapter
            Log.d("updateCar", "Dispatching updates to adapter...")
            diffResult.dispatchUpdatesTo(this@CurrentCarpoolAdapter)

            // Log to confirm dispatch completion
            Log.d("updateCar", "RecyclerView update dispatched.")
        }
    }
}
