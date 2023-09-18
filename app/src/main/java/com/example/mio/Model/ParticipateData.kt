package com.example.mio.Model

import com.google.gson.annotations.SerializedName

//참여 신청 할때

data class ParticipateData(
    @SerializedName("content")
    var content : String
)
