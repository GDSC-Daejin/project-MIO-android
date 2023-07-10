package com.example.mio

import com.example.mio.Model.*
import retrofit2.Call
import retrofit2.http.*

interface MioInterface {

    //여긴 나중에 게시글 정보 가져오기로 바꾸기기
    @GET("category/{categoryId}")
    fun getDataByPage(@Query("page") page : Int,
                      @Query("size") size : Int?) : Call<MyResponse>



    //게시글 생성
    //@Headers("Accept: application/json")
    //@Headers("Authorization : ")
    @POST("post/{categoryId}")
    fun addPostData(/*@Header("Content-Type") content_type : String,*/ @Body postData : AddPostData, @Path("categoryId") categoryId : Int) : Call<AddPostResponse>

    @POST("/post/1")
    fun addCarpoolPostData(@Body postData : AddPostData) : Call<AddPostResponse>

    //게시글 생성 순으로 조회
    @GET("/readAll")
    fun getServerPostData() : Call<PostReadAllResponse>

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
}