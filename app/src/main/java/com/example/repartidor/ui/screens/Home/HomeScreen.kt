package com.example.repartidor.ui.screens.Home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCerrarSesion: () -> Unit,
    onIrClientes: () -> Unit,
    onIrVenta: () -> Unit,
    onIrInventarios: () -> Unit,
    viewModel: HomeViewModel,
    sessionManager: SessionManager,
    onIrBluetooth: () -> Unit
) {
    val data = viewModel.homeData

    LaunchedEffect(Unit) {
        val username = sessionManager.getUser()
        username?.let {
            viewModel.cargarDatosPorUsername(it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Panel de Control",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Spacer(modifier = Modifier.height(8.dp))

                // 🌟 TARJETA DE INFORMACIÓN DEL REPARTIDOR
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Usuario",
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hola, ${data.usuario?.firstName ?: "Cargando..."}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Repartidor Activo",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = "Ruta", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ruta: ${data.ruta?.nombre ?: "Sin ruta asignada"}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = "Vehículo", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unidad: ${
                                    if (data.vehiculo != null) "${data.vehiculo.marca} - ${data.vehiculo.placa}"
                                    else "Sin vehículo"
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 🌟 GRID DE BOTONES TIPO DASHBOARD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Buscar Cliente (Mitad de pantalla)
                    DashboardActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Buscar\nCliente",
                        icon = Icons.Default.PersonSearch,
                        onClick = onIrClientes
                    )

                    // Botón Venta Rápida (Mitad de pantalla)
                    DashboardActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Venta\nRápida",
                        icon = Icons.Default.PointOfSale,
                        onClick = onIrVenta
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Inventario (Ancho completo)
                DashboardActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Inventario de Camioneta",
                    icon = Icons.Default.Inventory,
                    onClick = onIrInventarios,
                    isHorizontal = true
                )
            }

            // 🌟 ÁREA DE CONFIGURACIÓN Y SALIDA (Se va al fondo)
            Column(modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)) {
                OutlinedButton(
                    onClick = onIrBluetooth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Configurar impresora Bluetooth")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

// 🔥 COMPONENTE REUTILIZABLE PARA LOS BOTONES DEL MENÚ
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isHorizontal: Boolean = false
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(if (isHorizontal) 80.dp else 120.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isHorizontal) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Puedes mockear tus viewmodels aquí si quieres ver la preview
}