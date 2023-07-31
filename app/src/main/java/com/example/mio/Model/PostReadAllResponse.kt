package com.example.mio.Model

import com.google.gson.annotations.SerializedName

class PostReadAllResponse(
    @SerializedName("totalPages")
    var totalPages : Int,
    @SerializedName("totalElements")
    var totalElements : Int,
    @SerializedName("size")
    var size : Int,
    @SerializedName("content")
    var content : ArrayList<Content>,
    @SerializedName("number")
    var number : Int,
    @SerializedName("sort")
    var sort : Sort,
    @SerializedName("numberOfElements")
    var numberOfElements : Int,
    @SerializedName("pageable")
    var pageable: Pageable,
    @SerializedName("last")
    var last : Boolean,
    @SerializedName("first")
    var first : Boolean,
    @SerializedName("empty")
    var empty : Boolean,
): java.io.Serializable {

}

data class Content(
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
    var category : Category,
    @SerializedName("verifyGoReturn")
    var verifyGoReturn : Boolean,
    @SerializedName("numberOfPassengers")
    var numberOfPassengers : Int,
    @SerializedName("user")
    var user : User,
    @SerializedName("viewCount")
    var viewCount : Int,
    @SerializedName("verifyFinish")
    var verifyFinish : Boolean,
    @SerializedName("participants")
    var participants : ArrayList<Participants>,
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
): java.io.Serializable {

}

data class Category(
    @SerializedName("categoryId")
    var categoryId : Int,
    @SerializedName("categoryName")
    var categoryName : String,
): java.io.Serializable {

}

data class User(
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
) : java.io.Serializable {

}

data class Participants(
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
): java.io.Serializable {

}

data class Sort(
    @SerializedName("empty")
    var empty : Boolean,
    @SerializedName("sorted")
    var sorted : Boolean,
    @SerializedName("unsorted")
    var unsorted : Boolean,
): java.io.Serializable {

}

data class Pageable(
    @SerializedName("offset")
    var offset : Int,
    @SerializedName("sort")
    var sort : Sort,
    @SerializedName("pageNumber")
    var pageNumber : Int,
    @SerializedName("pageSize")
    var pageSize : Int,
    @SerializedName("paged")
    var paged : Boolean,
    @SerializedName("unpaged")
    var unpaged : Boolean,
): java.io.Serializable {

}