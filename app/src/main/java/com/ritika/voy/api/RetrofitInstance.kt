package com.ritika.voy.api

import com.ritika.voy.api.Utility.Companion.ROUTES_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
private const val BASE_URL = Utility.BASE_URL

    val api : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val mapApi : ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ROUTES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}