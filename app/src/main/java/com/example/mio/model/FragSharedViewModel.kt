package com.example.mio.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragSharedViewModel : ViewModel() {
    val selectedLocation = MutableLiveData<LocationReadAllResponse?>()
    val selectedAccountLocation = MutableLiveData<LocationReadAllResponse?>()
}