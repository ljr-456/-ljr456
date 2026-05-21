package com.example.bigwork.navigation

// 定义所有页面路由
sealed class NavRoutes(val route: String) {
    // 登录/注册
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")

    // 盲人端主页
    object BlindHome : NavRoutes("blind_home/{userId}") {
        fun createRoute(userId: String) = "blind_home/$userId"
    }
    // 志愿者端主页
    object VolunteerHome : NavRoutes("volunteer_home/{userId}") {
        fun createRoute(userId: String) = "volunteer_home/$userId"
    }

    // 用户详情页
    object UserDetail : NavRoutes("user_detail/{userId}") {
        fun createRoute(userId: String) = "user_detail/$userId"
    }
    // 预约详情页
    object ReserveDetail : NavRoutes("reserve_detail/{reserveId}") {
        fun createRoute(reserveId: String) = "reserve_detail/$reserveId"
    }

    // 创建预约页（盲人端）
    object CreateReserve : NavRoutes("create_reserve/{userId}") {
        fun createRoute(userId: String) = "create_reserve/$userId"
    }

    // 地图选点页
    object MapPicker : NavRoutes("map_picker")

    // 跑步页（志愿者端）
    object Running : NavRoutes("running/{reserveId}") {
        fun createRoute(reserveId: String) = "running/$reserveId"
    }
}