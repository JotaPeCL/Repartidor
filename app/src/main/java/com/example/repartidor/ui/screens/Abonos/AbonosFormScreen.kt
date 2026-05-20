package com.example.repartidor.ui.screens.Abonos

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.viewmodel.AbonoResult
import com.example.repartidor.viewmodel.AbonoViewModel
import com.example.repartidor.viewmodel.AbonosFormViewModel
import kotlinx.coroutines.launch
@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun AbonosFormScreen(
    ventaId: Int,
    viewModel: AbonosFormViewModel,
    abonoViewModel: AbonoViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val venta = viewModel.ventaDetalle
    var monto by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var montoConfirmado by remember { mutableStateOf(0.0) }
    var esPagoTotal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()
    val abonoResult = abonoViewModel.abonoResult
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var showPrinterErrorDialog by remember { mutableStateOf(false) }
    var pendingAbono by remember { mutableStateOf(false) }

    LaunchedEffect(abonoResult) {
        when (abonoResult) {
            is AbonoResult.Success -> {

                showLoadingDialog = false

                val print = abonoViewModel.printResult

                if (print is PrintResult.Success) {
                    showSuccessDialog = true
                } else {
                    // 🔥 error de impresora
                    showPrinterErrorDialog = true
                }

                abonoViewModel.resetResult()
            }

            is AbonoResult.Error -> {
                showLoadingDialog = false
                errorMessage = abonoResult.message
                showErrorDialog = true
                abonoViewModel.resetResult()
            }

            else -> {}

        }
    }

    LaunchedEffect(ventaId) {
        viewModel.cargarVenta(ventaId)
    }

    if (viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    venta?.let {

        val subtotal = it.productos.sumOf { p ->
            p.cantidad * p.precioUnitario
        }

        val descuentoDinero = subtotal - it.info.total

        Column(modifier = Modifier.padding(16.dp)) {

            Text("Cliente: ${it.info.clienteNombre}")
            Text("Negocio: ${it.info.clienteNegocio}")
            Text("Fecha: ${it.info.fecha}")
            Text("Tipo: ${it.info.tipoVenta}")

            Spacer(modifier = Modifier.height(12.dp))

            Text("PRODUCTOS")

            it.productos.forEach { p ->

                val subtotalProducto = p.cantidad * p.precioUnitario

                Column {
                    Text(p.nombre)
                    Text("${p.cantidad} x $${p.precioUnitario}")
                    Text("Subtotal: $${subtotalProducto}")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Subtotal: $${subtotal}")

            if (it.info.porcentajeDescuento > 0) {
                Text("Descuento: ${it.info.porcentajeDescuento}%")
                Text("Descuento en dinero: $${"%.2f".format(descuentoDinero)}")
            }

            Text("Total: $${it.info.total}")
            Text("Abonado: $${it.info.totalAbonado}")
            Text("Saldo pendiente: $${it.info.saldoPendiente}")
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = monto,
                onValueChange = {
                    // 🔥 solo números y punto
                    if (it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        monto = it
                    }
                },
                label = { Text("Dinero a abonar") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 BOTÓN ABONAR
            Button(
                onClick = {
                    montoConfirmado = monto.toDoubleOrNull() ?: 0.0
                    esPagoTotal = false
                    showDialog = true
                },
                enabled = monto.isNotBlank(), // 🔥 clave
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abonar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔥 BOTÓN PAGAR TODO
            Button(
                onClick = {
                    montoConfirmado = it.info.saldoPendiente
                    esPagoTotal = true
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pagar totalidad")
            }


        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        scope.launch {
                            val usuarioId = sessionManager.getUserId() ?: 0

                            if (usuarioId == 0) {
                                errorMessage = "Error: usuario no encontrado"
                                showErrorDialog = true
                                return@launch
                            }

                            showLoadingDialog = true
                            pendingAbono = true

                            abonoViewModel.registrarAbono(
                                ventaId = ventaId,
                                monto = montoConfirmado,
                                usuarioId = usuarioId,
                                clienteNombre = venta!!.info.clienteNombre,
                                negocio = venta.info.clienteNegocio,
                                total = venta.info.total,
                                saldoAnterior = venta.info.saldoPendiente,
                                imprimir = true
                            )
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            },
            title = {
                Text("Confirmar pago")
            },
            text = {
                Text(
                    if (esPagoTotal)
                        "¿Seguro que deseas liquidar la deuda de $${montoConfirmado}?"
                    else
                        "¿Seguro que deseas abonar $${montoConfirmado}?"
                )
            }
        )
    }
    if (showSuccessDialog) {

        val print = abonoViewModel.printResult

        val mensaje = when (print) {
            is PrintResult.Success -> "Abono registrado e impreso correctamente"
            is PrintResult.NoPrinter -> "Abono registrado (sin impresora)"
            is PrintResult.BluetoothOff -> "Abono registrado (Bluetooth apagado)"
            is PrintResult.Error -> "Abono registrado pero error al imprimir"
            else -> "Abono registrado correctamente"
        }

        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onSuccess()
                    }
                ) {
                    Text("Aceptar")
                }
            },
            title = {
                Text("Resultado")
            },
            text = {
                Text(mensaje)
            }
        )
    }
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showErrorDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            title = {
                Text("Error")
            },
            text = {
                Text(errorMessage)
            }
        )
    }
    if (showLoadingDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {
                Text("Procesando")
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Conectando con impresora...")
                }
            }
        )
    }

    if (showPrinterErrorDialog) {

        val print = abonoViewModel.printResult

        AlertDialog(
            onDismissRequest = { showPrinterErrorDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 🔥 seguir sin imprimir
                        showPrinterErrorDialog = false
                        showLoadingDialog = true

                        scope.launch {

                            val usuarioId = sessionManager.getUserId() ?: 0

                            if (usuarioId == 0) {
                                showLoadingDialog = false
                                errorMessage = "Error: usuario no encontrado"
                                showErrorDialog = true
                                return@launch
                            }

                            abonoViewModel.registrarAbono(
                                ventaId = ventaId,
                                monto = montoConfirmado,
                                usuarioId = usuarioId,
                                clienteNombre = venta!!.info.clienteNombre,
                                negocio = venta.info.clienteNegocio,
                                total = venta.info.total,
                                saldoAnterior = venta.info.saldoPendiente,
                                imprimir = false // 🔥 clave
                            )
                        }
                    }
                ) {
                    Text("Seguir sin imprimir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // 🔥 cancelar → regresa a la screen
                        showPrinterErrorDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            title = {
                Text("Impresora no disponible")
            },
            text = {
                Text(
                    when (print) {
                        is PrintResult.NoPrinter -> "No hay impresora configurada"
                        is PrintResult.BluetoothOff -> "Bluetooth apagado"
                        is PrintResult.Error -> "Error al conectar con impresora"
                        else -> "No se pudo imprimir"
                    }
                )
            }
        )
    }

}