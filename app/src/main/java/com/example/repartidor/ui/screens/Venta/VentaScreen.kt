package com.example.repartidor.ui.screens.Venta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.viewmodel.Venta.CarritoViewModel
import com.example.repartidor.viewmodel.Venta.VentaProcesoViewModel
import com.example.repartidor.viewmodel.Venta.VentaViewModel
import com.example.repartidor.ui.screens.components.* //Aqui estan los colores del tema

@Composable
fun VentaScreen(
    clienteId: Int?,
    onIrCarrito: () -> Unit,
    viewModel: VentaViewModel,
    carritoViewModel: CarritoViewModel,
    ventaProcesoViewModel: VentaProcesoViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(clienteId) {
        ventaProcesoViewModel.setCliente(clienteId)
    }

    val productos by viewModel.productos.collectAsState()
    var productoSeleccionado by remember { mutableStateOf<ProductoTerminadoEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val carrito by carritoViewModel.items.collectAsState()
    var mostrarDialogoSalir by remember { mutableStateOf(false) }
    var clienteNombre by remember { mutableStateOf<String?>(null) }

    // ── Estado para la búsqueda ──────────────────────────────────────────────
    var searchQuery by remember { mutableStateOf("") }

    // ── Lista filtrada reactiva ──────────────────────────────────────────────
    val filteredProductos by remember(searchQuery, productos) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                productos
            } else {
                productos.filter {
                    it.nombre.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

    LaunchedEffect(clienteId) {
        clienteNombre = clienteId?.let {
            viewModel.getClienteNombre(it)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────
    if (showDialog && productoSeleccionado != null) {
        VariacionesDialog(
            producto = productoSeleccionado!!,
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onAgregar = { seleccionados ->
                carritoViewModel.agregarProductos(seleccionados)
                showDialog = false
            }
        )
    }

    if (mostrarDialogoSalir) {
        ExitConfirmDialog(
            onDismiss = { mostrarDialogoSalir = false },
            onConfirm = {
                mostrarDialogoSalir = false
                carritoViewModel.limpiar()
                ventaProcesoViewModel.reset()
                onBack()
            }
        )
    }
    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Productos",
                subtitle = if (clienteId == null) "Venta Rápida" else "Cliente: ${clienteNombre ?: "Cargando..."}",
                onBackClick = {
                    if (carrito.isNotEmpty()) {
                        mostrarDialogoSalir = true
                    } else {
                        carritoViewModel.limpiar()
                        ventaProcesoViewModel.reset()
                        onBack()
                    }
                }
            )
        },
        bottomBar = {
            if (carrito.isNotEmpty()) {
                BottomCartBar(
                    cantidadItems = carrito.size,
                    onClick = onIrCarrito
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ── BARRA DE BÚSQUEDA ──
            if (productos.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = {
                        Text("Buscar producto...", color = TextMuted, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Borrar", tint = TextMuted)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = AccentBlue
                    ),
                    singleLine = true
                )
            }

            if (productos.isEmpty()) {
                // ── ESTADO: SIN PRODUCTOS DISPONIBLES ──
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
                                imageVector = Icons.Default.Inventory,
                                contentDescription = "Sin productos",
                                modifier = Modifier.size(42.dp),
                                tint = TextMuted.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay productos disponibles",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            } else if (filteredProductos.isEmpty()) {
                // ── ESTADO: SIN RESULTADOS DE BÚSQUEDA ──
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
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = "Sin resultados",
                                modifier = Modifier.size(42.dp),
                                tint = TextMuted.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron resultados",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Intenta buscar con otras palabras.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // ── LISTA DE PRODUCTOS FILTRADA ──
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProductos) { producto ->
                        ProductoCard(
                            producto = producto,
                            onClick = {
                                productoSeleccionado = producto
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── COMPONENTES UI ────────────────────────────────────────────────────────────
@Composable
private fun ProductoCard(
    producto: ProductoTerminadoEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = producto.nombre,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BottomCartBar(
    cantidadItems: Int,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceWhite,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding() // Respeta la barra de navegación de Android
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentIndigo,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ver Carrito ($cantidadItems)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// ── DIÁLOGO DE VARIACIONES (Estilo Soft) ──────────────────────────────────────
@Composable
private fun VariacionesDialog(
    producto: ProductoTerminadoEntity,
    viewModel: VentaViewModel,
    onDismiss: () -> Unit,
    onAgregar: (List<CarritoItem>) -> Unit
) {
    val variaciones by viewModel.getVariaciones(producto.id).collectAsState(initial = emptyList())
    val cantidades = remember { mutableStateMapOf<Int, String>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = producto.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
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

                // Lista de variaciones
                Column(
                    modifier = Modifier
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    variaciones.forEach { variacion ->
                        val cantidad = cantidades[variacion.id] ?: ""

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = variacion.presentacionNombre,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$${variacion.precio}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AccentTeal
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Stock: ${variacion.stockActual}",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Input de cantidad estilizado
                                OutlinedTextField(
                                    value = cantidad,
                                    onValueChange = { nuevo ->
                                        if (nuevo.isEmpty() || nuevo.all { it.isDigit() }) {
                                            cantidades[variacion.id] = nuevo
                                        }
                                    },
                                    placeholder = { Text("0", color = TextMuted) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceWhite,
                                        unfocusedContainerColor = SurfaceWhite,
                                        focusedBorderColor = AccentBlue,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de agregar
                Button(
                    onClick = {
                        val productosSeleccionados = variaciones.mapNotNull { variacion ->
                            val cantidad = cantidades[variacion.id]?.toIntOrNull() ?: 0
                            if (cantidad > 0) {
                                CarritoItem(
                                    productoVariacionId = variacion.id,
                                    productoNombre = producto.nombre,
                                    presentacionNombre = variacion.presentacionNombre,
                                    precio = variacion.precio,
                                    cantidad = cantidad
                                )
                            } else null
                        }
                        onAgregar(productosSeleccionados)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Agregar al carrito", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

// ── DIÁLOGO DE SALIDA (Estilo Logout) ─────────────────────────────────────────
@Composable
private fun ExitConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceWhite,
        shape            = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ErrorRedSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = ErrorRed,
                    modifier           = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text       = "¿Salir de la venta?",
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                color      = TextPrimary,
                textAlign  = TextAlign.Center
            )
        },
        text = {
            Text(
                text       = "Si sales ahora, se perderán los productos que tienes actualmente en tu carrito.",
                fontSize   = 14.sp,
                color      = TextMuted,
                textAlign  = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick  = onDismiss,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar", fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor   = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sí, salir y descartar", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}