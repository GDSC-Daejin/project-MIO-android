package com.example.mio.Adapter
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.Model.*
import com.example.mio.databinding.ParticipationItemLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class ParticipationAdapter : RecyclerView.Adapter<ParticipationAdapter.ParticipationViewHolder>(){
    private lateinit var binding : ParticipationItemLayoutBinding
    var participationItemData = ArrayList<ParticipationData>()
    private lateinit var context : Context
    var participantsUserData = ArrayList<User?>()

    //클릭된 아이템의 위치를 저장할 변수
    private var selectedItem = -1
    //private var cancelItem = -2
    init {
        setHasStableIds(true)
        Log.d("ParticipationAdapter", participationItemData.toString())
        Log.d("ParticipationAdapter", participantsUserData.toString())
    }

    inner class ParticipationViewHolder(private val binding : ParticipationItemLayoutBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        //var accountId = binding.accountId
        //var accountProfile = binding.accountImage
        var itemFilter = binding.participationFilterTv
        var itemContent = binding.participationContentTv

        fun bind(partData : ParticipationData, position : Int) {
            this.position = position
            //accountId.text = accountData.accountID
            //val s = context.getString(R.string.setText, accountData.postTargetDate, accountData.postTargetTime)
            itemContent.text = partData.content

            val gender = if (participantsUserData[position]?.gender != null) {
                if (participantsUserData[position]?.gender == true) {
                    "여성"
                } else {
                    "남성"
                }
            } else {
                "설정X"
            }

            val smoke = if (participantsUserData[position]?.verifySmoker != null) {
                if (participantsUserData[position]?.verifySmoker == true) {
                    "흡연 O"
                } else {
                    "흡연 X"
                }
            } else {
                "설정X"
            }


            itemFilter.text = "${participantsUserData[position]?.studentId} | $gender $smoke"


            if (partData.approvalOrReject == "APPROVAL") {
                val handler = Handler(Looper.getMainLooper())

                val r = Runnable {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context , R.color.mio_gray_4)) //승인

                    // 배경색 변경
                    binding.participationCsl.backgroundTintList = colorStateList
                    binding.participationItemLl.backgroundTintList = colorStateList

                    binding.participationRefuse.visibility = View.GONE
                    binding.participationApproval.visibility = View.GONE
                    selectedItem = position
                    notifyDataSetChanged()
                }

                handler.post(r)

            } else {
                val handler = Handler(Looper.getMainLooper())

                val r = Runnable {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context , R.color.mio_gray_1)) //취소됨

                    // 배경색 변경
                    binding.participationCsl.backgroundTintList = colorStateList
                    binding.participationItemLl.backgroundTintList = colorStateList

                    binding.participationRefuse.visibility = View.VISIBLE
                    binding.participationApproval.visibility = View.VISIBLE
                    selectedItem = -1
                    notifyDataSetChanged()
                }

                handler.post(r)
            }
            /*when (partData.approvalOrReject) {
                "APPROVAL" -> {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context , R.color.mio_gray_4)) //승인

                    // 배경색 변경
                    binding.participationCsl.backgroundTintList = colorStateList
                    binding.participationItemLl.backgroundTintList = colorStateList

                    binding.participationCancel.visibility = View.VISIBLE
                    binding.participationRefuse.visibility = View.GONE
                    binding.participationApproval.visibility = View.GONE

                }
                "REJECT" -> {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context , R.color.mio_gray_1)) //취소됨

                    // 배경색 변경
                    binding.participationCsl.backgroundTintList = colorStateList
                    binding.participationItemLl.backgroundTintList = colorStateList

                    binding.participationCancel.visibility = View.GONE
                    binding.participationRefuse.visibility = View.VISIBLE
                    binding.participationApproval.visibility = View.VISIBLE

                }
                else -> {
                    val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context , R.color.mio_gray_1)) //취소됨

                    // 배경색 변경
                    binding.participationCsl.backgroundTintList = colorStateList
                    binding.participationItemLl.backgroundTintList = colorStateList

                    binding.participationCancel.visibility = View.GONE
                    binding.participationRefuse.visibility = View.VISIBLE
                    binding.participationApproval.visibility = View.VISIBLE
                }
            }*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipationAdapter.ParticipationViewHolder {
        context = parent.context
        /*val view = LayoutInflater.from(parent.context).inflate(
            if (viewType == selectedItem) R.layout.participation_item_update_layout else R.layout.participation_item_layout,
            parent, false
        )*/
        binding = ParticipationItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        Log.d("ParticipationAdapter", participationItemData.toString())
        Log.d("ParticipationAdapter", participantsUserData.toString())
        return ParticipationViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ParticipationAdapter.ParticipationViewHolder, position: Int) {
        holder.bind(participationItemData[position], position)

        if(selectedItem == position) {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context , R.color.mio_gray_4)) //승인

            holder.itemView.backgroundTintList = colorStateList

            // 배경색 변경
            /*binding.participationCsl.backgroundTintList = colorStateList
            binding.participationItemLl.backgroundTintList = colorStateList*/

            binding.participationRefuse.visibility = View.GONE
            binding.participationApproval.visibility = View.GONE
        } else {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context , R.color.mio_gray_1)) //취소됨
            holder.itemView.backgroundTintList = colorStateList


            binding.participationRefuse.visibility = View.VISIBLE
            binding.participationApproval.visibility = View.VISIBLE
        }

        holder.itemView.findViewById<Button>(R.id.participation_approval).setOnClickListener {
            //itemClickListener.onClick(it, holder.adapterPosition, participationItemData[holder.adapterPosition].postId)
            val pos = position
            selectedItem = pos

            //notifyItemChanged(beforePos)
            notifyItemChanged(selectedItem)
        }




        binding.participationApproval.setOnClickListener {
            println("clclclclclclclclclclclclclclclappp")
            val approvalPosition = position
            val item = participationItemData[position]
            // 아이템 정보를 사용하여 서버 요청 보내기
            fetchItemDetails(item.participantId)
            //cancelItem = -1
            selectedItem = approvalPosition
            notifyDataSetChanged()
            sendAlarmData("예약", position, item)
        }

        binding.participationRefuse.setOnClickListener {
            //removeData(holder.adapterPosition)
            println("cancleellelelelelelelelee")
            val cancelPosition = position
            selectedItem = -1
            val item = participationItemData[position]
            ////cancelItem = cancelPosition
            notifyDataSetChanged()
            println(participationItemData[position].approvalOrReject)
            removeData(participationItemData[position].participantId, position)
            sendAlarmData("취소", position, item)
        }

        /*binding.participationCancel.setOnClickListener {
            println("cancleellelelelelelelelee")
            val cancelPosition = position
            selectedItem = -1
            val item = participationItemData[position]
            //cancelItem = cancelPosition
            notifyDataSetChanged()
            removeData(participationItemData[position].participantId, position)
            sendAlarmData("취소", position, item)
        }*/



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
        return participationItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == selectedItem) R.layout.participation_item_update_layout else R.layout.participation_item_layout
    }

    //데이터 Handle 함수
    fun removeData(participantsId: Int, position: Int) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        //Authorization jwt토큰 로그인
        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    /*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*/
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    return@Interceptor chain.proceed(newRequest)
                }
            } else {
                newRequest = chain.request()
            }
            chain.proceed(newRequest)
        }
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////////

        //삭제 테스트해보기 TODO
        CoroutineScope(Dispatchers.IO).launch {
            api.deleteParticipants(participantsId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("PART Remove Success ", response.code().toString())
                    } else {
                        Log.e("PART Remove ERROR ", response.code().toString())
                        Log.e("PART Remove ERROR ", response.errorBody().toString())
                        Log.e("PART Remove ERROR ", response.message().toString())
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("PART Remove ERROR ", t.toString())
                }
            })
        }
        participationItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

   /* fun patchData(participantsId: Int) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        //Authorization jwt토큰 로그인
        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                    return@Interceptor chain.proceed(newRequest)
                }
            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////////
        val temp = participationItemData[holder.adapterPosition]

        CoroutineScope(Dispatchers.IO).launch {
            api.patchParticipantsApproval(temp.userId).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {

                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {

                }
            })
        }
    }*/

    private fun fetchItemDetails(participantsId: Int) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context).toString()

        /////////interceptor
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        //Authorization jwt토큰 로그인
        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    return@Interceptor chain.proceed(newRequest)
                }
            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        /////////

        //200확인 완료
        CoroutineScope(Dispatchers.IO).launch {
            api.patchParticipantsApproval(participantsId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        println(response.code())
                    } else {
                        val stringToJson = JSONObject(response.errorBody()?.string()!!)
                        Log.e("YMC", "stringToJson: $stringToJson")
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("ERROR", t.toString())
                }
            })
        }
    }

    private fun sendAlarmData(status : String, dataPos : Int, data : ParticipationData) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context).toString()

        //.client(clientBuilder)
        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        //Authorization jwt토큰 로그인
        val interceptor = Interceptor { chain ->

            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    return@Interceptor chain.proceed(newRequest)
                }
            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MioInterface::class.java)
        ///////////////////////////////
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val currentDate = sdf.format(date)
        val formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        val result: Instant = Instant.from(formatter.parse(currentDate))

        //userId 가 알람 받는 사람
        val temp = AddAlarmData(result.toString(), "${status}${participantsUserData[dataPos]?.studentId}", data.postId, data.userId)

        //entity가 알람 받는 사람, user가 알람 전송한 사람
        CoroutineScope(Dispatchers.IO).launch {
            api.addAlarm(temp).enqueue(object : Callback<AddAlarmResponseData?> {
                override fun onResponse(
                    call: Call<AddAlarmResponseData?>,
                    response: Response<AddAlarmResponseData?>
                ) {
                    if (response.isSuccessful) {
                        println("succcc send alarm")
                    } else {
                        println("faafa alarm")
                        Log.d("alarm", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<AddAlarmResponseData?>, t: Throwable) {
                    Log.d("error", t.toString())
                }

            })
        }
    }



    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: ParticipationAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: ParticipationAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}