package com.example.repartidor.ui.screens.Inventario

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.viewmodel.CierreMiniBodegaViewModel
import com.example.repartidor.viewmodel.ReabastecimientoCarritoViewModel
import com.example.repartidor.viewmodel.ReabastecimientoProcesoViewModel

@Composable
fun PedidoReabastecimientoScreen(
    carritoViewModel: ReabastecimientoCarritoViewModel,
    onVolver: () -> Unit,
    reabastecimientoProcesoViewModel: ReabastecimientoProcesoViewModel,
    cierreMiniBodegaViewModel: CierreMiniBodegaViewModel
) {

    val items by carritoViewModel.items.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Pedido de Reabastecimiento", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Text("No hay productos en el pedido")
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
                        }

                        // 🔹 Botón -
                        IconButton(
                            onClick = {
                                val nueva = (item.cantidad - 1).coerceAtLeast(0)
                                carritoViewModel.actualizarCantidad(
                                    item.productoVariacionId,
                                    nueva
                                )
                            }
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge)
                        }

                        // 🔹 Cantidad editable
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
                                carritoViewModel.actualizarCantidad(
                                    item.productoVariacionId,
                                    nueva
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Button(onClick = onVolver) {
                    Text("Volver")
                }

                Button(
                    onClick = {
                        showConfirmDialog = true
                    }
                ) {
                    Text("Confirmar pedido")
                }
            }
        }
    }

    // 🔥 DIALOG DE CONFIRMACIÓN
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
                        text = "Confirmar pedido",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "¿Seguro que deseas enviar el pedido de reabastecimiento?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                showConfirmDialog = false
                            }
                        ) {
                            Text("No")
                        }
                        Button(
                            onClick = {
                                showConfirmDialog = false

                                reabastecimientoProcesoViewModel.enviarPedido(
                                    items = items,
                                    onSuccess = {

                                        // 🔥 SEGUNDO PASO: cerrar mini bodega
                                        cierreMiniBodegaViewModel.cerrarMiniBodega(
                                            onSuccess = {
                                                carritoViewModel.limpiar()
                                                onVolver()
                                            },
                                            onError = {
                                                println("Error cierre: $it")
                                            }
                                        )
                                    },
                                    onError = {
                                        println("Error pedido: $it")
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
}