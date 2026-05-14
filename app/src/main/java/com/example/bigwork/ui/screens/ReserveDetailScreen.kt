package com.example.bigwork.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bigwork.model.Reserve
import com.example.bigwork.viewmodel.MainViewModel
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveDetailScreen(
    navController: NavController,
    reserveId: String,
    viewModel: MainViewModel = viewModel()
) {
    val pendingReserves by viewModel.pendingReserves.collectAsStateWithLifecycle()
    var reserve by remember { mutableStateOf<Reserve?>(null) }

    // 根据reserveId查找对应的预约
    LaunchedEffect(reserveId) {
        reserve = pendingReserves.find { it.reserveId == reserveId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预约详情") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            reserve?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "预约信息",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        DetailItem(label = "预约ID", value = it.reserveId)
                        DetailItem(label = "预约地点", value = it.area)
                        DetailItem(label = "预约时间", value = it.createTime)
                        DetailItem(label = "陪跑要求", value = it.remark)
                        DetailItem(label = "预约状态", value = if (it.status == 0) "待接单" else "已接单")
                        DetailItem(label = "志愿者ID", value = it.volunteerUserId ?: "暂未分配")
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