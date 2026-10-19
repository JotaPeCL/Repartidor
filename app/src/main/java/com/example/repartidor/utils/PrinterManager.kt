
package com.example.repartidor.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.util.Log
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

    companion object {
        private const val TAG = "IMPRESORA"
    }

    private fun cancelarBusqueda() {
        try {
            // En Android 12+ requiere BLUETOOTH_SCAN.
            // En versiones anteriores requiere BLUETOOTH_ADMIN.
            adapter?.cancelDiscovery()
        } catch (e: SecurityException) {
            Log.w(TAG, "Sin permiso para cancelar búsqueda Bluetooth", e)
        }
    }

    fun print(
        device: BluetoothDevice?,
        text: String
    ): PrintResult {

        if (adapter == null) {
            return PrintResult.BluetoothOff
        }

        if (!adapter.isEnabled) {
            return PrintResult.BluetoothOff
        }

        if (device == null) {
            return PrintResult.NoPrinter
        }

        val inicioTotal = System.currentTimeMillis()

        repeat(2) { intento ->

            var socket: BluetoothSocket? = null

            try {
                Log.d(TAG, "Iniciando intento ${intento + 1}")

                cancelarBusqueda()
                val inicioConexion = System.currentTimeMillis()
                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket.connect()

                val tiempoConexion =
                    System.currentTimeMillis() - inicioConexion
                Log.d(
                    TAG,
                    "Conexión Bluetooth: ${tiempoConexion} ms"
                )
                val output = socket.outputStream

                // Mantener estabilización de conexión
                Thread.sleep(300)

                val inicioEnvio = System.currentTimeMillis()

                // Despertar impresora
                output.write("\n\n".toByteArray())

                // Configurar página de códigos
                output.write(byteArrayOf(0x1B, 0x74, 0x10))

                // Mantener codificación original
                output.write(
                    text.toByteArray(Charsets.ISO_8859_1)
                )

                output.flush()
                val tiempoEnvio =
                    System.currentTimeMillis() - inicioEnvio
                Log.d(
                    TAG,
                    "Envío del ticket: $tiempoEnvio ms"
                )
                // Mantener espera final
                Thread.sleep(200)

                val tiempoTotal =
                    System.currentTimeMillis() - inicioTotal

                Log.d(
                    TAG,
                    "Impresión enviada en $tiempoTotal ms"
                )

                return PrintResult.Success

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Intento ${intento + 1} falló: ${e.message}",
                    e
                )

                if (intento == 1) {
                    return PrintResult.Error(
                        e.message ?: "Error impresión"
                    )
                }

            } finally {

                try {
                    socket?.close()
                } catch (e: Exception) {
                    Log.w(TAG, "Error cerrando socket", e)
                }
            }

            // Esperar antes del segundo intento
            if (intento == 0) {
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return PrintResult.Error(
                        "Impresión interrumpida"
                    )
                }
            }
        }

        return PrintResult.Error("Fallo inesperado")
    }

    fun checkPrinter(
        device: BluetoothDevice?
    ): PrintResult {

        if (adapter == null) {
            return PrintResult.BluetoothOff
        }

        if (!adapter.isEnabled) {
            return PrintResult.BluetoothOff
        }

        if (device == null) {
            return PrintResult.NoPrinter
        }
        var socket: BluetoothSocket? = null
        return try {

            cancelarBusqueda()

            val inicio = System.currentTimeMillis()

            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()

            Log.d(
                TAG,
                "Prueba de conexión: ${System.currentTimeMillis() - inicio} ms"
            )

            PrintResult.Success
        } catch (e: Exception) {

            Log.e(TAG, "Error comprobando impresora", e)

            PrintResult.Error(
                "No se pudo conectar a la impresora"
            )

        } finally {

            try {
                socket?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error cerrando conexión de prueba", e)
            }
        }
    }
}
