package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class CreatePostRequestData(
    @SerializedName("categoryId")
    var postCategoryId : Int,

    @SerializedName("title")
    var postTitle : String,

    @SerializedName("content")
    var postContent : String,

    @SerializedName("targetDate")
    var postTargetDate : String,

    @SerializedName("targetTime")
    var postTargetTime : Any,

    @SerializedName("verifyGoReturn")
    var postVerifyGoReturn : Boolean,

    @SerializedName("numberOfPassengers")
    var postNumberOfPassengers : Int,

    @SerializedName("viewCount")
    var postViewCount : Int,

    @SerializedName("verifyFinish")
    var postVerifyFinish : Boolean,
): java.io.Serializable {

}
