package com.test.wearble

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.util.Log

class AdvertiseCallBackImpl : AdvertiseCallback() {
    override fun onStartFailure(errorCode: Int) {
        super.onStartFailure(errorCode)
        // Send error state to display
        val errorMessage = "Advertise failed with error: $errorCode"
        Log.d("observe", "Advertising failed")
    }

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
        super.onStartSuccess(settingsInEffect)
        Log.d("observe", "Advertising successfully started")
    }
}