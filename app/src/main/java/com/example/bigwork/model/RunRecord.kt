package com.example.bigwork.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_records")
data class RunRecord(
    @PrimaryKey val recordId: String,      // 记录ID（主键）
    val reserveId: String,                 // 关联的预约编号
    val blindUserId: String,               // 盲人ID
    val volunteerUserId: String,           // 志愿者ID
    val area: String,                      // 实际跑步地点
    val duration: Float,                   // 时长（单位：小时）
    val createTime: String                 // 记录生成时间
)