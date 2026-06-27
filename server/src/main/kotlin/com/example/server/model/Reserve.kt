package com.example.server.model

data class Reserve(
    val reserveId: String,
    val blindUserId: String,
    val volunteerUserId: String? = null,
    val area: String,
    val detailAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val remark: String,
    val status: Int,
    val createTime: String
)
