package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class BookMarkResponseData(
    @SerializedName("id")
    var id : Int,
    @SerializedName("user")
    var user : User,
    @SerializedName("post")
    var post : Content,
    @SerializedName("status")
    var status : Boolean
)
