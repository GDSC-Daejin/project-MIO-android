package com.example.mio.Model

data class PostData(
    var accountID : String,
    var postID : Int, //position
    var postTitle : String,
    var postContent : String,
    var postTargetDate : String,
    var postTargetTime : String,
    var postCategory : String,
    var postLocation : String,
    var postParticipation : Int,
    var postParticipationTotal : Int,
    var postCost : Int,
    var postVerifyGoReturn: Boolean, //등/하교 -> true 등, false 하
    var user : User

) : java.io.Serializable {

}