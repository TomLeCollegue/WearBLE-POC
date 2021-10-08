package com.test.wearble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class GattServerCallback(
    private val gattServer: BluetoothGattServer?
) : BluetoothGattServerCallback() {

    private val _receivedMessage = MutableLiveData<String?>()
    val message: LiveData<String?> = _receivedMessage

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        super.onConnectionStateChange(device, status, newState)
        val isSuccess = status == BluetoothGatt.GATT_SUCCESS
        val isConnected = newState == BluetoothProfile.STATE_CONNECTED
        Log.d(
            "observe",
            "onConnectionStateChange: Server $device ${device?.name} success: $isSuccess connected: $isConnected"
        )
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
        if (characteristic.uuid == BleServer.MESSAGE_UUID) {
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            val message = value?.toString(Charsets.UTF_8)
            message?.let {
                _receivedMessage.postValue(it)
                Log.d("observe", it)
            }
        }
    }

    override fun onCharacteristicReadRequest(device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
        if (characteristic?.uuid == BleServer.MESSAGE_UUID) {
            Log.d("observe", "readRequest")
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, BleServer.QR_TEST.toByteArray(Charsets.UTF_8))
        }
    }

}