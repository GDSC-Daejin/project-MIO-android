package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class EditPostData(
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("targetDate")
    val targetDate: String,
    @SerializedName("targetTime")
    val targetTime: String,
    @SerializedName("numberOfPassengers")
    val numberOfPassengers: Int,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("location")
    val location: String,
    @SerializedName("cost")
    val cost: Int,
    @SerializedName("region3Depth")
    val region3Depth: String,
) : java.io.Serializable