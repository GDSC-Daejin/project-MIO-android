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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.Model.*
import com.example.mio.databinding.ParticipationItemLayoutBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class ParticipationAdapter : RecyclerView.Adapter<ParticipationAdapter.ParticipationViewHolder>() {
    private lateinit var binding: ParticipationItemLayoutBinding
    var participationItemData = ArrayList<ParticipationData>()
    var participantsUserData = kotlin.collections.ArrayList<User?>()  // 사용자 데이터를 Map으로 변경하여 빠른 검색 가능
    private lateinit var context: Context
    private var selectedItemPosition: Int? = null
    var target = ""



    inner class ParticipationViewHolder(private val binding: ParticipationItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(partData: ParticipationData, position: Int) {
            val userId = partData.userId
            val user : User? = participantsUserData.find { it?.id == userId }

            Log.e("participantsUserDataMap", user.toString())
            Log.e("participantsUserDataMap", partData.toString())
            // UI 업데이트
            updateUI(binding, partData, user, position)
        }

        private fun updateUI(binding: ParticipationItemLayoutBinding, partData: ParticipationData, user: User?, position: Int) {
            Log.e("binding", user.toString())
            Log.e("binding", partData.toString())

            // 선택된 아이템인지 확인하고 배경 색상 설정
            if (selectedItemPosition == position) {
                setItemSelected(binding)
            } else {
                setItemUnselected(binding)
            }

            val gender = if (participantsUserData.find { it?.id == user?.id }?.gender != null) {
                if (participantsUserData.find {it?.id == user?.id}?.gender == true) {
                    "여성"
                } else {
                    "남성"
                }
            } else {
                "설정X"
            }

            val smoke = if (participantsUserData.find { it?.id == user?.id}?.verifySmoker != null) {
                if (participantsUserData.find {it?.id == user?.id}?.verifySmoker == true) {
                    "흡연 O"
                } else {
                    "흡연 X"
                }
            } else {
                "설정X"
            }

            binding.participationFilterTv.text = "${user?.studentId} | $gender $smoke"
            binding.participationContentTv.text = partData.content

            if (partData.approvalOrReject == "APPROVAL") {
                setItemSelected(binding)
            }

            // 승인, 거절, 취소 버튼 클릭 리스너 설정
            setButtonListeners(binding, partData, position)
        }

        private fun setItemSelected(binding: ParticipationItemLayoutBinding) {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mio_gray_5))
            binding.participationCsl.backgroundTintList = colorStateList
            binding.participationItemLl.backgroundTintList = colorStateList
            binding.participationRefuse.visibility = View.GONE
            binding.participationApproval.visibility = View.GONE
            binding.participationCancel.visibility = View.VISIBLE
        }

        private fun setItemUnselected(binding: ParticipationItemLayoutBinding) {
            val colorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mio_gray_1))
            binding.participationCsl.backgroundTintList = colorStateList
            binding.participationItemLl.backgroundTintList = colorStateList
            binding.participationRefuse.visibility = View.VISIBLE
            binding.participationApproval.visibility = View.VISIBLE
            binding.participationCancel.visibility = View.GONE
        }

        private fun setButtonListeners(binding: ParticipationItemLayoutBinding, partData: ParticipationData, position: Int) {
            binding.participationApproval.setOnClickListener {
                selectedItemPosition = position
                notifyDataSetChanged()
                fetchItemDetails(partData.participantId)
                itemClickListener.onApprovalClick(position, partData.participantId.toString())
            }

            binding.participationRefuse.setOnClickListener {
                removeData(partData.participantId, position)
                itemClickListener.onRefuseClick(position, partData.participantId.toString())
                notifyDataSetChanged()
            }

            binding.participationCancel.setOnClickListener {
                if (isTargetTimePassed(target)) {
                    Log.e("participation", "예정된")
                    Toast.makeText(context, "예정된 시간이 지나 취소하실 수 없습니다", Toast.LENGTH_SHORT).show()
                } else {
                    removeData(partData.participantId, position)
                    itemClickListener.onRefuseClick(position, partData.participantId.toString())
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipationViewHolder {
        context = parent.context
        binding = ParticipationItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipationViewHolder, position: Int) {
        holder.bind(participationItemData[position], position)
    }

    override fun getItemCount(): Int {
        return participationItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun isTargetTimePassed(targetDate: String): Boolean {
        // targetDate와 targetTime을 합쳐서 하나의 LocalDateTime으로 변환
        val targetDateTime = LocalDateTime.parse(targetDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // 현재 시간을 가져옴
        val now = LocalDateTime.now()

        // 현재 시간이 targetDateTime을 지났으면 true, 아니면 false 반환
        return now.isAfter(targetDateTime)
    }

    fun removeData(participantsId: Int, position: Int) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context).toString()

        /*/////////interceptor
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
                    *//*newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()*//*
                    Log.e("participation", "adapter")
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    Toast.makeText(context, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
        val api = retrofit2.create(MioInterface::class.java)*/
        /////////


        //당일 취소 불가능, 마감이면 취소 불가능,
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(context).deleteParticipants(participantsId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("PART Remove Success ", response.code().toString())
                    } else {
                        Log.e("PART Remove ERROR ", response.code().toString())
                        Log.e("PART Remove ERROR ", response.errorBody()?.string()!!)
                        Log.e("PART Remove ERROR ", response.message().toString())


                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("PART Remove ERROR ", t.toString())
                }
            })
        }
    }

    private fun fetchItemDetails(participantsId: Int) {
        val saveSharedPreferenceGoogleLogin = SaveSharedPreferenceGoogleLogin()
        val token = saveSharedPreferenceGoogleLogin.getToken(context).toString()
        val getExpireDate = saveSharedPreferenceGoogleLogin.getExpireDate(context).toString()

        /////////interceptor
        /*val SERVER_URL = BuildConfig.server_URL
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
                    Log.e("participation", "adapter")
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    Toast.makeText(context, "로그인이 만료되었습니다. 다시 로그인해주세요", Toast.LENGTH_SHORT).show()
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
        val api = retrofit2.create(MioInterface::class.java)*/
        /////////

        //200확인 완료
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitServerConnect.create(context).patchParticipantsApproval(participantsId).enqueue(object : Callback<Void> {
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


    // UI 갱신 함수
    fun updateData(newData: List<ParticipationData>, newUserData: List<User?>) {
        participationItemData.clear()
        participationItemData.addAll(newData)
        participantsUserData.clear()
        newUserData.forEach { user -> user?.id?.let { participantsUserData[it] = user } }
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onApprovalClick(position: Int, participantId: String)
        fun onRefuseClick(position: Int, participantId: String)
    }

    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}
