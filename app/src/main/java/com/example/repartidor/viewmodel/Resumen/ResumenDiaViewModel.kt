package com.example.repartidor.viewmodel.Resumen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.InventarioItem
import com.example.repartidor.data.model.dclass.ResumenDiaState
import com.example.repartidor.data.repository.InventarioRepository
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.ResumenDiaRepository
import com.example.repartidor.data.repository.SyncFinalRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.utils.ResumenTicketBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class ResumenDiaViewModel(
    private val repository: ResumenDiaRepository,
    private val sessionManager: SessionManager,
    private val inventarioRepository: InventarioRepository,
    private val syncFinalRepository: SyncFinalRepository
) : ViewModel() {

    var state by mutableStateOf(ResumenDiaState())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun cargarResumen() {
        viewModelScope.launch {

            isLoading = true

            val usuarioId = sessionManager.getUserId() ?: return@launch

            val inicioDia = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val finDia = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            Log.d("RESUMEN", "Inicio: $inicioDia")
            Log.d("RESUMEN", "Fin: $finDia")

            state = repository.obtenerResumen(
                inicioDia = inicioDia,
                finDia = finDia,
                usuarioId = usuarioId
            )

            isLoading = false
        }
    }

    suspend fun obtenerInventarioParaImpresion(): List<InventarioItem> {

        val username = sessionManager.getUser() ?: return emptyList()

        return inventarioRepository.obtenerInventario(username)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun obtenerInfoEncabezado(): Triple<String, String, String> {

        val username = sessionManager.getUser() ?: return Triple("-", "-", "-")

        val usuario = repository.getUsuarioByUsername(username)
            ?: return Triple("-", "-", "-")

        val ruta = repository.getRutaByUsuarioId(usuario.id)

        val nombreCompleto = "${usuario.firstName} ${usuario.lastName}"
        val nombreRuta = ruta?.nombre ?: "Sin ruta"

        val fecha = LocalDate.now().toString()

        return Triple(nombreCompleto, nombreRuta, fecha)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generarTicket(
        imprimirProductos: Boolean,
        imprimirDinero: Boolean,
        imprimirInventario: Boolean,
        imprimirDevoluciones: Boolean,
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {

            // 🔹 encabezado
            val (nombreUsuario, ruta, fecha) = obtenerInfoEncabezado()

            // 🔹 inventario (solo si se necesita)
            val inventario = if (imprimirInventario) {
                obtenerInventarioParaImpresion()
            } else {
                emptyList()
            }

            // 🔹 construir ticket
            val ticket = ResumenTicketBuilder.build(
                state = state,
                inventario = inventario,
                nombreUsuario = nombreUsuario,
                ruta = ruta,
                fecha = fecha,
                imprimirProductos = imprimirProductos,
                imprimirDinero = imprimirDinero,
                imprimirInventario = imprimirInventario,
                imprimirDevoluciones = imprimirDevoluciones
            )

            onResult(ticket)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun imprimirTicket(
        ticket: String,
        printerManager: PrinterManager,
        printerRepository: PrinterRepository,
        adapter: BluetoothAdapter?,
        onResult: (PrintResult) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            val device = adapter?.let {
                printerRepository.getSavedPrinter(it)
            }

            val result = printerManager.print(device, ticket)

            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun syncFinalDelDia(
        inicio: Long,
        fin: Long,
        fechaHoy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSyncing = true

            val result = syncFinalRepository.syncTodo(inicio, fin, fechaHoy)

            isSyncing = false

            result
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Error en sync") }
        }
    }

}