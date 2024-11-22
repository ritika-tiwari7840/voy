package com.ritika.voy.api

import com.ritika.voy.api.dataclasses.ForgotRequest
import com.ritika.voy.api.dataclasses.ForgotResponse
import com.ritika.voy.api.dataclasses.LoginRequest
import com.ritika.voy.api.dataclasses.LoginResponse
import com.ritika.voy.api.dataclasses.ResetRequest
import com.ritika.voy.api.dataclasses.ResetResponse
import com.ritika.voy.api.dataclasses.EmailVerifyRequest
import com.ritika.voy.api.dataclasses.EmailVerifyResponse
import com.ritika.voy.api.dataclasses.GetUserResponse
import com.ritika.voy.api.dataclasses.mapsDataClasses.RoutesRequest
import com.ritika.voy.api.dataclasses.mapsDataClasses.RoutesResponse
import com.ritika.voy.api.dataclasses.PhoneVerifyRequest
import com.ritika.voy.api.dataclasses.UserResponseData
import com.ritika.voy.api.dataclasses.VerifyRequest
import com.ritika.voy.api.dataclasses.VerifyResponse
import com.ritika.voy.api.dataclasses.phoneVerifyResponseX
import com.ritika.voy.api.dataclasses.resendEmailOTP
import com.ritika.voy.api.dataclasses.resendEmailRequest
import com.ritika.voy.api.dataclasses.resendForgotOTP
import com.ritika.voy.api.dataclasses.resendOTPRequest
import com.ritika.voy.api.dataclasses.resendPhoneOTP
import com.ritika.voy.api.dataclasses.resendPhoneRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @POST("login/")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("forgot-password/")
    suspend fun forgotPassword(@Body forgotRequest: ForgotRequest): ForgotResponse

    @POST("verify-otp/")
    suspend fun verifyOtp(@Body verifyRequest: VerifyRequest): VerifyResponse

    @POST("reset-password/")
    suspend fun resetPassword(@Body resetRequest: ResetRequest): ResetResponse

    @POST("verify-email/")
    suspend fun EmailVerify(@Body request: EmailVerifyRequest): EmailVerifyResponse

    @POST("verify-phone/")
    suspend fun PhoneVerify(@Body request: PhoneVerifyRequest): phoneVerifyResponseX

    @POST("resend-otp/")
    suspend fun resendOtp(@Body request: resendOTPRequest): resendForgotOTP

    @POST("resend-emailotp/")
    suspend fun resendEmailOtp(@Body request: resendEmailRequest): resendEmailOTP

    @POST("resend-phoneotp/")
    suspend fun resendPhoneOtp(@Body request: resendPhoneRequest): resendPhoneOTP

    @GET("user/")
    suspend fun getUserData(@Header("Authorization") authHeader: String): GetUserResponse

    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Body request: RoutesRequest,
        @Query("key") apiKey: String,
        @Query("fields") fieldMask: String = "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline",
    ): RoutesResponse

    @Multipart
    @PUT("user/")
    suspend fun updateUserData(
        @Header("Authorization") token: String,
        @Part profile_photo: MultipartBody.Part,
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("emergency_contact_phone") emergencyContactPhone: RequestBody,
    ): UserResponseData
}