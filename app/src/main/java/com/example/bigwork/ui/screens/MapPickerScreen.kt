package com.example.bigwork.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult
import com.example.bigwork.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    navController: NavController
) {
    val viewModel: MainViewModel = viewModel(LocalContext.current as ComponentActivity)
    val context = LocalContext.current

    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    // Manual input fields
    var manualAddress by remember { mutableStateOf("") }
    var manualLat by remember { mutableStateOf("") }
    var manualLng by remember { mutableStateOf("") }
    var manualError by remember { mutableStateOf<String?>(null) }

    // GeoCoder instance
    val geoCoder = remember { GeoCoder.newInstance() }

    // 定位蓝点图标（在 MapView 创建之前生成，供 MyLocationConfiguration 使用）
    val locationDotIcon = remember {
        val size = 48
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF_4A_90_D9.toInt() }
        c.drawCircle(size / 2f, size / 2f, size / 2f - 4, p)
        p.color = 0xFF_FF_FF_FF.toInt()
        c.drawCircle(size / 2f, size / 2f, 10f, p)
        BitmapDescriptorFactory.fromBitmap(bmp)
    }

    // MapView — 在 remember 中同步设置定位，确保在 onResume 之前生效
    val mapView = remember {
        MapView(context).also { mv ->
            mv.map.apply {
                uiSettings.isZoomGesturesEnabled = true
                uiSettings.isScrollGesturesEnabled = true
                setMapStatus(MapStatusUpdateFactory.newLatLngZoom(LatLng(23.0207, 113.7518), 14f))
                // 定位必须在 onResume 之前启用，否则定位层不会初始化
                isMyLocationEnabled = true
                setMyLocationConfiguration(
                    MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.NORMAL,
                        true,
                        locationDotIcon
                    )
                )
            }
        }
    }

    // === 百度定位客户端 — 显式请求定位数据，传递给地图 ===
    var firstLocationReceived by remember { mutableStateOf(false) }
    val locationClient = remember {
        LocationClient(context).apply {
            locOption = LocationClientOption().apply {
                locationMode = LocationClientOption.LocationMode.Hight_Accuracy
                isOpenGps = true
                coorType = "bd09ll"
                scanSpan = 2000
                isNeedAddress = false
                isOnceLocation = false
            }
        }
    }
    val locationListener = remember {
        object : BDAbstractLocationListener() {
            override fun onReceiveLocation(loc: BDLocation?) {
                loc ?: return
                if (loc.locType == BDLocation.TypeGpsLocation
                    || loc.locType == BDLocation.TypeNetWorkLocation
                    || loc.locType == BDLocation.TypeOffLineLocation
                ) {
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    mapView.map.setMyLocationData(
                        MyLocationData.Builder()
                            .latitude(loc.latitude)
                            .longitude(loc.longitude)
                            .direction(loc.direction)
                            .accuracy(loc.radius)
                            .build()
                    )
                    if (!firstLocationReceived) {
                        firstLocationReceived = true
                        mapView.map.animateMapStatus(
                            MapStatusUpdateFactory.newLatLngZoom(latLng, 16f)
                        )
                    }
                }
            }
        }
    }

    // 定位权限请求
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            mapView.map.isMyLocationEnabled = true
            locationClient.start()
        }
    }

    // Reverse geocode setup
    DisposableEffect(Unit) {
        geoCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
            override fun onGetGeoCodeResult(result: com.baidu.mapapi.search.geocode.GeoCodeResult?) {}
            override fun onGetReverseGeoCodeResult(result: ReverseGeoCodeResult?) {
                val addr = result?.address ?: "未知地址"
                selectedAddress = addr
            }
        })
        onDispose { geoCoder.destroy() }
    }

    // 用代码生成 marker 位图，避免矢量图兼容问题
    val markerIcon = remember {
        val w = 72; val h = 96
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF_E5_39_35.toInt() }
        c.drawCircle(w / 2f, 30f, 28f, p)
        c.drawRect(w / 4f, 50f, w * 3 / 4f, 52f, p)
        BitmapDescriptorFactory.fromBitmap(bmp)
    }

    // Map click listener
    LaunchedEffect(Unit) {
        mapView.map.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                try {
                    selectedLatLng = latLng
                    selectedAddress = "正在获取地址..."
                    val bm = mapView.map
                    bm.clear()
                    bm.addOverlay(
                        MarkerOptions()
                            .position(latLng)
                            .icon(markerIcon)
                    )
                    geoCoder.reverseGeoCode(ReverseGeoCodeOption().location(latLng))
                } catch (_: Exception) {
                    selectedAddress = "选点失败，请重试"
                }
            }

            override fun onMapPoiClick(poi: com.baidu.mapapi.map.MapPoi?) {}
        })
    }

    // 请求定位权限（仅处理未授权场景，已授权时定位在 remember 中已启用）
    LaunchedEffect(Unit) {
        val fineOk = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineOk || !coarseOk) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Lifecycle management — onResume 启动地图渲染和定位客户端
    DisposableEffect(mapView) {
        mapView.onResume()
        locationClient.registerLocationListener(locationListener)
        // 权限已授权时启动定位客户端，未授权时等待权限弹窗回调再启动
        val fineOk = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineOk) {
            locationClient.start()
        }
        onDispose {
            locationClient.stop()
            locationClient.unRegisterLocationListener(locationListener)
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择地点", fontWeight = FontWeight.Bold) },
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
        ) {
            // === 地图区域 ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                )

                // 提示文字（未选点时）
                if (selectedLatLng == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            "点击地图选择地点",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // 手动输入切换按钮
                IconButton(
                    onClick = { showManualInput = !showManualInput },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "手动输入坐标",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // === 底部面板 ===
            if (showManualInput) {
                // 手动输入模式
                ManualInputPanel(
                    address = manualAddress,
                    onAddressChange = { manualAddress = it },
                    lat = manualLat,
                    onLatChange = { manualLat = it },
                    lng = manualLng,
                    onLngChange = { manualLng = it },
                    error = manualError,
                    onConfirm = {
                        when {
                            manualAddress.isBlank() -> manualError = "请填写地点名称"
                            manualLat.isBlank() || manualLng.isBlank() -> manualError = "请填写坐标"
                            manualLat.toDoubleOrNull() == null || manualLng.toDoubleOrNull() == null ->
                                manualError = "坐标格式不正确"
                            else -> {
                                viewModel.setSelectedMapLocation(
                                    address = manualAddress,
                                    latitude = manualLat.toDouble(),
                                    longitude = manualLng.toDouble()
                                )
                                navController.popBackStack()
                            }
                        }
                    }
                )
            } else {
                // 地图选点结果面板
                SelectionPanel(
                    latLng = selectedLatLng,
                    address = selectedAddress,
                    onConfirm = {
                        selectedLatLng?.let { ll ->
                            viewModel.setSelectedMapLocation(
                                address = selectedAddress,
                                latitude = ll.latitude,
                                longitude = ll.longitude
                            )
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectionPanel(
    latLng: LatLng?,
    address: String,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (latLng != null) {
                Text("已选位置", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("确认选择此地点")
                }
            } else {
                Text(
                    "请在地图上点击选择地点",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ManualInputPanel(
    address: String,
    onAddressChange: (String) -> Unit,
    lat: String,
    onLatChange: (String) -> Unit,
    lng: String,
    onLngChange: (String) -> Unit,
    error: String?,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("手动输入坐标", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("地点名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = lat,
                    onValueChange = onLatChange,
                    label = { Text("纬度") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = lng,
                    onValueChange = onLngChange,
                    label = { Text("经度") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            if (error != null) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("确认选择")
            }
        }
    }
}
