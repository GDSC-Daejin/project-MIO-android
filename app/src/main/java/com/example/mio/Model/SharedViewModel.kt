package com.example.mio.Model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mio.RetrofitServerConnect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.*


class SharedViewModel : ViewModel() {

    private val _isNotDeadLine = MutableStateFlow<Boolean>(false)
    val isNotDeadLine: StateFlow<Boolean> get() = _isNotDeadLine

    fun fetchIsBeforeDeadLine(context: Context, postId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitServerConnect.create(context)
                    .getPostIdDetailSearch(postId)
                    .awaitResponse()

                if (response.isSuccessful) {
                    val responseData = response.body()
                    _isNotDeadLine.value = responseData?.postType == "BEFORE_DEADLINE" && responseData.isDeleteYN != "Y"

                    // Log the response data
                    Log.d("fetchIsBeforeDeadLine", "Response Code: ${response.code()}")
                    Log.d("fetchIsBeforeDeadLine", "Response Body: ${responseData?.toString()}")
                    Log.d("fetchIsBeforeDeadLine", "${_isNotDeadLine.value}")
                } else {
                    // Log the error body
                    Log.e("fetchIsBeforeDeadLine", "Response Error Code: ${response.code()}")
                    Log.e("fetchIsBeforeDeadLine", "Response Error Body: ${response.errorBody()?.string()!!}")
                    _isNotDeadLine.value = false
                }
            } catch (e: Exception) {
                // Log the exception
                Log.e("fetchIsBeforeDeadLine", "Exception: ${e.message}")
                _isNotDeadLine.value = false
            }
        }
    }






    //최신순 등
    private val _checkSearchFilter : MutableLiveData<String> = MutableLiveData()
    val checkSearchFilter : LiveData<String> = _checkSearchFilter
    fun postCheckSearchFilter(searchWord : String) {
        _checkSearchFilter.value = searchWord
    }

    //성별, 흡연, 등
    private val _checkFilter : MutableLiveData<String> = MutableLiveData()
    val checkFilter : LiveData<String> = _checkFilter
    fun postCheckFilter(searchWord : String) {
        _checkFilter.value = searchWord
    }

    /*private val _checkSchoolFilter : MutableLiveData<String> = MutableLiveData()
    val checkSchoolFilter : LiveData<String> = _checkSchoolFilter
    fun postCheckSchoolFilter(searchWord : String) {
        _checkSchoolFilter.value = searchWord
    }

    private val _checkSmokeFilter : MutableLiveData<String> = MutableLiveData()
    val checkSmokeFilter : LiveData<String> = _checkSmokeFilter
    fun postCheckSmokeFilter(searchWord : String) {
        _checkSmokeFilter.value = searchWord
    }

    private val _checkGenderFilter : MutableLiveData<String> = MutableLiveData()
    val checkGenderFilter : LiveData<String> = _checkGenderFilter
    fun postCheckGenderFilter(searchWord : String) {
        _checkGenderFilter.value = searchWord
    }*/



    //edit 페이지체크
    private val _checkCurrentPage : MutableLiveData<Int> = MutableLiveData()
    val checkCurrentPage : LiveData<Int> = _checkCurrentPage
    fun postCheckPage(page : Int) {
        _checkCurrentPage.value = page
    }

    //모든 페이지 내용 작성 만족 시 다음 페이지 버튼 활성화
    private val _checkComplete : MutableLiveData<Boolean> = MutableLiveData()
    val checkComplete : LiveData<Boolean> = _checkComplete
    fun postCheckComplete(complete : Boolean) {
        _checkComplete.value = complete
    }

    //모든 조건 체크
    private val _allCheck : MutableLiveData<RequirementData> = MutableLiveData()
    val allCheck : LiveData<RequirementData> = _allCheck
    fun postCheckValue(check : RequirementData) {
        _allCheck.value = check
    }

    //댓글의 타입 확인 true = 대댓글 쓰기 , false = 댓글 쓰기
    private val _isReply : MutableLiveData<Boolean> = MutableLiveData()
    val isReply : LiveData<Boolean> = _isReply
    fun postReply(reply : Boolean) {
        _isReply.value = reply
    }

    //등하교
    private val _isGSchool : MutableLiveData<Boolean> = MutableLiveData()
    val isGSchool : LiveData<Boolean> = _isGSchool
    fun postGSchool(GSchool : Boolean) {
        _isGSchool.value = GSchool
    }

    //성별
    private val _isGender : MutableLiveData<Boolean> = MutableLiveData()
    val isGender : LiveData<Boolean> = _isGender
    fun postGender(Gender : Boolean) {
        _isGender.value = Gender
    }

    //흡연여부
    private val _isSmoker : MutableLiveData<Boolean> = MutableLiveData()
    val isSmoker : LiveData<Boolean> = _isSmoker
    fun postSmoker(Smoker : Boolean) {
        _isSmoker.value = Smoker
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