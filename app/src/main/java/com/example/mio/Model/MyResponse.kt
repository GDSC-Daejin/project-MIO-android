package com.example.mio.Model

import com.google.gson.annotations.SerializedName

class Paging (
    @SerializedName("total_pages")
    var total_pages : Int,

    @SerializedName("current_page")
    var current_page : Int,

    @SerializedName("is_last_page")
    var is_last_page : Boolean
): java.io.Serializable {


}
class UserResponseData ( //세부적인 유저 모든 정보 가져올때 사용
    //@SerializedName("todos")
    //var todos : List<TodoListData>,

    @SerializedName("paging")
    var paging : Paging
): java.io.Serializable {


}

class PostResponseData ( //post에서 내용
    @SerializedName("todos")
    var posts : List<PostData>,

    @SerializedName("totalPages")
    var paging : Paging
): java.io.Serializable {


}

class MyResponse( //연결하여 정보를 가져올 때 사용
    @SerializedName("status")
    var status : Boolean,

    /*@SerializedName("userData")
    var userAllData : UserResponseData,

    var postAllData : PostData,*/

    @SerializedName("data")
    var userInfoData : LoginGoogleResponse,

    var userToken : String

) : java.io.Serializable {


}
