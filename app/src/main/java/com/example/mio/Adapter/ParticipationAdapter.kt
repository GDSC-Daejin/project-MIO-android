package com.example.mio.Adapter

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mio.*
import com.example.mio.Model.*
import com.example.mio.databinding.ParticipationItemLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class ParticipationAdapter : RecyclerView.Adapter<ParticipationAdapter.ParticipationViewHolder>() {
    private lateinit var binding: ParticipationItemLayoutBinding
    var participationItemData = ArrayList<ParticipationData>()
    var participantsUserData = kotlin.collections.ArrayList<User?>()
    private lateinit var context: Context
    private val selectedItemPositions = HashSet<Int>() // 선택된 항목의 position을 저장하는 Set
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
            // 선택된 아이템인지 확인하고 배경 색상 설정
            if (selectedItemPositions.contains(position)) {
                Log.e("setItemSelected", "setItemSelected")
                setItemSelected(binding)
            } else {
                Log.e("setItemUnselected", "setItemUnselected")
                setItemUnselected(binding)
            }

            val gender = if (participantsUserData.find { it?.id == user?.id }?.gender != null) {
                if (participantsUserData.find { it?.id == user?.id }?.gender == true) {
                    "여성"
                } else {
                    "남성"
                }
            } else {
                "설정X"
            }

            val smoke = if (participantsUserData.find { it?.id == user?.id }?.verifySmoker != null) {
                if (participantsUserData.find { it?.id == user?.id }?.verifySmoker == true) {
                    "흡연 O"
                } else {
                    "흡연 X"
                }
            } else {
                "설정X"
            }

            binding.participationFilterTv.text = "${user?.studentId} | $gender $smoke"
            binding.participationContentTv.text = partData.content

            /*if (partData.approvalOrReject == "APPROVAL" ) {
                setItemSelected(binding)
            }*/

            // 조건에 따라 선택 상태 처리 (isDeleteYN == "N" && approvalOrReject == "APPROVAL")
            if (partData.isDeleteYN == "N" && partData.approvalOrReject == "APPROVAL" || partData.isDeleteYN == "N" && partData.approvalOrReject == "FINISH") {
                setItemSelected(binding)
            } else {
                setItemUnselected(binding)
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
                // 선택 상태 토글
                if (selectedItemPositions.contains(position)) {
                    selectedItemPositions.remove(position)
                } else {
                    selectedItemPositions.add(position)
                }

                // 서버에 승인 요청
                fetchItemDetails(partData.participantId) { isSuccess ->
                    if (isSuccess) {
                        // 서버 응답 후 아이템 갱신
                        participationItemData[position].approvalOrReject = "APPROVAL"
                        notifyItemChanged(position)
                    } else {
                        // 에러 처리 (필요시)
                        Log.e("fetchItemDetails", "Failed to approve participation")
                    }
                }

                itemClickListener.onApprovalClick(position, partData.participantId.toString())
            }

            binding.participationRefuse.setOnClickListener {
                removeData(partData.participantId, position)
                itemClickListener.onRefuseClick(position, partData.participantId.toString())
                notifyItemChanged(position)
            }

            binding.participationCancel.setOnClickListener {
                if (isTargetTimePassed(target)) {
                    Log.e("participation", "예정된")
                    Toast.makeText(context, "예정된 시간이 지나 취소하실 수 없습니다", Toast.LENGTH_SHORT).show()
                } else {
                    removeData(partData.participantId, position)
                    itemClickListener.onRefuseClick(position, partData.participantId.toString())
                    notifyItemChanged(position)
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

        //당일 취소 불가능, 마감이면 취소 불가능,
        RetrofitServerConnect.create(context).deleteParticipants(participantsId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("PART Remove Success ", response.code().toString())
                    participationItemData[position].approvalOrReject = "REJECT"
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

    private fun fetchItemDetails(participantsId: Int, callback: (Boolean) -> Unit) {
        RetrofitServerConnect.create(context).patchParticipantsApproval(participantsId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("fetchItemDetails", response.code().toString())
                    callback(true)  // 성공 시 true 반환
                } else {
                    Log.e("fetchItemDetails", response.errorBody()?.string()!!)
                    callback(false)  // 실패 시 false 반환
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d("ERROR", t.toString())
                callback(false)  // 실패 시 false 반환
            }
        })
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
