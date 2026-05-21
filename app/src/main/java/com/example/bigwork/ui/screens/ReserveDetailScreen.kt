package com.example.bigwork.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bigwork.model.Reserve
import com.example.bigwork.model.User
import com.example.bigwork.navigation.NavRoutes
import com.example.bigwork.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveDetailScreen(
    navController: NavController,
    reserveId: String,
    viewModel: MainViewModel = viewModel()
) {
    val pendingReserves by viewModel.pendingReserves.collectAsStateWithLifecycle()
    val blindUsers by viewModel.blindUsers.collectAsStateWithLifecycle()
    val volunteers by viewModel.volunteers.collectAsStateWithLifecycle()
    val currentUserId = viewModel.currentUser.collectAsStateWithLifecycle().value?.userId
    val allUsers = blindUsers + volunteers

    var reserve by remember { mutableStateOf<Reserve?>(null) }
    var blindUser by remember { mutableStateOf<User?>(null) }
    var volunteerUser by remember { mutableStateOf<User?>(null) }

    // 查找预约并加载关联用户
    LaunchedEffect(reserveId, pendingReserves, allUsers) {
        reserve = pendingReserves.find { it.reserveId == reserveId }
        reserve?.let { r ->
            blindUser = allUsers.find { it.userId == r.blindUserId }
            r.volunteerUserId?.let { vid ->
                volunteerUser = allUsers.find { it.userId == vid }
            }
        }
    }

    val statusText = when (reserve?.status) {
        0 -> "待接单"
        1 -> "已接单"
        2 -> "已完成"
        3 -> "已取消"
        else -> "未知"
    }

    val isVolunteerAccepted = reserve?.volunteerUserId == currentUserId && reserve?.status == 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预约详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            reserve?.let { r ->
                // 预约信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text("预约信息", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailItem("预约状态", statusText)
                        DetailItem("预约时间", r.createTime)
                        DetailItem("预约地点", r.area)
                        if (r.detailAddress.isNotBlank()) {
                            DetailItem("详细地址", r.detailAddress)
                        }
                        DetailItem("特殊要求", r.remark)
                        if (r.latitude != 0.0 || r.longitude != 0.0) {
                            DetailItem("坐标", "%.5f, %.5f".format(r.latitude, r.longitude))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 盲人用户信息
                blindUser?.let { bu ->
                    UserInfoCard(title = "盲人用户信息", user = bu)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 志愿者信息（如果已接单）
                volunteerUser?.let { vu ->
                    UserInfoCard(title = "志愿者信息", user = vu)
                    Spacer(modifier = Modifier.height(12.dp))
                } ?: run {
                    if (r.status == 0 && currentUserId != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "暂无志愿者接单",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 志愿者端：已接单的预约显示"开始跑步"
                if (isVolunteerAccepted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            navController.navigate(NavRoutes.Running.createRoute(r.reserveId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("开始跑步")
                    }
                }
            } ?: run {
                Text(
                    text = "预约不存在",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun UserInfoCard(title: String, user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = user.userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text("角色：${if (user.userType == 0) "盲人用户" else "志愿者"}")
            Text("年龄：${user.age}岁 | 性别：${user.gender}")
            Text("电话：${user.phone ?: "未填写"}")
        }
    }
}
