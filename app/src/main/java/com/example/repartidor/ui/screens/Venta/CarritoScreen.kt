package com.example.repartidor.ui.screens.Venta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.viewmodel.CarritoViewModel
import com.example.repartidor.viewmodel.VentaProcesoViewModel
import com.example.repartidor.utils.PrintResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Paleta de colores (Heredada de Home y Venta) ─────────────────────────────
private val BackgroundLight  = Color(0xFFF4F6FB)
private val SurfaceWhite     = Color(0xFFFFFFFF)
private val AccentBlue       = Color(0xFF3A6FD8)
private val AccentBlueSoft   = Color(0xFFEBF0FC)
private val AccentIndigo     = Color(0xFF5B4CF5)
private val AccentTeal       = Color(0xFF0F9E82)
private val AccentTealSoft   = Color(0xFFE6F6F2)
private val TextPrimary      = Color(0xFF111827)
private val TextMuted        = Color(0xFF9CA3AF)
private val ErrorRed         = Color(0xFFDC2626)
private val ErrorRedSoft     = Color(0xFFFEF2F2)
private val WarningOrange    = Color(0xFFF59E0B)
private val WarningOrangeSoft= Color(0xFFFEF3C7)
// ─────────────────────────────────────────────────────────────────────────────

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

    val subtotal = remember(items) { items.sumOf { it.precio * it.cantidad } }
    val porcentajeDescuento = cliente?.porcentajeDescuento ?: 0.0
    val descuento = remember(subtotal, porcentajeDescuento) {
        if (porcentajeDescuento > 0) subtotal * (porcentajeDescuento / 100) else 0.0
    }
    val totalFinal = subtotal - descuento

    var isCheckingPrinter by remember { mutableStateOf(false) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var printerStatus by remember { mutableStateOf<PrintResult?>(null) }
    var imprimir by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun verificarImpresoraConLoading(onSuccess: () -> Unit, onError: () -> Unit) {
        isCheckingPrinter = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                ventaProcesoViewModel.verificarImpresora()
            }
            isCheckingPrinter = false
            if (result is PrintResult.Success) {
                imprimir = true
                onSuccess()
            } else {
                printerStatus = result
                onError()
            }
        }
    }

    // ── DIÁLOGOS DE LA PANTALLA ───────────────────────────────────────────────

    if (showConfirmDialog) {
        SoftDialog(
            icon = Icons.Default.ShoppingCart,
            iconColor = AccentBlue,
            iconBg = AccentBlueSoft,
            title = "Confirmar venta",
            message = "¿Seguro que deseas proceder con la venta de estos artículos por un total de $${"%.2f".format(totalFinal)}?",
            confirmText = "Sí, confirmar",
            cancelText = "Cancelar",
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                isLoading = true
                ventaProcesoViewModel.confirmarVenta(
                    items = items,
                    imprimir = imprimir,
                    onSuccess = { result ->
                        isLoading = false
                        mensajeResultado = when (result) {
                            is PrintResult.Success -> "Venta realizada y ticket impreso correctamente."
                            is PrintResult.NoPrinter -> "Venta realizada.\nNo hay impresora configurada."
                            is PrintResult.BluetoothOff -> "Venta realizada.\nBluetooth apagado."
                            is PrintResult.Error -> "Venta realizada.\nError al imprimir:\n${result.msg}"
                        }
                        showResultDialog = true
                        carritoViewModel.limpiar()
                        ventaProcesoViewModel.reset()
                    },
                    onError = { error ->
                        isLoading = false
                        mensajeResultado = "Error en la venta:\n$error"
                        showResultDialog = true
                    }
                )
            }
        )
    }

    if (showPrinterDialog) {
        SoftDialog(
            icon = Icons.Default.Print,
            iconColor = WarningOrange,
            iconBg = WarningOrangeSoft,
            title = "Impresora no disponible",
            message = when (printerStatus) {
                is PrintResult.BluetoothOff -> "El Bluetooth está apagado."
                is PrintResult.NoPrinter -> "No hay impresora configurada."
                is PrintResult.Error -> "No se pudo conectar a la impresora."
                else -> "Error desconocido."
            } + "\n\n¿Deseas continuar sin imprimir o cancelar?",
            confirmText = "Continuar sin imprimir",
            cancelText = "Cancelar",
            onDismiss = {
                showPrinterDialog = false // <-- Solo cierra la modal, cancelando el proceso
            },
            onConfirm = {
                imprimir = false
                showPrinterDialog = false
                showConfirmDialog = true
            },
            confirmIsDestructive = false
        )
    }

    if (showResultDialog) {
        val isError = mensajeResultado.contains("Error en la venta", ignoreCase = true)
        SoftDialog(
            icon = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
            iconColor = if (isError) ErrorRed else AccentTeal,
            iconBg = if (isError) ErrorRedSoft else AccentTealSoft,
            title = if (isError) "Venta fallida" else "¡Venta Exitosa!",
            message = mensajeResultado,
            confirmText = "Aceptar",
            cancelText = null,
            onDismiss = {
                showResultDialog = false
                if (!isError) onVentaExitosa()
            },
            onConfirm = {
                showResultDialog = false
                if (!isError) onVentaExitosa()
            }
        )
    }

    if (isLoading || isCheckingPrinter) {
        LoadingDialog(mensaje = if (isLoading) "Procesando venta..." else "Verificando impresora...")
    }

    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Mi Carrito",
                onBackClick = onVolver
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                CarritoBottomBar(
                    subtotal = subtotal,
                    porcentajeDescuento = porcentajeDescuento,
                    descuento = descuento,
                    totalFinal = totalFinal,
                    onConfirmar = {
                        verificarImpresoraConLoading(
                            onSuccess = { showConfirmDialog = true },
                            onError = { showPrinterDialog = true }
                        )
                    }
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SurfaceWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Carrito Vacío",
                            tint = TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "El carrito está vacío",
                        fontSize = 16.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        CarritoItemCard(
                            item = item,
                            viewModel = carritoViewModel
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun CarritoItemCard(
    item: com.example.repartidor.data.model.CarritoItem,
    viewModel: CarritoViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${item.productoNombre} - ${item.presentacionNombre}",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Precio: $${"%.2f".format(item.precio)}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Subtotal: $${"%.2f".format(item.precio * item.cantidad)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                }

                // Controles de cantidad Soft UI
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón Menos
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundLight)
                            .clickable {
                                val nueva = (item.cantidad - 1).coerceAtLeast(0)
                                viewModel.actualizarCantidad(item.productoVariacionId, nueva)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-", tint = TextPrimary, modifier = Modifier.size(18.dp))
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
                                    viewModel.actualizarCantidad(item.productoVariacionId, nuevaCantidad)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier
                            .width(72.dp)
                            //.height(44.dp)
                            .padding(horizontal = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    // Botón Más
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentBlueSoft)
                            .clickable {
                                val nueva = item.cantidad + 1
                                viewModel.actualizarCantidad(item.productoVariacionId, nueva)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+", tint = AccentBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CarritoBottomBar(
    subtotal: Double,
    porcentajeDescuento: Double,
    descuento: Double,
    totalFinal: Double,
    onConfirmar: () -> Unit
) {
    Surface(
        color = SurfaceWhite,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Subtotal", fontSize = 13.sp, color = TextMuted)
                    Text("$${"%.2f".format(subtotal)}", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)

                    if (porcentajeDescuento > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Descuento (${porcentajeDescuento.toInt()}%)", fontSize = 12.sp, color = TextMuted)
                        Text("-$${"%.2f".format(descuento)}", fontSize = 13.sp, color = ErrorRed, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total", fontSize = 14.sp, color = TextMuted)
                    Text(
                        text = "$${"%.2f".format(totalFinal)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentIndigo
                    )
                }

                Button(
                    onClick = onConfirmar,
                    modifier = Modifier
                        .height(54.dp)
                        .width(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── DIÁLOGOS REUTILIZABLES (Estilo Soft) ─────────────────────────────────────

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
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
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