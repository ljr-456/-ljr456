package com.example.bigwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.ui.theme.BigWorkTheme
import com.example.bigwork.viewmodel.MainViewModel
import java.util.UUID
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bigwork.navigation.NavRoutes
import com.example.bigwork.ui.screens.UserDetailScreen
import com.example.bigwork.ui.screens.ReserveDetailScreen
import com.example.bigwork.ui.screens.LoginScreen
import com.example.bigwork.ui.screens.RegisterScreen
import com.example.bigwork.ui.screens.BlindHomeScreen
import com.example.bigwork.ui.screens.VolunteerHomeScreen
import com.example.bigwork.ui.screens.CreateReserveScreen
import com.example.bigwork.ui.screens.MapPickerScreen
import com.example.bigwork.ui.screens.RunningScreen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BigWorkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 修复1: 调用MainScreen而不是HomeScreen，MainScreen包含导航宿主
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    // 创建导航控制器
    val navController = rememberNavController()

    // 导航宿主，管理所有页面
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {
        // 登录页
        composable(NavRoutes.Login.route) {
            LoginScreen(navController = navController)
        }

        // 注册页
        composable(NavRoutes.Register.route) {
            RegisterScreen(navController = navController)
        }

        // 盲人端主页
        composable(NavRoutes.BlindHome.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            BlindHomeScreen(navController = navController, userId = userId)
        }

        // 志愿者端主页
        composable(NavRoutes.VolunteerHome.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            VolunteerHomeScreen(navController = navController, userId = userId)
        }

        // 用户详情页
        composable(NavRoutes.UserDetail.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserDetailScreen(navController = navController, userId = userId)
        }

        // 预约详情页
        composable(NavRoutes.ReserveDetail.route) { backStackEntry ->
            val reserveId = backStackEntry.arguments?.getString("reserveId") ?: ""
            ReserveDetailScreen(navController = navController, reserveId = reserveId)
        }

        // 创建预约页（盲人端）
        composable(NavRoutes.CreateReserve.route) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            CreateReserveScreen(navController = navController, userId = userId)
        }

        // 地图选点页
        composable(NavRoutes.MapPicker.route) {
            MapPickerScreen(navController = navController)
        }

        // 跑步页
        composable(NavRoutes.Running.route) { backStackEntry ->
            val reserveId = backStackEntry.arguments?.getString("reserveId") ?: ""
            RunningScreen(navController = navController, reserveId = reserveId)
        }
    }
}

// 修复2: 为HomeScreen添加navController参数
// MainActivity.kt（主界面数据绑定版）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    // 收集ViewModel状态（生命周期安全）
    val blindUsers by viewModel.blindUsers.collectAsStateWithLifecycle()
    val volunteers by viewModel.volunteers.collectAsStateWithLifecycle()
    val pendingReserves by viewModel.pendingReserves.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("助盲跑预约平台") },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { innerPadding : PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 加载状态
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 错误提示
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(message)
                }
            }

            // 数据列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 操作按钮区
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("创建用户")
                        }

                        Button(
                            onClick = {
                                val newReserve = Reserve(
                                    reserveId = UUID.randomUUID().toString(),
                                    blindUserId = "blind001",
                                    volunteerUserId = null,
                                    area = "东莞体育中心",
                                    remark = "需要陪跑1小时，速度慢一些",
                                    status = 0,
                                    createTime = "2026-05-15 ${(8..18).random()}:00:00"
                                )
                                viewModel.createReserve(newReserve)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("创建预约")
                        }

                        Button(
                            onClick = { viewModel.clearAllData() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("清空数据")
                        }
                    }
                }

                // 盲人用户列表
                item {
                    Text(
                        text = "盲人用户 (${blindUsers.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (blindUsers.isEmpty()) {
                    item { EmptyStateCard(text = "暂无盲人用户") }
                } else {
                    items(blindUsers) { user ->
                        UserCard(user = user, navController = navController)
                    }
                }

                // 志愿者用户列表
                item {
                    Text(
                        text = "志愿者用户 (${volunteers.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (volunteers.isEmpty()) {
                    item { EmptyStateCard(text = "暂无志愿者用户") }
                } else {
                    items(volunteers) { user ->
                        UserCard(user = user, navController = navController)
                    }
                }

                // 待接单预约列表
                item {
                    Text(
                        text = "待接单预约 (${pendingReserves.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (pendingReserves.isEmpty()) {
                    item { EmptyStateCard(text = "暂无待接单预约") }
                } else {
                    items(pendingReserves) { reserve ->
                        ReserveCard(reserve = reserve, navController = navController)
                    }
                }
            }
        }
    }
}

// 用户卡片组件（添加点击跳转）
@Composable
fun UserCard(user: User, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { // 添加点击事件
                navController.navigate(NavRoutes.UserDetail.createRoute(user.userId))
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // 原有卡片内容保持不变
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = user.userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "年龄：${user.age}岁 | 性别：${user.gender}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "电话：${user.phone ?: "未填写"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// 预约卡片组件（添加点击跳转）
@Composable
fun ReserveCard(reserve: Reserve, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { // 添加点击事件
                navController.navigate(NavRoutes.ReserveDetail.createRoute(reserve.reserveId))
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        // 原有卡片内容保持不变
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "地点：${reserve.area}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "时间：${reserve.createTime}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "要求：${reserve.remark}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// 空状态卡片组件
@Composable
fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.2f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}