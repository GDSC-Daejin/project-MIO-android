package com.gdsc.mio.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostData(
    val accountID : String,
    val postID : Int, //position
    val postTitle : String,
    val postContent : String,
    val postCreateDate : String,
    val postTargetDate : String,
    val postTargetTime : String,
    val postCategory : String,
    val postLocation : String,
    val postParticipation : Int, //현재 함께할 손님 인원수
    val postParticipationTotal : Int, //총 받을 손님 인원수
    val postCost : Int,
    val postVerifyGoReturn: Boolean, //등/하교 -> true 등, false 하
    val user : User,
    val postlatitude : Double,
    val postlongitude : Double

    ) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostData

        if (postID != other.postID) return false
        if (postTitle != other.postTitle) return false
        if (postContent != other.postContent) return false
        if (postCreateDate != other.postCreateDate) return false
        if (postTargetDate != other.postTargetDate) return false
        if (postTargetTime != other.postTargetTime) return false
        if (postCategory != other.postCategory) return false
        if (postLocation != other.postLocation) return false
        if (postParticipation != other.postParticipation) return false
        if (postParticipationTotal != other.postParticipationTotal) return false
        if (postCost != other.postCost) return false
        if (postVerifyGoReturn != other.postVerifyGoReturn) return false
        if (user != other.user) return false
        if (postlatitude != other.postlatitude) return false
        if (postlongitude != other.postlongitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = postID
        result = 31 * result + postTitle.hashCode()
        result = 31 * result + postContent.hashCode()
        result = 31 * result + postCreateDate.hashCode()
        result = 31 * result + postTargetDate.hashCode()
        result = 31 * result + postTargetTime.hashCode()
        result = 31 * result + postCategory.hashCode()
        result = 31 * result + postLocation.hashCode()
        result = 31 * result + postParticipation
        result = 31 * result + postParticipationTotal
        result = 31 * result + postCost.hashCode()
        result = 31 * result + postVerifyGoReturn.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + postlatitude.hashCode()
        result = 31 * result + postlongitude.hashCode()
        return result
    }
}
