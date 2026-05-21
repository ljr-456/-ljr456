package com.example.bigwork.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reserves")
data class Reserve(
    @PrimaryKey val reserveId: String,     // 预约唯一编号（主键）
    val blindUserId: String,               // 发起预约的盲人ID
    val volunteerUserId: String?,          // 接单的志愿者ID（未接单为空）
    val area: String,                      // 预约地点（地图选点名称）
    val detailAddress: String = "",        // 手动填写的详细地址
    val latitude: Double = 0.0,            // 地图选点纬度
    val longitude: Double = 0.0,           // 地图选点经度
    val remark: String,                    // 特殊要求
    val status: Int,                       // 0=待接单 1=已接单 2=已完成 3=已取消
    val createTime: String                 // 预约时间
)
