package com.example.bigwork.model

import com.google.gson.annotations.SerializedName

// 对应jsonplaceholder的/posts接口返回的数据
data class Post(
    @SerializedName("userId") val userId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)