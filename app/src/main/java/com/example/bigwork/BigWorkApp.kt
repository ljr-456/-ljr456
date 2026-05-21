package com.example.bigwork

import android.app.Application
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.CoordType

class BigWorkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.GCJ02)
    }
}
