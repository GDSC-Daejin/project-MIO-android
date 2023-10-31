package com.example.mio.Model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragSharedViewModel : ViewModel() {
    val selectedLocation = MutableLiveData<LocationReadAllResponse?>()
}