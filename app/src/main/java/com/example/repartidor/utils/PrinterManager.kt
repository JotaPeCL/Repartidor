package com.example.repartidor.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.util.UUID

sealed class PrintResult {
    object Success : PrintResult()
    object NoPrinter : PrintResult()
    object BluetoothOff : PrintResult()
    data class Error(val msg: String) : PrintResult()
}

class PrinterManager(
    private val adapter: BluetoothAdapter?
) {

    private val uuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun print(device: BluetoothDevice?, text: String): PrintResult {

        if (adapter == null || !adapter.isEnabled) {
            return PrintResult.BluetoothOff
        }

        if (device == null) {
            return PrintResult.NoPrinter
        }

        return try {

            val socket: BluetoothSocket =
                device.createRfcommSocketToServiceRecord(uuid)

            socket.connect()

            val output = socket.outputStream
            output.write(text.toByteArray(Charsets.UTF_8))
            output.flush()

            socket.close()

            PrintResult.Success

        } catch (e: Exception) {
            PrintResult.Error(e.message ?: "Error impresión")
        }
    }
}