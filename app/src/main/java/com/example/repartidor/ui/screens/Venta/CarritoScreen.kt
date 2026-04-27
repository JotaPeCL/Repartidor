package com.example.repartidor.ui.screens.Venta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.viewmodel.CarritoViewModel
import com.example.repartidor.viewmodel.VentaProcesoViewModel
import com.example.repartidor.utils.PrintResult


@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun CarritosScreen(
    carritoViewModel: CarritoViewModel,
    ventaProcesoViewModel: VentaProcesoViewModel,
    onVolver: () -> Unit,
    onVentaExitosa: () -> Unit
) {
    val items by carritoViewModel.items.collectAsState()
    val cliente by ventaProcesoViewModel.cliente.collectAsState()
    val subtotal = remember(items) {
        items.sumOf { it.precio * it.cantidad }
    }

    val porcentajeDescuento = cliente?.porcentajeDescuento ?: 0.0

    val descuento = remember(subtotal, porcentajeDescuento) {
        if (porcentajeDescuento > 0) {
            subtotal * (porcentajeDescuento / 100)
        } else 0.0
    }

    val totalFinal = subtotal - descuento


    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Carrito",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {Text(
                            text = "Subtotal",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                            Text(
                                text = "$${"%.2f".format(subtotal)}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (porcentajeDescuento > 0) {
                                Text(
                                    text = "Descuento (${porcentajeDescuento.toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-$${"%.2f".format(descuento)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "$${"%.2f".format(totalFinal)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Confirmar",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(text = "Confirmar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Carrito Vacío",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "El carrito está vacío",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items) { item ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${item.productoNombre} - ${item.presentacionNombre}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Precio: $${item.precio}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Subtotal: $${item.precio * item.cantidad}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Controles de cantidad
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        FilledTonalIconButton(
                                            onClick = {
                                                val nueva = (item.cantidad - 1).coerceAtLeast(0)
                                                carritoViewModel.actualizarCantidad(item.productoVariacionId, nueva)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                                        }

                                        var textoCantidad by remember { mutableStateOf(item.cantidad.toString()) }
                                        LaunchedEffect(item.cantidad) { textoCantidad = item.cantidad.toString() }

                                        OutlinedTextField(
                                            value = textoCantidad,
                                            onValueChange = { nuevo ->
                                                if (nuevo.isEmpty() || nuevo.all { it.isDigit() }) {
                                                    textoCantidad = nuevo
                                                    val nuevaCantidad = nuevo.toIntOrNull()
                                                    if (nuevaCantidad != null) {
                                                        carritoViewModel.actualizarCantidad(
                                                            item.productoVariacionId,
                                                            nuevaCantidad
                                                        )
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .width(64.dp)
                                                .padding(horizontal = 8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            )
                                        )

                                        FilledTonalIconButton(
                                            onClick = {
                                                val nueva = item.cantidad + 1
                                                carritoViewModel.actualizarCantidad(item.productoVariacionId, nueva)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 🎨 DIALOG DE CONFIRMACIÓN (AlertDialog M3)
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(text = "Confirmar venta", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "¿Seguro que deseas proceder con la venta de estos artículos?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        isLoading = true

                        ventaProcesoViewModel.confirmarVenta(
                            items = items,
                            onSuccess = { result ->
                                isLoading = false
                                mensajeResultado = when (result) {
                                    is PrintResult.Success -> "✅ Venta realizada\n🖨 Ticket impreso correctamente"
                                    is PrintResult.NoPrinter -> "✅ Venta realizada\n⚠ No hay impresora configurada"
                                    is PrintResult.BluetoothOff -> "✅ Venta realizada\n⚠ Bluetooth apagado"
                                    is PrintResult.Error -> "✅ Venta realizada\n❌ Error al imprimir:\n${result.msg}"
                                }
                                showResultDialog = true
                                carritoViewModel.limpiar()
                                ventaProcesoViewModel.reset()
                            },
                            onError = { error ->
                                isLoading = false
                                mensajeResultado = "❌ Error en la venta:\n$error"
                                showResultDialog = true
                            }
                        )
                    }
                ) {
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

    // 🎨 DIALOG DE CARGA MEJORADO
    if (isLoading) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Procesando venta...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // 🎨 DIALOG DE RESULTADO (AlertDialog M3)
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                onVentaExitosa()
            },
            title = {
                Text(text = "Resultado", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = mensajeResultado,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    showResultDialog = false
                    onVentaExitosa()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }
}