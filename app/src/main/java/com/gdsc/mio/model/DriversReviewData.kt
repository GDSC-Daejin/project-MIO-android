package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class DriversReviewData(
    @SerializedName("manner")
    var manner : String,
    @SerializedName("content")
    var content : String
)
