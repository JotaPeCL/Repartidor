package com.example.repartidor.viewmodel.Devoluciones

import android.Manifest
import android.bluetooth.BluetoothAdapter
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.DetalleDevolucionUI
import com.example.repartidor.data.model.dclass.DevolucionUI
import com.example.repartidor.data.model.dclass.ResultadoCancelacion
import com.example.repartidor.data.model.dclass.SustitucionDevolucionUI
import com.example.repartidor.data.repository.CancelacionDevolucionRepository
import com.example.repartidor.data.repository.DevolucionesDiaRepository
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.TicketDevolucionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DevolucionesDiaViewModel(
    private val repository: DevolucionesDiaRepository,
    private val cancelacionDevolucionRepository: CancelacionDevolucionRepository,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val sessionManager: SessionManager
) : ViewModel() {

    var devoluciones by mutableStateOf<List<DevolucionUI>>(emptyList())
        private set

    var detalleDevolucion by mutableStateOf<List<DetalleDevolucionUI>>(emptyList())
        private set

    var sustitucionesDevolucion by mutableStateOf<List<SustitucionDevolucionUI>>(emptyList())
        private set

    var devolucionSeleccionada by mutableStateOf<DevolucionUI?>(null)
        private set

    var mostrarDialogo by mutableStateOf(false)
        private set

    var cancelandoDevolucion by mutableStateOf(false)
        private set

    var resultadoCancelacion by mutableStateOf<ResultadoCancelacion?>(null)
        private set

    fun cargarDevoluciones() {
        viewModelScope.launch {

            val usuarioId = sessionManager.getUserId()
                ?: return@launch

            val data =
                repository.getDevolucionesDelDia(usuarioId)

            devoluciones = data
        }
    }

    fun seleccionarDevolucion(
        devolucion: DevolucionUI
    ) {
        viewModelScope.launch {

            devolucionSeleccionada = devolucion

            val (detalle, sustituciones) =
                repository.getDetalleDevolucion(
                    devolucion.id
                )

            detalleDevolucion = detalle
            sustitucionesDevolucion = sustituciones

            mostrarDialogo = true
        }
    }

    fun cerrarDialogo() {
        mostrarDialogo = false
        devolucionSeleccionada = null
        detalleDevolucion = emptyList()
        sustitucionesDevolucion = emptyList()
    }

    fun cancelarDevolucion(
        motivo: String = ""
    ) {
        if (cancelandoDevolucion) return

        viewModelScope.launch {

            val devolucion =
                devolucionSeleccionada
                    ?: return@launch

            val usuarioId =
                sessionManager.getUserId()
                    ?: return@launch

            val miniBodegaId =
                sessionManager.getMiniBodegaId()
                    ?: return@launch

            cancelandoDevolucion = true
            resultadoCancelacion = null

            try {

                withContext(Dispatchers.IO) {

                    cancelacionDevolucionRepository.cancelarDevolucion(
                        devolucionId = devolucion.id,
                        usuarioId = usuarioId,
                        miniBodegaId = miniBodegaId,
                        motivo = motivo
                    )
                }

                devoluciones =
                    repository.getDevolucionesDelDia(
                        usuarioId
                    )

                cerrarDialogo()

                resultadoCancelacion =
                    ResultadoCancelacion(
                        exitoso = true,
                        mensaje =
                            "Devolución cancelada correctamente"
                    )

            } catch (e: Exception) {

                resultadoCancelacion =
                    ResultadoCancelacion(
                        exitoso = false,
                        mensaje =
                            e.message
                                ?: "No se pudo cancelar la devolución"
                    )

            } finally {

                cancelandoDevolucion = false
            }
        }
    }

    fun limpiarResultadoCancelacion() {
        resultadoCancelacion = null
    }

    @RequiresPermission(
        Manifest.permission.BLUETOOTH_CONNECT
    )
    fun imprimirDevolucionSeleccionada(
        onResult: (PrintResult) -> Unit
    ) {
        viewModelScope.launch {

            val devolucion =
                devolucionSeleccionada
                    ?: return@launch

            try {

                val adapter = bluetoothAdapter

                if (adapter == null) {
                    onResult(PrintResult.BluetoothOff)
                    return@launch
                }

                if (!adapter.isEnabled) {
                    onResult(PrintResult.BluetoothOff)
                    return@launch
                }

                val device =
                    printerRepository.getSavedPrinter(
                        adapter
                    )

                if (device == null) {
                    onResult(PrintResult.NoPrinter)
                    return@launch
                }

                val (items, sustitucionesPorProducto) =
                    repository.getDatosParaImpresion(
                        devolucion.id
                    )

                val ticket =
                    TicketDevolucionBuilder.build(
                        items = items,
                        sustitucionesPorProducto =
                            sustitucionesPorProducto,
                        clienteNombre =
                            if (devolucion.clienteId != null) {
                                devolucion.nombreCliente
                            } else {
                                null
                            },
                        motivo = devolucion.tipo,
                        observacion = devolucion.descripcion,
                        usuario = sessionManager.getUser(),
                        fecha = devolucion.fecha
                    )

                val result =
                    withContext(Dispatchers.IO) {
                        printerManager.print(
                            device,
                            ticket
                        )
                    }

                onResult(result)

            } catch (e: Exception) {

                onResult(
                    PrintResult.Error(
                        e.message
                            ?: "Error al imprimir"
                    )
                )
            }
        }
    }
}