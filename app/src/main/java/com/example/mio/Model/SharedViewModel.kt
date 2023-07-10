package com.example.mio.Model

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel


class SharedViewModel : ViewModel() {
    private val _checkCurrentPage : MutableLiveData<Int> = MutableLiveData()
    val checkCurrentPage : LiveData<Int> = _checkCurrentPage
    fun postCheckPage(page : Int) {
        _checkCurrentPage.value = page
    }

    private val _checkComplete : MutableLiveData<Boolean> = MutableLiveData()
    val checkComplete : LiveData<Boolean> = _checkComplete
    fun postCheckComplete(complete : Boolean) {
        _checkComplete.value = complete
    }


    private val _allCheck : MutableLiveData<RequirementData> = MutableLiveData()
    val allCheck : LiveData<RequirementData> = _allCheck
    fun postCheckValue(check : RequirementData) {
        _allCheck.value = check
    }
    //추가된 targetDate data 받기
    private val liveData = MutableLiveData<ArrayList<String>>()
    var notificationLiveData = MutableLiveData<ArrayList<NotificationData>>()
    /*private val _users: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>().also {
            loadUsers()
        }
    }
    val users: LiveData<List<User>> = _users

    fun getUsers(userLise: List<User>>) {
        _users.value = userLise
    }

    // Model쪽에 List<User> 값을 요청하는 메소드
    fun loadUsers() {
    	// 유저를 불러오는 기능...
    }

    */
    //선택된 캘린더 데이터 받기
    var calendarLiveData = MutableLiveData<HashMap<String, ArrayList<PostData>>>()
    fun getLiveData(): LiveData<ArrayList<String>> {
        return liveData
    }

    //livedata로 사용할 데이터를 저장하는 함수수
   fun setLiveData(arr: ArrayList<String>) {
        liveData.value = arr
    }

    fun getCalendarLiveData(): LiveData<HashMap<String, ArrayList<PostData>>> {
        return calendarLiveData
    }

    fun setCalendarLiveData(key : String, arr: HashMap<String, ArrayList<PostData>>) {
        if (key == "add") {
            calendarLiveData.value = arr
        }
    }

    fun getNotificationLiveData(): LiveData<ArrayList<NotificationData>> {
        return notificationLiveData
    }

    fun setNotificationLiveData(key : String, arr: ArrayList<NotificationData>) {
        if (key == "add") {
            notificationLiveData.value = arr
        }
    }

    fun removeCategoryLiveData() {
        calendarLiveData.value!!.clear()
    }
}