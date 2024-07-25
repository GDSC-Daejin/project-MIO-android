package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class SSEData(
    @SerializedName("timeout")
    val timeOut : Long
)
