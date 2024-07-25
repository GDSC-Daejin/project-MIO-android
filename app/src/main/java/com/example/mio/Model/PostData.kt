package com.example.mio.Model

data class PostData(
    var accountID : String,
    var postID : Int, //position
    var postTitle : String,
    var postContent : String,
    var postCreateDate : String,
    var postTargetDate : String,
    var postTargetTime : String,
    var postCategory : String,
    var postLocation : String,
    var postParticipation : Int, //현재 함께할 손님 인원수
    var postParticipationTotal : Int, //총 받을 손님 인원수
    var postCost : Int,
    var postVerifyGoReturn: Boolean, //등/하교 -> true 등, false 하
    var user : User,
    var postlatitude : Double,
    var postlongitude : Double

    ) : java.io.Serializable {

}
