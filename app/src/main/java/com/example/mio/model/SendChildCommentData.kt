package com.example.mio.model

import com.google.gson.annotations.SerializedName

data class SendChildCommentData(
    @SerializedName("content")
    var content : String,
    @SerializedName("createDate")
    var createDate : String,
    @SerializedName("postId")
    var postId : Int,
    var parentId : Int
)
