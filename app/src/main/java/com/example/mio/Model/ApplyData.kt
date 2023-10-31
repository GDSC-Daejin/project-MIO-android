package com.example.mio.Model

data class ApplyData(
    var applyUserData : String, //참여 신청을 한 유저의 데이터(나중에 배열로 할지 생각 Todo)
    var applyUserNumberOfPeople : Int, //참여 신청을 한 유저 총 인원 수
    var applyUserCheckNumber : Int //참여 신청한 유저 수
) : java.io.Serializable {

}
