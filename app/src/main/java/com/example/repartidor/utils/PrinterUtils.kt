package com.example.repartidor.utils

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import com.example.repartidor.data.remote.BluetoothState
import androidx.core.content.edit

fun guardarImpresora(context: Context, device: BluetoothDevice) {
    val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    prefs.edit { putString("printer_address", device.address) }
}

fun obtenerDispositivoGuardado(context: Context): BluetoothDevice? {

    val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    val savedAddress = prefs.getString("printer_address", null)

    val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val bluetoothAdapter = bluetoothManager.adapter

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return null
    }

    return bluetoothAdapter?.bondedDevices?.find {
        it.address == savedAddress
    }
}

fun getBluetoothState(context: Context, activity: Activity): BluetoothState {

    val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val adapter = bluetoothManager.adapter

    val isSupported = adapter != null
    val isEnabled = adapter?.isEnabled == true

    val hasPermission =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

    return BluetoothState(
        isSupported = isSupported,
        isEnabled = isEnabled,
        hasPermission = hasPermission
    )
}