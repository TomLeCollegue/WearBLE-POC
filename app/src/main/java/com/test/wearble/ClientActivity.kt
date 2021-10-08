package com.test.wearble

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.test.wearble.databinding.ActivityWatchBinding
import java.util.UUID

class ClientActivity : AppCompatActivity() {

    private val sharedPreferences by lazy {
        getPreferences(Context.MODE_PRIVATE)
    }

    private lateinit var binding: ActivityWatchBinding
    private lateinit var adapter: BluetoothAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences.getString(DCC_SHARED_PREF, "")?.let {
            if (it != "") {
                displayDcc(it)
            } else {
                hideDcc()
            }
        }
        checkLocationPermission()
    }

    private fun displayDcc(valueDcc: String) {

        val barcodeEncoder = BarcodeEncoder()
        binding.apply {
            barcodeImageView.setImageBitmap(
                barcodeEncoder.encodeBitmap(
                    valueDcc,
                    BarcodeFormat.QR_CODE,
                    400,
                    400
                )
            )
            barcodeLayout.isVisible = true
            noPassLayout.isVisible = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initBeaconListener() {
        val builder = ScanFilter.Builder()
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val filter = builder.build()
        val filters = listOf(filter)
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

        val bluetoothManager: BluetoothManager = application.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        val intent = Intent(this, BroadCastReceiverBLE::class.java)
        intent.putExtra("o-scan", true)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        adapter.bluetoothLeScanner.startScan(filters, settings, pendingIntent)
    }

    private fun hideDcc() {
        binding.apply {
            barcodeImageView.setImageBitmap(null)
            barcodeLayout.isVisible = false
            noPassLayout.isVisible = true
        }
    }

    private fun scanForDevices() {
        Log.d("observe", "scanDevices")
        adapter = BluetoothAdapter.getDefaultAdapter()
        val builder = ScanFilter.Builder()
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val filter = builder.build()
        val filters = listOf(filter)
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        adapter.bluetoothLeScanner.startScan(filters, settings, callback)
    }

    private val callback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d("observe", "BatchResults")
            results?.forEach { result ->
                if (adapter.bondedDevices.any { it.address == result.device.address }) {
                    result.device.connectGatt(this@ClientActivity, false, gattCallBack)
                }
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("observe", "ScanResult")
            result?.device?.connectGatt(this@ClientActivity, false, gattCallBack)
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
                val characteristic = discoveredGatt.getService(SERVICE_UUID).getCharacteristic(MESSAGE_UUID)
                characteristic.value = BleServer.QR_TEST.toByteArray(Charsets.UTF_8)
                discoveredGatt.writeCharacteristic(characteristic)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkLocationPermission() {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            Log.d("observe", "HasPermission")
            // scanForDevices()
            initBeaconListener()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    companion object {

        val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
        val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
        val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

        const val DCC_SHARED_PREF: String = "shared_pref_dcc"

    }
}