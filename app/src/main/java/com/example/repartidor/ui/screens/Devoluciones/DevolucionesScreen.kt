package com.example.repartidor.ui.screens.Devoluciones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.repartidor.data.model.CarritoItem
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.viewmodel.CarritoDevolucionViewModel
import com.example.repartidor.viewmodel.DevolucionProductosViewModel

@Composable
fun DevolucionesScreen(
    onIrCarrito: () -> Unit,
    viewModel: DevolucionProductosViewModel,
    carritoViewModel: CarritoDevolucionViewModel,
    onBack: () -> Unit
) {
    val productos by viewModel.productos.collectAsState()
    val carrito by carritoViewModel.items.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    var productoSeleccionado by remember { mutableStateOf<ProductoTerminadoEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    // ── Dialog de variaciones ──
    if (showDialog && productoSeleccionado != null) {
        VariacionesDialogDevolucion(
            producto = productoSeleccionado!!,
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onAgregar = {
                carritoViewModel.agregarProductos(it)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            StandardTopBar(
                title = "Devoluciones",
                onBackClick = {
                    if (carrito.isNotEmpty()) {
                        showExitDialog = true
                    } else {
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
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // 🔍 Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // 📦 Lista
            LazyColumn {
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

    if (showExitDialog) {

        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Salir de devolución") },
            text = { Text("Tienes productos en el carrito. ¿Seguro que quieres salir?") },
            confirmButton = {

                TextButton(onClick = {
                    carritoViewModel.limpiar()
                    showExitDialog = false
                    onBack()
                }) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {

                TextButton(onClick = {
                    showExitDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VariacionesDialogDevolucion(
    producto: ProductoTerminadoEntity,
    viewModel: DevolucionProductosViewModel,
    onDismiss: () -> Unit,
    onAgregar: (List<CarritoItem>) -> Unit
) {

    val variaciones by viewModel
        .getVariaciones(producto.id)
        .collectAsState(initial = emptyList())

    val cantidades = remember { mutableStateMapOf<Int, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(producto.nombre) },
        text = {

            if (variaciones.isEmpty()) {
                Text("Sin variaciones disponibles")
            } else {

                LazyColumn {
                    items(variaciones) { item ->

                        val cantidad = cantidades[item.id] ?: 0  // 🔥 FIX

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {

                            Text(item.presentacionNombre) // 🔥 FIX

                            Text(
                                text = "Stock: ${item.stockActual}", // 🔥 FIX
                                style = MaterialTheme.typography.bodySmall
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val cantidad = cantidades[item.id] ?: ""

                                OutlinedTextField(
                                    value = cantidad,
                                    onValueChange = { nuevo ->
                                        if (nuevo.isEmpty() || nuevo.all { it.isDigit() }) {
                                            cantidades[item.id] = nuevo
                                        }
                                    },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )

                            }
                        }
                    }
                }
            }
        },
        confirmButton = {

            Button(onClick = {

                val seleccionados = variaciones.mapNotNull { item ->

                    val cantidad = cantidades[item.id]?.toIntOrNull() ?: 0

                    if (cantidad > 0) {
                        CarritoItem(
                            productoVariacionId = item.id,              // 🔥 FIX
                            productoNombre = producto.nombre,
                            presentacionNombre = item.presentacionNombre, // 🔥 FIX
                            precio = item.precio,
                            cantidad = cantidad
                        )
                    } else null
                }

                if (seleccionados.isNotEmpty()) {
                    onAgregar(seleccionados)
                }

            }) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BottomCartBar(
    cantidadItems: Int,
    onClick: () -> Unit
) {

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(Icons.Default.ShoppingCart, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text("Carrito")
            }

            Text("$cantidadItems items")
        }
    }
}

@Composable
fun ProductoCard(
    producto: ProductoTerminadoEntity,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleMedium
            )

        }
    }
}