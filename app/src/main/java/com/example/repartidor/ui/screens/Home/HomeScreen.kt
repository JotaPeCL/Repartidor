package com.example.repartidor.ui.screens.Home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.Home.HomeViewModel
import com.example.repartidor.ui.screens.components.*

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
    onIrRotacion: () -> Unit,
    onIrRotacionesDia: () -> Unit
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

    Column(
        modifier = Modifier.fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader(
            nombre = data.usuario?.firstName,
            ruta = data.ruta?.nombre,
            vehiculo = data.vehiculo?.let { "${it.marca} · ${it.placa}" },
            onCerrarSesion = { showLogoutDialog = true }
        )

        Spacer(Modifier.height(20.dp))

        Column(Modifier.padding(horizontal = 20.dp)) {
            // ACCIONES PRINCIPALES
            SectionLabel("Acciones Principales")
            Spacer(Modifier.height(12.dp))

            ModernActionCard(
                title = "Venta Rápida",
                subtitle = "Venta de mostrador directa",
                icon = Icons.Default.PointOfSale,
                accent = AccentIndigo,
                accentBg = AccentIndigoSoft,
                featured = true,
                onClick = { if (!finalDia) onIrVenta() else showFinalizadoToast(context) }
            )
            Spacer(Modifier.height(10.dp))
            ModernActionCard(
                title = "Venta por Cliente",
                subtitle = "Buscar cliente en el directorio",
                icon = Icons.Default.PersonSearch,
                accent = AccentBlue,
                accentBg = AccentBlueSoft,
                featured = true,
                onClick = { if (!finalDia) onIrClientes() else showFinalizadoToast(context) }
            )

            Spacer(Modifier.height(24.dp))

            // OTRAS OPERACIONES
            SectionLabel("Otras Operaciones")
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModernActionCard(
                    title = "Recibir Abono",
                    subtitle = "Pago de crédito",
                    icon = Icons.Default.AttachMoney,
                    accent = AccentGreen,
                    accentBg = AccentGreenSoft,
                    compact = true,
                    modifier = Modifier.weight(1f),
                    onClick = { if (!finalDia) onIrAbonos() else showFinalizadoToast(context) }
                )
                ModernActionCard(
                    title = "Control Devolución",
                    subtitle = "Mermas / Retornos",
                    icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                    accent = AccentRed,
                    accentBg = AccentRedSoft,
                    compact = true,
                    modifier = Modifier.weight(1f),
                    onClick = { if (!finalDia) onIrDevoluciones() else showFinalizadoToast(context) }
                )
            }

            Spacer(Modifier.height(10.dp))
            ModernActionCard(
                title = "Rotación de productos",
                subtitle = "Realizar cambios directos con clientes",
                icon = Icons.Default.SwapHoriz,
                accent = AccentPurple,
                accentBg = AccentPurpleSoft,
                onClick = { if (!finalDia) onIrRotacion() else showFinalizadoToast(context) }
            )

            Spacer(Modifier.height(24.dp))

            // GESTIÓN DE RUTA
            SectionLabel("Gestión de Ruta")
            Spacer(Modifier.height(12.dp))

            ModernActionCard(
                "Inventario de camioneta", "Ver stock actual disponible",
                Icons.Default.Inventory, AccentTeal, AccentTealSoft,
                onClick = onIrInventarios
            )
            Spacer(Modifier.height(10.dp))
            ModernActionCard(
                "Ventas del día", "Ver y reimprimir tickets de cobro",
                Icons.Default.Receipt, AccentIndigo, AccentIndigoSoft,
                onClick = onIrVentasDia
            )
            Spacer(Modifier.height(10.dp))
            ModernActionCard(
                "Devoluciones del día", "Historial, estatus y cancelaciones",
                Icons.Default.History, AccentRed, AccentRedSoft,
                onClick = onIrDevolucionesDia
            )
            Spacer(Modifier.height(10.dp))
            ModernActionCard(
                "Rotaciones del día", "Historial, tickets y cancelaciones",
                Icons.Default.Autorenew, AccentPurple, AccentPurpleSoft,
                onClick = onIrRotacionesDia
            )

            Spacer(Modifier.height(24.dp))

            // MI RENDIMIENTO
            SectionLabel("Mi Rendimiento")
            Spacer(Modifier.height(12.dp))

            ModernActionCard(
                title = "Resumen general",
                subtitle = "Métricas, saldos y cobranza acumulada",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accent = Color(0xFFD97706),
                accentBg = Color(0xFFFFF3DC),
                featured = true,
                onClick = onIrResumenDia
            )


            Spacer(Modifier.height(24.dp))

            // CONFIGURACIÓN
            SectionLabel("Configuración")
            Spacer(Modifier.height(12.dp))
            ModernActionCard(
                title = "Impresora Bluetooth",
                subtitle = "Configurar dispositivo MPT-II",
                icon = Icons.Default.Print,
                accent = AccentBlue,
                accentBg = AccentBlueSoft,
                onClick = onIrBluetooth
            )
            Spacer(Modifier.height(36.dp))
        }
    }
}

private fun showFinalizadoToast(context: android.content.Context) {
    Toast.makeText(context, "El día ya fue finalizado", Toast.LENGTH_SHORT).show()
}

// TARJETAS MODERNAS
@Composable
private fun ModernActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    accentBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    featured: Boolean = false
) {
    val shape = RoundedCornerShape(19.dp)
    val gradient = if (featured) {
        Brush.horizontalGradient(listOf(accentBg, Color.White, accentBg.copy(alpha = 0.8f)))
    } else {
        Brush.horizontalGradient(listOf(Color.White, accentBg.copy(alpha = 0.65f)))
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(
            if (compact) 122.dp else if (featured) 90.dp else 78.dp
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(gradient)
                .border(1.dp, Color.White.copy(alpha = 0.9f), shape)
                .padding(horizontal = if (compact) 12.dp else 15.dp, vertical = 12.dp)
        ) {
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    IconTile(icon, accent, accentBg, 42.dp)
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            lineHeight = 18.sp, maxLines = 2, color = TextPrimary)
                        Text(subtitle, fontSize = 13.sp, lineHeight = 16.sp,
                            maxLines = 2, color = TextMuted)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconTile(icon, accent, accentBg, if (featured) 52.dp else 45.dp)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, fontSize = if (featured) 16.sp else 15.sp,
                            fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                        Spacer(Modifier.height(3.dp))
                        Text(subtitle, fontSize = 13.sp, color = TextMuted,
                            maxLines = 2, lineHeight = 17.sp)
                    }
                    Spacer(Modifier.width(5.dp))
                    Box(
                        Modifier.size(31.dp).clip(CircleShape).background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IconTile(icon: ImageVector, accent: Color, accentBg: Color, size: Dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(accentBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(25.dp))
    }
}

// ENCABEZADO
@Composable
private fun HomeHeader(
    nombre: String?,
    ruta: String?,
    vehiculo: String?,
    onCerrarSesion: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.linearGradient(listOf(
                Color(0xFF123C87), Color(0xFF2478E5), Color(0xFF60A5FA)
            )))
            .padding(top = 52.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(50.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Hola, ${nombre ?: "Cargando…"}",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape)
                            .background(Color(0xFF6EF0B0)))
                        Spacer(Modifier.width(6.dp))
                        Text("Repartidor activo", color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp)
                    }
                }
                IconButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HeaderChip(Icons.Default.Map, ruta ?: "Sin ruta", Modifier.weight(1f))
                HeaderChip(Icons.Default.LocalShipping, vehiculo ?: "Sin unidad",
                    Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeaderChip(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White,
            modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White, fontSize = 11.sp,
            fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

// TÍTULOS DE SECCIÓN
@Composable
private fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(19.dp)
            .clip(RoundedCornerShape(3.dp)).background(AccentBlue))
        Spacer(Modifier.width(9.dp))
        Text(text, color = TextPrimary, fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(Modifier.weight(1f), thickness = 1.dp,
            color = Color(0xFFE2E8F0))
    }
}

// DIÁLOGO PARA CERRAR SESIÓN
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
                Modifier.size(56.dp).clip(CircleShape).background(ErrorRedSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null, tint = ErrorRed,
                    modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text("¿Cerrar sesión?", fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = TextPrimary, textAlign = TextAlign.Center)
        },
        text = {
            Text(
                if (nombre != null)
                    "Hola, $nombre. ¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo."
                else
                    "¿Seguro que deseas salir?\nTendrás que iniciar sesión de nuevo.",
                fontSize = 14.sp, color = TextMuted,
                textAlign = TextAlign.Center, lineHeight = 20.sp
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
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
                    containerColor = ErrorRed, contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}