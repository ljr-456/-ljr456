# 助盲跑预约平台 (BigWork)

基于 Kotlin Multi-Module 架构的助盲跑预约平台，包含 Android 客户端（Jetpack Compose）和 Ktor 后端服务，为视障人士和志愿者提供跑步陪跑预约、实时位置追踪等功能。

## 核心功能
- **双端模式**：盲人端 / 志愿者端
- **需求发布与接单匹配**：盲人发布跑步需求，志愿者浏览并接单
- **GPS 实时追踪**：跑步过程中 GPS 定位（2 秒采样），Haversine 距离计算
- **运动数据留存**：时长、距离等数据存储与查看

## 运行环境要求

| 项目 | 最低要求 |
|------|----------|
| **Android 系统版本** | Android 7.0 (API 24) 及以上 |
| **JDK** | JDK 17 或更高 |
| **Gradle** | Wrapper 自动下载（AGP 9.1.0 + Kotlin 2.2.10） |
| **Android Studio** | 推荐 Hedgehog (2023.1) 及以上 |
| **内存** | 建议 8GB 以上（构建时 Gradle 守护进程需 4GB，Kotlin 编译器守护进程需 3GB） |

> **注意**：Gradle 和 Kotlin 编译器守护进程均需较大内存，已在 `gradle.properties` 中配置 `-Xmx4096m`（Gradle）和 `-Xmx3072m`（Kotlin Daemon）。

## 项目目录结构

```
bigWork/
├── app/                                   # Android 客户端模块
│   └── src/main/
│       ├── java/com/example/bigwork/
│       │   ├── BigWorkApp.kt              # Application 入口（初始化百度 SDK）
│       │   ├── MainActivity.kt            # 单 Activity 入口，NavHost 路由
│       │   ├── dao/                       # Room DAO 层
│       │   │   ├── UserDao.kt             # 用户 CRUD + 登录 + 按类型查询
│       │   │   ├── ReserveDao.kt          # 预约 CRUD + 状态筛选（Flow）
│       │   │   └── RunRecordDao.kt        # 跑步记录 CRUD
│       │   ├── database/                  # Room 数据库
│       │   │   └── AppDatabase.kt         # 数据库单例（3 实体，离线优先）
│       │   ├── location/                  # GPS 定位服务
│       │   │   └── LocationHelper.kt      # FusedLocationProviderClient 封装
│       │   ├── model/                     # 数据模型
│       │   │   ├── User.kt                # 用户实体（userType: 0=盲人/1=志愿者）
│       │   │   ├── Reserve.kt             # 预约实体（status: 待接单/已接单/已完成/已取消）
│       │   │   └── RunRecord.kt           # 跑步记录实体
│       │   ├── navigation/                # 导航路由
│       │   │   └── NavRoutes.kt           # Sealed class 路由定义
│       │   ├── network/                   # 网络层
│       │   │   ├── ApiService.kt          # Retrofit 接口定义
│       │   │   └── RetrofitClient.kt      # Retrofit 单例（Gson + OkHttp）
│       │   ├── repository/                # 数据仓库
│       │   │   └── AppRepository.kt       # API 优先 + Room 兜底
│       │   ├── viewmodel/                 # ViewModel 层
│       │   │   └── MainViewModel.kt       # MVVM ViewModel（Flow → StateFlow）
│       │   └── ui/
│       │       ├── theme/                 # Material 3 主题
│       │       │   ├── Color.kt
│       │       │   ├── Theme.kt
│       │       │   └── Type.kt
│       │       └── screens/               # Compose 页面
│       │           ├── LoginScreen.kt            # 登录页
│       │           ├── RegisterScreen.kt         # 注册页
│       │           ├── BlindHomeScreen.kt        # 盲人首页（创建/查看预约）
│       │           ├── VolunteerHomeScreen.kt    # 志愿者首页（接单/管理）
│       │           ├── CreateReserveScreen.kt    # 创建预约（时间/地点/备注）
│       │           ├── MapPickerScreen.kt        # 地图选点
│       │           ├── ReserveDetailScreen.kt    # 预约详情
│       │           ├── UserDetailScreen.kt       # 用户信息
│       │           └── RunningScreen.kt          # 跑步追踪（GPS + 计时）
│       ├── res/                           # 资源文件
│       └── AndroidManifest.xml            # 应用清单（权限、百度地图 Key）
├── server/                                # Ktor 后端服务模块
│   └── src/main/
│       ├── kotlin/com/example/server/
│       │   ├── Application.kt             # 服务入口（Netty 引擎，端口 8080）
│       │   ├── model/                     # 数据模型
│       │   │   ├── User.kt
│       │   │   ├── Reserve.kt
│       │   │   └── RunRecord.kt
│       │   ├── repository/                # 数据库初始化与 DAO
│       │   │   └── DatabaseFactory.kt     # H2 嵌入式数据库
│       │   └── routes/                    # REST API 路由
│       │       ├── UserRoutes.kt
│       │       ├── ReserveRoutes.kt
│       │       └── RunRecordRoutes.kt
│       └── resources/
│           └── logback.xml                # 日志配置
├── gradle/
│   ├── libs.versions.toml                 # 版本目录（统一依赖版本管理）
│   └── wrapper/                           # Gradle Wrapper
├── gradle.properties                      # Gradle 全局配置（JVM 内存、编码等）
├── settings.gradle.kts                    # 项目设置（模块注册）
├── build.gradle.kts                       # 根构建脚本（插件声明）
└── local.properties                       # 本地 SDK 路径（不纳入版本控制）
```

## 技术栈

| 层 | 技术 |
|----|------|
| **语言** | Kotlin 2.2.10 |
| **UI** | Jetpack Compose + Material 3（动态取色，Android 12+） |
| **数据库** | Room 2.7.2（离线优先，fallbackToDestructiveMigration） |
| **网络** | Retrofit 2.11.0 + Gson + OkHttp 日志拦截器 |
| **导航** | Navigation Compose 2.8.4（单 Activity 架构） |
| **架构** | MVVM（Model → DAO → Repository → ViewModel → Composable） |
| **定位** | Google Play Services FusedLocationProviderClient |
| **地图** | 百度地图 SDK |
| **后端** | Ktor 2.3.12（Netty 引擎）+ H2 嵌入式数据库 |

## 快速开始

```bash
# 1. 配置 local.properties（指向本机 Android SDK 路径）
#    编辑 local.properties 文件，设置 sdk.dir=YOUR_SDK_PATH

# 2. 启动后端服务器（默认端口 8080）
./gradlew :server:run

# 3. 构建并安装 Android 客户端
./gradlew :app:assembleDebug
```
