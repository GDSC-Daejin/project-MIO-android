package com.example.mio.viewmodel

import android.app.LauncherActivity
import android.content.Context
import android.util.Log
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

class MoreCarpoolViewModel : ViewModel() {
    private val _moreCarpoolPostData = MutableStateFlow<List<PostData?>>(emptyList())
    val moreCarpoolPostData: StateFlow<List<PostData?>> get() = _moreCarpoolPostData

    private val _isLoading = MutableStateFlow(false) // 로딩 상태를 추적
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var currentPage = 0
    private var totalPages = 0

    // Carpool 데이터를 설정하는 메서드
    fun setCarpoolPostData(newData: List<PostData?>) {
        // 데이터 중복 제거 후 새로운 리스트로 설정
        _moreCarpoolPostData.value = newData.distinctBy { it?.postID }.toList()
    }

    fun setTotalPages(pages : Int) {
        totalPages = pages
    }

    // 더 많은 Carpool 데이터를 추가하는 메서드
    fun setMoreCarpoolPostData(newData: List<PostData?>) {
        // 기존 데이터에 새로운 데이터를 추가하고 중복 제거
        _moreCarpoolPostData.value = (_moreCarpoolPostData.value + newData).distinctBy { it?.postID }.toList()
    }

    // 정렬 방식에 따른 데이터 정렬 메서드
    fun sortCarpoolData(sortType: String) {
        Log.e("sortCarpoolData", sortType)

        val sortedData = when (sortType) {
            "최신 순" -> _moreCarpoolPostData.value
                .filterNotNull()
                .sortedByDescending { it.postCreateDate }
            "마감 임박 순" -> {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                _moreCarpoolPostData.value
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
            "낮은 가격 순" -> _moreCarpoolPostData.value
                .filterNotNull()
                .sortedBy { it.postCost }
            else -> _moreCarpoolPostData.value
        }

        // 정렬된 데이터를 StateFlow에 설정 (toList()로 변경 감지 유도)
        _moreCarpoolPostData.value = sortedData.toList()

        Log.e("sortCarpoolData", "${_moreCarpoolPostData.value}")
    }

    // 무한 스크롤을 위한 데이터 요청
    fun getMoreCarpoolData(context : Context) {
        if (_isLoading.value || currentPage >= totalPages) return

        _isLoading.value = true

        // 로딩 아이템 추가
        val currentData = _moreCarpoolPostData.value.toMutableList().apply {
            add(null) // 로딩 아이템으로 null 추가
        }
        _moreCarpoolPostData.value = currentData

        RetrofitServerConnect.create(context)
            .getCategoryPostData(1, "createDate,desc", currentPage+1, 5)
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
                            val updatedData = _moreCarpoolPostData.value.toMutableList().apply {
                                removeAll { it == null } // null로 된 로딩 아이템 삭제
                                addAll(newItems)
                            }
                            Log.e("updatedData", "$updatedData")
                            _moreCarpoolPostData.value = updatedData
                            Log.e("updatedData", "${_moreCarpoolPostData.value}")
                            currentPage += 1
                        }
                    } else {
                        Log.d("Error", "Response code: ${response.code()}")
                    }
                    _isLoading.value = false
                }

                override fun onFailure(call: Call<PostReadAllResponse>, t: Throwable) {
                    Log.d("Error", "Failure: ${t.message}")
                    _isLoading.value = false
                }
            })
    }
}