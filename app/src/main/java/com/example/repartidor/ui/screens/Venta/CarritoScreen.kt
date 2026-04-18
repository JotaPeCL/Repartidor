package com.example.repartidor.ui.screens.Venta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.repartidor.viewmodel.CarritoViewModel
import com.example.repartidor.viewmodel.VentaProcesoViewModel
import androidx.compose.ui.window.Dialog
import com.example.repartidor.utils.PrintResult

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun CarritosScreen(
    carritoViewModel: CarritoViewModel,
    ventaProcesoViewModel: VentaProcesoViewModel,
    onVolver: () -> Unit,
    onVentaExitosa: () -> Unit
) {
    val items by carritoViewModel.items.collectAsState()
    val total = remember(items) {
        items.sumOf { it.precio * it.cantidad }
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Carrito", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Text("El carrito está vacío")
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.productoNombre} - ${item.presentacionNombre}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text("Precio unitario: $${item.precio}")

                            Text("Subtotal: $${item.precio * item.cantidad}")
                        }

                        // 🔹 Botón -
                        IconButton(
                            onClick = {
                                val nueva = (item.cantidad - 1).coerceAtLeast(0)
                                carritoViewModel.actualizarCantidad(item.productoVariacionId, nueva)
                            }
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }

                        // 🔹 Cantidad
                        var textoCantidad by remember { mutableStateOf(item.cantidad.toString()) }

                        LaunchedEffect(item.cantidad) {
                            textoCantidad = item.cantidad.toString()
                        }

                        TextField(
                            value = textoCantidad,
                            onValueChange = { nuevo ->
                                if (nuevo.all { it.isDigit() }) {
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier.width(70.dp)
                        )

                        // 🔹 Botón +
                        IconButton(
                            onClick = {
                                val nueva = item.cantidad + 1
                                carritoViewModel.actualizarCantidad(item.productoVariacionId, nueva)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar")
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Total: $${total}", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onVolver) {
                    Text("Volver")
                }

                Button(onClick = {
                    showConfirmDialog = true
                }) {
                    Text("Confirmar")
                }
            }
        }
    }
    if (showConfirmDialog) {

        Dialog(onDismissRequest = { showConfirmDialog = false }) {

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Confirmar venta",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¿Seguro que deseas confirmar la venta?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // NO
                        Button(
                            onClick = {
                                showConfirmDialog = false
                            }
                        ) {
                            Text("No")
                        }

                        // SÍ
                        Button(
                            onClick = {
                                showConfirmDialog = false
                                isLoading = true

                                ventaProcesoViewModel.confirmarVenta(
                                    items = items,
                                    onSuccess = { result ->
                                        isLoading = false
                                        mensajeResultado = when (result) {

                                            is PrintResult.Success ->
                                                "✅ Venta realizada\n🖨 Ticket impreso correctamente"

                                            is PrintResult.NoPrinter ->
                                                "✅ Venta realizada\n⚠ No hay impresora configurada"

                                            is PrintResult.BluetoothOff ->
                                                "✅ Venta realizada\n⚠ Bluetooth apagado"

                                            is PrintResult.Error ->
                                                "✅ Venta realizada\n❌ Error al imprimir:\n${result.msg}"
                                        }
                                        showResultDialog = true
                                        carritoViewModel.limpiar()
                                        ventaProcesoViewModel.reset()
                                    },
                                    onError = {
                                        isLoading = false
                                        mensajeResultado = "❌ Error en la venta:\n$it"
                                        showResultDialog = true
                                    }
                                )
                            }
                        ) {
                            Text("Sí")
                        }
                    }
                }
            }
        }
    }

    if (isLoading) {
        Dialog(onDismissRequest = { }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    androidx.compose.material3.CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Procesando venta...")
                }
            }
        }
    }
    if (showResultDialog) {
        Dialog(onDismissRequest = {
            showResultDialog = false
            onVentaExitosa() // 🔥 aquí ya sales
        }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Resultado",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = mensajeResultado,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = {
                        showResultDialog = false
                        onVentaExitosa()
                    }) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }

}