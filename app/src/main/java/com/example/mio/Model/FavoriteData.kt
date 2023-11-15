package com.example.mio.Model

data class FavoriteData(
    var FavoriteUserData : String, // 좋아요 누른 유저 체크(나중에 배열인지 생각 Todo)
    var FavoriteUserNumberOfPeople : Int, //좋아요 누른 유저 수
) : java.io.Serializable {

}