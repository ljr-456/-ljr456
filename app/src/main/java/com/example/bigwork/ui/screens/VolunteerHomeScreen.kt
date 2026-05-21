package com.example.bigwork.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bigwork.model.Reserve
import com.example.bigwork.navigation.NavRoutes
import com.example.bigwork.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerHomeScreen(
    navController: NavController,
    userId: String,
    viewModel: MainViewModel = viewModel()
) {
    val user by viewModel.getUserById(userId).collectAsStateWithLifecycle()
    val pendingReserves by viewModel.pendingReserves.collectAsStateWithLifecycle()
    val myAccepted by viewModel.getMyReservesAsVolunteer(userId).collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = user?.userName?.let { "$it，你好" } ?: "志愿者端",
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "退出登录")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 待接单预约
            item {
                Text(
                    text = "待接单预约 (${pendingReserves.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (pendingReserves.isEmpty()) {
                item { EmptyHintCard("暂无待接单的预约") }
            } else {
                items(pendingReserves) { reserve ->
                    PendingReserveCard(
                        reserve = reserve,
                        onAccept = {
                            val updated = reserve.copy(
                                volunteerUserId = userId,
                                status = 1
                            )
                            viewModel.createReserve(updated)
                        },
                        onTap = {
                            navController.navigate(NavRoutes.ReserveDetail.createRoute(reserve.reserveId))
                        }
                    )
                }
            }

            // 我的接单
            item {
                Text(
                    text = "我的接单 (${myAccepted.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (myAccepted.isEmpty()) {
                item { EmptyHintCard("暂无已接受的预约") }
            } else {
                items(myAccepted) { reserve ->
                    AcceptedReserveCard(
                        reserve = reserve,
                        onRun = { navController.navigate(NavRoutes.Running.createRoute(reserve.reserveId)) },
                        onComplete = {
                            val updated = reserve.copy(status = 2)
                            viewModel.updateReserve(updated)
                        },
                        onTap = {
                            navController.navigate(NavRoutes.ReserveDetail.createRoute(reserve.reserveId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingReserveCard(reserve: Reserve, onAccept: () -> Unit, onTap: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onTap
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reserve.area,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "时间：${reserve.createTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (reserve.remark.isNotBlank()) {
                        Text(
                            text = "要求：${reserve.remark}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("接单")
                }
            }
        }
    }
}

@Composable
private fun AcceptedReserveCard(reserve: Reserve, onRun: () -> Unit, onComplete: () -> Unit, onTap: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onTap
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reserve.area,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "时间：${reserve.createTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (reserve.status == 1) {
                    Column {
                        Button(
                            onClick = onRun,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("开始跑步")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            )
                        ) {
                            Text("完成")
                        }
                    }
                } else {
                    Text(
                        text = "已完成",
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
