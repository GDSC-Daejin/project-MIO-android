package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class LoginResponsesData(
    @SerializedName("grantType")
    val grantType : String,

    @SerializedName("accessToken")
    val accessToken : String,

    @SerializedName("refreshToken")
    val refreshToken : String,

    @SerializedName("accessTokenExpiresIn")
    val accessTokenExpiresIn : Long
)
