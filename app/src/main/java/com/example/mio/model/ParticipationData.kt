package com.example.mio.model

import com.google.gson.annotations.SerializedName

//참여자 데이터
data class ParticipationData(
    @SerializedName("participantId")
    var participantId : Int,
    @SerializedName("postId")
    var postId : Int,
    @SerializedName("userId")
    var userId : Int,
    @SerializedName("postUserId")
    var postUserId : Int,
    @SerializedName("content")
    var content : String,
    @SerializedName("approvalOrReject")
    var approvalOrReject : String,
    @SerializedName("driverMannerFinish")
    var driverMannerFinish : Boolean,
    @SerializedName("passengerMannerFinish")
    var passengerMannerFinish : Boolean,
    @SerializedName("verifyFinish")
    var verifyFinish : Boolean,
    @SerializedName("isDeleteYN")
    var isDeleteYN : String?,

) : java.io.Serializable {

}
