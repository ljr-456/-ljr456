package com.example.bigwork.network

import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.model.RunRecord
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path

interface ApiService {
    // ------------------- 用户相关接口 -------------------
    @GET("users")
    suspend fun getAllUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): User

    @POST("users")
    suspend fun createUser(@Body user: User): User

    // ------------------- 预约相关接口 -------------------
    @GET("posts") // 用posts模拟预约接口
    suspend fun getAllReserves(): List<Reserve>

    @POST("posts")
    suspend fun createReserve(@Body reserve: Reserve): Reserve

    // ------------------- 跑步记录相关接口 -------------------
    @GET("comments") // 用comments模拟跑步记录接口
    suspend fun getAllRunRecords(): List<RunRecord>

    @POST("comments")
    suspend fun createRunRecord(@Body record: RunRecord): RunRecord
}