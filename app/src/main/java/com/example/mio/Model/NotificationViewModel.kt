package com.example.mio.Model


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableLiveData<List<AddAlarmResponseData>>()
    val notifications: LiveData<List<AddAlarmResponseData>> get() = _notifications

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

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