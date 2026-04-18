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

        repeat(2) { intento ->

            try {
                val socket = device.createRfcommSocketToServiceRecord(uuid)

                socket.connect()

                val output = socket.outputStream

                Thread.sleep(300) // 🔥 deja estabilizar conexión

                // 🔥 despertar impresora
                output.write("\n\n".toByteArray())

                output.write(text.toByteArray(Charsets.UTF_8))
                output.flush()

                Thread.sleep(200)

                socket.close()

                return PrintResult.Success

            } catch (e: Exception) {

                println("❌ Intento ${intento + 1} falló: ${e.message}")

                if (intento == 1) {
                    return PrintResult.Error(e.message ?: "Error impresión")
                }

                Thread.sleep(500) // 🔁 espera antes de reintentar
            }
        }

        return PrintResult.Error("Fallo inesperado")
    }
}