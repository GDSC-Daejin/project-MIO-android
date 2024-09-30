package com.example.mio.model

import com.google.gson.annotations.SerializedName

data class AddPostData(
    @SerializedName("title")
    var title: String,
    @SerializedName("content")
    var content: String,
    @SerializedName("targetDate")
    var targetDate: String,
    @SerializedName("targetTime")
    var targetTime: String,
    @SerializedName("verifyGoReturn")
    var verifyGoReturn: Boolean,
    @SerializedName("numberOfPassengers")
    var numberOfPassengers: Int,
    @SerializedName("viewCount")
    var viewCount: Int,
    @SerializedName("verifyFinish")
    var verifyFinish: Boolean,
    @SerializedName("latitude")
    var latitude: Double?,
    @SerializedName("longitude")
    var longitude: Double?,
    @SerializedName("location")
    var location: String,
    @SerializedName("cost")
    var cost: Int,
    @SerializedName("region3Depth")
    var region3Depth: String,
) : java.io.Serializable {

}

