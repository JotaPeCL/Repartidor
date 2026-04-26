package com.example.repartidor.ui.screens.Inventario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.data.model.ReabastecimientoItem
import com.example.repartidor.viewmodel.ReabastecimientoCarritoViewModel
import com.example.repartidor.viewmodel.ReabastecimientoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReabastecimientoScreen(
    onIrPedido: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReabastecimientoViewModel,
    carritoViewModel: ReabastecimientoCarritoViewModel
) {
    val productos by viewModel.productos.collectAsState()
    val items by carritoViewModel.items.collectAsState()
    var productoSeleccionado by remember { mutableStateOf<ProductoTerminadoEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reabastecimiento",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Selecciona productos para tu camioneta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (items.isNotEmpty()) showExitDialog = true else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Barra inferior con el resumen y acceso al carrito de reabastecimiento
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = onIrPedido,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = items.isNotEmpty() // Desactivado si no hay nada
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = "Ver Pedido",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Ver Pedido (${items.size})",
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando catálogo...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 🎨 DIALOG DE VARIACIONES (Restilizado para Reabastecimiento)
    if (showDialog && productoSeleccionado != null) {
        val variaciones by viewModel
            .getVariaciones(productoSeleccionado!!.id)
            .collectAsState(initial = emptyList())

        val cantidades = remember { mutableStateMapOf<Int, String>() }

        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
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
                    Text(
                        text = "Selecciona las presentaciones a pedir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
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
                                            text = "En Camioneta: ${variacion.stockActual}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = cantidad,
                                        onValueChange = { nuevo ->
                                            if (nuevo.isEmpty() || nuevo.all { it.isDigit() }) {
                                                cantidades[variacion.id] = nuevo
                                            }
                                        },
                                        label = { Text("Cantidad a pedir") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
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
                                variaciones.forEach { variacion ->
                                    val cantidad = cantidades[variacion.id]?.toIntOrNull() ?: 0
                                    if (cantidad > 0) {
                                        carritoViewModel.agregarItem(
                                            ReabastecimientoItem(
                                                productoVariacionId = variacion.id,
                                                productoNombre = productoSeleccionado!!.nombre,
                                                presentacionNombre = variacion.presentacionNombre,
                                                cantidad = cantidad
                                            )
                                        )
                                    }
                                }
                                showDialog = false
                            }
                        ) {
                            Text("Agregar al pedido")
                        }
                    }
                }
            }
        }
    }

    // 🎨 DIALOG DE SALIDA (Uso de AlertDialog oficial M3)
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
            title = { Text("¿Abandonar pedido?") },
            text = { Text("Tienes productos seleccionados para reabastecer. Si sales ahora, se borrará tu selección actual.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        carritoViewModel.limpiar()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar y Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continuar pidiendo")
                }
            }
        )
    }
}