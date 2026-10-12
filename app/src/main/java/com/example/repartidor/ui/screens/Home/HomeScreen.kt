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
import com.example.repartidor.ui.screens.components.*

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
    onIrDevolucionesDia: () -> Unit,
    onIrAbonos: () -> Unit,
    onIrResumenDia: () -> Unit,
    onIrRotacion: () -> Unit
) {
    val data = viewModel.homeData
    val context = LocalContext.current

    var showLogoutDialog by remember { mutableStateOf(false) }
    val finalDia by sessionManager.finalDiaFlow.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        val username = sessionManager.getUser()
        username?.let { viewModel.cargarDatosPorUsername(it) }
    }

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
                nombre = data.usuario?.firstName,
                ruta = data.ruta?.nombre,
                vehiculo = if (data.vehiculo != null)
                    "${data.vehiculo.marca} · ${data.vehiculo.placa}" else null,
                onCerrarSesion = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // ── BLOQUE 1: VENTAS PRINCIPALES ─────────────────────────────
                SectionLabel("Acciones Principales")
                Spacer(modifier = Modifier.height(10.dp))

                PrimaryActionCard(
                    title = "Venta Rápida",
                    subtitle = "Venta de mostrador directa",
                    icon = Icons.Default.PointOfSale,
                    accent = AccentIndigo,
                    accentBg = AccentIndigoSoft,
                    onClick = { if (!finalDia) onIrVenta() else showFinalizadoToast(context) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrimaryActionCard(
                    title = "Venta por Cliente",
                    subtitle = "Buscar cliente en el directorio",
                    icon = Icons.Default.PersonSearch,
                    accent = AccentBlue,
                    accentBg = AccentBlueSoft,
                    onClick = { if (!finalDia) onIrClientes() else showFinalizadoToast(context) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── BLOQUE 2: OTRAS OPERACIONES ──────────────────────────────
                SectionLabel("Otras Operaciones")
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCardCompact(
                        modifier = Modifier.weight(1f),
                        title = "Recibir Abono",
                        subtitle = "Pago de crédito",
                        icon = Icons.Default.AttachMoney,
                        accent = AccentGreen,
                        accentBg = AccentGreenSoft,
                        onClick = { if (!finalDia) onIrAbonos() else showFinalizadoToast(context) }
                    )
                    ActionCardCompact(
                        modifier = Modifier.weight(1f),
                        title = "Control Devolución",
                        subtitle = "Mermas / Retornos",
                        icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                        accent = AccentRed,
                        accentBg = AccentRedSoft,
                        onClick = { if (!finalDia) onIrDevoluciones() else showFinalizadoToast(context) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ActionCardFullWidth(
                    title = "Rotación de productos",
                    subtitle = "Realizar cambios directos con clientes",
                    icon = Icons.Default.SwapHoriz,
                    accent = AccentPurple,
                    accentBg = AccentPurpleSoft,
                    onClick = { if (!finalDia) onIrRotacion() else showFinalizadoToast(context) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── BLOQUE 3: GESTIÓN DE RUTA ────────────────────────────────
                SectionLabel("Gestión de Ruta")
                Spacer(modifier = Modifier.height(10.dp))

                InventoryCard(onClick = onIrInventarios)
                Spacer(modifier = Modifier.height(10.dp))
                VentasDiaCard(onClick = onIrVentasDia)
                Spacer(modifier = Modifier.height(10.dp))
                DevolucionesDiaCard(onClick = onIrDevolucionesDia)

                Spacer(modifier = Modifier.height(24.dp))

                // ── BLOQUE 4: MI RENDIMIENTO ─────────────────────────────────
                SectionLabel("Mi Rendimiento")
                Spacer(modifier = Modifier.height(10.dp))

                ResumenGeneralCard(onClick = onIrResumenDia)

                Spacer(modifier = Modifier.height(24.dp))

                // ── BLOQUE 5: CONFIGURACIÓN ──────────────────────────────────
                SectionLabel("Configuración")
                Spacer(modifier = Modifier.height(10.dp))

                FooterActionRow(
                    icon = Icons.Default.Print,
                    label = "Impresora Bluetooth",
                    sublabel = "Configurar dispositivo MPT-II",
                    onClick = onIrBluetooth
                )

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

// Función auxiliar para notificar día finalizado
private fun showFinalizadoToast(context: android.content.Context) {
    Toast.makeText(context, "El día ya fue finalizado", Toast.LENGTH_SHORT).show()
}

// ── COMPONENTES RE DISEÑADOS PARA MÁXIMA LIMPIEZA ────────────────────────────

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
            .height(86.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
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
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCardCompact(
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
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    maxLines = 1
                )
                Text(
                    subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCardFullWidth(
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
            .height(74.dp),
        shape = RoundedCornerShape(18.dp),
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
                    .size(40.dp)
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
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    subtitle,
                    color = TextMuted,
                    fontSize = 11.sp
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
private fun InventoryCard(onClick: () -> Unit) {
    StandardRouteCard(
        title = "Inventario de camioneta",
        subtitle = "Ver stock actual disponible",
        icon = Icons.Default.Inventory,
        accent = AccentTeal,
        accentBg = AccentTealSoft,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VentasDiaCard(onClick: () -> Unit) {
    StandardRouteCard(
        title = "Ventas del día",
        subtitle = "Ver y reimprimir tickets de cobro",
        icon = Icons.Default.Receipt,
        accent = AccentIndigo,
        accentBg = AccentIndigoSoft,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevolucionesDiaCard(onClick: () -> Unit) {
    StandardRouteCard(
        title = "Devoluciones del día",
        subtitle = "Historial, estatus y cancelaciones",
        icon = Icons.Default.History,
        accent = AccentRed,
        accentBg = AccentRedSoft,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StandardRouteCard(
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
            .height(72.dp),
        shape = RoundedCornerShape(18.dp),
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
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = TextMuted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResumenGeneralCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Resumen general", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Métricas, saldos y cobranza acumulada", color = TextMuted, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AccentPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FooterActionRow(
    icon: ImageVector,
    label: String,
    sublabel: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = AccentBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(sublabel, color = TextMuted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

// ── COMPONENTES COMPLEMENTARIOS (HEADER Y DIÁLOGOS) ──────────────────────────

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
                modifier = Modifier.size(56.dp).clip(CircleShape).background(ErrorRedSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(26.dp))
            }
        },
        title = { Text(text = "¿Cerrar sesión?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center) },
        text = {
            Text(
                text = if (nombre != null) "Hola, $nombre. ¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo."
                else "¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo.",
                fontSize = 14.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 20.sp
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

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
            .background(Brush.linearGradient(listOf(HeaderGradStart, HeaderGradEnd)))
            .padding(top = 52.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Hola, ${nombre ?: "Cargando…"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF6EF0B0)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Repartidor activo", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                IconButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HeaderChip(Icons.Default.Map, ruta ?: "Sin ruta", Modifier.weight(1f))
                HeaderChip(Icons.Default.LocalShipping, vehiculo ?: "Sin unidad", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeaderChip(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.18f)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FB)
@Composable
fun HomeScreenPreview() {}