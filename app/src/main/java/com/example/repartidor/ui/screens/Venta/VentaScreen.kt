package com.example.repartidor.ui.screens.Venta

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.viewmodel.VentaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    clienteId: Int?,
    onIrCarrito: () -> Unit,
    viewModel: VentaViewModel
) {

    val productos by viewModel.productos.collectAsState()
    var productoSeleccionado by remember { mutableStateOf<ProductoTerminadoEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }


    Column(modifier = Modifier.padding(16.dp)) {

        if (clienteId != null) {
            Text("Venta con cliente ID: $clienteId")
        } else {
            Text("Venta rápida")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Lista de productos
        LazyColumn {
            items(productos) { producto ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            productoSeleccionado = producto
                            showDialog = true
                        }
                ) {
                    Text(
                        text = producto.nombre,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onIrCarrito) {
            Text("Ir al carrito")
        }
    }

    // 🔥 DIALOG DE VARIACIONES
    if (showDialog && productoSeleccionado != null) {

        val variaciones by viewModel
            .getVariaciones(productoSeleccionado!!.id)
            .collectAsState(initial = emptyList())

        val cantidades = remember { mutableStateMapOf<Int, String>() }

        Dialog(onDismissRequest = { showDialog = false }) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = productoSeleccionado!!.nombre,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {

                        variaciones.forEach { variacion ->

                            val cantidad = cantidades[variacion.id] ?: "0"

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {

                                Text("Presentación: ${variacion.presentacionNombre}")
                                Text("Precio: $${variacion.precio}")
                                Text("Stock: ${variacion.stockActual}")

                                Spacer(modifier = Modifier.height(4.dp))

                                // 🔥 TEXTFIELD SOLO NÚMEROS
                                TextField(
                                    value = cantidad,
                                    onValueChange = { nuevo ->
                                        if (nuevo.all { it.isDigit() }) {
                                            cantidades[variacion.id] = nuevo
                                        }
                                    },
                                    label = { Text("Cantidad") },
                                    singleLine = true
                                )

                                HorizontalDivider(
                                    Modifier,
                                    DividerDefaults.Thickness,
                                    DividerDefaults.color
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // 🔹 VOLVER
                        Button(onClick = { showDialog = false }) {
                            Text("Volver")
                        }

                        // 🔥 AGREGAR AL CARRITO
                        Button(
                            onClick = {

                                val seleccionados = variaciones.mapNotNull { variacion ->
                                    val cantidad = cantidades[variacion.id]?.toIntOrNull() ?: 0

                                    if (cantidad > 0) {
                                        Pair(variacion, cantidad)
                                    } else null
                                }

                                // 🔥 aquí mandas al carrito
                                seleccionados.forEach { (variacion, cantidad) ->
                                    println("Agregar: ${variacion.presentacionNombre} x$cantidad")
                                }

                                showDialog = false
                            }
                        ) {
                            Text("Agregar al carrito")
                        }
                    }
                }
            }
        }
    }


}