package com.example.repartidor.ui.screens.Abonos

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.viewmodel.AbonoResult
import com.example.repartidor.viewmodel.AbonoViewModel
import com.example.repartidor.viewmodel.AbonosFormViewModel
import kotlinx.coroutines.delay // Importamos el delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.repartidor.ui.screens.components.* // Aquí están los colores del tema

@OptIn(ExperimentalMaterial3Api::class)
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

    // 🔥 NUEVO: Variable para controlar el parpadeo de la primera carga
    var primeraCarga by remember { mutableStateOf(true) }

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
        when (val result = abonoViewModel.abonoResult) {
            is AbonoResult.Success -> {
                showLoadingDialog = false
                showSuccessDialog = true
                abonoViewModel.resetResult()
            }
            is AbonoResult.PrintError -> {
                showLoadingDialog = false
                showPrinterErrorDialog = true
                abonoViewModel.resetResult()
            }
            is AbonoResult.Error -> {
                showLoadingDialog = false
                errorMessage = result.message
                showErrorDialog = true
                abonoViewModel.resetResult()
            }
            else -> {}
        }
    }

    // 🔥 NUEVO: Agregamos el delay(100) para dar tiempo al ViewModel
    LaunchedEffect(ventaId) {
        viewModel.cargarVenta(ventaId)
        delay(100)
        primeraCarga = false
    }

    val montoFormateado = "%.2f".format(montoConfirmado)
    if (showDialog) {
        SoftDialog(
            icon = Icons.Default.AccountBalanceWallet,
            iconColor = AccentBlue,
            iconBg = AccentBlueSoft,
            title = "Confirmar pago",
            message = if (esPagoTotal) "¿Seguro que deseas liquidar la deuda de $${montoFormateado}?" else "¿Seguro que deseas abonar $${montoConfirmado}?",
            confirmText = "Confirmar",
            cancelText = "Cancelar",
            onDismiss = { showDialog = false },
            onConfirm = {
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
        )
    }

    if (showSuccessDialog) {
        val print = abonoViewModel.printResult
        val mensaje = when (print) {
            is PrintResult.Success -> "Abono registrado e impreso correctamente."
            is PrintResult.NoPrinter -> "Abono registrado.\n(Sin impresora configurada)"
            is PrintResult.BluetoothOff -> "Abono registrado.\n(Bluetooth apagado)"
            is PrintResult.Error -> "Abono registrado.\n(Error al imprimir)"
            else -> "Abono registrado correctamente."
        }
        SoftDialog(
            icon = Icons.Default.CheckCircle,
            iconColor = AccentTeal,
            iconBg = AccentTealSoft,
            title = "¡Éxito!",
            message = mensaje,
            confirmText = "Aceptar",
            cancelText = null,
            onDismiss = { showSuccessDialog = false; onSuccess() },
            onConfirm = { showSuccessDialog = false; onSuccess() }
        )
    }

    if (showErrorDialog) {
        SoftDialog(
            icon = Icons.Default.Warning,
            iconColor = ErrorRed,
            iconBg = ErrorRedSoft,
            title = "Error",
            message = errorMessage,
            confirmText = "Aceptar",
            cancelText = null,
            onDismiss = { showErrorDialog = false },
            onConfirm = { showErrorDialog = false }
        )
    }

    if (showLoadingDialog) {
        LoadingDialog(mensaje = "Procesando pago...")
    }

    if (showPrinterErrorDialog) {
        val print = abonoViewModel.printResult
        val mensajeImpresora = when (print) {
            is PrintResult.NoPrinter -> "No hay impresora configurada."
            is PrintResult.BluetoothOff -> "El Bluetooth está apagado."
            is PrintResult.Error -> "Error al conectar con la impresora."
            else -> "No se pudo imprimir."
        }
        SoftDialog(
            icon = Icons.Default.Print,
            iconColor = WarningOrange,
            iconBg = WarningOrangeSoft,
            title = "Impresora no disponible",
            message = "$mensajeImpresora\n\n¿Deseas continuar sin imprimir?",
            confirmText = "Seguir sin imprimir",
            cancelText = "Cancelar",
            onDismiss = { showPrinterErrorDialog = false },
            onConfirm = {
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
                        imprimir = false
                    )
                }
            }
        )
    }
    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BackgroundLight,
        topBar = { StandardTopBar(title = "Detalle del Abono", onBackClick = onBack) }
    ) { paddingValues ->

        // 🔥 NUEVO: Ahora también validamos primeraCarga aquí
        if (viewModel.isLoading || primeraCarga) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
            return@Scaffold
        }

        venta?.let { detalle ->
            val subtotal = detalle.productos.sumOf { p -> p.cantidad * p.precioUnitario }
            val descuentoDinero = subtotal - detalle.info.total

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1. TARJETA DE CLIENTE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlueSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(detalle.info.clienteNombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                if (detalle.info.clienteNegocio.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(detalle.info.clienteNegocio, color = TextMuted, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderLight)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Fecha de venta", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    text = formatearFecha(detalle.info.fecha.toString()),
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Tipo", fontSize = 12.sp, color = TextMuted)
                                Text(detalle.info.tipoVenta, fontSize = 14.sp, color = WarningOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. TARJETA DE PRODUCTOS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = AccentBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resumen de artículos", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        detalle.productos.forEach { p ->
                            val subtotalProducto = p.cantidad * p.precioUnitario
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.nombre, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                    Text("${p.cantidad} x $${p.precioUnitario}", fontSize = 12.sp, color = TextMuted)
                                }
                                Text("$${"%.2f".format(subtotalProducto)}", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // 3. TARJETA DE ESTADO DE CUENTA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = TextMuted, fontSize = 14.sp)
                            Text("$${"%.2f".format(subtotal)}", color = TextPrimary, fontSize = 14.sp)
                        }

                        if (detalle.info.porcentajeDescuento > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Descuento (${detalle.info.porcentajeDescuento}%)", color = AccentTeal, fontSize = 14.sp)
                                Text("-$${"%.2f".format(descuentoDinero)}", color = AccentTeal, fontSize = 14.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Original", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("$${"%.2f".format(detalle.info.total)}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Abonado hasta ahora", color = TextMuted, fontSize = 14.sp)
                            Text("$${"%.2f".format(detalle.info.totalAbonado)}", color = TextMuted, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentBlueSoft)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Saldo Pendiente", color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("$${"%.2f".format(detalle.info.saldoPendiente)}", color = AccentBlue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // 4. SECCIÓN DE PAGO
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Etiqueta estática en lugar de label animado
                    Text(
                        text = "Monto a abonar",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = monto,
                        onValueChange = {
                            if (it.matches(Regex("^\\d*\\.?\\d*\$"))) monto = it
                        },
                        leadingIcon = { Text("$", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Principal
                    Button(
                        onClick = {
                            montoConfirmado = monto.toDoubleOrNull() ?: 0.0
                            esPagoTotal = false
                            showDialog = true
                        },
                        enabled = monto.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Registrar Abono", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SurfaceWhite)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Secundario
                    OutlinedButton(
                        onClick = {
                            montoConfirmado = detalle.info.saldoPendiente
                            esPagoTotal = true
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, AccentBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceWhite,
                            contentColor = AccentBlue
                        )
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Liquidar Deuda ($${"%.2f".format(detalle.info.saldoPendiente)})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── FUNCIÓN DE AYUDA PARA FORMATEAR EL TIMESTAMP ──────────────────────────────
private fun formatearFecha(fechaRaw: String): String {
    return try {
        val timestamp = fechaRaw.toLong()
        val ms = if (fechaRaw.length <= 10) timestamp * 1000 else timestamp
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(ms))
    } catch (e: Exception) {
        fechaRaw
    }
}

// ── COMPONENTE SOFT DIALOG (Actualizado para usar AlertDialog de Material 3) ──
@Composable
private fun SoftDialog(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmIsDestructive: Boolean = false
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
                    .background(iconBg),
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
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = cancelText?.let {
            {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (confirmIsDestructive) ErrorRed else AccentBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

// ── COMPONENTE LOADING DIALOG (Actualizado) ──────────────────────────────────
@Composable
private fun LoadingDialog(mensaje: String) {
    Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier.width(280.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = mensaje,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}