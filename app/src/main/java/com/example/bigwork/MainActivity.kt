package com.example.bigwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.ui.theme.BigWorkTheme
import com.example.bigwork.viewmodel.MainViewModel
import java.util.UUID
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BigWorkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RepositoryTestScreen()
                }
            }
        }
    }
}

@Composable
fun RepositoryTestScreen(viewModel: MainViewModel = viewModel()) {
    // 使用collectAsStateWithLifecycle（Compose最佳实践，自动管理生命周期）
    val blindUsers by viewModel.blindUsers.collectAsStateWithLifecycle()
    val volunteers by viewModel.volunteers.collectAsStateWithLifecycle()
    val pendingReserves by viewModel.pendingReserves.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(ScrollState(0))
    ) {
        Text(
            text = "离线优先Repository测试",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ==================== 流程说明 ====================
        Text(
            text = "测试流程：\n" +
                    "1. 首次打开：先显示空列表（本地无数据），然后网络请求成功后自动更新\n" +
                    "2. 关闭网络再次打开：直接显示上次缓存的数据\n" +
                    "3. 点击创建按钮：先调用网络接口，成功后自动更新本地和UI",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ==================== 用户数据测试 ====================
        Text(
            text = "一、用户数据（自动从网络同步）",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "盲人用户（${blindUsers.size}人）：\n" +
                    if (blindUsers.isEmpty()) "  暂无数据"
                    else blindUsers.joinToString("\n") { "  - ${it.userName}" },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "志愿者用户（${volunteers.size}人）：\n" +
                    if (volunteers.isEmpty()) "  暂无数据"
                    else volunteers.joinToString("\n") { "  - ${it.userName}" },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                val newUser = User(
                    userId = UUID.randomUUID().toString().substring(0, 8),
                    password = "123456",
                    userName = "新用户${(100..999).random()}",
                    userType = (0..1).random(),
                    age = (18..60).random(),
                    gender = if ((0..1).random() == 0) "男" else "女",
                    phone = "138${(10000000..99999999).random()}"
                )
                viewModel.createUser(newUser)
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("创建随机用户")
        }

        // ==================== 预约数据测试 ====================
        Text(
            text = "二、待接单预约（自动从网络同步）",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = if (pendingReserves.isEmpty()) "暂无待接单预约"
            else pendingReserves.joinToString("\n\n") {
                "地点：${it.area}\n要求：${it.remark}\n时间：${it.createTime}"
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                val newReserve = Reserve(
                    reserveId = UUID.randomUUID().toString(),
                    blindUserId = "blind001",
                    volunteerUserId = null,
                    area = "东莞体育中心",
                    remark = "需要陪跑1小时，速度慢一些",
                    status = 0,
                    createTime = "2026-05-12 ${(8..18).random()}:00:00"
                )
                viewModel.createReserve(newReserve)
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("创建新预约")
        }

        // ==================== 清空数据 ====================
        Button(
            onClick = { viewModel.clearAllData() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("清空所有本地数据")
        }
    }
}