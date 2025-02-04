package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class CommentResponseData(
    @SerializedName("commentId")
    val commentId : Int,
    @SerializedName("content")
    val content : String,
    @SerializedName("createDate")
    val createDate : String,
    @SerializedName("postId")
    val postId : Int,
    @SerializedName("user")
    val user : CommentUser,
    @SerializedName("childComments")
    val childComments : List<CommentData>?
)

data class CommentData(
    @SerializedName("commentId")
    val commentId : Int,
    @SerializedName("content")
    val content : String,
    @SerializedName("createDate")
    val createDate : String,
    @SerializedName("postId")
    val postId : Int,
    @SerializedName("user")
    val user : CommentUser,
    @SerializedName("childComments")
    var childComments : List<CommentData>?,
    var isParent : Boolean? // true 부모, false 댓글
)

data class CommentUser(
    @SerializedName("id")
    val id : Int,
    @SerializedName("email")
    val email : String,
    @SerializedName("studentId")
    val studentId : String,
    @SerializedName("profileImageUrl")
    val profileImageUrl : String,
    @SerializedName("name")
    val name : String,
    @SerializedName("accountNumber")
    val accountNumber : String,
    @SerializedName("gender")
    val gender : Boolean,
    @SerializedName("verifySmoker")
    val verifySmoker : Boolean,
    @SerializedName("roleType")
    val roleType : String,
    @SerializedName("status")
    val status : String,
    @SerializedName("mannerCount")
    val mannerCount : Int,
    @SerializedName("grade")
    val grade : String,
)