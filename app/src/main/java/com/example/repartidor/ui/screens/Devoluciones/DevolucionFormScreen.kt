package com.example.repartidor.ui.screens.Devoluciones

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.ui.screens.Cliente.ClienteCard
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.viewmodel.CarritoDevolucionViewModel
import com.example.repartidor.viewmodel.ClienteViewModel
import com.example.repartidor.viewmodel.DevolucionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevolucionFormScreen(
    carritoViewModel: CarritoDevolucionViewModel,
    clienteViewModel: ClienteViewModel,
    devolucionViewModel: DevolucionViewModel,
    onBack: () -> Unit,
    onDevolucionExitosa: () -> Unit = {}
) {

    val items by carritoViewModel.items.collectAsState()

    val cliente by remember { derivedStateOf { clienteViewModel.cliente } }
    val resultados = clienteViewModel.resultados

    val isLoading = devolucionViewModel.isLoading
    val error = devolucionViewModel.error
    val success = devolucionViewModel.success

    var motivo by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }

    var clienteBuscado by remember { mutableStateOf("") }
    var clienteNulo by remember { mutableStateOf(false) }
    var esExito by remember { mutableStateOf(false) }

    var imprimir by remember { mutableStateOf(true) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var printerStatus by remember { mutableStateOf<PrintResult?>(null) }

    val motivos = listOf(
        "Devolución de cliente",
        "Producto dañado",
        "Merma",
        "Producto roto",
        "Error de manejo",
        "Otro"
    )

    val totalProductos = remember(items) {
        items.sumOf { it.cantidad }
    }

    // ─────────────────────────────
    // 🔥 RESULTADO REAL DEL VIEWMODEL
    // ─────────────────────────────
    /*
    LaunchedEffect(success) {
        if (success) {
            esExito = true
            mensajeResultado = "Devolución registrada correctamente"
            showResultDialog = true
            carritoViewModel.limpiar()
            devolucionViewModel.reset()
        }
    }*/

    LaunchedEffect(error) {
        error?.let {
            esExito = false
            mensajeResultado = it
            showResultDialog = true
        }
    }
    val scope = rememberCoroutineScope()

    fun intentarRegistrar(imprimir: Boolean) {

        val clienteIdFinal = if (clienteNulo) null else cliente?.id

        // 🔥 ACTIVAR LOADING
        devolucionViewModel.setLoading(true)

        scope.launch {

            delay(100) // 👈 deja que Compose pinte el loading

            val result = devolucionViewModel.verificarImpresora()

            printerStatus = result

            when (result) {

                is PrintResult.Success -> {
                    devolucionViewModel.registrarDevolucion(
                        clienteId = clienteIdFinal,
                        clienteNulo = clienteNulo,
                        motivo = motivo,
                        observacion = observacion,
                        carrito = items,
                        imprimir = imprimir
                    )
                }

                else -> {
                    devolucionViewModel.setLoading(false)
                    showPrinterDialog = true
                }
            }
        }
    }


    // ─────────────────────────────
    // CONFIRM DIALOG
    // ─────────────────────────────
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar devolución") },
            text = { Text("¿Deseas registrar esta devolución?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false

                    // 🔥 NUEVO: aquí entra la lógica de impresora
                    intentarRegistrar(imprimir = true)
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showPrinterDialog) {

        val mensaje = when (printerStatus) {
            is PrintResult.BluetoothOff -> "El Bluetooth está apagado."
            is PrintResult.NoPrinter -> "No hay impresora configurada."
            is PrintResult.Error -> "No se pudo conectar a la impresora."
            else -> "Impresora no disponible."
        }

        AlertDialog(
            onDismissRequest = { showPrinterDialog = false },

            title = { Text("Impresora no disponible") },

            text = {
                Text("$mensaje\n\n¿Deseas continuar sin imprimir?")
            },

            confirmButton = {
                TextButton(onClick = {
                    showPrinterDialog = false

                    // 🔥 continuar sin imprimir
                    intentarRegistrar(imprimir = false)
                }) {
                    Text("Continuar sin imprimir")
                }
            },

            dismissButton = {
                TextButton(onClick = {
                    // ❌ no sale → solo cierra dialog
                    showPrinterDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LaunchedEffect(success) {
        if (success) {

            val print = devolucionViewModel.printResult

            esExito = true

            mensajeResultado = when (print) {
                is PrintResult.Success ->
                    "Devolución registrada e impresa correctamente"

                is PrintResult.NoPrinter ->
                    "Devolución registrada (sin impresora)"

                is PrintResult.BluetoothOff ->
                    "Devolución registrada (Bluetooth apagado)"

                is PrintResult.Error ->
                    "Devolución registrada pero error al imprimir"

                else ->
                    "Devolución registrada correctamente"
            }

            showResultDialog = true
            carritoViewModel.limpiar()
            devolucionViewModel.reset()
        }
    }

    // ─────────────────────────────
    // RESULTADO DIALOG
    // ─────────────────────────────
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                if (esExito) {
                    onDevolucionExitosa()
                }
            },
            title = { Text("Resultado") },
            text = { Text(mensajeResultado) },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    if (esExito) {
                        onDevolucionExitosa()
                    }
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // ─────────────────────────────
    // UI PRINCIPAL
    // ─────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devolución") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Button(
                    onClick = {
                        if (motivo.isBlank()) {
                            mensajeResultado = "Debes seleccionar un motivo"
                            showResultDialog = true
                        } else {
                            showConfirmDialog = true
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text("Confirmar devolución")
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (items.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay productos en devolución")
                }
            } else {

                // ─────────────────────────────
                // LISTA PRODUCTOS
                // ─────────────────────────────
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(items) { item ->
                        CarritoItemDevolucionCard(
                            item = item,
                            viewModel = carritoViewModel
                        )
                    }
                }

                // ─────────────────────────────
                // FORMULARIO
                // ─────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    // ───────── MOTIVO ─────────
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {

                        OutlinedTextField(
                            value = motivo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Motivo de devolución *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            motivos.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        motivo = opcion
                                        expanded = false

                                        if (opcion != "Devolución de cliente") {
                                            clienteBuscado = ""
                                            clienteNulo = false
                                            clienteViewModel.limpiarBusqueda()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ───────── CLIENTE ─────────
                    if (motivo == "Devolución de cliente") {

                        OutlinedTextField(
                            value = clienteBuscado,
                            onValueChange = {
                                clienteBuscado = it
                                clienteNulo = false
                                clienteViewModel.limpiarBusqueda()
                            },
                            label = { Text("Buscar cliente") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                clienteViewModel.buscarCliente(clienteBuscado)
                            },
                            enabled = clienteBuscado.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Buscar")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = clienteNulo,
                                onCheckedChange = {
                                    clienteNulo = it
                                    if (it) {
                                        clienteBuscado = ""
                                        clienteViewModel.limpiarBusqueda()
                                    }
                                }
                            )
                            Text("Cliente nulo (venta rápida)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        cliente?.let {
                            ClienteCard(
                                nombre = it.nombre,
                                id = it.id,
                                negocio = it.nombreNegocio,
                                seleccionado = true,
                                onClick = {}
                            )
                        }

                        LazyColumn {
                            items(resultados.filter { it.id != cliente?.id }) { item ->
                                ClienteCard(
                                    nombre = item.nombre,
                                    id = item.id,
                                    negocio = item.nombreNegocio,
                                    seleccionado = cliente?.id == item.id,
                                    onClick = {
                                        clienteViewModel.seleccionarCliente(item)
                                        clienteNulo = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ───────── OBSERVACIÓN ─────────
                    OutlinedTextField(
                        value = observacion,
                        onValueChange = { observacion = it },
                        label = { Text("Observación") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Productos: $totalProductos"
                    )
                }
            }
        }
    }

}

@Composable
fun CarritoItemDevolucionCard(
    item: CarritoItem,
    viewModel: CarritoDevolucionViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            Text("${item.productoNombre} - ${item.presentacionNombre}")

            Row(verticalAlignment = Alignment.CenterVertically) {

                IconButton({
                    viewModel.actualizarCantidad(
                        item.productoVariacionId,
                        item.cantidad - 1
                    )
                }) {
                    Icon(Icons.Default.Remove, null)
                }

                Text(item.cantidad.toString())

                IconButton({
                    viewModel.actualizarCantidad(
                        item.productoVariacionId,
                        item.cantidad + 1
                    )
                }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    }
}

