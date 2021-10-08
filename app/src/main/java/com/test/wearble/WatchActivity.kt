package com.test.wearble

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.test.wearble.databinding.ActivityWatchBinding

class WatchActivity : AppCompatActivity() {

    var server: BleServer? = null

    private val sharedPreferences by lazy {
        getPreferences(Context.MODE_PRIVATE)
    }

    private lateinit var binding: ActivityWatchBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*sharedPreferences.getString(ClientActivity.DCC_SHARED_PREF, "")?.let {
            if (it != "") {
                displayDcc(it)
            } else {
                hideDcc()
            }
        }*/
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

    private fun hideDcc() {
        binding.apply {
            barcodeImageView.setImageBitmap(null)
            barcodeLayout.isVisible = false
            noPassLayout.isVisible = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkLocationPermission() {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            Log.d("observe", "HasPermission")
            startChatServer()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    fun manageDCC(result: String) {
        /*sharedPreferences.edit {
            putString(DCC_SHARED_PREF, result)
        }*/
        if (result != "") {
            displayDcc(result)
        } else {
            hideDcc()
        }
    }

    private fun startChatServer() {

        server = BleServer(application)
        server?.startServer()

        server?.message?.observe(this) {

            Log.d("observe", "message")
            if (it != null) {
                manageDCC(it)
            }
        }

    }
}
