package com.example.repartidor.ui.screens.DevolucionesDia

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
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
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
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.DetalleDevolucionUI
import com.example.repartidor.data.model.dclass.DevolucionUI
import com.example.repartidor.data.model.dclass.SustitucionDevolucionUI
import com.example.repartidor.ui.screens.components.*
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.formatearFechaHora
import com.example.repartidor.viewmodel.Devoluciones.DevolucionesDiaViewModel

@RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DevolucionesDiaScreen(
    onBack: () -> Unit,
    viewModel: DevolucionesDiaViewModel,
    sessionManager: SessionManager
) {
    val devoluciones = viewModel.devoluciones
    val mostrarDialogo = viewModel.mostrarDialogo
    val detalle = viewModel.detalleDevolucion
    val sustituciones = viewModel.sustitucionesDevolucion
    val devolucionSeleccionada = viewModel.devolucionSeleccionada
    val cancelandoDevolucion = viewModel.cancelandoDevolucion
    val finalDia by sessionManager.finalDiaFlow.collectAsState(initial = false)

    var showConfirmPrint by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }
    var isPrinting by remember { mutableStateOf(false) }
    var isSuccessPrint by remember { mutableStateOf(false) }

    var showConfirmCancel by remember { mutableStateOf(false) }
    var showResultCancel by remember { mutableStateOf(false) }
    var mensajeCancelacion by remember { mutableStateOf("") }
    var isSuccessCancel by remember { mutableStateOf(false) }
    var motivoCancelacion by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarDevoluciones()
    }

    LaunchedEffect(viewModel.resultadoCancelacion) {
        viewModel.resultadoCancelacion?.let { resultado ->
            mensajeCancelacion = resultado.mensaje
            isSuccessCancel = resultado.exitoso
            showResultCancel = true
        }
    }

    // ── DIÁLOGO DE DETALLE DE DEVOLUCIÓN ──
    if (mostrarDialogo && devolucionSeleccionada != null) {
        DetalleDevolucionDialog(
            devolucion = devolucionSeleccionada,
            detalle = detalle,
            sustituciones = sustituciones,
            finalDia = finalDia,
            onDismiss = { viewModel.cerrarDialogo() },
            onPrintRequest = { showConfirmPrint = true },
            onCancelRequest = {
                motivoCancelacion = ""
                showConfirmCancel = true
            }
        )
    }

    // ── DIÁLOGO CONFIRMAR IMPRESIÓN ──
    if (showConfirmPrint) {
        ConfirmActionDialog(
            title = "Imprimir Ticket",
            message = "¿Deseas imprimir nuevamente el ticket de esta devolución?",
            confirmText = "Sí, imprimir",
            icon = Icons.Default.Print,
            iconColor = AccentRed,
            iconBgColor = AccentRedSoft,
            onDismiss = {
                showConfirmPrint = false
            },
            onConfirm = {
                showConfirmPrint = false
                isPrinting = true

                viewModel.imprimirDevolucionSeleccionada { result ->
                    isPrinting = false
                    isSuccessPrint = result is PrintResult.Success

                    mensajeResultado = when (result) {
                        is PrintResult.Success ->
                            "Ticket impreso correctamente."

                        is PrintResult.NoPrinter ->
                            "No hay impresora configurada."

                        is PrintResult.BluetoothOff ->
                            "El Bluetooth está apagado."

                        is PrintResult.Error -> {
                            val msg = result.msg.lowercase()

                            when {
                                msg.contains("timeout") ->
                                    "No se pudo conectar a la impresora (timeout)."

                                msg.contains("socket") ->
                                    "Error de conexión con la impresora Bluetooth."

                                msg.contains("connect") ->
                                    "No se pudo establecer conexión con la impresora."

                                else ->
                                    "Error al imprimir:\n${result.msg}"
                            }
                        }
                    }

                    showResultDialog = true
                }
            }
        )
    }

    // ── DIÁLOGO CONFIRMAR CANCELACIÓN ──
    if (showConfirmCancel && devolucionSeleccionada != null) {
        CancelarDevolucionDialog(
            motivo = motivoCancelacion,
            onMotivoChange = {
                motivoCancelacion = it
            },
            cancelandoDevolucion = cancelandoDevolucion,
            onDismiss = {
                if (!cancelandoDevolucion) {
                    showConfirmCancel = false
                }
            },
            onConfirm = {
                if (motivoCancelacion.isNotBlank()) {
                    showConfirmCancel = false

                    viewModel.cancelarDevolucion(
                        motivo = motivoCancelacion.trim()
                    )
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
            onDismiss = {
                showResultDialog = false
            }
        )
    }

    // ── DIÁLOGO RESULTADO CANCELACIÓN ──
    if (showResultCancel) {
        ResultDialog(
            title = if (isSuccessCancel) {
                "Devolución cancelada"
            } else {
                "Error"
            },
            message = mensajeCancelacion,
            isSuccess = isSuccessCancel,
            onDismiss = {
                showResultCancel = false
                viewModel.limpiarResultadoCancelacion()
            }
        )
    }

    // ── DIÁLOGO DE CARGA ──
    if (isPrinting) {
        LoadingDialog(
            mensaje = "Imprimiendo ticket..."
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Devoluciones del Día",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (devoluciones.isEmpty()) {

                // ── ESTADO VACÍO ──
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                contentDescription = "Sin devoluciones",
                                modifier = Modifier.size(42.dp),
                                tint = AccentRed.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Aún no hay devoluciones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Las devoluciones que realices hoy aparecerán aquí.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

            } else {

                // ── LISTA DE DEVOLUCIONES ──
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = devoluciones,
                        key = { it.id }
                    ) { devolucion ->

                        DevolucionCardRediseñada(
                            devolucion = devolucion,
                            onClick = {
                                viewModel.seleccionarDevolucion(devolucion)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── TARJETA DE DEVOLUCIÓN ─────────────────────────────────────────────────────

@Composable
private fun DevolucionCardRediseñada(
    devolucion: DevolucionUI,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ── ICONO ──
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentRedSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ── INFORMACIÓN ──
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = devolucion.nombreCliente,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )

                if (!devolucion.nombreNegocio.isNullOrBlank()) {
                    Text(
                        text = devolucion.nombreNegocio,
                        color = TextMuted,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatearFechaHora(devolucion.fecha),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── TOTAL Y TIPO ──
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${"%.2f".format(devolucion.total)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentRed
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentRedSoft)
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {
                    Text(
                        text = devolucion.tipo.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }
        }
    }
}

// ── DIÁLOGO DE DETALLE ───────────────────────────────────────────────────────

@Composable
private fun DetalleDevolucionDialog(
    devolucion: DevolucionUI,
    detalle: List<DetalleDevolucionUI>,
    sustituciones: List<SustitucionDevolucionUI>,
    finalDia: Boolean,
    onDismiss: () -> Unit,
    onPrintRequest: () -> Unit,
    onCancelRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardReturn,
                            contentDescription = null,
                            tint = AccentRed
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Detalle de Devolución",
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
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── INFORMACIÓN CLIENTE ──
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = BackgroundLight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = devolucion.nombreCliente,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (!devolucion.nombreNegocio.isNullOrBlank()) {
                            Text(
                                text = devolucion.nombreNegocio,
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatearFechaHora(devolucion.fecha),
                                fontSize = 12.sp,
                                color = TextMuted
                            )

                            Text(
                                text = devolucion.tipo.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── LISTA DE PRODUCTOS ──
                Column(
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    Text(
                        text = "Productos devueltos",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    detalle.forEach { item ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = item.nombreCompleto,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.cantidad} x $${"%.2f".format(item.precioUnitario)}",
                                    fontSize = 13.sp,
                                    color = TextMuted
                                )

                                Text(
                                    text = "$${"%.2f".format(item.subtotal)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }

                        HorizontalDivider(
                            color = BackgroundLight
                        )
                    }

                    // ── SUSTITUCIONES ──
                    if (sustituciones.isNotEmpty()) {

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Productos entregados por cambio",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        sustituciones.forEach { item ->

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = item.nombreCompleto,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.cantidad} x $${"%.2f".format(item.precioUnitario)}",
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )

                                    Text(
                                        text = "$${"%.2f".format(item.subtotal)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = BackgroundLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── TOTAL ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total devolución",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "$${"%.2f".format(devolucion.total)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── IMPRIMIR ──
                Button(
                    onClick = onPrintRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        "Imprimir Ticket",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // ── CANCELAR ──
                if (!finalDia) {

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onCancelRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "Cancelar Devolución",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ── DIÁLOGO CONFIRMAR ACCIÓN ──────────────────────────────────────────────────

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
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cancelar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    confirmText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

// ── DIÁLOGO CANCELAR DEVOLUCIÓN ───────────────────────────────────────────────

@Composable
private fun CancelarDevolucionDialog(
    motivo: String,
    onMotivoChange: (String) -> Unit,
    cancelandoDevolucion: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!cancelandoDevolucion) {
                onDismiss()
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ErrorRedSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Cancelar devolución",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {

                Text(
                    text = "¿Estás seguro de cancelar esta devolución? Se revertirán los movimientos de inventario y se eliminará la merma correspondiente.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = motivo,
                    onValueChange = onMotivoChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Motivo de cancelación")
                    },
                    placeholder = {
                        Text("Escribe el motivo...")
                    },
                    minLines = 3,
                    maxLines = 4,
                    enabled = !cancelandoDevolucion,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ErrorRed,
                        focusedLabelColor = ErrorRed,
                        cursorColor = ErrorRed
                    )
                )

                if (motivo.isBlank()) {

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "El motivo es obligatorio.",
                        fontSize = 12.sp,
                        color = ErrorRed
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !cancelandoDevolucion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Regresar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = motivo.isNotBlank() && !cancelandoDevolucion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cancelandoDevolucion) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    if (cancelandoDevolucion) {
                        "Cancelando..."
                    } else {
                        "Sí, cancelar"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

// ── RESULTADO ─────────────────────────────────────────────────────────────────

@Composable
private fun ResultDialog(
    title: String,
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    val iconColor = if (isSuccess) SuccessGreen else ErrorRed
    val iconBgColor = if (isSuccess) SuccessGreenSoft else ErrorRedSoft
    val icon = if (isSuccess) {
        Icons.Default.CheckCircle
    } else {
        Icons.Default.Info
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Aceptar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

// ── CARGA ─────────────────────────────────────────────────────────────────────

@Composable
private fun LoadingDialog(mensaje: String) {
    Dialog(
        onDismissRequest = {}
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceWhite
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = AccentBlue,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = mensaje,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}