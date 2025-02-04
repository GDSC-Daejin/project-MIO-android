package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class PassengersReviewData(
    @SerializedName("manner")
    var manner : String?,
    @SerializedName("content")
    var content : String?,
    @SerializedName("postId")
    var postId : Int?
)
