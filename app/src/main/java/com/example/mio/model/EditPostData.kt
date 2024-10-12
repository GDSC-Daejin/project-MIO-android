package com.example.mio.model

import com.google.gson.annotations.SerializedName

data class EditPostData(
    @SerializedName("title")
    var title: String,
    @SerializedName("content")
    var content: String,
    @SerializedName("categoryId")
    var categoryId: Int,
    @SerializedName("targetDate")
    var targetDate: String,
    @SerializedName("targetTime")
    var targetTime: String,
    @SerializedName("numberOfPassengers")
    var numberOfPassengers: Int,
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
) : java.io.Serializable