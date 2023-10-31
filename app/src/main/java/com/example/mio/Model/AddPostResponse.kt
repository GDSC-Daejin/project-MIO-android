package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class AddPostResponse(
    @SerializedName("postId")
    var postId : Int,
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
    @SerializedName("category")
    var category : AddCategory,
    @SerializedName("verifyGoReturn")
    var verifyGoReturn : Boolean,
    @SerializedName("numberOfPassengers")
    var numberOfPassengers : Int,
    @SerializedName("user")
    var user : AddUser,
    @SerializedName("viewCount")
    var viewCount : Int,
    @SerializedName("verifyFinish")
    var verifyFinish : Boolean,
    @SerializedName("participants")
    var participants : ArrayList<AddParticipants>,
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
    var cost : Int
)

data class AddCategory(
    @SerializedName("categoryId")
    var categoryId : Int,
    @SerializedName("categoryName")
    var categoryName : String,
)

data class AddUser(
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

data class AddParticipants(
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

