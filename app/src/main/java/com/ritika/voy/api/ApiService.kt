package com.ritika.voy.api

import com.ritika.voy.api.dataclasses.LoginRequest
import com.ritika.voy.api.dataclasses.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login/")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}