package com.example.repartidor.ui.screens.Resumen

import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.viewmodel.ResumenDiaViewModel
@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenDiaScreen(
    viewModel: ResumenDiaViewModel,
    printerManager: PrinterManager,
    printerRepository: PrinterRepository,
    bluetoothAdapter: BluetoothAdapter?,
    onBack: () -> Unit,
    onGoInventario: () -> Unit = {}
) {

    val state = viewModel.state
    val isLoading = viewModel.isLoading

    // Checkboxes impresión
    var imprimirProductos by remember { mutableStateOf(true) }
    var imprimirDinero by remember { mutableStateOf(true) }
    var imprimirInventario by remember { mutableStateOf(false) }
    var imprimirDevoluciones by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }

    var mensajeResultado by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarResumen()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen del Día") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "")
                    }
                }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {

            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                // 💰 ================= DINERO =================
                Text("Dinero", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(8.dp))

                Card {
                    Column(modifier = Modifier.padding(12.dp)) {

                        Text("Efectivo en ventas: $${state.efectivoVentas}")
                        Text("Abonos: $${state.totalAbonos}")
                        Text("Total efectivo: $${state.totalEfectivo}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "${state.cantidadCreditos} créditos pendientes dan una suma de $${state.totalPendiente}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📦 ================= PRODUCTOS =================
                Text("Productos vendidos", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(8.dp))

                Card {
                    Column(modifier = Modifier.padding(12.dp)) {

                        state.productosVendidos.forEach {
                            Text("${it.nombre}: ${it.cantidad}")
                        }

                        if (state.productosVendidos.isEmpty()) {
                            Text("No hubo ventas de productos")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔄 ================= DEVOLUCIONES =================
                Text("Productos devueltos", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(8.dp))

                Card {
                    Column(modifier = Modifier.padding(12.dp)) {

                        state.productosDevueltos.forEach {
                            Text("${it.nombre}: ${it.cantidad}")
                        }

                        if (state.productosDevueltos.isEmpty()) {
                            Text("No hubo devoluciones")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🚚 BOTÓN INVENTARIO
                Button(
                    onClick = onGoInventario,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ir a inventario de camioneta")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 🧾 ================= IMPRESIÓN =================
                Text("Opciones de impresión", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(8.dp))

                Card {
                    Column(modifier = Modifier.padding(12.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = imprimirProductos,
                                onCheckedChange = { imprimirProductos = it }
                            )
                            Text("Total de productos vendidos")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = imprimirDinero,
                                onCheckedChange = { imprimirDinero = it }
                            )
                            Text("Dinero en ventas")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = imprimirInventario,
                                onCheckedChange = { imprimirInventario = it }
                            )
                            Text("Inventario de camioneta")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = imprimirDevoluciones,
                                onCheckedChange = { imprimirDevoluciones = it }
                            )
                            Text("Productos devueltos")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                showConfirmDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Imprimir")
                        }
                    }
                }
            }
        }
    }
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar") },
            text = { Text("¿Seguro que quieres imprimir el resumen?") },
            confirmButton = {
                TextButton(onClick = {

                    showConfirmDialog = false
                    showLoadingDialog = true

                    viewModel.generarTicket(
                        imprimirProductos,
                        imprimirDinero,
                        imprimirInventario,
                        imprimirDevoluciones
                    ) { ticket ->

                        viewModel.imprimirTicket(
                            ticket = ticket,
                            printerManager = printerManager,
                            printerRepository = printerRepository,
                            adapter = bluetoothAdapter
                        ) { result ->

                            showLoadingDialog = false

                            mensajeResultado = when (result) {
                                is PrintResult.Success -> "Impresión completada correctamente"
                                is PrintResult.NoPrinter -> "No hay impresora configurada"
                                is PrintResult.BluetoothOff -> "Bluetooth apagado"
                                is PrintResult.Error -> {
                                    when {
                                        result.msg.contains("timeout", true) ->
                                            "No se pudo conectar con la impresora"

                                        result.msg.contains("read failed", true) ->
                                            "Se perdió la conexión con la impresora"

                                        result.msg.contains("socket", true) ->
                                            "Error de conexión con la impresora"

                                        else ->
                                            "Ocurrió un error al imprimir"
                                    }
                                }
                            }

                            showResultDialog = true
                        }
                    }

                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                }) {
                    Text("No")
                }
            }
        )
    }
    if (showLoadingDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Imprimiendo") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Conectando con la impresora...")
                }
            }
        )
    }
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("Resultado") },
            text = { Text(mensajeResultado) },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                }) {
                    Text("OK")
                }
            }
        )
    }


}