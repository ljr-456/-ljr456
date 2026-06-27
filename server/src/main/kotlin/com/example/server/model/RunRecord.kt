package com.example.server.model

data class RunRecord(
    val recordId: String,
    val reserveId: String,
    val blindUserId: String,
    val volunteerUserId: String,
    val area: String,
    val duration: Float,
    val distance: Float = 0f,
    val createTime: String
)
