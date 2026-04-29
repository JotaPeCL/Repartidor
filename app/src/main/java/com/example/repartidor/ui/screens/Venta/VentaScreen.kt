package com.example.repartidor.ui.screens.Venta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.viewmodel.CarritoViewModel
import com.example.repartidor.viewmodel.VentaProcesoViewModel
import com.example.repartidor.viewmodel.VentaViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(clienteId) {
        clienteNombre = clienteId?.let {
            viewModel.getClienteNombre(it)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    // 🌟 Uso de Scaffold para una estructura moderna
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Productos",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (clienteId != null) "Cliente: ${clienteNombre ?: "Cargando..."}" else "Venta Rápida",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (carrito.isNotEmpty()) {
                            mostrarDialogoSalir = true
                        } else {
                            carritoViewModel.limpiar()
                            ventaProcesoViewModel.reset()
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Barra inferior fija para el carrito
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = onIrCarrito,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Carrito",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Ver Carrito (${carrito.size})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (productos.isEmpty()) {
                // Pantalla vacía si no hay productos
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay productos disponibles.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Lista de productos con diseño mejorado
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(productos) { producto ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    productoSeleccionado = producto
                                    showDialog = true
                                },
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 🎨 DIALOG DE VARIACIONES MEJORADO
    if (showDialog && productoSeleccionado != null) {
        val variaciones by viewModel
            .getVariaciones(productoSeleccionado!!.id)
            .collectAsState(initial = emptyList())

        val cantidades = remember { mutableStateMapOf<Int, String>() }

        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = productoSeleccionado!!.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        variaciones.forEach { variacion ->
                            val cantidad = cantidades[variacion.id] ?: "0"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = variacion.presentacionNombre,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "$${variacion.precio}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Stock: ${variacion.stockActual}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Campo de texto más limpio
                                    OutlinedTextField(
                                        value = cantidad,
                                        onValueChange = { nuevo ->
                                            if (nuevo.isEmpty() || nuevo.all { it.isDigit() }) {
                                                cantidades[variacion.id] = nuevo
                                            }
                                        },
                                        label = { Text("Cantidad") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val productosSeleccionados = variaciones.mapNotNull { variacion ->
                                    val cantidad = cantidades[variacion.id]?.toIntOrNull() ?: 0
                                    if (cantidad > 0) {
                                        CarritoItem(
                                            productoVariacionId = variacion.id,
                                            productoNombre = productoSeleccionado!!.nombre,
                                            presentacionNombre = variacion.presentacionNombre,
                                            precio = variacion.precio,
                                            cantidad = cantidad
                                        )
                                    } else null
                                }
                                carritoViewModel.agregarProductos(productosSeleccionados)
                                showDialog = false
                            }
                        ) {
                            Text("Agregar")
                        }
                    }
                }
            }
        }
    }

    // 🎨 DIALOG DE SALIDA MEJORADO
    if (mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSalir = false },
            title = {
                Text("¿Salir de la venta?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Si sales ahora, se perderán los productos que tienes actualmente en tu carrito.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        mostrarDialogoSalir = false
                        carritoViewModel.limpiar()
                        ventaProcesoViewModel.reset()
                        onBack()
                    }
                ) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSalir = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}