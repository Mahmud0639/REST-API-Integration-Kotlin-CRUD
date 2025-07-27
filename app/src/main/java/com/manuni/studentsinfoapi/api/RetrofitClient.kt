package com.manuni.studentsinfoapi.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TIME_OUT:Long = 120
    private val gson = GsonBuilder().create()
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(TIME_OUT,TimeUnit.SECONDS)
        .connectTimeout(TIME_OUT,TimeUnit.SECONDS)
        .build()

    val retrofit: ApiInterface by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(ApiEndPoints.BASE_URL)
            .client(okHttpClient)
            .build()
            .create(ApiInterface::class.java)
    }
}