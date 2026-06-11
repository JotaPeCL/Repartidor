package com.example.repartidor.viewmodel.Abonos

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.repository.AbonoRepository
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketAbonoBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbonoViewModel(
    private val repository: AbonoRepository,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?
) : ViewModel() {

    var abonoResult by mutableStateOf<AbonoResult>(AbonoResult.Idle)
        private set
    var isLoading by mutableStateOf(false)
        private set

    var printResult by mutableStateOf<PrintResult?>(null)
        private set

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun registrarAbono(
        ventaId: Int,
        monto: Double,
        usuarioId: Int,
        clienteNombre: String,
        negocio: String,
        total: Double,
        saldoAnterior: Double,
        imprimir: Boolean
    ) {
        viewModelScope.launch {

            isLoading = true
            abonoResult = AbonoResult.Idle
            printResult = null

            try {

                // 🔥 1. GENERAR TICKET (ANTES)
                val ticket = TicketAbonoBuilder.build(
                    cliente = clienteNombre,
                    negocio = negocio,
                    total = total,
                    abono = monto,
                    saldoRestante = saldoAnterior - monto,
                    fecha = System.currentTimeMillis()
                )

                // 🔥 2. OBTENER IMPRESORA
                val device = bluetoothAdapter?.let {
                    printerRepository.getSavedPrinter(it)
                }

                // 🔥 3. IMPRIMIR PRIMERO
                printResult = if (imprimir) {
                    withContext(Dispatchers.IO) {

                        val firstPrint = printerManager.print(device, ticket)

                        if (firstPrint is PrintResult.Success) {
                            printerManager.print(device, ticket)
                        } else {
                            firstPrint // ❌ falla → NO guarda
                        }
                    }
                } else {
                    PrintResult.NoPrinter
                }

                // 🔥 4. VALIDAR RESULTADO DE IMPRESIÓN
                if (imprimir && printResult !is PrintResult.Success) {
                    abonoResult = AbonoResult.PrintError(printResult!!)
                    return@launch
                }

                // 🔥 5. AHORA SÍ GUARDAR
                repository.registrarAbono(ventaId, monto, usuarioId)

                // 🔥 6. FINAL OK
                abonoResult = AbonoResult.Success

            } catch (e: Exception) {
                abonoResult = AbonoResult.Error(
                    e.message ?: "Error al registrar abono"
                )
            } finally {
                isLoading = false
            }
        }
    }



    fun resetResult() {
        abonoResult = AbonoResult.Idle
    }
}
sealed class AbonoResult {
    object Idle : AbonoResult()
    object Success : AbonoResult()
    data class PrintError(val printResult: PrintResult) : AbonoResult()
    data class Error(val message: String) : AbonoResult()
}