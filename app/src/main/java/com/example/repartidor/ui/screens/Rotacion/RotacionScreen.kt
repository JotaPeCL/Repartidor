
package com.example.repartidor.ui.screens.Rotacion

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.window.DialogProperties
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.ui.screens.components.*
import com.example.repartidor.viewmodel.Rotacion.CarritoRotacionViewModel
import com.example.repartidor.viewmodel.Rotacion.RotacionViewModel

@Composable
fun RotacionScreen(
    onIrFormulario: () -> Unit,
    viewModel: RotacionViewModel,
    carritoViewModel: CarritoRotacionViewModel,
    onBack: () -> Unit
) {
    val productos by viewModel.productos.collectAsState()
    val carrito by carritoViewModel.items.collectAsState()

    var busqueda by remember { mutableStateOf("") }
    var productoSeleccionado by remember { mutableStateOf<ProductoTerminadoEntity?>(null) }
    var mostrarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    val productosFiltrados = remember(busqueda, productos) {
        if (busqueda.isBlank()) productos
        else productos.filter { it.nombre.contains(busqueda, ignoreCase = true) }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Rotación",
                onBackClick = {
                    if (carrito.isNotEmpty()) mostrarSalida = true
                    else onBack()
                }
            )
        },
        bottomBar = {
            if (carrito.isNotEmpty()) {
                BottomCartBarRotacion(
                    cantidadItems = carrito.size,
                    onClick = onIrFormulario
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (productos.isNotEmpty()) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = {
                        Text("Buscar producto...", color = TextMuted, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextMuted)
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

            when {
                productos.isEmpty() -> {
                    EstadoVacioRotacion(
                        icono = Icons.Default.Inventory,
                        titulo = "No hay productos disponibles",
                        descripcion = "No hay productos para realizar una rotación."
                    )
                }

                productosFiltrados.isEmpty() -> {
                    EstadoVacioRotacion(
                        icono = Icons.Default.SearchOff,
                        titulo = "No se encontraron resultados",
                        descripcion = "Intenta buscar con otras palabras."
                    )
                }

                else -> {
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
                        items(
                            items = productosFiltrados,
                            key = { it.id }
                        ) { producto ->
                            ProductoRotacionCard(
                                producto = producto,
                                onClick = {
                                    productoSeleccionado = producto
                                    viewModel.cargarVariaciones(producto.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    productoSeleccionado?.let { producto ->
        VariacionesDialogRotacion(
            producto = producto,
            viewModel = viewModel,
            carrito = carrito,
            onDismiss = { productoSeleccionado = null },
            onAgregar = { nuevos ->
                if (nuevos.isNotEmpty()) carritoViewModel.agregarProductos(nuevos)
                productoSeleccionado = null
            }
        )
    }

    if (mostrarSalida) {
        AlertDialog(
            onDismissRequest = { mostrarSalida = false },
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
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "¿Salir de rotación?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Tienes productos en la lista. Si sales ahora, se perderán y tendrás que volver a agregarlos.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        carritoViewModel.limpiar()
                        mostrarSalida = false
                        onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sí, salir y descartar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarSalida = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun ProductoRotacionCard(
    producto: ProductoTerminadoEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clickable(onClick = onClick),
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
                    .background(AccentPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = AccentPurple,
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
                imageVector = Icons.Default.Add,
                contentDescription = "Seleccionar presentación",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EstadoVacioRotacion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    descripcion: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = TextMuted.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = descripcion,
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BottomCartBarRotacion(
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
                .navigationBarsPadding()
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
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Ver rotación",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Ver Rotación ($cantidadItems)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}


@Composable
private fun VariacionesDialogRotacion(
    producto: ProductoTerminadoEntity,
    viewModel: RotacionViewModel,
    carrito: List<CarritoItem>,
    onDismiss: () -> Unit,
    onAgregar: (List<CarritoItem>) -> Unit
) {
    val variaciones by viewModel.variaciones.collectAsState()

    // Recuperar las cantidades existentes en el carrito
    val cantidades = remember(variaciones, carrito) {
        mutableStateMapOf<Int, String>().apply {
            variaciones.forEach { variacion ->
                val itemCarrito = carrito.find {
                    it.productoVariacionId == variacion.id
                }

                if (itemCarrito != null) {
                    this[variacion.id] = itemCarrito.cantidad.toString()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // ── ENCABEZADO ──
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
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (variaciones.isEmpty()) {
                    Text(
                        text = "Sin variaciones disponibles",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {

                    // ── LISTA DE VARIACIONES ──
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
                                colors = CardDefaults.cardColors(
                                    containerColor = BackgroundLight
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 0.dp
                                )
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
                                        text = if (variacion.stockActual > 0) {
                                            "Stock: ${variacion.stockActual}"
                                        } else {
                                            "Sin stock"
                                        },
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // ── CANTIDAD ──
                                    OutlinedTextField(
                                        value = cantidad,
                                        onValueChange = { nuevo ->
                                            if (nuevo.isEmpty() || nuevo.all { it.isDigit() }) {
                                                cantidades[variacion.id] = nuevo
                                            }
                                        },
                                        placeholder = {
                                            Text("0", color = TextMuted)
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number
                                        ),
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

                    // ── AGREGAR / ACTUALIZAR CARRITO ──
                    Button(
                        onClick = {
                            val seleccionados = variaciones.mapNotNull { variacion ->
                                val cantidad = cantidades[variacion.id]
                                    ?.toIntOrNull() ?: 0

                                if (cantidad > 0) {
                                    CarritoItem(
                                        productoVariacionId = variacion.id,
                                        productoNombre = producto.nombre,
                                        presentacionNombre = variacion.presentacionNombre,
                                        precio = variacion.precio,
                                        cantidad = cantidad
                                    )
                                } else {
                                    null
                                }
                            }

                            onAgregar(seleccionados)
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
                        Text(
                            text = "Agregar a rotación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
