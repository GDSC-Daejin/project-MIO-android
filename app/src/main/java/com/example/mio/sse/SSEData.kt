package com.example.mio.sse

import com.google.gson.annotations.SerializedName

data class SSEData(
    @SerializedName("timeout")
    val timeOut : Long
)
