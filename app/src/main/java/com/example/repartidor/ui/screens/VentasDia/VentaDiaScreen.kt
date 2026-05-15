package com.example.repartidor.ui.screens.VentasDia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.model.VentaUI
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.formatearFechaHora
import com.example.repartidor.viewmodel.VentasDiaViewModel

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun VentaDiaScreen(
    onBack: () -> Unit,
    viewModel: VentasDiaViewModel
) {

    val ventas = viewModel.ventas
    val mostrarDialogo = viewModel.mostrarDialogo
    val detalle = viewModel.detalleVenta
    val ventaSeleccionada = viewModel.ventaSeleccionada
    val totalAbonos = viewModel.totalAbonos

    var showConfirmPrint by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }
    var isPrinting by remember { mutableStateOf(false) }

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
                    Text(
                        text = "Tipo: ${ventaSeleccionada.tipoVenta.uppercase()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
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
                    Spacer(modifier = Modifier.height(6.dp))

                    if (ventaSeleccionada.tipoVenta == "CREDITO") {

                        val totalAbonos = viewModel.totalAbonos
                        val saldoPendiente = totalFinal - totalAbonos

                        Text(
                            text = "Abonado: $${"%.2f".format(totalAbonos)}",
                            color = Color(0xFF2E7D32)
                        )

                        Text(
                            text = "Saldo pendiente: $${"%.2f".format(saldoPendiente)}",
                            fontWeight = FontWeight.SemiBold,
                            color = if (saldoPendiente > 0) Color.Red else Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // BOTONES
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
                            showConfirmPrint = true
                        }) {
                            Text("Imprimir")
                        }
                    }
                }
            }
        )
    }
    if (showConfirmPrint) {
        AlertDialog(
            onDismissRequest = { showConfirmPrint = false },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmPrint = false
                    isPrinting = true

                    viewModel.imprimirVentaSeleccionada { result ->

                        isPrinting = false

                        mensajeResultado = when (result) {

                            is PrintResult.Success -> "Ticket impreso correctamente"

                            is PrintResult.NoPrinter -> "No hay impresora configurada"

                            is PrintResult.BluetoothOff -> "Bluetooth apagado"

                            is PrintResult.Error -> {

                                val msg = result.msg.lowercase()

                                when {
                                    msg.contains("timeout") ->
                                        "No se pudo conectar a la impresora (timeout)"

                                    msg.contains("socket") ->
                                        "Error de conexión con la impresora Bluetooth"

                                    msg.contains("connect") ->
                                        "No se pudo establecer conexión con la impresora"

                                    else ->
                                        "Error al imprimir:\n${result.msg}"
                                }
                            }
                        }

                        showResultDialog = true
                    }
                }) {
                    Text("Imprimir")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmPrint = false
                }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Confirmar impresión") },
            text = { Text("¿Deseas imprimir este ticket nuevamente?") }
        )
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                }) {
                    Text("OK")
                }
            },
            title = { Text("Resultado") },
            text = { Text(mensajeResultado) }
        )
    }
    if (isPrinting) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Imprimiendo...")
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