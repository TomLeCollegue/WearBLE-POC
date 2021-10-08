package com.test.wearble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class BroadCastReceiverBLE : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val bleCallbackType: Int? = intent?.getIntExtra(BluetoothLeScanner.EXTRA_CALLBACK_TYPE, -1)
        if (bleCallbackType != -1) {
            Log.d("observe", "Passive background scan callback type: $bleCallbackType")
            val scanResults: ArrayList<ScanResult>? = intent?.getParcelableArrayListExtra(
                BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT
            )
            scanResults?.let { results ->

                val adapter = BluetoothAdapter.getDefaultAdapter()
                results.forEach { result ->
                    if (adapter.bondedDevices.any { it.address == result.device.address }) {
                        result.device.connectGatt(context, false, gattCallBack)
                    }
                }
            }
            // Do something with your ScanResult list here.
            // These contain the data of your matching BLE advertising packets
        }
    }

    private val gattCallBack = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d("observe", "onConnectionStateChange: Client $gatt  success: $isSuccess connected: $isConnected")
            // try to send a message to the other device as a test
            if (isSuccess && isConnected) {
                // discover services
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("observe", "onServicesDiscovered: Have gatt $discoveredGatt")
                val characteristic = discoveredGatt.getService(ClientActivity.SERVICE_UUID).getCharacteristic(ClientActivity.MESSAGE_UUID)
                characteristic.value = BleServer.QR_TEST.toByteArray(Charsets.UTF_8)
                discoveredGatt.writeCharacteristic(characteristic)
            }
        }
    }
}