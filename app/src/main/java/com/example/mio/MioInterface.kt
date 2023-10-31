package com.example.mio

import com.example.mio.Model.*
import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface MioInterface {

    //게시글 생성
    @POST("/post/{categoryId}")
    fun addPostData(/*@Header("Content-Type") content_type : String,*/ /*@PartMap postData : Map<String, RequestBody>*/@Body postData : AddPostData, @Path("categoryId") categoryId : Int) : Call<AddPostResponse>

    @POST("/post/1")
    fun addCarpoolPostData(@Body postData : AddPostData) : Call<AddPostResponse>

    //게시글 생성 순으로 조회
    @GET("/readAll")
    fun getServerPostData() : Call<PostReadAllResponse>

    //게시글 마감날짜순
    @GET("/readAll/targetDate")
    fun getServerDateData() : Call<PostReadAllResponse>
    @GET("/readAll/cost")
    fun getServerCostData() : Call<PostReadAllResponse>
// ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
/*    @GET("/post/location")
    fun getLocationPostData(@Query("latitude") latitude : Double, @Query("longitude") longitude : Double) : Call<List<LocationReadAllResponse>>*/

    @GET("/post/location2")
    fun getLocationPostData(@Query("location") location : String) : Call<List<LocationReadAllResponse>>

    @GET("/post/distance/{postId}")
    fun getNearByPostData(@Path("postId") postId : Int) : Call<List<LocationReadAllResponse>>
// ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

    /////////////////////////////////


    //회원가입
    @POST("/auth/google")
    fun addUserInfoData(@Body token: TokenRequest) : Call<LoginResponsesData>

    //회원가입 수정
    @PUT("todo/{id}")
    fun updateData(@Body userInfoData: LoginGoogleResponse, @Path("id") userId : String) : Call<MyResponse>

    /*@DELETE("todo/{id}")
    fun deleteData(@Path("id") todoId : String) : Call<MyResponse>

    @GET("todo/trash")
    fun getTrashDataByPage(@Query("page") page : Int,
                           @Query("size") size : Int?) : Call<MyResponse>
    */

    ////////////////////////////////////
    //댓글

    //부모댓글 조회
    @GET("/comments/parent/{postId}")
    fun getCommentData(@Path("postId") postId : Int) : Call<List<CommentResponseData>>
    //부모댓글작성
    @POST("/comments/parent/{postId}")
    fun addCommentData(@Body commentData: SendCommentData, @Path("postId") postId : Int) : Call<CommentData>
    @POST("/comments/child/{parentId}")
    fun addChildCommentData(@Body commentData: SendCommentData, @Path("parentId") parentId : Int) : Call<CommentData>



    //여긴 나중에 게시글 정보 가져오기로 바꾸기기
    @GET("category/{categoryId}")
    fun getDataByPage(@Query("page") page : Int,
                      @Query("size") size : Int?) : Call<MyResponse>
}