package com.example.mio.Model

data class PostData(
    var accountID : String,
    var postID : Int, //position
    var postTitle : String,
    var postContent : String,
    var postTargetDate : String
) : java.io.Serializable {

}
