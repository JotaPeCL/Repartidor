
package com.example.repartidor.ui.screens.Inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.ui.screens.components.*
import com.example.repartidor.viewmodel.Inventario.InventarioViewModel
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale

// ── ORDEN PERSONALIZADO DE PRESENTACIONES ──
private val ORDEN_PRESENTACIONES = listOf(
    "Minis", "Chico", "Mediano", "Grande", "250g", "500g",
    "1kg", "Kiosko", "Grande 150g", "Grande 140g", "Pieza"
)

// ── ORDEN PERSONALIZADO DE PRODUCTOS ──
private val ORDEN_PRODUCTOS = listOf(
    "PISTACHE", "ENCHILADO", "JAPONES", "JAPONES FUEGO", "MACHO",
    "HORNEADO", "CASCARITA", "SURTIDO", "HABA PELADA", "HABA CON CASCARA",
    "GARBANZA", "GARAPIÑADO", "GOMITA", "LUNETA", "HUEVITO CHOCOLATE",
    "GALLETA MARINA", "CIRUELA", "PASAS", "SEMILLA DE CALABAZA",
    "SEMILLA DE GIRASOL", "NUEZ", "ALMENDRA", "GRANOLA", "CHILE DE ARBOL",
    "CHILE PULLA", "CHILE CASCABEL", "CAMARON", "CAMARON MOLIDO",
    "CHARAL NATURAL", "CHARAL ADOBADO", "CECINA NATURAL", "CECINA ADOBADA",
    "CECINA DE LIMON", "CECINA CHILTEPIN", "CECINA CHIPOTLE",
    "CECINA HABANERO", "CECINA ARTESANAL", "PAPA FRITA",
    "CHURRO DE MASA", "CHURRO DELGADO", "CHICHARRON", "CHICHARRON CRAC",
    "PLATANO", "TOSTADA", "MIEL", "TIRA DE CC MINI", "TIRA DE DULCE",
    "MIX VERDE", "MIX ROJO"
)

private fun String.normalizarInventario(): String =
    Normalizer.normalize(trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .uppercase(Locale.ROOT)

private val INDICE_PRODUCTOS = ORDEN_PRODUCTOS.mapIndexed { index, nombre ->
    nombre.normalizarInventario() to index
}.toMap()

private val INDICE_PRESENTACIONES = ORDEN_PRESENTACIONES.mapIndexed { index, nombre ->
    nombre.normalizarInventario() to index
}.toMap()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    viewModel: InventarioViewModel,
    sessionManager: SessionManager,
    onIrReabastecimiento: () -> Unit,
    onBack: () -> Unit
) {
    val lista = viewModel.inventario
    var searchQuery by remember { mutableStateOf("") }
    val finalDia by sessionManager.finalDiaFlow.collectAsState(initial = false)

    // ── VALOR TOTAL DEL INVENTARIO ──
    val valorTotalInventario = lista.sumOf {
        it.cantidadActual * it.precioUnitario
    }
    val formatoMoneda = remember {
        NumberFormat.getCurrencyInstance(
            Locale.Builder().setLanguage("es").setRegion("MX").build()
        )
    }

    // ── BÚSQUEDA Y ORDEN PERSONALIZADO ──
    val filteredList = remember(searchQuery, lista) {
        lista.filter {
            searchQuery.isBlank() ||
                    it.productoNombre.contains(searchQuery, ignoreCase = true) ||
                    it.presentacion.contains(searchQuery, ignoreCase = true)
        }.sortedWith(
            compareBy(
                { INDICE_PRODUCTOS[it.productoNombre.normalizarInventario()] ?: Int.MAX_VALUE },
                { if (it.productoNombre.normalizarInventario() in INDICE_PRODUCTOS) "" else it.productoNombre.normalizarInventario() },
                { INDICE_PRESENTACIONES[it.presentacion.normalizarInventario()] ?: Int.MAX_VALUE },
                { it.presentacion.normalizarInventario() }
            )
        )
    }

    LaunchedEffect(Unit) {
        val username = sessionManager.getUser()
        username?.let { viewModel.cargarInventario(it) }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Inventario Actual",
                onBackClick = onBack
            )
        },
        bottomBar = {
            // ── BOTÓN ORIGINAL DE REABASTECIMIENTO ──
            if (finalDia) {
                Surface(
                    color = SurfaceWhite,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Button(
                            onClick = onIrReabastecimiento,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Reabastecer",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ir a Reabastecimiento",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // ── TARJETA DE VALOR TOTAL ──
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AccentTealSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    Text(
                        text = "VALOR TOTAL DEL INVENTARIO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentTeal,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatoMoneda.format(valorTotalInventario),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Valor de venta de las existencias actuales",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // ── BARRA DE BÚSQUEDA ──
            if (lista.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    placeholder = {
                        Text(
                            "Buscar producto o presentación...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = TextMuted
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Borrar",
                                    tint = TextMuted
                                )
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

            if (lista.isEmpty()) {
                // ── INVENTARIO VACÍO ──
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = "Inventario Vacío",
                                modifier = Modifier.size(42.dp),
                                tint = TextMuted.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Inventario vacío",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No hay productos registrados\nactualmente en la unidad.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else if (filteredList.isEmpty()) {
                // ── SIN RESULTADOS DE BÚSQUEDA ──
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(90.dp)
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
                // ── LISTA ORIGINAL CON ORDEN PERSONALIZADO ──
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productoNombre,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Presentación: ${item.presentacion}",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AccentTealSoft)
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "STOCK",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentTeal,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    SpanStyle(
                                                        fontSize = 21.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF102A43)
                                                    )
                                                ) {
                                                    append("${item.cantidadActual}")
                                                }
                                                withStyle(
                                                    SpanStyle(
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextMuted
                                                    )
                                                ) {
                                                    append("/${item.cantidadInicial}")
                                                }
                                            }
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