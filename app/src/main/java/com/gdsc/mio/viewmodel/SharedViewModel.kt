package com.gdsc.mio.viewmodel

import android.content.Context
//import android.util.Log
import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc.mio.RetrofitServerConnect
import com.gdsc.mio.model.PostData
import com.gdsc.mio.model.RequirementData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.*


class SharedViewModel : ViewModel() {

    private val _isNotDeadLine = MutableStateFlow(false)
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
                } else {
                    _isNotDeadLine.value = false
                }
            } catch (e: Exception) {
                // Log the exception
                _isNotDeadLine.value = false
            }
        }
    }



    private val _notificationType : MutableLiveData<String> = MutableLiveData()
    val notificationType : LiveData<String> = _notificationType
    fun setNotificationType (type :String) {
        _notificationType.value = type
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



    //edit 페이지체크
    private val _checkCurrentPage : MutableLiveData<Int> = MutableLiveData()
    val checkCurrentPage : LiveData<Int> = _checkCurrentPage
    fun postCheckPage(page : Int) {
        _checkCurrentPage.value = page
        //Log.e("postPage", page.toString())
        updateCheckComplete()
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
       /* Log.e("postCheckValue", check.isFirstVF.isTitle.toString())
        Log.e("postCheckValue", check.isFirstVF.isCalendar.toString())
        Log.e("postCheckValue", check.isFirstVF.isTime.toString())
        Log.e("postCheckValue", check.isFirstVF.isParticipants.toString())
        Log.e("postCheckValue", check.isFirstVF.isFirst.toString())*/
        updateCheckComplete()
    }
    private fun updateCheckComplete() {
        val checkData = _allCheck.value ?: return
        val isComplete = when (_checkCurrentPage.value) {
            1 -> checkData.isFirstVF.let { it.isTitle && it.isCalendar && it.isParticipants && it.isTime && it.isFirst }
            2 -> checkData.isSecondVF.let { it.isPlaceName && it.isPlaceRode && it.isSecond }
            3 -> checkData.isThirdVF.let { it.isAmount && (it.isGSchool || it.isASchool) && (it.isMGender || it.isWGender) && (it.isSmoke || it.isNSmoke) && it.isThird }
            4 -> checkData.isFourthVF.let { it.isContent && it.isFourth }
            else -> false
        }
        //Log.e("_checkCurrentPage", _checkCurrentPage.value.toString())
        _checkComplete.value = isComplete
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

    //선택된 캘린더 데이터 받기
    private var calendarLiveData = MutableLiveData<HashMap<String, ArrayList<PostData>>>()
    fun getLiveData(): LiveData<ArrayList<String>> {
        return liveData
    }

    fun getCalendarLiveData(): LiveData<HashMap<String, ArrayList<PostData>>> {
        return calendarLiveData
    }
}