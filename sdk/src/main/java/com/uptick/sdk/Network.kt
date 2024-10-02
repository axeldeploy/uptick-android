package com.uptick.sdk

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class Network {
    private val okHttpClient by lazy {
        OkHttpClient.Builder().apply {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            addInterceptor(loggingInterceptor)
        }.build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.uptick.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService by lazy {
        retrofit.create<Api>()
    }
}