package com.example.repartidor.ui.screens.Inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCartCheckout
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
import com.example.repartidor.data.model.dclass.ReabastecimientoItem
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.viewmodel.Resumen.CierreMiniBodegaViewModel
import com.example.repartidor.viewmodel.Inventario.ReabastecimientoCarritoViewModel
import com.example.repartidor.viewmodel.Inventario.ReabastecimientoProcesoViewModel
import com.example.repartidor.ui.screens.components.* //Aqui estan los colores del tema

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoReabastecimientoScreen(
    carritoViewModel: ReabastecimientoCarritoViewModel,
    onVolver: () -> Unit,
    reabastecimientoProcesoViewModel: ReabastecimientoProcesoViewModel,
    onPedidoCompleto: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 🔹 Nuevos estados dinámicos para el diálogo de éxito
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successTitle by remember { mutableStateOf("¡Pedido Enviado!") }
    var successMessage by remember { mutableStateOf("") }

    val items by carritoViewModel.items.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val totalArticulos = remember(items) { items.sumOf { it.cantidad } }

    // ── DIÁLOGOS DE LA PANTALLA ───────────────────────────────────────────────

    // 1. Confirmación
    if (showConfirmDialog) {
        SoftDialog(
            icon = Icons.Default.Send,
            iconColor = AccentBlue,
            iconBg = AccentBlueSoft,
            title = "Confirmar pedido",
            message = "¿Seguro que deseas enviar el pedido de reabastecimiento a la matriz por un total de $totalArticulos artículos?",
            confirmText = "Sí, enviar",
            cancelText = "Cancelar",
            onDismiss = { if (!isLoading) showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                isLoading = true
                errorMessage = null

                reabastecimientoProcesoViewModel.enviarPedido(
                    items = items,
                    onSuccess = {
                        isLoading = false
                        carritoViewModel.limpiar()

                        successTitle = "¡Pedido Enviado!"
                        successMessage =
                            "El pedido de reabastecimiento se ha enviado correctamente."

                        showSuccessDialog = true
                    },
                    onError = { error ->
                        // Si falla aquí, el pedido NO se envió.
                        isLoading = false
                        errorMessage = "Error al enviar el pedido:\n$error"
                    }
                )
            }
        )
    }

    // 2. Éxito
    if (showSuccessDialog) {
        SoftDialog(
            icon = Icons.Default.CheckCircle,
            iconColor = AccentTeal,
            iconBg = AccentTealSoft,
            title = successTitle, // 🔹 Usamos la variable dinámica
            message = successMessage, // 🔹 Usamos la variable dinámica
            confirmText = "Aceptar",
            cancelText = null,
            onDismiss = {
                showSuccessDialog = false
                onPedidoCompleto()
            },
            onConfirm = {
                showSuccessDialog = false
                onPedidoCompleto()
            }
        )
    }

    // 3. Error
    if (errorMessage != null) {
        SoftDialog(
            icon = Icons.Default.Warning,
            iconColor = ErrorRed,
            iconBg = ErrorRedSoft,
            title = "Ocurrió un error",
            message = errorMessage ?: "Error desconocido",
            confirmText = "Aceptar",
            cancelText = null,
            onDismiss = { errorMessage = null },
            onConfirm = { errorMessage = null }
        )
    }

    // 4. Cargando
    if (isLoading) {
        LoadingDialog(mensaje = "Procesando pedido...\nPor favor, no cierres la app.")
    }
    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Resumen de Pedido",
                onBackClick = onVolver
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                ReabastecimientoBottomBar(
                    totalArticulos = totalArticulos,
                    onConfirmar = { showConfirmDialog = true },
                    enabled = !isLoading
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
                // ... (Mismo código de carrito vacío)
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        ReabastecimientoItemCard(
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
private fun EmptyState() {
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
                imageVector = Icons.Default.ShoppingCartCheckout,
                contentDescription = "Pedido Vacío",
                tint = TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu pedido está vacío",
            fontSize = 16.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── COMPONENTES ESPECÍFICOS (ReabastecimientoItemCard y BottomBar se mantienen igual) ──
@Composable
private fun ReabastecimientoItemCard(
    item: ReabastecimientoItem,
    viewModel: ReabastecimientoCarritoViewModel
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
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Cantidad solicitada:",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "-",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
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
                                    viewModel.actualizarCantidad(
                                        item.productoVariacionId,
                                        nuevaCantidad
                                    )
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
                            .padding(horizontal = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

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
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "+",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReabastecimientoBottomBar(
    totalArticulos: Int,
    onConfirmar: () -> Unit,
    enabled: Boolean
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total a solicitar", fontSize = 14.sp, color = TextMuted)
                    Text(
                        text = "$totalArticulos artículos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentIndigo
                    )
                }

                Button(
                    onClick = onConfirmar,
                    enabled = enabled,
                    modifier = Modifier
                        .height(54.dp)
                        .width(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentIndigo,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── DIÁLOGOS REUTILIZABLES (Estilo Soft) ──
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
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}