package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class CheckParticipateData(
    @SerializedName("check")
    var check : Boolean?
)
