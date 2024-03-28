package com.example.mio

import com.example.mio.Model.*
import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface MioInterface {

    //게시글 생성
    @POST("/post/{categoryId}")
    fun addPostData(/*@Header("Content-Type") content_type : String,*/ /*@PartMap postData : Map<String, RequestBody>*/@Body postData : AddPostData, @Path("categoryId") categoryId : Int) : Call<AddPostResponse>

    /*@POST("/post/1")
    fun addCarpoolPostData(@Body postData : AddPostData) : Call<AddPostResponse>
    */
    //게시글 삭제
    @DELETE("/post/{id}")
    fun deletePostData(@Path("id") postId : Int) : Call<PostReadAllResponse>

    //게시글 수정
    @PATCH("/post/{id}")
    fun editPostData(@Body postData : AddPostData, @Path("id") postId : Int) : Call<AddPostResponse>


    //게시글 생성 순으로 조회
    @GET("/readAll")
    fun getServerPostData(@Query("sort") sort : String,
                          @Query("page") page : Int,
                          @Query("size") size : Int) : Call<PostReadAllResponse>

    //카테고리에 따른 게시글 생성 순 조회 1=카풀, 2=택시
    @GET("/categoryPost/{categoryId}")
    fun getCategoryPostData(@Path("categoryId") categoryId: Int,
                            @Query("sort") sort : String,
                            @Query("page") page : Int,
                            @Query("size") size : Int) : Call<PostReadAllResponse>


    @GET("/readAll")
    fun getCurrentServerPostData(@Query("sort") sort : String) : Call<PostReadAllResponse>

    //게시글 마감날짜순
    @GET("/readAll/targetDate")
    fun getServerDateData() : Call<PostReadAllResponse>
    @GET("/readAll/cost")
    fun getServerCostData() : Call<PostReadAllResponse>
// ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
/*    @GET("/post/location")
    fun getLocationPostData(@Query("latitude") latitude : Double, @Query("longitude") longitude : Double) : Call<List<LocationReadAllResponse>>*/

    @PATCH("/post/verfiyFinish/{id}")
    fun patchVerifyFinish(@Body verifyFinish : Boolean, @Path("id") id : Int) : Call<AddPostResponse>

    @GET("/post/location2")
    fun getLocationPostData(@Query("location") location : String) : Call<List<LocationReadAllResponse>>

    @GET("/post/distance/{postId}")
    fun getNearByPostData(@Path("postId") postId : Int) : Call<List<LocationReadAllResponse>>
// ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ


    /////////////////////////////////


    //refreshToken
    @POST("/token")
    fun refreshTokenProcess(@Body refreshToken: RefreshTokenRequest) : Call<LoginResponsesData>

    //회원가입
    @POST("/auth/google")
    fun addUserInfoData(@Body token: TokenRequest) : Call<LoginResponsesData>


    ////////////////////////////////////
    //댓글

    //부모댓글 조회
    @GET("/comments/parent/{postId}")
    fun getCommentData(@Path("postId") postId : Int) : Call<List<CommentResponseData>>

    //부모댓글작성
    @POST("/comments/parent/{postId}")
    fun addCommentData(@Body commentData: SendCommentData, @Path("postId") postId : Int) : Call<CommentData>

    //대댓글 작성
    @POST("/comments/child/{parentId}")
    fun addChildCommentData(@Body commentData: SendChildCommentData, @Path("parentId") parentId : Int) : Call<CommentData>


    //댓글 수정
    @PATCH("/comments/{commentId}")
    fun editCommentData(@Body commentData: SendCommentData, @Path("commentId") commentId : Int) : Call<CommentData>

    //댓글 삭제
    @PATCH("/comments/delete/{commentId}")
    fun deleteCommentData(@Path("commentId") commentId : Int) : Call<CommentData>


    /////////////////////////////////////////
    //계정관리


    //자기가 쓴 글 가져오기
    @GET("/memberPost/{userId}")
    fun getMyPostData(@Path("userId") userId : Int,
                      @Query("sort") sort : String,
                      @Query("page") page : Int,
                      @Query("size") size : Int) : Call<PostReadAllResponse>





    //사용자 정보 가져오기 - 이메일
    @GET("/user/email/{userEmail}")
    fun getAccountData(@Path("userEmail") userEmail : String) : Call<User>

    //사용자 유저 정보 가져오기 - 유저id
    @GET("/user/id/{userId}")
    fun getUserProfileData(@Path("userId") userId : Int) : Call<User>


    //유저 정보 추가입력
    @PATCH("/user/{userId}")
    fun editMyAccountData(@Path("userId") userId : Int, @Body editData : EditAccountData) : Call<User>

    ///////////////////////////
    //포스트 id로 참가 신청한 내역 가져오기
    @GET("/{postId}/participants")
    fun getParticipationData(@Path("postId") postId : Int) : Call<List<ParticipationData>>

    ////////////////////////////////
    //위치가져오기
    @GET("/post/location")
    fun getMyLocation(@Query("latitude") latitude : Double,
                      @Query("longitude") longitude : Double) : Call<List<LocationReadAllResponse>>




    ///////////////////////////////
    //참여

    //유저가 게시글에 참여신청
    @POST("/{postId}/participate")
    fun addParticipate(@Path("postId") postId: Int, @Body content: ParticipateData) : Call<String?>

    //유저가 게시글에 참여를 취소
    @DELETE("/{postId}/participate")
    fun deleteParticipate(@Path("postId") postId: Int) : Call<Void>


    //같은 날 신청하려고 하는 등/하교가 있는지 (수정 해야함)
    @GET("/{postId}/check")
    fun checkParticipate(@Path("postId") postId : Int) : Call<Boolean>

    //유저가 참여한 게시글 조회
    @GET("/user/participants")
    fun getMyParticipantsData(@Query("page") page : Int,
                              @Query("size") size : Int) : Call<List<Content>>

    @PATCH("/{participantId}/participate") //void는 response의 값이 없음을 나타내기 위해 Void를 사용, 성공 코드만 call받기위함
    fun patchParticipantsApproval(@Path("participantId") participantId : Int) : Call<Void>
    //suspend fun fetchData(@Path("participantId") participantId : Int) : Response<ParticipateData>


    //작성자가 참여한 사람 거절
    @DELETE("/{participantId}/reject")
    fun deleteParticipants(@Path("participantId") participantId : Int) : Call<Void>

    ////////////////////////////////
    //알람

    @POST("/alarm/create")
    fun addAlarm(@Body alarmSendData : AddAlarmData) : Call<AddAlarmResponseData>

    @GET("/alarm/readAll")
    fun getMyAlarm() : Call<List<AddAlarmResponseData>>



    //////////////////////////////////
    //평가 리뷰 (후기)

    //유저가 받은 후기
    @GET("/manners/get/{userId}")
    fun getMyMannersReceiveReview(@Query("userId") userId: Int) : Call<List<MyAccountReviewData>>

    //작성가능한 후기
    @GET("/post/review")
    fun getMyMannersWriteableReview(@Query("sort") sort : String,
                              @Query("page") page : Int,
                              @Query("size") size : Int) : Call<PostReadAllResponse>


    //유저가 보낸 후기
    @GET("/manners/post/{userId}")
    fun getMyMannersSendReview(@Query("userId") userId: Int) : Call<List<MyAccountReviewData>>

    //탑승자 평가
    @POST("/post/{userId}/evaluation/passenger")
    fun addPassengersReview(@Path("userId") userId: Int, @Body passengersReviewData : PassengersReviewData) : Call<PassengersReviewData>

    //기사 평가
    @POST("/post/{postId}/evaluation/driver")
    fun addDriversReview(@Path("postId") postId: Int, @Body driversReviewData : DriversReviewData) : Call<PassengersReviewData>
}