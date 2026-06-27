package com.example.bigwork.location

import android.content.Context
import android.util.Log
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object BaiduLocationManager {

    private const val TAG = "BaiduLocationManager"
    private const val MIN_ACCURACY_METERS = 100f

    private var appContext: Context? = null
    private var locationClient: LocationClient? = null

    private val _currentLocation = MutableStateFlow<BDLocation?>(null)
    val currentLocation: StateFlow<BDLocation?> = _currentLocation.asStateFlow()

    private val _locationFlow = MutableSharedFlow<BDLocation>(replay = 1, extraBufferCapacity = 4)
    val locationFlow: SharedFlow<BDLocation> = _locationFlow.asSharedFlow()

    private val _fixQuality = MutableStateFlow(FixQuality.NO_FIX)
    val fixQuality: StateFlow<FixQuality> = _fixQuality.asStateFlow()

    enum class FixQuality { NO_FIX, NETWORK_FIX, GPS_FIX }

    fun init(context: Context) {
        appContext = context.applicationContext
        LocationClient.setAgreePrivacy(true)
    }

    @Synchronized
    private fun ensureClient(): LocationClient? {
        if (locationClient != null) return locationClient
        val ctx = appContext ?: return null
        return try {
            LocationClient(ctx).apply {
                locOption = LocationClientOption().apply {
                    locationMode = LocationClientOption.LocationMode.Hight_Accuracy
                    coorType = "bd09ll"
                    setIsNeedAddress(true)
                    scanSpan = 2000
                    setIsNeedLocationDescribe(true)
                    setNeedNewVersionRgc(true)
                    setWifiCacheTimeOut(5 * 60 * 1000)
                }
                registerLocationListener(object : BDAbstractLocationListener() {
                    override fun onReceiveLocation(location: BDLocation?) {
                        if (location == null) return
                        if (location.latitude == 0.0 && location.longitude == 0.0) return

                        val accuracy = location.radius
                        val type = location.locType

                        val isGps = type == BDLocation.TypeGpsLocation ||
                                type == BDLocation.TypeOffLineLocation
                        val isNetwork = type == BDLocation.TypeNetWorkLocation ||
                                type == BDLocation.TypeCacheLocation

                        Log.d(TAG, "loc type=$type accuracy=$accuracy lat=${location.latitude} lng=${location.longitude}")

                        if (isGps) {
                            _fixQuality.value = FixQuality.GPS_FIX
                            _currentLocation.value = location
                            _locationFlow.tryEmit(location)
                        } else if (isNetwork && accuracy < MIN_ACCURACY_METERS) {
                            if (_fixQuality.value != FixQuality.GPS_FIX) {
                                _fixQuality.value = FixQuality.NETWORK_FIX
                            }
                            _currentLocation.value = location
                            _locationFlow.tryEmit(location)
                        } else if (isNetwork) {
                            Log.d(TAG, "network fix discarded, accuracy=$accuracy > threshold=$MIN_ACCURACY_METERS")
                            if (_fixQuality.value == FixQuality.NO_FIX) {
                                _currentLocation.value = location
                                _locationFlow.tryEmit(location)
                                _fixQuality.value = FixQuality.NETWORK_FIX
                            }
                        }
                    }
                })
            }.also { locationClient = it }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create LocationClient", e)
            null
        }
    }

    private var startCount = 0

    fun start() {
        val client = ensureClient() ?: return
        if (startCount == 0) {
            client.start()
        }
        startCount++
    }

    fun stop() {
        if (startCount > 0) {
            startCount--
            if (startCount == 0) {
                locationClient?.stop()
            }
        }
    }
}
