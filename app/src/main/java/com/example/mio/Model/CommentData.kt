package com.example.mio.Model

data class CommentData(
    var commentAccountID : String,
    var commentContent : String,
    var commentPosition : Int //position
) : java.io.Serializable {

}
