package com.gdsc.mio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdsc.mio.model.MyAccountReviewData

class ReviewWrittenViewModel : ViewModel() {
    private val _reviews = MutableLiveData<List<MyAccountReviewData>>()
    val reviews: LiveData<List<MyAccountReviewData>> get() = _reviews

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // 데이터를 설정하는 메서드 추가
    fun setReviews(reviews: List<MyAccountReviewData>) {
        _reviews.value = reviews
    }

    // 로딩 상태를 설정하는 메서드 추가
    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    // 에러 메시지를 설정하는 메서드 추가
    fun setError(errorMessage: String) {
        _error.value = errorMessage
    }
}