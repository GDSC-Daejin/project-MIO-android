package com.gdsc.mio.model

import com.google.gson.annotations.SerializedName

data class SendCommentData(
    @SerializedName("content")
    var content : String,
)
