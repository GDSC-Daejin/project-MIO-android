package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class BookMarkResponseData(
    @SerializedName("id")
    var id : Int,
    @SerializedName("userId")
    var userId : Int,
    @SerializedName("postId")
    var postId : Int,
    @SerializedName("status")
    var status : Boolean
)
