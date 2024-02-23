package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("token")
    val token : String,
    @SerializedName("url")
    val url : String,
    @SerializedName("method")
    val method : String
)