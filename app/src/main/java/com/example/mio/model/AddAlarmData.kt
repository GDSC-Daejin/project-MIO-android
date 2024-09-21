package com.example.mio.model

import com.google.gson.annotations.SerializedName

data class AddAlarmData(
    @SerializedName("content")
    var content : String,
    @SerializedName("postId")
    var postId : Int,
    @SerializedName("userId")
    var userId : Int // 알람을 받아야 될 사람
)
