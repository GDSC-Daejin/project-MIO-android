package com.example.mio.Model

import com.google.gson.annotations.SerializedName

//참여자 데이터
data class ParticipationData(
    @SerializedName("postId")
    var postId : Int,
    @SerializedName("userId")
    var userId : Int,
    @SerializedName("content")
    var content : String
) : java.io.Serializable {

}
