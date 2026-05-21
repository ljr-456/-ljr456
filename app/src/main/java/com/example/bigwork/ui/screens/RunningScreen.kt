package com.example.bigwork.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bigwork.model.RunRecord
import com.example.bigwork.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunningScreen(
    navController: NavController,
    reserveId: String,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val reserve by viewModel.getReserveById(reserveId).collectAsStateWithLifecycle()

    // 跑步状态
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0) }
    var distanceKm by remember { mutableFloatStateOf(0f) }
    var lastLat by remember { mutableStateOf<Double?>(null) }
    var lastLng by remember { mutableStateOf<Double?>(null) }

    // 对话框
    var showStopDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var finalTime by remember { mutableStateOf("") }
    var finalDistance by remember { mutableStateOf("") }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (isRunning && !isPaused) {
                        val lat = location.latitude
                        val lng = location.longitude
                        if (lastLat != null && lastLng != null) {
                            val dist = haversine(lastLat!!, lastLng!!, lat, lng)
                            distanceKm += dist.toFloat()
                        }
                        lastLat = lat
                        lastLng = lng
                    }
                }
            }
        }
    }

    // 计时器
    LaunchedEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                elapsedSeconds++
            }
        }
    }

    // 定位控制
    DisposableEffect(isRunning, isPaused) {
        if (isRunning && !isPaused) {
            startLocationUpdates(fusedClient, locationCallback)
        } else {
            stopLocationUpdates(fusedClient, locationCallback)
        }
        onDispose { stopLocationUpdates(fusedClient, locationCallback) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("跑步中") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isRunning) {
                            finalTime = formatElapsed(elapsedSeconds)
                            finalDistance = "%.2f".format(distanceKm)
                            showStopDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A237E),
                            Color(0xFF0D47A1),
                            Color(0xFF01579B)
                        )
                    )
                )
        ) {
            // 中央信息展示
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 计时器
                Text(
                    text = formatElapsed(elapsedSeconds),
                    fontSize = 56.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 距离
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "%.2f km".format(distanceKm),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // 配速
                if (distanceKm > 0.01f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val pace = (elapsedSeconds / 60f) / distanceKm
                    Text(
                        text = "配速 %.1f min/km".format(pace),
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                if (isRunning) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isPaused) "已暂停" else "GPS定位中...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // 底部控制按钮
            if (isRunning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 暂停/继续
                    Button(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier
                            .width(80.dp)
                            .height(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    ) {
                        Icon(
                            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(0.45f),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(40.dp))

                    // 停止
                    Button(
                        onClick = {
                            finalTime = formatElapsed(elapsedSeconds)
                            finalDistance = "%.2f".format(distanceKm)
                            showStopDialog = true
                        },
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(0.4f),
                            tint = Color.White
                        )
                    }
                }
            } else {
                // 开始跑步
                Button(
                    onClick = { isRunning = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("开 始 跑 步", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // 停止确认
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("结束跑步", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("跑步时间：$finalTime")
                    Text("跑步距离：${finalDistance} km")
                    Text("确定要结束本次跑步吗？")
                }
            },
            confirmButton = {
                Button(onClick = {
                    isRunning = false
                    isPaused = false
                    showStopDialog = false
                    showCompleteDialog = true
                }) {
                    Text("确认结束")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("继续跑步")
                }
            }
        )
    }

    // 保存记录
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showCompleteDialog = false
                navController.popBackStack()
            },
            title = { Text("跑步完成", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("总时间：$finalTime")
                    Text("总距离：${finalDistance} km")
                    Text("记录已保存！")
                }
            },
            confirmButton = {
                Button(onClick = {
                    reserve?.let { r ->
                        val record = RunRecord(
                            recordId = UUID.randomUUID().toString(),
                            reserveId = r.reserveId,
                            blindUserId = r.blindUserId,
                            volunteerUserId = r.volunteerUserId ?: "",
                            area = r.area,
                            duration = elapsedSeconds / 3600f,
                            createTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                        viewModel.saveRunRecord(record)
                        viewModel.updateReserve(r.copy(status = 2))
                    }
                    showCompleteDialog = false
                    stopLocationUpdates(fusedClient, locationCallback)
                    navController.popBackStack()
                }) {
                    Text("确定")
                }
            }
        )
    }
}

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

private fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c / 1000.0
}

private fun startLocationUpdates(client: FusedLocationProviderClient, callback: LocationCallback) {
    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
        .setMinUpdateIntervalMillis(1000)
        .build()
    try {
        client.requestLocationUpdates(request, callback, null)
    } catch (_: SecurityException) {}
}

private fun stopLocationUpdates(client: FusedLocationProviderClient, callback: LocationCallback) {
    client.removeLocationUpdates(callback)
}
