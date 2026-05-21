package com.example.bigwork.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Mock API的基础地址（注意末尾必须加/）
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // 日志拦截器：调试时可以看到完整的请求和响应
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp客户端
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Retrofit实例
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 对外提供ApiService实例
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}