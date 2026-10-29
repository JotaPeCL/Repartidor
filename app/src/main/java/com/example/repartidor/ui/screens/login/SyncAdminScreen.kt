package com.example.repartidor.ui.screens.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.repository.GrupoSync
import com.example.repartidor.viewmodel.Login.SyncAdminViewModel
import com.example.repartidor.ui.screens.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncAdminScreen(viewModel: SyncAdminViewModel, onBack: () -> Unit) {
    var completa by remember { mutableStateOf(true) }
    var seleccionados by remember { mutableStateOf(setOf<GrupoSync>()) }
    var confirmar by remember { mutableStateOf(false) }

    val grupos = GrupoSync.entries.toList()
    val nombres = mapOf(
        GrupoSync.USUARIOS to "Usuarios y roles",
        GrupoSync.RUTAS to "Rutas y vehículos",
        GrupoSync.CLIENTES to "Clientes y días de visita",
        GrupoSync.PRODUCTOS to "Productos, presentaciones y precios",
        GrupoSync.INVENTARIO to "MiniBodegas e inventario"
    )

    fun regresar() {
        if (!viewModel.isLoading) onBack()
    }

    BackHandler(enabled = viewModel.isLoading || viewModel.pinValidado) { regresar() }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Sincronización Administrativa",
                onBackClick = { regresar() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // INFORMACIÓN GENERAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AccentBlueSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                            .background(SurfaceWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(26.dp))
                    }
                    Column {
                        Text("Actualización de datos", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Actualiza la información local desde el servidor.",
                            color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp
                        )
                    }
                }
            }

            // TIPO DE SINCRONIZACIÓN
            AdminSyncCard(
                title = "Tipo de Sincronización",
                icon = Icons.Default.Sync,
                accent = AccentIndigo,
                accentBg = AccentIndigo.copy(alpha = 0.1f)
            ) {
                AdminSyncOption(
                    title = "Sincronización completa",
                    description = "Actualizar todos los grupos de datos",
                    selected = completa,
                    enabled = !viewModel.isLoading,
                    onClick = { completa = true }
                )

                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

                AdminSyncOption(
                    title = "Sincronización parcial",
                    description = "Elegir únicamente los datos necesarios",
                    selected = !completa,
                    enabled = !viewModel.isLoading,
                    onClick = { completa = false }
                )
            }

            // GRUPOS DE SINCRONIZACIÓN
            if (!completa) {
                AdminSyncCard(
                    title = "Grupos a Sincronizar",
                    icon = Icons.Default.Inventory,
                    accent = AccentBlue,
                    accentBg = AccentBlueSoft
                ) {
                    grupos.forEachIndexed { index, grupo ->
                        val icono = when (grupo) {
                            GrupoSync.USUARIOS -> Icons.Default.People
                            GrupoSync.RUTAS -> Icons.Default.Route
                            GrupoSync.CLIENTES -> Icons.Default.Groups
                            GrupoSync.PRODUCTOS -> Icons.Default.Inventory
                            GrupoSync.INVENTARIO -> Icons.Default.LocalShipping
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable(enabled = !viewModel.isLoading) {
                                    seleccionados = if (grupo in seleccionados) seleccionados - grupo else seleccionados + grupo
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                                    .background(BackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icono, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                nombres[grupo].orEmpty(),
                                modifier = Modifier.weight(1f),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Checkbox(
                                checked = grupo in seleccionados,
                                onCheckedChange = { marcado ->
                                    seleccionados = if (marcado) seleccionados + grupo else seleccionados - grupo
                                },
                                enabled = !viewModel.isLoading,
                                colors = CheckboxDefaults.colors(checkedColor = AccentBlue, uncheckedColor = BorderLight)
                            )
                        }
                        if (index < grupos.lastIndex) HorizontalDivider(color = BorderLight)
                    }

                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(BackgroundLight).padding(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                            Text(
                                "Los grupos relacionados se incluirán automáticamente cuando sean necesarios.",
                                fontSize = 12.sp, color = TextMuted, lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // ESTADO DE SINCRONIZACIÓN
            if (viewModel.isLoading) {
                AdminSyncCard(
                    title = "Sincronizando información",
                    icon = Icons.Default.Sync,
                    accent = AccentBlue,
                    accentBg = AccentBlueSoft
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentBlue,
                        trackColor = AccentBlueSoft
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Actualizando información. No cierres la aplicación.",
                        color = TextMuted, fontSize = 14.sp
                    )
                }
            }

            viewModel.errorMensaje?.let { mensaje ->
                AdminSyncCard(
                    title = "Error de sincronización",
                    icon = Icons.Default.Warning,
                    accent = ErrorRed,
                    accentBg = ErrorRedSoft
                ) {
                    Text(mensaje, color = ErrorRed, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }

            viewModel.mensajeExito?.let { mensaje ->
                AdminSyncCard(
                    title = "Sincronización completada",
                    icon = Icons.Default.CheckCircle,
                    accent = AccentTeal,
                    accentBg = AccentTealSoft
                ) {
                    Text(mensaje, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }

            // BOTONES
            Button(
                onClick = { confirmar = true },
                enabled = !viewModel.isLoading && (completa || seleccionados.isNotEmpty()),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Iniciar Sincronización", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            OutlinedButton(
                onClick = { regresar() },
                enabled = !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite, contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Regresar al Login", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN
    if (confirmar) {
        val gruposElegidos = if (completa) GrupoSync.entries.toSet() else seleccionados

        AlertDialog(
            onDismissRequest = { confirmar = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(AccentBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Text(
                    "Confirmar Sincronización",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Se actualizarán los datos locales de los grupos seleccionados. ¿Deseas continuar?",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundLight)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                if (completa) "Sincronización completa" else "Sincronización parcial",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            gruposElegidos.forEach { grupo ->
                                Text("• ${nombres[grupo].orEmpty()}", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmar = false
                        viewModel.sincronizar(gruposElegidos) {}
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White)
                ) {
                    Text("Sincronizar", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmar = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun AdminSyncCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    accentBg: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun AdminSyncOption(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(description, color = TextMuted, fontSize = 12.sp)
        }
    }
}