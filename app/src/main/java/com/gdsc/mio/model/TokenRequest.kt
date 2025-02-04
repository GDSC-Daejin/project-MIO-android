package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("token")
    val token : String?,
)