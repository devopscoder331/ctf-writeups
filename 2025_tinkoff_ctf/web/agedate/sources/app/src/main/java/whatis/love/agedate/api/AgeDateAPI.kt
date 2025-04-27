package whatis.love.agedate.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import whatis.love.agedate.api.client.APIResponse
import whatis.love.agedate.api.requests.FillQuestionnaireRequest
import whatis.love.agedate.api.requests.RegisterOrLoginRequest
import whatis.love.agedate.api.requests.SendMessageRequest
import whatis.love.agedate.api.requests.SetBanRequest
import whatis.love.agedate.api.requests.SetLikeRequest
import whatis.love.agedate.api.requests.SetStatusRequest
import whatis.love.agedate.api.responses.ChatsResponse
import whatis.love.agedate.api.responses.DiscoverQueueResponse
import whatis.love.agedate.api.responses.FillQuestionnaireResponse
import whatis.love.agedate.api.responses.GetLikedResponse
import whatis.love.agedate.api.responses.GetMeResponse
import whatis.love.agedate.api.responses.GetMessagesResponse
import whatis.love.agedate.api.responses.GetProfileLikedStatusResponse
import whatis.love.agedate.api.responses.GetProfileResponse
import whatis.love.agedate.api.responses.GetStatusesResponse
import whatis.love.agedate.api.responses.HomePageContentResponse
import whatis.love.agedate.api.responses.Ok
import whatis.love.agedate.api.responses.RegisterOrLoginResponse
import whatis.love.agedate.api.responses.SendMessageResponse
import whatis.love.agedate.api.responses.SetBanResponse
import whatis.love.agedate.api.responses.SetLikeResponse
import whatis.love.agedate.api.responses.SetStatusResponse

interface AgeDateAPI {
    @POST("register")
    fun register(
        @Body request: RegisterOrLoginRequest,
    ): Call<APIResponse<RegisterOrLoginResponse>>

    @POST("login")
    fun login(
        @Body request: RegisterOrLoginRequest,
    ): Call<APIResponse<RegisterOrLoginResponse>>

    @POST("logout")
    fun logout(): Call<APIResponse<Ok>>

    @GET("me/profile")
    fun me(): Call<APIResponse<GetMeResponse>>

    @POST("me/questionnaire")
    fun questionnaire(
        @Body request: FillQuestionnaireRequest,
    ): Call<APIResponse<FillQuestionnaireResponse>>

    @GET("me/liked")
    fun liked(
        @Query("limit") limit: Int = 10,
    ): Call<APIResponse<GetLikedResponse>>

    @POST("me/status")
    fun setStatus(
        @Body request: SetStatusRequest,
    ): Call<APIResponse<SetStatusResponse>>

    @GET("me/home")
    fun home(): Call<APIResponse<HomePageContentResponse>>

    @GET("me/discover")
    fun discover(
        @Query("includeDisliked") includeDisliked: Boolean,
        @Query("limit") limit: Int = 10,
    ): Call<APIResponse<DiscoverQueueResponse>>

    @GET("profile/{id}")
    fun profile(
        @Path("id") id: String,
    ): Call<APIResponse<GetProfileResponse>>

    @GET("profile/{id}/statuses")
    fun profileStatuses(
        @Path("id") id: String,
    ): Call<APIResponse<GetStatusesResponse>>

    @POST("profile/{id}/like")
    fun likeProfile(
        @Path("id") id: String,
        @Body request: SetLikeRequest,
    ): Call<APIResponse<SetLikeResponse>>

    @GET("profile/{id}/liked")
    fun profileLiked(
        @Path("id") id: String,
    ): Call<APIResponse<GetProfileLikedStatusResponse>>

    @GET("chats")
    fun chats(): Call<APIResponse<ChatsResponse>>

    @GET("chats/{id}")
    fun messages(
        @Path("id") id: String,
    ): Call<APIResponse<GetMessagesResponse>>

    @POST("chats/{id}")
    fun sendMessage(
        @Path("id") id: String,
        @Body request: SendMessageRequest,
    ): Call<APIResponse<SendMessageResponse>>

    @POST("profile/{id}/ban")
    fun banProfile(
        @Path("id") id: String,
        @Body request: SetBanRequest,
    ): Call<APIResponse<SetBanResponse>>
}
