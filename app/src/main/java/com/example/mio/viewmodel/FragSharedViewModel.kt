package com.example.mio.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mio.model.LocationReadAllResponse

class FragSharedViewModel : ViewModel() {
    val selectedLocation = MutableLiveData<LocationReadAllResponse?>()
    val selectedAccountLocation = MutableLiveData<LocationReadAllResponse?>()
}