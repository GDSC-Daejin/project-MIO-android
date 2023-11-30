package com.example.mio.Model

import com.google.gson.annotations.SerializedName

data class EditAccountData(
    @SerializedName("gender")
    var gender : Boolean, //false 남 true여
    @SerializedName("verifySmoker")
    var verifySmoker : Boolean,//false 비흡, true 흡
    @SerializedName("accountNumber")
    var accountNumber : String,
    @SerializedName("activityLocation")
    var activityLocation : String
)
