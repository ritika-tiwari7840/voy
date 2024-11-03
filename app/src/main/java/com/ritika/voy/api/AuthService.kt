package com.ritika.voy.api

import com.ritika.voy.api.dataclasses.SignUpRequest
import com.ritika.voy.api.dataclasses.SignUpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthService {
    @POST("register/")
    fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>
}