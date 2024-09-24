package com.example.mio.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mio.model.AddAlarmResponseData
import com.example.mio.model.Content
import com.example.mio.model.MyAccountReviewData
import com.example.mio.model.PostData

class CurrentDataViewModel : ViewModel() {
    private val _currentCarpoolLiveData = MutableLiveData<List<Content>>()
    val currentCarpoolLiveData: LiveData<List<Content>> get()= _currentCarpoolLiveData

    private val _currentTaxiLiveData = MutableLiveData<List<PostData>>()
    val currentTaxiLiveData: LiveData<List<PostData>> get()= _currentTaxiLiveData
   /* private val _reviews = MutableLiveData<List<MyAccountReviewData>>()
    val reviews: LiveData<List<MyAccountReviewData>> get() = _reviews*/


    fun setCurrentData(newData: List<Content>) {
        Log.e("setCurrentData", newData.toString())
        _currentCarpoolLiveData.value = newData
    }

    /*fun setReviews(reviews: List<MyAccountReviewData>) {
        _reviews.value = reviews.toList()
    }*/
    fun setTaxiCurrentData(newData: List<PostData>) {
        Log.e("setTaxiCurrentData", newData.toString())
        _currentTaxiLiveData.value = newData
    }
}