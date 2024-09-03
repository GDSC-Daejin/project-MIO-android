package com.example.mio

import com.example.mio.Model.*
import com.example.mio.sse.SSEData
import retrofit2.Call
import retrofit2.http.*

interface MioInterface {

    //게시글 생성
    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/post/{categoryId}")
    fun addPostData(/*@Header("Content-Type") content_type : String,*/ /*@PartMap postData : Map<String, RequestBody>*/@Body postData : AddPostData, @Path("categoryId") categoryId : Int) : Call<AddPostResponse>
    //"application/json; charset=utf-8"
    /*@POST("/post/1")
    fun addCarpoolPostData(@Body postData : AddPostData) : Call<AddPostResponse>
    */
    //게시글 삭제
    @PATCH("/post/delete/{id}")
    fun deletePostData(@Path("id") postId : Int) : Call<Void>

    //게시글 수정
    @PATCH("/post/{id}")
    fun editPostData(@Path("id") id : Int, @Body postData : EditPostData) : Call<AddPostResponse>


    //게시글 생성 순으로 조회
    @GET("/readAll")
    fun getServerPostData(@Query("sort") sort : String,
                          @Query("page") page : Int,
                          @Query("size") size : Int) : Call<PostReadAllResponse>

    //게시글로 id로 상세조회
    @GET("/detail/{id}")
    fun getPostIdDetailSearch(@Path("id") postId : Int) : Call<Content>

    @GET("/detail/{id}")
    suspend fun getSuspendPostIdDetailSearch(@Path("id") postId : Int) : Call<Content>

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

    //게시글 상태 변경 deadline verfi

    @PATCH("/post/verfiyFinish/{id}")
    fun patchVerifyFinish(@Body verifyFinish : VerifyFinishData, @Path("id") id : Int) : Call<AddPostResponse>

    //마감기한 지난거 또는 마감할때
    @PATCH("/post/deadLine/{postId}")
    fun patchDeadLinePost(@Path("postId") postId : Int) : Call<Content>

    //카풀완료 후
    @PATCH("/post/complete/{id}")
    fun patchCompletePost(@Path("id") id : Int) : Call<Content>


    @GET("/post/location2") //위치로 게시글 조회
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


    //유저 활동 지역에서 가져오기
    @GET("/activityLocation")
    fun getActivityLocation(@Query("sort") sort : String,
                            @Query("page") page : Int,
                            @Query("size") size : Int) : Call<PostReadAllResponse>



    ///////////////////////////////
    //참여

    //유저가 게시글에 참여신청
    @POST("/{postId}/participate")
    fun addParticipate(@Path("postId") postId: Int, @Body content: ParticipateData) : Call<ParticipationData>

    //유저가 게시글에 참여를 취소
    @PATCH("/{postId}/participateCancel")
    fun deleteParticipate(@Path("postId") postId: Int) : Call<Void>


    //같은 날 신청하려고 하는 등/하교가 있는지 (수정 해야함)
    @GET("/{postId}/check")
    fun checkParticipate(@Path("postId") postId : Int) : Call<CheckParticipateData>

    //전부 + 작성자 제거
    @GET("/user/participants")
    fun getMyParticipantsData() : Call<List<ParticipationData>>

    //승인된 참여 + 작성자 제거안함
    @GET("/user/participants/carpool")
    fun getMyParticipantsUserData() : Call<List<Content>>



    @PATCH("/{participantId}/participateAccept") //void는 response의 값이 없음을 나타내기 위해 Void를 사용, 성공 코드만 call받기위함
    fun patchParticipantsApproval(@Path("participantId") participantId : Int) : Call<Void>
    //suspend fun fetchData(@Path("participantId") participantId : Int) : Response<ParticipateData>


    //작성자가 참여한 사람 거절
    @PATCH("/{participantId}/reject")
    fun deleteParticipants(@Path("participantId") participantId : Int) : Call<Void>

    ////////////////////////////////
    //알람

    @POST("/alarm/create")
    fun addAlarm(@Body alarmSendData : AddAlarmData) : Call<AddAlarmResponseData>

    @GET("/alarm/readAll")
    fun getMyAlarm() : Call<List<AddAlarmResponseData>>

    @DELETE("/alarm/delete/{id}")
    fun deleteMyAlarm(@Path("id") id : Int) : Call<Void>


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
    //---------------//

    //북마크
    @POST("/bookmark/{postId}")
    fun addBookmark(@Path("postId") postId: Int) : Call<Void>

    @GET("/bookmark/read")
    fun getBookmark() : Call<List<BookMarkResponseData>>

    //북마크 실시간알람
    @GET("/subscribe/{user_id}")
    @Headers("Accept: text/event-stream")
    fun getRealTimeBookMarkAlarm(@Path("user_id") userId: Long) : Call<SSEData>
}