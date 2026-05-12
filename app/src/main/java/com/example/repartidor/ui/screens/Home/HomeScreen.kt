package com.example.repartidor.ui.screens.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.HomeViewModel

// ── Paleta clara ──────────────────────────────────────────────────────────────
private val BackgroundLight  = Color(0xFFF4F6FB)
private val SurfaceWhite     = Color(0xFFFFFFFF)
private val AccentBlue       = Color(0xFF3A6FD8)
private val AccentBlueSoft   = Color(0xFFEBF0FC)
private val AccentIndigo     = Color(0xFF5B4CF5)
private val AccentIndigoSoft = Color(0xFFEFEDFD)
private val AccentTeal       = Color(0xFF0F9E82)
private val AccentTealSoft   = Color(0xFFE6F6F2)
private val TextPrimary      = Color(0xFF111827)
private val TextMuted        = Color(0xFF9CA3AF)
private val ErrorRed         = Color(0xFFDC2626)
private val ErrorRedSoft     = Color(0xFFFEF2F2)
private val HeaderGradStart  = Color(0xFF3A6FD8)
private val HeaderGradEnd    = Color(0xFF5B4CF5)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onCerrarSesion: () -> Unit,
    onIrClientes: () -> Unit,
    onIrVenta: () -> Unit,
    onIrInventarios: () -> Unit,
    viewModel: HomeViewModel,
    sessionManager: SessionManager,
    onIrBluetooth: () -> Unit,
    onIrVentasDia: () -> Unit,
    onIrDevoluciones: () -> Unit,
) {
    val data = viewModel.homeData

    // ── Estado del diálogo ────────────────────────────────────────────────────
    var showLogoutDialog by remember { mutableStateOf(false) }
    // ─────────────────────────────────────────────────────────────────────────

    LaunchedEffect(Unit) {
        val username = sessionManager.getUser()
        username?.let { viewModel.cargarDatosPorUsername(it) }
    }

    // ── Diálogo de confirmación ───────────────────────────────────────────────
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            nombre   = data.usuario?.firstName,
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onCerrarSesion()
            }
        )
    }
    // ─────────────────────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeader(
                nombre         = data.usuario?.firstName,
                ruta           = data.ruta?.nombre,
                vehiculo       = if (data.vehiculo != null)
                    "${data.vehiculo.marca} · ${data.vehiculo.placa}" else null,
                onCerrarSesion = { showLogoutDialog = true }  // ← abre el diálogo
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                SectionLabel("Acciones rápidas")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        modifier  = Modifier.weight(1f),
                        title     = "Buscar\nCliente",
                        subtitle  = "Directorio",
                        icon      = Icons.Default.PersonSearch,
                        accent    = AccentBlue,
                        accentBg  = AccentBlueSoft,
                        onClick   = onIrClientes
                    )
                    ActionCard(
                        modifier  = Modifier.weight(1f),
                        title     = "Venta\nRápida",
                        subtitle  = "Nueva venta",
                        icon      = Icons.Default.PointOfSale,
                        accent    = AccentIndigo,
                        accentBg  = AccentIndigoSoft,
                        onClick   = onIrVenta
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                InventoryCard(onClick = onIrInventarios)
                Spacer(modifier = Modifier.height(12.dp))

                VentasDiaCard(
                    onClick = onIrVentasDia
                )
                Spacer(modifier = Modifier.height(12.dp))

                DevolucionesCard(
                    onClick = onIrDevoluciones
                )

                Spacer(modifier = Modifier.height(32.dp))

                SectionLabel("Configuración")
                Spacer(modifier = Modifier.height(12.dp))

                FooterActionRow(
                    icon     = Icons.Default.Print,
                    label    = "Impresora Bluetooth",
                    sublabel = "Configurar dispositivo",
                    onClick  = onIrBluetooth
                )

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

// ── DIÁLOGO DE CIERRE DE SESIÓN ───────────────────────────────────────────────
@Composable
private fun LogoutConfirmDialog(
    nombre: String?,
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
                    imageVector        = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint               = ErrorRed,
                    modifier           = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text       = "¿Cerrar sesión?",
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                color      = TextPrimary,
                textAlign  = TextAlign.Center
            )
        },
        text = {
            Text(
                text      = if (nombre != null)
                    "Hola, $nombre. ¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo."
                else
                    "¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo.",
                fontSize  = 14.sp,
                color     = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor   = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector        = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
// ─────────────────────────────────────────────────────────────────────────────

// ── HEADER ────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    nombre: String?,
    ruta: String?,
    vehiculo: String?,
    onCerrarSesion: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(HeaderGradStart, HeaderGradEnd))
            )
            .padding(top = 56.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color      = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize   = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Hola, ${nombre ?: "Cargando…"}",
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6EF0B0))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text       = "Repartidor activo",
                            color      = Color.White.copy(alpha = 0.85f),
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick  = onCerrarSesion,   // ← ahora llama al lambda del diálogo
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector        = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeaderChip(Icons.Default.Map, ruta ?: "Sin ruta", Modifier.weight(1f))
                HeaderChip(Icons.Default.LocalShipping, vehiculo ?: "Sin unidad", Modifier.weight(1f))
            }
        }
    }
}


@Composable
private fun HeaderChip(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    accentBg: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(136.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp)
                Text(subtitle, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(78.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(AccentTealSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Inventario de camioneta", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Ver stock actual", color = TextMuted, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VentasDiaCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentIndigoSoft), // puedes cambiar color si quieres
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = AccentIndigo,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Ventas del día",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    "Ver y reimprimir tickets",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevolucionesCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFE5E5)), // rojo suave
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.KeyboardReturn, // icono de devolución
                    contentDescription = null,
                    tint = Color(0xFFD32F2F), // rojo fuerte
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Devoluciones",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    "Registrar merma o productos devueltos",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FooterActionRow(
    icon: ImageVector,
    label: String,
    sublabel: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val iconColor  = if (isDestructive) ErrorRed else AccentBlue
    val iconBg     = if (isDestructive) ErrorRedSoft else AccentBlueSoft
    val labelColor = if (isDestructive) ErrorRed else TextPrimary

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(68.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(19.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = labelColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(sublabel, color = TextMuted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FB)
@Composable
fun HomeScreenPreview() {}