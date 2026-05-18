package com.example.repartidor.ui.screens.VentasDia

import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.model.VentaUI
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.formatearFechaHora
import com.example.repartidor.viewmodel.VentasDiaViewModel
import com.example.repartidor.ui.screens.components.* //Aqui estan los colores del tema

@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
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
    var isSuccessPrint by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarVentas()
    }

    // ── DIÁLOGO DE DETALLE DE VENTA ──
    if (mostrarDialogo && ventaSeleccionada != null) {
        DetalleVentaDialog(
            venta = ventaSeleccionada,
            detalle = detalle,
            totalAbonos = totalAbonos,
            onDismiss = { viewModel.cerrarDialogo() },
            onPrintRequest = { showConfirmPrint = true }
        )
    }

    // ── DIÁLOGO CONFIRMAR IMPRESIÓN ──
    if (showConfirmPrint) {
        ConfirmActionDialog(
            title = "Imprimir Ticket",
            message = "¿Deseas imprimir nuevamente el ticket de esta venta?",
            confirmText = "Sí, imprimir",
            icon = Icons.Default.Print,
            iconColor = AccentBlue,
            iconBgColor = AccentBlueSoft,
            onDismiss = { showConfirmPrint = false },
            onConfirm = {
                showConfirmPrint = false
                isPrinting = true

                viewModel.imprimirVentaSeleccionada { result ->
                    isPrinting = false
                    isSuccessPrint = result is PrintResult.Success
                    mensajeResultado = when (result) {
                        is PrintResult.Success -> "Ticket impreso correctamente."
                        is PrintResult.NoPrinter -> "No hay impresora configurada."
                        is PrintResult.BluetoothOff -> "El Bluetooth está apagado."
                        is PrintResult.Error -> {
                            val msg = result.msg.lowercase()
                            when {
                                msg.contains("timeout") -> "No se pudo conectar a la impresora (timeout)."
                                msg.contains("socket") -> "Error de conexión con la impresora Bluetooth."
                                msg.contains("connect") -> "No se pudo establecer conexión con la impresora."
                                else -> "Error al imprimir:\n${result.msg}"
                            }
                        }
                    }
                    showResultDialog = true
                }
            }
        )
    }

    // ── DIÁLOGO RESULTADO IMPRESIÓN ──
    if (showResultDialog) {
        ResultDialog(
            title = if (isSuccessPrint) "Éxito" else "Error",
            message = mensajeResultado,
            isSuccess = isSuccessPrint,
            onDismiss = { showResultDialog = false }
        )
    }

    // ── DIÁLOGO DE CARGA (IMPRIMIENDO) ──
    if (isPrinting) {
        LoadingDialog(mensaje = "Imprimiendo ticket...")
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Ventas del Día",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (ventas.isEmpty()) {
                // ── ESTADO VACÍO ──
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Sin ventas",
                                modifier = Modifier.size(42.dp),
                                tint = TextMuted.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no hay ventas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Las ventas que realices hoy aparecerán aquí.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // ── LISTA DE VENTAS ──
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ventas) { venta ->
                        VentaCardRediseñada(
                            venta = venta,
                            onClick = { viewModel.seleccionarVenta(venta) }
                        )
                    }
                }
            }
        }
    }
}

// ── COMPONENTES UI ────────────────────────────────────────────────────────────

@Composable
fun VentaCardRediseñada(
    venta: VentaUI,
    onClick: () -> Unit
) {
    val isCredito = venta.tipoVenta.equals("CREDITO", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Izquierdo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información Central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = venta.nombreCliente,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                if (!venta.nombreNegocio.isNullOrBlank()) {
                    Text(
                        text = venta.nombreNegocio,
                        color = TextMuted,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatearFechaHora(venta.fecha),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Total y Tag
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${"%.2f".format(venta.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentTeal
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Chip de Tipo de Venta
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCredito) WarningOrangeSoft else SuccessGreenSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = venta.tipoVenta.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredito) WarningOrange else SuccessGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun DetalleVentaDialog(
    venta: VentaUI,
    detalle: List<com.example.repartidor.data.model.DetalleVentaUI>,
    totalAbonos: Double,
    onDismiss: () -> Unit,
    onPrintRequest: () -> Unit
) {
    val subtotal = detalle.sumOf { it.subtotal }
    val totalFinal = venta.total
    val descuento = subtotal - totalFinal
    val isCredito = venta.tipoVenta.equals("CREDITO", ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // ── HEADER ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = AccentBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Detalle de Venta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BackgroundLight)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── INFO CLIENTE ──
                Card(
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = venta.nombreCliente, fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (!venta.nombreNegocio.isNullOrBlank()) {
                            Text(text = venta.nombreNegocio, fontSize = 13.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = formatearFechaHora(venta.fecha), fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = venta.tipoVenta.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCredito) WarningOrange else SuccessGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── LISTA PRODUCTOS ──
                Column(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    detalle.forEach { item ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text(text = item.nombreCompleto, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.cantidad} x $${item.precioUnitario}", fontSize = 13.sp, color = TextMuted)
                                Text("$${"%.2f".format(item.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                        }
                        HorizontalDivider(color = BackgroundLight)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── TOTALES ──
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = 14.sp, color = TextMuted)
                        Text("$${"%.2f".format(subtotal)}", fontSize = 14.sp, color = TextPrimary)
                    }
                    if (descuento > 0) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Descuento", fontSize = 14.sp, color = TextMuted)
                            Text("-$${"%.2f".format(descuento)}", fontSize = 14.sp, color = ErrorRed)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("$${"%.2f".format(totalFinal)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentTeal)
                    }

                    // SALDOS SI ES CRÉDITO
                    if (isCredito) {
                        val saldoPendiente = totalFinal - totalAbonos
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Abonado", fontSize = 14.sp, color = TextMuted)
                            Text("$${"%.2f".format(totalAbonos)}", fontSize = 14.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saldo Pendiente", fontSize = 14.sp, color = TextMuted)
                            Text(
                                text = "$${"%.2f".format(saldoPendiente)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (saldoPendiente > 0) ErrorRed else SuccessGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── BOTÓN IMPRIMIR ──
                Button(
                    onClick = onPrintRequest,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Imprimir Ticket", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── DIÁLOGOS DE UTILIDAD (Genéricos para el rediseño) ──

@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
        },
        text = {
            Text(text = message, fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 20.sp)
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun ResultDialog(
    title: String,
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    val iconColor = if (isSuccess) SuccessGreen else ErrorRed
    val iconBgColor = if (isSuccess) SuccessGreenSoft else ErrorRedSoft
    val icon = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
        },
        text = {
            Text(text = message, fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 20.sp)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aceptar", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun LoadingDialog(mensaje: String) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite,
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = mensaje, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
        }
    }
}