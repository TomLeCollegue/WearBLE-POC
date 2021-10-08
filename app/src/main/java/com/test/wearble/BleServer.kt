package com.test.wearble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import java.util.UUID

class BleServer(
    private val context: Application
) {

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var bluetoothManager: BluetoothManager
    private var gattServerCallback: GattServerCallback? = null
    private var gattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()
    private var advertiseData: AdvertiseData = buildAdvertiseData()

    lateinit var message: LiveData<String?>

    fun startServer() {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        setupGATTServer()
        startAdvertisement()
        message = gattServerCallback!!.message
    }

    private fun setupGATTServer() {
        gattServerCallback = GattServerCallback(gattServer)
        gattServer = bluetoothManager.openGattServer(
            context,
            gattServerCallback
        ).apply {
            addService(setupGattService())
        }
    }

    private fun setupGattService(): BluetoothGattService {
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val messageCharacteristic = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)
        val confirmCharacteristic = BluetoothGattCharacteristic(
            CONFIRM_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(confirmCharacteristic)

        return service
    }

    private fun startAdvertisement() {
        advertiser = adapter.bluetoothLeAdvertiser
        if (advertiseCallback == null) {
            advertiseCallback = AdvertiseCallBackImpl()

            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
    }

    private fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
        advertiseCallback = null
    }

    private fun buildAdvertiseData(): AdvertiseData {
        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(true)
        return dataBuilder.build()
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .build()
    }

    companion object {

        val QR_TEST: String = "HC1:6BFOXN%TSMAHN-HLN4BMPCRLQAT *MY72I:IR0L8YV8VVC89B\$NA IM*4M7DB6F NI4EFSYS:%OD3PYE9*FJ9QMQC8\$.AIGCY0K5\$0V-AVB82\$33B4R9US:H85OS1OOPPYE97NVA.D9B9+HF9B9LW4G%89-8CNNL2JI 0VD9%.OMRE/IE%TE6UG+ZE V1+GO9+PGF6Z6NC8P\$WA3AAPEPBDSM+Q9I6O671D77Q4UYQD*O%+QUOQWEL9YLY1S7HOPC5NDO4A7E:7LYPWOQ9AD KMIFGSKE MCAOI8%MJ H.10W2DH2CAAOC.UIR6Y18:347CP8%MMG2V9E6LF+71MZNQ+MN/QP9QE8QTSON8V68HZ\$J36E*UFLME*L5TQF62I*SMPEFABW1%6\$6S9 CK7S7AF RM\$GAO-6LB9*9QRZ58CO9X5:NQQ5AY7S7395%SHPC2JDCY6KFUS50ACJK2"

        val QR = "http://google.fr"
        val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
        val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
        val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

    }
}