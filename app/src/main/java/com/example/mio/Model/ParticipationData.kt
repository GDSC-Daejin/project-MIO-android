package com.example.mio.Model

import com.google.gson.annotations.SerializedName

//참여자 데이터
data class ParticipationData(
    @SerializedName("postId")
    var postId : Int,
    @SerializedName("userId")
    var userId : Int,
    @SerializedName("postUserId")
    var postUserId : Int,
    @SerializedName("content")
    var content : String,
    @SerializedName("approvalOrReject")
    var approvalOrReject : String
) : java.io.Serializable {

}
