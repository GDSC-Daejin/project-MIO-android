package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class AddAlarmData(
    @SerializedName("content")
    val content : String,
    @SerializedName("postId")
    val postId : Int,
    @SerializedName("userId")
    val userId : Int // 알람을 받아야 될 사람
)
