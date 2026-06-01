package com.example.repartidor.ui.screens.Resumen

import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.viewmodel.CierreMiniBodegaViewModel
import com.example.repartidor.viewmodel.ResumenDiaViewModel
import com.example.repartidor.ui.screens.components.*
import kotlinx.coroutines.delay

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenDiaScreen(
    viewModel: ResumenDiaViewModel,
    cerrarMiniBodegaViewModel: CierreMiniBodegaViewModel,
    printerManager: PrinterManager,
    printerRepository: PrinterRepository,
    bluetoothAdapter: BluetoothAdapter?,
    onBack: () -> Unit,
    onGoInventario: () -> Unit = {}
) {
    val state = viewModel.state
    val isLoading = viewModel.isLoading

    // 🔥 NUEVO: Variable para controlar el parpadeo de la primera carga
    var primeraCarga by remember { mutableStateOf(true) }

    // Checkboxes impresión
    var imprimirProductos by remember { mutableStateOf(true) }
    var imprimirDinero by remember { mutableStateOf(true) }
    var imprimirInventario by remember { mutableStateOf(false) }
    var imprimirDevoluciones by remember { mutableStateOf(false) }

    // Estados de modales
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }
    var showCerrarDiaDialog by remember { mutableStateOf(false) }

    var textoConfirmacion by remember { mutableStateOf("") }
    var isClosing by remember { mutableStateOf(false) }
    var errorCierre by remember { mutableStateOf<String?>(null) }
    var successCierre by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }

    fun getInicioDelDia6AM(): Long {
        val now = java.time.ZonedDateTime.now()
        val inicio = now.withHour(6).withMinute(0).withSecond(0).withNano(0)
        return if (now.isBefore(inicio)) {
            inicio.minusDays(1).toInstant().toEpochMilli()
        } else {
            inicio.toInstant().toEpochMilli()
        }
    }

    fun getFechaHoy(): String {
        return java.time.LocalDate.now().toString()
    }

    val inicioDia = remember { getInicioDelDia6AM() }
    val fechaHoy = remember { getFechaHoy() }

    fun ejecutarCierre() {
        isClosing = true
        loadingMessage = "Enviando inventario..."

        viewModel.syncFinalDelDia(
            inicio = inicioDia,
            fin = System.currentTimeMillis(),
            fechaHoy = fechaHoy,
            onSuccess = {
                cerrarMiniBodegaViewModel.cerrarMiniBodega(
                    onSuccess = {
                        isClosing = false
                        successCierre = true
                    },
                    onError = { error ->
                        isClosing = false
                        errorCierre = error
                    }
                )
            },
            onError = { error ->
                isClosing = false
                errorCierre = error
            }
        )
    }

    // 🔥 NUEVO: Agregamos un pequeño delay para dar tiempo al ViewModel de actualizar
    LaunchedEffect(Unit) {
        viewModel.cargarResumen()
        delay(100)
        primeraCarga = false
    }

    // ── DIÁLOGOS DE LA PANTALLA ───────────────────────────────────────────────

    if (showConfirmDialog) {
        SoftDialog(
            icon = Icons.Default.Print,
            iconColor = AccentBlue,
            iconBg = AccentBlueSoft,
            title = "Confirmar impresión",
            message = "¿Seguro que quieres imprimir el resumen del día?",
            confirmText = "Sí, imprimir",
            cancelText = "Cancelar",
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                loadingMessage = "Conectando con la impresora..."
                showLoadingDialog = true

                viewModel.generarTicket(
                    imprimirProductos, imprimirDinero, imprimirInventario, imprimirDevoluciones
                ) { ticket ->
                    viewModel.imprimirTicket(
                        ticket = ticket,
                        printerManager = printerManager,
                        printerRepository = printerRepository,
                        adapter = bluetoothAdapter
                    ) { result ->
                        showLoadingDialog = false
                        mensajeResultado = when (result) {
                            is PrintResult.Success -> "Impresión completada correctamente."
                            is PrintResult.NoPrinter -> "No hay impresora configurada."
                            is PrintResult.BluetoothOff -> "Bluetooth apagado."
                            is PrintResult.Error -> "Ocurrió un error al imprimir.\nVerifica la conexión."
                        }
                        showResultDialog = true
                    }
                }
            }
        )
    }

    if (showResultDialog) {
        val isError = !mensajeResultado.contains("correctamente")
        SoftDialog(
            icon = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
            iconColor = if (isError) WarningOrange else AccentTeal,
            iconBg = if (isError) WarningOrangeSoft else AccentTealSoft,
            title = if (isError) "Atención" else "¡Éxito!",
            message = mensajeResultado,
            confirmText = "Aceptar",
            cancelText = null,
            onDismiss = { showResultDialog = false },
            onConfirm = { showResultDialog = false }
        )
    }

    if (errorCierre != null) {
        SoftDialog(
            icon = Icons.Default.Warning,
            iconColor = ErrorRed,
            iconBg = ErrorRedSoft,
            title = "Error al cerrar día",
            message = errorCierre!!,
            confirmText = "Reintentar",
            cancelText = "Cancelar",
            onDismiss = { errorCierre = null },
            onConfirm = {
                errorCierre = null
                ejecutarCierre()
            },
            confirmIsDestructive = true
        )
    }

    if (successCierre) {
        SoftDialog(
            icon = Icons.Default.CheckCircle,
            iconColor = AccentTeal,
            iconBg = AccentTealSoft,
            title = "¡Día finalizado!",
            message = "Inventario enviado y día cerrado correctamente.",
            confirmText = "OK",
            cancelText = null,
            onDismiss = { successCierre = false; onBack() },
            onConfirm = { successCierre = false; onBack() }
        )
    }

    if (showCerrarDiaDialog) {
        AlertDialog(
            onDismissRequest = { showCerrarDiaDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(ErrorRedSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Text("Cerrar día", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Esta acción cerrará el día y ya no podrás realizar ventas, abonos ni devoluciones.",
                        fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Etiqueta superior en lugar de Label flotante
                    Text(
                        text = "Escribe 'Aceptar' para confirmar:",
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
                        textAlign = TextAlign.Start
                    )

                    OutlinedTextField(
                        value = textoConfirmacion,
                        onValueChange = { textoConfirmacion = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BackgroundLight,
                            unfocusedContainerColor = BackgroundLight,
                            focusedBorderColor = ErrorRed,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCerrarDiaDialog = false
                        ejecutarCierre()
                    },
                    enabled = textoConfirmacion == "Aceptar",
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar Cierre", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCerrarDiaDialog = false
                        textoConfirmacion = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showLoadingDialog || isClosing) {
        LoadingDialog(mensaje = loadingMessage)
    }

    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Resumen del Día",
                onBackClick = onBack
            )
        }
    ) { padding ->

        // 🔥 NUEVO: Evaluamos también la primeraCarga aquí
        if (isLoading || primeraCarga) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 💰 TARJETA DINERO
                SummaryCard(
                    title = "Dinero",
                    icon = Icons.Default.AttachMoney,
                    accent = AccentTeal,
                    accentBg = AccentTealSoft
                ) {
                    InfoRow("Efectivo en ventas", "$${"%.2f".format(state.efectivoVentas)}")
                    InfoRow("Abonos recibidos", "$${"%.2f".format(state.totalAbonos)}")

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔥 SECCIÓN PROTAGONISTA: Total Efectivo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AccentTealSoft)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Efectivo",
                                color = AccentTeal,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$${"%.2f".format(state.totalEfectivo)}",
                                color = AccentTeal,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Aviso de créditos pendientes
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BackgroundLight)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${state.cantidadCreditos} créditos pendientes suman $${"%.2f".format(state.totalPendiente)}",
                            fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 📦 TARJETA PRODUCTOS
                SummaryCard(
                    title = "Productos Vendidos",
                    icon = Icons.Default.ShoppingCart,
                    accent = AccentBlue,
                    accentBg = AccentBlueSoft
                ) {
                    if (state.productosVendidos.isEmpty()) {
                        Text("No hubo ventas de productos hoy.", color = TextMuted, fontSize = 14.sp)
                    } else {
                        state.productosVendidos.forEach {
                            // Agregamos .toInt() para eliminar los decimales
                            InfoRow(it.nombre, "${it.cantidad.toInt()} pz.")
                        }
                    }
                }

                // 🔄 TARJETA DEVOLUCIONES
                SummaryCard(
                    title = "Productos Devueltos",
                    icon = Icons.Default.KeyboardReturn,
                    accent = WarningOrange,
                    accentBg = WarningOrangeSoft
                ) {
                    if (state.productosDevueltos.isEmpty()) {
                        Text("No hubo devoluciones hoy.", color = TextMuted, fontSize = 14.sp)
                    } else {
                        state.productosDevueltos.forEach {
                            InfoRow(it.nombre, "${it.cantidad} un.")
                        }
                    }
                }

                // 🚚 BOTÓN INVENTARIO (Estilo Tonal)
                Button(
                    onClick = onGoInventario,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentIndigo.copy(alpha = 0.1f), // Fondo extra suave
                        contentColor = AccentIndigo // Texto e ícono fuerte
                    )
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ir a inventario de camioneta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // 🧾 TARJETA IMPRESIÓN
                SummaryCard(
                    title = "Opciones de Impresión",
                    icon = Icons.Default.Print,
                    accent = TextPrimary,
                    accentBg = BorderLight
                ) {
                    CustomCheckboxRow("Total de productos vendidos", imprimirProductos) { imprimirProductos = it }
                    CustomCheckboxRow("Dinero en ventas", imprimirDinero) { imprimirDinero = it }
                    CustomCheckboxRow("Inventario de camioneta", imprimirInventario) { imprimirInventario = it }
                    CustomCheckboxRow("Productos devueltos", imprimirDevoluciones) { imprimirDevoluciones = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔥 BOTÓN IMPRIMIR (Estilo Tonal)
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlueSoft,
                            contentColor = AccentBlue
                        )
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Imprimir Resumen", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // 🔒 BOTÓN FINALIZAR DÍA (Estilo Tonal)

                OutlinedButton(
                    onClick = { showCerrarDiaDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, ErrorRed), // Contorno firme y definido
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ErrorRedSoft,
                        contentColor = ErrorRed // Texto e ícono en rojo
                    )
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar el Día", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// ── COMPONENTES UI INTERNOS ───────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    accentBg: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = if (isBold) TextPrimary else TextMuted, fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp)
        Text(text = value, color = TextPrimary, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun CustomCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = AccentBlue, uncheckedColor = BorderLight)
        )
        Text(text = label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ── DIÁLOGOS REUTILIZABLES ───────────────────────────────────────────────────

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
                modifier = Modifier.size(56.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
        },
        text = {
            Text(
                text = message, fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = cancelText?.let {
            {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
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