package com.gdsc.mio.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationReadAllResponse(
    @SerializedName("postId")
    var postId: Int,
    @SerializedName("title")
    var title: String,
    @SerializedName("content")
    var content: String,
    @SerializedName("createDate")
    var createDate: String,
    @SerializedName("targetDate")
    var targetDate: String,
    @SerializedName("targetTime")
    var targetTime: String,
    @SerializedName("category")
    var category: LocationCategory?,
    @SerializedName("verifyGoReturn")
    var verifyGoReturn: Boolean,
    @SerializedName("numberOfPassengers")
    var numberOfPassengers: Int,
    @SerializedName("user")
    var user: User?,
    @SerializedName("viewCount")
    var viewCount: Int,
    @SerializedName("verifyFinish")
    var verifyFinish: Boolean,
    @SerializedName("participants")
    var participants: ArrayList<LocationParticipants>?,
    @SerializedName("latitude")
    var latitude: Double,
    @SerializedName("longitude")
    var longitude: Double,
    @SerializedName("bookMarkCount")
    var bookMarkCount: Int,
    @SerializedName("participantsCount")
    var participantsCount: Int,
    @SerializedName("location")
    var location: String,
    @SerializedName("cost")
    var cost: Int,
    @SerializedName("isDeleteYN")
    var isDeleteYN : String?,
    @SerializedName("postType")
    var postType : String? //BEFORE_DEADLINE, DEADLINE, COMPLETED


) :Parcelable

@Parcelize
data class LocationCategory(
    @SerializedName("categoryId")
    var categoryId : Int,
    @SerializedName("categoryName")
    var categoryName : String,
): Parcelable

/*
data class LocationUser(
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
*/

@Parcelize
data class LocationParticipants(
    @SerializedName("id")
    var id : Int?,
    @SerializedName("email")
    var email : String?,
    @SerializedName("studentId")
    var studentId : String?,
    @SerializedName("profileImageUrl")
    var profileImageUrl : String?,
    @SerializedName("name")
    var name : String?,
    @SerializedName("accountNumber")
    var accountNumber : String?,
    @SerializedName("gender")
    var gender : Boolean?,
    @SerializedName("verifySmoker")
    var verifySmoker : Boolean?,
    @SerializedName("roleType")
    var roleType : String?,
    @SerializedName("status")
    var status : String?,
    @SerializedName("mannerCount")
    var mannerCount : Int?,
    @SerializedName("grade")
    var grade : String?,
) : Parcelable

