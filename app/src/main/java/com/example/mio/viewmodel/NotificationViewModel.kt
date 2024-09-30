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
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableLiveData<List<AddAlarmResponseData>>()
    val notifications: LiveData<List<AddAlarmResponseData>> get() = _notifications

    private val _notificationsPostData = MutableLiveData<List<PostData>>()
    val notificationsPostData: LiveData<List<PostData>> get() = _notificationsPostData

    private val _notificationsParticipationData = MutableLiveData<List<Pair<Int, ArrayList<ParticipationData>?>>>()
    val notificationsParticipationData: LiveData<List<Pair<Int, ArrayList<ParticipationData>?>>> get() = _notificationsParticipationData
    /*private var notificationPostAllData : ArrayList<PostData?> = ArrayList()
    private var notificationPostParticipationAllData : ArrayList<Pair<Int, ArrayList<ParticipationData>?>> = ArrayList()*/


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

                    // Log the response data
                    Log.d("fetchNotificationData", "Response Code: ${response.code()}")
                    Log.d("fetchNotificationData", "Response Body: ${responseData?.toString()}")
                    Log.d("fetchNotificationData", "${_notifications.value}")
                } else {
                    // Log the error body
                    Log.e("fetchIsBeforeDeadLine", "Response Error Code: ${response.code()}")
                    Log.e("fetchNotificationData", "Response Error Body: ${response.errorBody()?.string()!!}")
                    _notifications.value = emptyList()
                }
            } catch (e: Exception) {
                // Log the exception
                Log.e("fetchNotificationData", "Exception: ${e.message}")
                _notifications.value = emptyList()
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