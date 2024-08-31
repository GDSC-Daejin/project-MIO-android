package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class CommentResponseData(
    @SerializedName("commentId")
    var commentId : Int,
    @SerializedName("content")
    var content : String,
    @SerializedName("createDate")
    var createDate : String,
    @SerializedName("postId")
    var postId : Int,
    @SerializedName("user")
    var user : CommentUser,
    @SerializedName("childComments")
    var childComments : List<CommentData>?
)

data class CommentData(
    @SerializedName("commentId")
    var commentId : Int,
    @SerializedName("content")
    var content : String,
    @SerializedName("createDate")
    var createDate : String,
    @SerializedName("postId")
    var postId : Int,
    @SerializedName("user")
    var user : CommentUser,
    @SerializedName("childComments")
    var childComments : List<CommentData>?,


    //var childAllData : ArrayList<CommentData>
)

data class CommentUser(
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