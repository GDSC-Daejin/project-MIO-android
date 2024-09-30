package com.example.mio.model

import com.google.gson.annotations.SerializedName

data class AddAlarmResponseData(
    @SerializedName("id")
    val id : Int,
    @SerializedName("createDate")
    val createDate : String,
    @SerializedName("content")
    val content : String,
    @SerializedName("postId")
    val postId : Int,
    @SerializedName("userId")
    val userId : Int,
)

/*
data class AlarmPost(
    @SerializedName("id")
    var id : Int,
    @SerializedName("title")
    var title : String,
    @SerializedName("content")
    var content : String,
    @SerializedName("createDate")
    var createDate : String,
    @SerializedName("targetDate")
    var targetDate : String,
    @SerializedName("targetTime")
    var targetTime : String,
    @SerializedName("verifyGoReturn")
    var verifyGoReturn : Boolean,
    @SerializedName("numberOfPassengers")
    var numberOfPassengers : Int,
    @SerializedName("viewCount")
    var viewCount : Int,
    @SerializedName("verifyFinish")
    var verifyFinish : Boolean,
    @SerializedName("latitude")
    var latitude : Double,
    @SerializedName("longitude")
    var longitude : Double,
    @SerializedName("bookMarkCount")
    var bookMarkCount : Int,
    @SerializedName("participantsCount")
    var participantsCount : Int,
    @SerializedName("location")
    var location : String,
    @SerializedName("cost")
    var cost : Int,
    @SerializedName("category")
    var category : Category,
    @SerializedName("commentList")
    var commentList : ArrayList<AlarmCommentList>,
    @SerializedName("user")
    var user : User,
    @SerializedName("participants")
    var participants : ArrayList<AlarmParticipants>,
) : java.io.Serializable {

}

data class AlarmParticipants(
    @SerializedName("id")
    var id : Int,
    @SerializedName("post")
    var post : String,
    @SerializedName("user")
    var user : AddUser,
    @SerializedName("approvalOrReject")
    var approvalOrReject : ApprovalStatus,
    // 스웨거에서 응답으로 받은 문자열 값  val responseValue = "APPROVAL" // 예시
    // Kotlin Enum으로 변환 val approvalStatus = ApprovalStatus.valueOf(responseValue)
    @SerializedName("verifyFinish")
    var verifyFinish : Boolean,
    @SerializedName("driverMannerFinish")
    var driverMannerFinish : Boolean,
    @SerializedName("passengerMannerFinish")
    var passengerMannerFinish : Boolean,
    @SerializedName("content")
    var content : String,
)

data class AlarmUserEntity(
    @SerializedName("id")
    var id : Int,
    @SerializedName("email")
    var email : String,
    @SerializedName("studentId")
    var studentId : String,
    @SerializedName("profileImageUrl")
    var profileImageUrl : String,
    @SerializedName("name")
    var name : String,
    @SerializedName("accountNumber")
    var accountNumber : String,
    @SerializedName("gender")
    var gender : Boolean,
    @SerializedName("verifySmoker")
    var verifySmoker : Boolean,
    @SerializedName("roleType")
    var roleType : String,
    @SerializedName("status")
    var status : String,
    @SerializedName("mannerCount")
    var mannerCount : Int,
    @SerializedName("grade")
    var grade : String,
)

data class AlarmCommentList(
    @SerializedName("commentId")
    var commentId : Int,
    @SerializedName("content")
    var content : String,
    @SerializedName("createDate")
    var createDate : String,
    @SerializedName("post")
    var post : String,
    @SerializedName("user")
    var user : User,
    @SerializedName("parentComment")
    var parentComment : String,
    @SerializedName("childComments")
    var childComments : ArrayList<String>,
)*/
