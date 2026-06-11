package com.example.repartidor.ui.screens.Home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.Home.HomeViewModel
import com.example.repartidor.ui.screens.components.* // Aquí están los colores del tema

// -----------------------------------------------------------------------------------------

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
    onIrAbonos: () -> Unit,
    onIrResumenDia: () -> Unit
) {
    val data = viewModel.homeData
    val context = LocalContext.current

    // ── Estado del diálogo ────────────────────────────────────────────────────
    var showLogoutDialog by remember { mutableStateOf(false) }
    // ─────────────────────────────────────────────────────────────────────────
    val finalDia by sessionManager.finalDiaFlow.collectAsState(initial = false)
    LaunchedEffect(Unit) {
        val username = sessionManager.getUser()
        username?.let { viewModel.cargarDatosPorUsername(it) }
    }

    // ── Diálogo de confirmación ───────────────────────────────────────────────
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            nombre = data.usuario?.firstName,
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
            .background(BackgroundLight) // Tu color de fondo base
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeader(
                nombre = data.usuario?.firstName,
                ruta = data.ruta?.nombre,
                vehiculo = if (data.vehiculo != null)
                    "${data.vehiculo.marca} · ${data.vehiculo.placa}" else null,
                onCerrarSesion = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // ── SECCIÓN: VENTAS (Énfasis Principal en blanco) ─────────────────
                SectionLabel("Generar Venta")
                Spacer(modifier = Modifier.height(12.dp))

                PrimaryActionCard(
                    title = "Venta Rápida",
                    subtitle = "Venta de mostrador directa",
                    icon = Icons.Default.PointOfSale,
                    accent = AccentIndigo,
                    accentBg = AccentIndigoSoft,
                    onClick = {
                        if (!finalDia) {
                            onIrVenta()
                        } else {
                            Toast.makeText(context, "El día ya fue finalizado", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryActionCard(
                    title = "Venta por Cliente",
                    subtitle = "Buscar cliente en el directorio",
                    icon = Icons.Default.PersonSearch,
                    accent = AccentBlue,
                    accentBg = AccentBlueSoft,
                    onClick = {
                        if (!finalDia) {
                            onIrClientes()
                        } else {
                            Toast.makeText(context, "El día ya fue finalizado", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── SECCIÓN: OTRAS OPERACIONES (Abonos y Devoluciones) ────────
                SectionLabel("Otras Operaciones")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Recibir\nAbono",
                        subtitle = "Pago de crédito",
                        icon = Icons.Default.AttachMoney,
                        accent = AccentGreen,
                        accentBg = AccentGreenSoft,
                        onClick = {
                            if (!finalDia) {
                                onIrAbonos()
                            } else {
                                Toast.makeText(
                                    context,
                                    "El día ya fue finalizado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Control\nDevolución",
                        subtitle = "Mermas / Retornos",
                        icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                        accent = AccentRed,
                        accentBg = AccentRedSoft,
                        onClick = {
                            if (!finalDia) {
                                onIrDevoluciones()
                            } else {
                                Toast.makeText(
                                    context,
                                    "El día ya fue finalizado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── SECCIÓN: GESTIÓN DE RUTA ──────────────────────────────────
                SectionLabel("Gestión de ruta")
                Spacer(modifier = Modifier.height(12.dp))

                InventoryCard(onClick = onIrInventarios)
                Spacer(modifier = Modifier.height(12.dp))

                VentasDiaCard(onClick = onIrVentasDia)

                Spacer(modifier = Modifier.height(32.dp))

                // ── SECCIÓN DE RENDIMIENTO ────────────────────────────────────
                SectionLabel("Mi Rendimiento")
                Spacer(modifier = Modifier.height(12.dp))

                ResumenGeneralCard(
                    onClick = onIrResumenDia
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── SECCIÓN: CONFIGURACIÓN ────────────────────────────────────
                SectionLabel("Configuración")
                Spacer(modifier = Modifier.height(12.dp))

                FooterActionRow(
                    icon = Icons.Default.Print,
                    label = "Impresora Bluetooth",
                    sublabel = "Configurar dispositivo",
                    onClick = onIrBluetooth
                )

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

// ── NUEVO COMPONENTE: PRIMARY ACTION CARD (Blanco con acentos) ───────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrimaryActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    accentBg: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────

// ── COMPONENTE: RESUMEN GENERAL ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResumenGeneralCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Resumen general",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Métricas, saldos y cobranza del día",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(AccentPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = "Resumen General",
                    tint = AccentPurple,
                    modifier = Modifier.size(28.dp)
                )
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
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "¿Cerrar sesión?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = if (nombre != null)
                    "Hola, $nombre. ¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo."
                else
                    "¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

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
                        text = nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hola, ${nombre ?: "Cargando…"}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
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
                            text = "Repartidor activo",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeaderChip(Icons.Default.Map, ruta ?: "Sin ruta", Modifier.weight(1f))
                HeaderChip(
                    Icons.Default.LocalShipping,
                    vehiculo ?: "Sin unidad",
                    Modifier.weight(1f)
                )
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
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
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
                    .background(AccentTealSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Inventory,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Inventario de camioneta",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text("Ver stock actual", color = TextMuted, fontSize = 12.sp)
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
                    .background(AccentIndigoSoft),
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
private fun FooterActionRow(
    icon: ImageVector,
    label: String,
    sublabel: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val iconColor = if (isDestructive) ErrorRed else AccentBlue
    val iconBg = if (isDestructive) ErrorRedSoft else AccentBlueSoft
    val labelColor = if (isDestructive) ErrorRed else TextPrimary

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        shape = RoundedCornerShape(16.dp),
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
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = labelColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(sublabel, color = TextMuted, fontSize = 11.sp)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FB)
@Composable
fun HomeScreenPreview() {
}