package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class SendCommentData(
    @SerializedName("content")
    var content : String,
    @SerializedName("createDate")
    var createDate : String,
    @SerializedName("postId")
    var postId : Int
)
