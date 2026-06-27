package com.example.server.model

data class User(
    val userId: String,
    val password: String,
    val userName: String,
    val userType: Int,
    val age: Int,
    val gender: String,
    val phone: String? = null
)
