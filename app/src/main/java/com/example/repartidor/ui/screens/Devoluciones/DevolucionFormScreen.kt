package com.example.repartidor.ui.screens.Devoluciones

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PrintDisabled
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.ui.screens.Cliente.ClienteCard
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.viewmodel.CarritoDevolucionViewModel
import com.example.repartidor.viewmodel.ClienteViewModel
import com.example.repartidor.viewmodel.DevolucionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.repartidor.ui.screens.components.* //Aqui estan los colores del tema


@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevolucionFormScreen(
    carritoViewModel: CarritoDevolucionViewModel,
    clienteViewModel: ClienteViewModel,
    devolucionViewModel: DevolucionViewModel,
    onBack: () -> Unit,
    onDevolucionExitosa: () -> Unit = {}
) {
    val items by carritoViewModel.items.collectAsState()
    val cliente by remember { derivedStateOf { clienteViewModel.cliente } }
    val resultados = clienteViewModel.resultados

    val error = devolucionViewModel.error
    val success = devolucionViewModel.success

    var motivo by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }

    var clienteBuscado by remember { mutableStateOf("") }
    var clienteNulo by remember { mutableStateOf(false) }
    var esExito by remember { mutableStateOf(false) }

    var imprimir by remember { mutableStateOf(true) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var printerStatus by remember { mutableStateOf<PrintResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Estado para la animación de ocultar/mostrar detalles (Inicia oculto)
    var showDetalles by remember { mutableStateOf(false) }

    val motivos = listOf(
        "Devolución de cliente",
        "Producto dañado",
        "Merma",
        "Producto roto",
        "Error de manejo",
        "Otro"
    )

    val totalProductos = remember(items) { items.sumOf { it.cantidad } }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            esExito = false
            mensajeResultado = it
            showResultDialog = true
            isProcessing = false
        }
    }

    LaunchedEffect(success) {
        if (success) {
            val print = devolucionViewModel.printResult
            esExito = true
            mensajeResultado = when (print) {
                is PrintResult.Success -> "Devolución registrada e impresa correctamente"
                is PrintResult.NoPrinter -> "Devolución registrada (sin impresora)"
                is PrintResult.BluetoothOff -> "Devolución registrada (Bluetooth apagado)"
                is PrintResult.Error -> "Devolución registrada pero error al imprimir"
                else -> "Devolución registrada correctamente"
            }
            showResultDialog = true
            carritoViewModel.limpiar()
            devolucionViewModel.reset()
            isProcessing = false
        }
    }

    fun intentarRegistrar(imprimir: Boolean) {
        val clienteIdFinal = if (clienteNulo) null else cliente?.id
        isProcessing = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                devolucionViewModel.verificarImpresora()
            }
            printerStatus = result
            when (result) {
                is PrintResult.Success -> {
                    devolucionViewModel.registrarDevolucion(
                        clienteId = clienteIdFinal,
                        clienteNombre = cliente?.nombre,
                        clienteNulo = clienteNulo,
                        motivo = motivo,
                        observacion = observacion,
                        carrito = items,
                        imprimir = imprimir
                    )
                }
                else -> {
                    isProcessing = false
                    showPrinterDialog = true
                }
            }
        }
    }

    // ── DIÁLOGOS ESTILIZADOS ──────────────────────────────────────────────────
    if (showConfirmDialog) {
        CustomStyledDialog(
            title = "Confirmar devolución",
            message = "¿Deseas registrar esta devolución con los productos y motivos especificados?",
            icon = Icons.Default.Info,
            iconColor = AccentBlue,
            iconBgColor = AccentBlueSoft,
            confirmText = "Confirmar",
            onConfirm = {
                showConfirmDialog = false
                intentarRegistrar(imprimir = true)
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    if (showPrinterDialog) {
        val mensaje = when (printerStatus) {
            is PrintResult.BluetoothOff -> "El Bluetooth está apagado."
            is PrintResult.NoPrinter -> "No hay impresora configurada."
            is PrintResult.Error -> "No se pudo conectar a la impresora."
            else -> "Impresora no disponible."
        }
        CustomStyledDialog(
            title = "Impresora no disponible",
            message = "$mensaje\n\n¿Deseas continuar sin imprimir el ticket?",
            icon = Icons.Default.PrintDisabled,
            iconColor = ErrorRed,
            iconBgColor = ErrorRedSoft,
            confirmText = "Continuar sin imprimir",
            onConfirm = {
                showPrinterDialog = false
                val clienteIdFinal = if (clienteNulo) null else cliente?.id

                devolucionViewModel.registrarDevolucion(
                    clienteId = clienteIdFinal,
                    clienteNombre = cliente?.nombre,
                    clienteNulo = clienteNulo,
                    motivo = motivo,
                    observacion = observacion,
                    carrito = items,
                    imprimir = false // 🔥 directo sin validar impresora otra vez
                )
            },
            onDismiss = { showPrinterDialog = false }
        )
    }

    if (showResultDialog) {
        CustomStyledDialog(
            title = "Resultado",
            message = mensajeResultado,
            icon = if (esExito) Icons.Default.CheckCircle else Icons.Default.Warning,
            iconColor = if (esExito) AccentTeal else ErrorRed,
            iconBgColor = if (esExito) Color(0xFFE6F6F2) else ErrorRedSoft,
            confirmText = "Aceptar",
            onConfirm = {
                showResultDialog = false
                if (esExito) onDevolucionExitosa()
            },
            onDismiss = {
                showResultDialog = false
                if (esExito) onDevolucionExitosa()
            },
            showDismissButton = false
        )
    }

    if (isProcessing) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceWhite,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Procesando devolución...",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Devolución",
                onBackClick = onBack
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── LISTA DE PRODUCTOS ──
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = TextMuted.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay productos en devolución",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        CarritoItemDevolucionCard(item = item, viewModel = carritoViewModel)
                    }
                }
            }

            // ── SECCIÓN DE DETALLES ANIMADA (BOTTOM) ──
            if (items.isNotEmpty()) {
                Surface(
                    color = SurfaceWhite,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // Respeta botones de navegación en Android
                    ) {
                        // Header Colapsable
                        val rotacion by animateFloatAsState(targetValue = if (showDetalles) 0f else 180f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDetalles = !showDetalles }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Detalles de Devolución",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Indicador de obligatoriedad visual
                                    if (motivo.isBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = ErrorRedSoft,
                                            contentColor = ErrorRed
                                        ) {
                                            Text(
                                                text = "Obligatorio",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completado",
                                            tint = AccentTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "$totalProductos productos",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Expandir/Colapsar",
                                tint = TextMuted,
                                modifier = Modifier.rotate(rotacion)
                            )
                        }

                        // Formulario animado
                        AnimatedVisibility(
                            visible = showDetalles,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 350.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 20.dp)
                            ) {
                                HorizontalDivider(color = BackgroundLight)
                                Spacer(modifier = Modifier.height(16.dp))

                                // ── 1. MOTIVO ──
                                Text(
                                    text = "Motivo de devolución *",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                                )
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = motivo,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = { Text("Selecciona un motivo", color = TextMuted) }, // Texto que se ve antes de elegir
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = defaultTextFieldColors()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(SurfaceWhite)
                                    ) {
                                        motivos.forEach { opcion ->
                                            DropdownMenuItem(
                                                text = { Text(opcion) },
                                                onClick = {
                                                    motivo = opcion
                                                    expanded = false
                                                    if (opcion != "Devolución de cliente") {
                                                        clienteBuscado = ""
                                                        clienteNulo = false
                                                        clienteViewModel.limpiarBusqueda()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // ── 2. CLIENTE ──
                                if (motivo == "Devolución de cliente") {
                                    Text(
                                        text = "Buscar cliente",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                                    )
                                    OutlinedTextField(
                                        value = clienteBuscado,
                                        onValueChange = {
                                            clienteBuscado = it
                                            clienteNulo = false
                                            clienteViewModel.limpiarBusqueda()
                                        },
                                        placeholder = { Text("Nombre o ID del cliente", color = TextMuted) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = defaultTextFieldColors()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { clienteViewModel.buscarCliente(clienteBuscado) },
                                        enabled = clienteBuscado.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo)
                                    ) {
                                        Text("Buscar")
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = clienteNulo,
                                            onCheckedChange = {
                                                clienteNulo = it
                                                if (it) {
                                                    clienteBuscado = ""
                                                    clienteViewModel.limpiarBusqueda()
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                                        )
                                        Text("Cliente nulo (venta rápida)", color = TextPrimary, fontSize = 14.sp)
                                    }

                                    cliente?.let {
                                        ClienteCard(
                                            nombre = it.nombre, id = it.id, negocio = it.nombreNegocio,
                                            seleccionado = true, onClick = {}
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    resultados.filter { it.id != cliente?.id }.forEach { item ->
                                        ClienteCard(
                                            nombre = item.nombre, id = item.id, negocio = item.nombreNegocio,
                                            seleccionado = cliente?.id == item.id,
                                            onClick = {
                                                clienteViewModel.seleccionarCliente(item)
                                                clienteNulo = false
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // ── 3. OBSERVACIÓN ──
                                Text(
                                    text = "Observación (opcional)",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                                )
                                OutlinedTextField(
                                    value = observacion,
                                    onValueChange = { observacion = it },
                                    placeholder = { Text("Agrega detalles adicionales...", color = TextMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = defaultTextFieldColors()
                                )
                                Spacer(modifier = Modifier.height(24.dp)) // Un poco más de espacio al final
                            }
                        }

                        // Botón Confirmar Fijo
                        Button(
                            onClick = {
                                if (motivo.isBlank()) {
                                    // UX: Abre los detalles si el usuario intentó confirmar sin llenarlos
                                    showDetalles = true
                                    mensajeResultado = "Debes seleccionar un motivo"
                                    showResultDialog = true
                                } else {
                                    showConfirmDialog = true
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Confirmar Devolución", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── COMPONENTES UI Y ESTILOS ──────────────────────────────────────────────────

@Composable
fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = BackgroundLight,
    unfocusedContainerColor = BackgroundLight,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = Color.Transparent,
    cursorColor = AccentBlue,
    focusedLabelColor = AccentBlue,
    unfocusedLabelColor = TextMuted
)

@Composable
fun CarritoItemDevolucionCard(
    item: CarritoItem,
    viewModel: CarritoDevolucionViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productoNombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = item.presentacionNombre,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            // Controles de cantidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(BackgroundLight, RoundedCornerShape(12.dp))
            ) {
                IconButton(
                    onClick = { viewModel.actualizarCantidad(item.productoVariacionId, item.cantidad - 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, "Quitar", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = item.cantidad.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentBlue,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { viewModel.actualizarCantidad(item.productoVariacionId, item.cantidad + 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, "Agregar", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CustomStyledDialog(
    title: String,
    message: String,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDismissButton: Boolean = true
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
        dismissButton = if (showDismissButton) {
            {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        } else null,
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = iconColor, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}