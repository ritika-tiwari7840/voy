package com.ritika.voy.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Retrofit {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Utility.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api : AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}
