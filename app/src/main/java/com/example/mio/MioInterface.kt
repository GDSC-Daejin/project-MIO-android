package com.example.mio

import com.example.mio.Model.*
import retrofit2.Call
import retrofit2.http.*

interface MioInterface {

    //현재 등록된 회원정보 가져오기
    @GET("user/{userId}")
    fun getUserData() : Call<MyResponse>

    //여긴 나중에 게시글 정보 가져오기로 바꾸기기
    @GET("post/{}")
    fun getDataByPage(@Query("page") page : Int,
                      @Query("size") size : Int?) : Call<MyResponse>

    @POST("post/{categoryId}")
    fun addPostData(@Body postListData: PostData) : Call<MyResponse>

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