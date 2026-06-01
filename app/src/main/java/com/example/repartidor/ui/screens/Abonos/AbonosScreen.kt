package com.example.repartidor.ui.screens.Abonos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.model.VentaCredito
import com.example.repartidor.viewmodel.AbonosViewModel
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.ui.screens.components.* // Colores del tema
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbonosScreen(
    viewModel: AbonosViewModel,
    onIrForm: (Int) -> Unit,
    onBack: () -> Unit
) {
    val ventas = viewModel.ventas

    // Estado búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var ventaSeleccionada by remember { mutableStateOf<VentaCredito?>(null) }

    // 🔥 NUEVO: Variable para controlar el parpadeo de la primera carga
    var primeraCarga by remember { mutableStateOf(true) }

    val filteredVentas = remember(searchQuery, ventas) {
        if (searchQuery.isBlank()) {
            ventas
        } else {
            ventas.filter {
                (it.nombre ?: "").lowercase().contains(searchQuery.lowercase()) ||
                        (it.nombreNegocio ?: "").lowercase().contains(searchQuery.lowercase())
            }
        }
    }

    // 🔥 NUEVO: Agregamos un pequeño delay para dar tiempo al ViewModel
    LaunchedEffect(Unit) {
        viewModel.cargarVentas()
        delay(100) // Damos un respiro para que el VM actualice su estado
        primeraCarga = false
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Recibir Abono",
                onBackClick = onBack
            )
        },
        bottomBar = {
            // BARRA INFERIOR ORIGINAL (Con sombra y diseño original)
            Surface(
                color = SurfaceWhite,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            ventaSeleccionada?.let { onIrForm(it.id) }
                        },
                        enabled = ventaSeleccionada != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            disabledContainerColor = AccentBlue.copy(alpha = 0.5f),
                            contentColor = SurfaceWhite,
                            disabledContentColor = SurfaceWhite.copy(alpha = 0.8f)
                        )
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Abono", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // BUSCADOR ORIGINAL (Rectangular con 16.dp de corner)
            if (ventas.isNotEmpty() || !primeraCarga) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    placeholder = {
                        Text("Buscar cliente o negocio...", color = TextMuted, fontSize = 14.sp)
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
                        unfocusedBorderColor = BorderLight,
                        cursorColor = AccentBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // ESTADOS Y PANTALLAS PRINCIPALES ORIGINALES
            when {
                // 🔥 NUEVO: Evaluamos también la primeraCarga aquí
                viewModel.isLoading || primeraCarga -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }

                ventas.isEmpty() -> {
                    EmptyStatePlaceholder(
                        message = "No hay cuentas de crédito vigentes",
                        subMessage = "Todas las cuentas están al corriente."
                    )
                }

                filteredVentas.isEmpty() -> {
                    EmptyStatePlaceholder(
                        message = "No se encontraron clientes",
                        subMessage = "Prueba con otro nombre o negocio."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 4.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredVentas) { venta ->
                            val isSelected = ventaSeleccionada == venta

                            // TARJETA MEJORADA (Premium con más info)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ventaSeleccionada = if (isSelected) null else venta
                                    },
                                shape = RoundedCornerShape(20.dp), // Esquinas más suaves
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = if (isSelected) BorderStroke(1.5.dp, AccentBlue) else BorderStroke(1.dp, BorderLight),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.Top // Alineación arriba para el avatar
                                ) {
                                    // AVATAR DINÁMICO (Cambia a check al seleccionar)
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) AccentBlueSoft else BackgroundLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isSelected) AccentBlue else TextMuted.copy(alpha = 0.7f),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // COLUMNA PRINCIPAL DE TEXTOS
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = venta.nombre ?: "Cliente sin nombre",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )

                                        if (!venta.nombreNegocio.isNullOrBlank()) {
                                            Text(
                                                text = venta.nombreNegocio,
                                                color = TextMuted,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // BARRA DE PROGRESO DE PAGO (Calculado)
                                        val totalOriginal = venta.total.coerceAtLeast(1.0) // Evitar división por cero
                                        val totalPagado = totalOriginal - venta.saldoCalculado
                                        val porcentajePagado = (totalPagado / totalOriginal).toFloat().coerceIn(0f, 1f)

                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            LinearProgressIndicator(
                                                progress = porcentajePagado,
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                                color = AccentTeal, // Color verde para progreso
                                                trackColor = BorderLight
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${(porcentajePagado * 100).toInt()}% Pagado",
                                                color = AccentTeal,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.End
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(5.dp))

                                        // SECCIÓN DE SALDOS FINALES
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Column {
                                                Text("Deuda inicial", color = TextMuted, fontSize = 11.sp)
                                                Text(
                                                    text = "$${"%.2f".format(venta.total)}",
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Saldo Pendiente", color = TextMuted, fontSize = 11.sp)
                                                Text(
                                                    text = "$${"%.2f".format(venta.saldoCalculado)}",
                                                    color = if (isSelected) AccentBlue else AccentTeal,
                                                    fontWeight = FontWeight.ExtraBold, // Muy negrita para el dinero
                                                    fontSize = 17.sp // Un punto más grande
                                                )
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
    }
}

// COMPONENTE DE ESTADO VACÍO ORIGINAL
@Composable
private fun EmptyStatePlaceholder(message: String, subMessage: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subMessage,
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}