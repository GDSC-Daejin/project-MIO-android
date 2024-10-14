package com.example.mio.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.mio.RetrofitServerConnect
import com.example.mio.model.PostData
import com.example.mio.model.PostReadAllResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MoreTaxiViewModel : ViewModel() {
    private val _moreTaxiPostData = MutableStateFlow<List<PostData?>>(emptyList())
    val moreTaxiPostData: StateFlow<List<PostData?>> get() = _moreTaxiPostData

    private val _isLoading = MutableStateFlow(false) // 로딩 상태를 추적
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var currentPage = 0
    private var totalPages = 1

    // Carpool 데이터를 설정하는 메서드
    fun setTaxiPostData(newData: List<PostData?>) {
        // 데이터 중복 제거 후 새로운 리스트로 설정
        _moreTaxiPostData.value = newData.distinctBy { it?.postID }.toList()
    }

    fun setTotalPages(pages : Int) {
        totalPages = pages
    }

    fun setCurrentPages(pages : Int) {
        currentPage = pages
    }

    fun setLoading(isLoading : Boolean) {
        _isLoading.value = isLoading
    }

    // 더 많은 Carpool 데이터를 추가하는 메서드
    fun setMoreTaxiPostData(newData: List<PostData?>) {
        // 기존 데이터에 새로운 데이터를 추가하고 중복 제거
        _moreTaxiPostData.value = (_moreTaxiPostData.value + newData).distinctBy { it?.postID }.toList()
    }

    // 정렬 방식에 따른 데이터 정렬 메서드
    fun sortTaxiData(sortType: String) {
        val sortedData = when (sortType) {
            "최신 순" -> _moreTaxiPostData.value
                .filterNotNull()
                .sortedByDescending { it.postCreateDate }
            "마감 임박 순" -> {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                _moreTaxiPostData.value
                    .filterNotNull()
                    .sortedWith { t1, t2 ->
                        val dateComparison = LocalDate.parse(t1.postTargetDate, dateFormatter)
                            .compareTo(LocalDate.parse(t2.postTargetDate, dateFormatter))

                        if (dateComparison == 0) {
                            LocalTime.parse(t1.postTargetTime, timeFormatter)
                                .compareTo(LocalTime.parse(t2.postTargetTime, timeFormatter))
                        } else {
                            dateComparison
                        }
                    }
            }
            "낮은 가격 순" -> _moreTaxiPostData.value
                .filterNotNull()
                .sortedBy { it.postCost }
            else -> _moreTaxiPostData.value
        }

        _moreTaxiPostData.value = sortedData.toList()
    }

    // 무한 스크롤을 위한 데이터 요청
    //날짜, 탑승 수, 담배, 성별, 학교 순서 등,  //최신순, 가까운 순 등
    fun getMoreTaxiData(context: Context, onComplete: () -> Unit) {
        if (_isLoading.value || currentPage >= totalPages) return

        _isLoading.value = true
        // 로딩 아이템 추가
        val currentData = _moreTaxiPostData.value.toMutableList().apply {
            add(null) // 로딩 아이템으로 null 추가
        }
        _moreTaxiPostData.value = currentData

        RetrofitServerConnect.create(context)
            .getCategoryPostData(2,"createDate,desc", currentPage, 5)
            .enqueue(object : Callback<PostReadAllResponse> {
                override fun onResponse(call: Call<PostReadAllResponse>, response: Response<PostReadAllResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { responseData ->
                            totalPages = responseData.totalPages

                            val newItems = responseData.content.filter { item ->
                                item.isDeleteYN == "N" && item.postType == "BEFORE_DEADLINE"
                            }.map { item ->
                                PostData(
                                    item.user.studentId,
                                    item.postId,
                                    item.title,
                                    item.content,
                                    item.createDate,
                                    item.targetDate,
                                    item.targetTime,
                                    item.category.categoryName,
                                    item.location,
                                    item.participantsCount,
                                    item.numberOfPassengers,
                                    item.cost,
                                    item.verifyGoReturn,
                                    item.user,
                                    item.latitude,
                                    item.longitude
                                )
                            }

                            // 로딩 아이템 제거 및 새로운 데이터 추가
                            val updatedData = _moreTaxiPostData.value.toMutableList().apply {
                                removeAll { it == null } // null로 된 로딩 아이템 삭제
                                addAll(newItems)
                            }
                            _moreTaxiPostData.value = updatedData
                            currentPage += 1
                        }
                    }
                    _isLoading.value = false
                    onComplete() // 데이터 로드가 완료되면 필터 적용 콜백 실행
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    _isLoading.value = false
                    onComplete() // 실패했을 때도 필터 적용 콜백 실행
                }
            })
    }
}