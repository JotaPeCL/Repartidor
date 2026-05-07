package com.example.repartidor.ui.screens.VentasDia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.model.VentaUI
import com.example.repartidor.utils.formatearFechaHora
import com.example.repartidor.viewmodel.VentasDiaViewModel

@Composable
fun VentaDiaScreen(
    onBack: () -> Unit,
    viewModel: VentasDiaViewModel
    ){

    val ventas = viewModel.ventas
    val mostrarDialogo = viewModel.mostrarDialogo
    val detalle = viewModel.detalleVenta
    val ventaSeleccionada = viewModel.ventaSeleccionada

    LaunchedEffect(Unit) {
        viewModel.cargarVentas()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Ventas del día",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(ventas) { venta ->
                VentaCard(venta) {
                    viewModel.seleccionarVenta(venta)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // 🔥 DIALOG
    if (mostrarDialogo && ventaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarDialogo() },
            confirmButton = {},
            text = {

                Column {

                    // 🔥 CLIENTE
                    Text(
                        text = ventaSeleccionada.nombreCliente,
                        fontWeight = FontWeight.Bold
                    )

                    ventaSeleccionada.nombreNegocio?.let {
                        Text(text = it)
                    }

                    // 🔥 FECHA
                    Text(
                        text = formatearFechaHora(ventaSeleccionada.fecha),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 🔥 DETALLE PRODUCTOS
                    detalle.forEach {

                        Column(modifier = Modifier.fillMaxWidth()) {

                            Text(it.nombreCompleto)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cant: ${it.cantidad}")
                                Text("Unitario:$${it.precioUnitario}")
                                Text("Sub: $${it.subtotal}")
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 🔥 DESCUENTO
                    val subtotal = detalle.sumOf { it.subtotal }
                    val totalFinal = ventaSeleccionada.total
                    val descuento = subtotal - totalFinal


                    Text(
                        text = "Subtotal: $${"%.2f".format(subtotal)}"
                    )

                    if (descuento > 0) {
                        Text(
                            text = "Descuento: -$${"%.2f".format(descuento)}",
                            color = Color.Red
                        )
                    }

                    Text(
                        text = "Total: $${"%.2f".format(totalFinal)}",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔥 BOTONES
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            viewModel.cerrarDialogo()
                        }) {
                            Text("Cerrar")
                        }

                        TextButton(onClick = {
                            // imprimir
                        }) {
                            Text("Imprimir")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun VentaCard(
    venta: VentaUI,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = venta.nombreCliente,
                fontWeight = FontWeight.Bold
            )

            venta.nombreNegocio?.let {
                Text(text = it, fontSize = 12.sp)
            }

            Text(
                text = formatearFechaHora(venta.fecha), // 🔥 FECHA
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Total: $${venta.total}",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}