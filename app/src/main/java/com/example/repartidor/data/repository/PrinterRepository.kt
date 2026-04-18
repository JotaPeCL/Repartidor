package com.example.repartidor.data.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission

class PrinterRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)

    fun savePrinter(device: BluetoothDevice) {
        prefs.edit()
            .putString("printer_address", device.address)
            .apply()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getSavedPrinter(adapter: BluetoothAdapter): BluetoothDevice? {
        val address = prefs.getString("printer_address", null) ?: return null

        return adapter.bondedDevices.find {
            it.address == address
        }
    }
}