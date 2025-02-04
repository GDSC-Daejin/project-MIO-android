package com.gdsc.mio.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdsc.mio.model.LocationReadAllResponse

class FragSharedViewModel : ViewModel() {
    val selectedLocation = MutableLiveData<LocationReadAllResponse?>()
    val selectedAccountLocation = MutableLiveData<LocationReadAllResponse?>()
}