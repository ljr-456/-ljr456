package com.example.bigwork.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,        // 用户唯一ID（主键）
    val password: String,                  // 密码
    val userName: String,                  // 昵称
    val userType: Int,                     // 0=盲人 1=志愿者
    val age: Int,                          // 年龄
    val gender: String,                    // 性别
    val phone: String?                     // 电话（可空）
)