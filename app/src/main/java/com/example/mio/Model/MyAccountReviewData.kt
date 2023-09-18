package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class MyAccountReviewData(
    @SerializedName("id")
    var id : Int,
    @SerializedName("manner")
    var manner : String,
    @SerializedName("content")
    var content : String,
    @SerializedName("userId")
    var userId : Int,
    @SerializedName("createDate")
    var createDate : String
)
