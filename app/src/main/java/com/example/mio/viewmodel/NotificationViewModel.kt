package com.example.mio.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mio.RetrofitServerConnect
import com.example.mio.model.AddAlarmResponseData
import com.example.mio.model.ParticipationData
import com.example.mio.model.PostData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableLiveData<List<AddAlarmResponseData>>()
    val notifications: LiveData<List<AddAlarmResponseData>> get() = _notifications

    private val _notificationsPostData = MutableStateFlow<List<PostData>>(emptyList())
    val notificationsPostData: StateFlow<List<PostData>> get() = _notificationsPostData

    private val _notificationsParticipationData = MutableStateFlow<List<Pair<Int, ArrayList<ParticipationData>?>>>(
        emptyList()
    )
    val notificationsParticipationData: StateFlow<List<Pair<Int, ArrayList<ParticipationData>?>>> get() = _notificationsParticipationData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchNotificationData(context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitServerConnect.create(context)
                    .getMyAlarm()
                    .awaitResponse()

                if (response.isSuccessful) {
                    val responseData = response.body()
                    _notifications.value = responseData ?: emptyList()

                    fetchNotificationsPostData(context, _notifications.value)
                } else {
                    // Log the error body
                    _notifications.value = emptyList()
                }
            } catch (e: Exception) {
                // Log the exception
                _notifications.value = emptyList()
            }
        }
    }


    private fun fetchNotificationsPostData(context: Context, alarmList: List<AddAlarmResponseData>?) {
        viewModelScope.launch {
            try {
                alarmList?.let { list ->
                    for (i in list.indices) {
                        val response = RetrofitServerConnect.create(context)
                            .getPostIdDetailSearch(list[i].postId)
                            .awaitResponse()

                        if (response.isSuccessful) {
                            val responseData = response.body()
                            if (responseData != null && responseData.isDeleteYN == "N") {
                                val postData = PostData(
                                    responseData.user.studentId,
                                    responseData.postId,
                                    responseData.title,
                                    responseData.content,
                                    responseData.createDate,
                                    responseData.targetDate,
                                    responseData.targetTime,
                                    responseData.category.categoryName,
                                    responseData.location,
                                    responseData.participantsCount,
                                    responseData.numberOfPassengers,
                                    responseData.cost,
                                    responseData.verifyGoReturn,
                                    responseData.user,
                                    responseData.latitude,
                                    responseData.longitude
                                )

                                // 새로운 데이터를 기존 리스트에 추가하고 StateFlow를 업데이트
                                _notificationsPostData.value = _notificationsPostData.value + postData

                                if (responseData.participants != null) {
                                    _notificationsParticipationData.value = _notificationsParticipationData.value + Pair(responseData.postId, responseData.participants)
                                }
                            }
                        } else {
                            Log.e("fetchNotificationData", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("fetchNotificationData", "Exception: ${e.message}")
            }
        }
    }

    // 데이터를 설정하는 메서드 추가
    fun setNotifications(notifications: List<AddAlarmResponseData>) {
        _notifications.value = notifications.toList()
    }

    // 로딩 상태를 설정하는 메서드 추가
    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    // 에러 메시지를 설정하는 메서드 추가
    fun setError(errorMessage: String) {
        _error.value = errorMessage
    }

    fun deleteNotification(itemId: Int) {
        _notifications.value = _notifications.value?.filter { it.id != itemId }
    }
}