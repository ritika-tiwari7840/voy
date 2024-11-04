package com.ritika.voy.api

import com.ritika.voy.api.dataclasses.ForgotRequest
import com.ritika.voy.api.dataclasses.ForgotResponse
import com.ritika.voy.api.dataclasses.LoginRequest
import com.ritika.voy.api.dataclasses.LoginResponse
import com.ritika.voy.api.dataclasses.ResetRequest
import com.ritika.voy.api.dataclasses.ResetResponse
import com.ritika.voy.api.dataclasses.VerifyRequest
import com.ritika.voy.api.dataclasses.VerifyResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login/")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("forgot-password/")
    suspend fun forgotPassword(@Body forgotRequest: ForgotRequest): ForgotResponse

    @POST("verify-otp/")
    suspend fun verifyOtp(@Body verifyRequest: VerifyRequest): VerifyResponse

    @POST("reset-password/")
    suspend fun resetPassword(@Body resetRequest: ResetRequest): ResetResponse
}