package com.example.repartidor.ui.screens.Inventario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.viewmodel.CierreMiniBodegaViewModel
import com.example.repartidor.viewmodel.ReabastecimientoCarritoViewModel
import com.example.repartidor.viewmodel.ReabastecimientoProcesoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoReabastecimientoScreen(
    carritoViewModel: ReabastecimientoCarritoViewModel,
    onVolver: () -> Unit,
    reabastecimientoProcesoViewModel: ReabastecimientoProcesoViewModel,
    cierreMiniBodegaViewModel: CierreMiniBodegaViewModel,
    onPedidoCompleto: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val items by carritoViewModel.items.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Resumen de Pedido",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver, enabled = !isLoading) {
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
            if (items.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    tonalElevation = 8.dp
                ) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Confirmar",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Confirmar y Enviar Pedido",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCartCheckout,
                            contentDescription = "Pedido Vacío",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tu pedido está vacío",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items) { item ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${item.productoNombre} - ${item.presentacionNombre}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Controles de cantidad estilizados
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        FilledTonalIconButton(
                                            onClick = {
                                                val nueva = (item.cantidad - 1).coerceAtLeast(0)
                                                carritoViewModel.actualizarCantidad(item.productoVariacionId, nueva)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Disminuir")
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
                                                        carritoViewModel.actualizarCantidad(
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
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .width(64.dp)
                                                .padding(horizontal = 8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            )
                                        )

                                        FilledTonalIconButton(
                                            onClick = {
                                                val nueva = item.cantidad + 1
                                                carritoViewModel.actualizarCantidad(item.productoVariacionId, nueva)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 🎨 DIALOG DE CONFIRMACIÓN (M3)
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showConfirmDialog = false },
            title = {
                Text(text = "Confirmar pedido", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("¿Seguro que deseas enviar el pedido de reabastecimiento a la matriz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        isLoading = true
                        errorMessage = null

                        reabastecimientoProcesoViewModel.enviarPedido(
                            items = items,
                            onSuccess = {
                                // SEGUNDO PASO: cerrar mini bodega
                                cierreMiniBodegaViewModel.cerrarMiniBodega(
                                    onSuccess = {
                                        isLoading = false
                                        carritoViewModel.limpiar()
                                        onPedidoCompleto()
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = "Error al cerrar bodega:\n$error"
                                        println("Error cierre: $error")
                                    }
                                )
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = "Error al enviar pedido:\n$error"
                                println("Error pedido: $error")
                            }
                        )
                    },
                    enabled = !isLoading
                ) {
                    Text("Sí, enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    enabled = !isLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 🎨 DIALOG DE CARGA
    if (isLoading) {
        Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Procesando pedido...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Por favor, no cierres la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // 🎨 DIALOG DE ERROR (M3)
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = "Ocurrió un error")
            },
            text = {
                Text(
                    text = errorMessage ?: "Error desconocido",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("Aceptar")
                }
            }
        )
    }
}