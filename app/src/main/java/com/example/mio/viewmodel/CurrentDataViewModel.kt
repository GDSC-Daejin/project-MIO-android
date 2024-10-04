package com.example.mio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mio.model.PostData

class CurrentDataViewModel : ViewModel() {
    private val _currentCarpoolLiveData = MutableLiveData<List<PostData?>>()
    val currentCarpoolLiveData: LiveData<List<PostData?>> get()= _currentCarpoolLiveData

    private val _currentTaxiLiveData = MutableLiveData<List<PostData>>()
    val currentTaxiLiveData: LiveData<List<PostData>> get()= _currentTaxiLiveData
   /* private val _reviews = MutableLiveData<List<MyAccountReviewData>>()
    val reviews: LiveData<List<MyAccountReviewData>> get() = _reviews*/


    fun setCurrentData(newData: List<PostData?>) {
        _currentCarpoolLiveData.value = newData
    }

    /*fun setReviews(reviews: List<MyAccountReviewData>) {
        _reviews.value = reviews.toList()
    }*/
    fun setTaxiCurrentData(newData: List<PostData>) {
        _currentTaxiLiveData.value = newData
    }
}