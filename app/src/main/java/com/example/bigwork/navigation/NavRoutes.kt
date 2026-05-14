package com.example.bigwork.navigation

// 定义所有页面路由
sealed class NavRoutes(val route: String) {
    // 主界面
    object Main : NavRoutes("main")
    // 用户详情页，接收userId参数
    object UserDetail : NavRoutes("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
    // 预约详情页，接收reserveId参数
    object ReserveDetail : NavRoutes("reserve_detail/{reserveId}") {
        fun createRoute(reserveId: String) = "reserve_detail/$reserveId"
    }
}