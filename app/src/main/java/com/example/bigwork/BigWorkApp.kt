package com.example.bigwork

import android.app.Application
import com.baidu.mapapi.SDKInitializer

class BigWorkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
    }
}
