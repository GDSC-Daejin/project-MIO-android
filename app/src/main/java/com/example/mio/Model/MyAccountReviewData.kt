package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class MyAccountReviewData(
    @SerializedName("id")
    var id : Int,
    @SerializedName("manner")
    var manner : String,
    @SerializedName("content")
    var content : String,
    @SerializedName("getUserId")
    var getUserId : Int,
    @SerializedName("postUserId")
    var postUserId : Int,
    @SerializedName("createDate")
    var createDate : String
)