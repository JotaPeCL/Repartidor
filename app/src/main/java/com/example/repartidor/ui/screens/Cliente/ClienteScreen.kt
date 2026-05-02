package com.example.repartidor.ui.screens.Cliente

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.repartidor.viewmodel.ClienteViewModel

// Paleta de colores...
private val BackgroundLight = Color(0xFFF4F6FB)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val AccentBlue = Color(0xFF3A6FD8)
private val AccentBlueSoft = Color(0xFFEBF0FC)
private val AccentTeal = Color(0xFF0F9E82)
private val AccentTealSoft = Color(0xFFE6F6F2)
private val TextPrimary = Color(0xFF111827)
private val TextMuted = Color(0xFF9CA3AF)
private val ErrorRed = Color(0xFFDC2626)
private val ErrorRedSoft = Color(0xFFFEF2F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(
    viewModel: ClienteViewModel,
    onClienteSeleccionado: (Int) -> Unit,
    onIrQrScanner: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var textoBusqueda by remember { mutableStateOf("") }
    val cliente = viewModel.cliente
    val resultados = viewModel.resultados
    val error = viewModel.error
    var mostrarDialogo by remember { mutableStateOf(false) }

    var tienePermiso by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        tienePermiso = granted
        if (granted) onIrQrScanner()
    }

    if (mostrarDialogo) {
        ExitConfirmDialog(
            onDismiss = { mostrarDialogo = false },
            onConfirm = {
                mostrarDialogo = false
                viewModel.limpiarClienteSeleccionado()
                onBack()
            }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Buscar Cliente", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (cliente != null) mostrarDialogo = true
                        else { viewModel.limpiarClienteSeleccionado(); onBack() }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
            if (cliente != null) {
                Surface(
                    color = SurfaceWhite,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                onClienteSeleccionado(cliente.id)
                                viewModel.limpiarClienteSeleccionado()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White)
                        ) {
                            Text(
                                text = "Continuar con ${cliente.nombre}", // 👈 Cambio de texto aquí
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis // Para evitar que nombres muy largos rompan el diseño
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa el ID, Nombre o escanea el código QR del cliente para iniciar la venta.",
                fontSize = 14.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                    viewModel.limpiarBusqueda()
                },
                placeholder = { Text("ID o Nombre del cliente", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.buscarCliente(textoBusqueda)
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = AccentBlue
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.buscarCliente(textoBusqueda)
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        disabledContainerColor = AccentBlueSoft
                    ),
                    enabled = textoBusqueda.isNotBlank()
                ) {
                    Text("Buscar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        if (tienePermiso) onIrQrScanner() else launcher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlueSoft,
                        contentColor = AccentBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escanear", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRedSoft)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = ErrorRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = error, color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── RESULTADOS (Mantenemos la lista siempre que haya datos) ──────
            if (resultados.isNotEmpty()) {
                Text(
                    text = "RESULTADOS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(resultados) { clienteResult ->
                        // Evaluamos si esta tarjeta en particular es la seleccionada
                        val isSelected = cliente?.id == clienteResult.id

                        ClienteCard(
                            nombre = clienteResult.nombre,
                            id = clienteResult.id,
                            negocio = clienteResult.nombreNegocio,
                            seleccionado = isSelected,
                            onClick = { viewModel.seleccionarCliente(clienteResult) }
                        )
                    }
                }
            }
        }
    }
}

// ── COMPONENTE DE TARJETA ───────────────────────────────────────────────────
@Composable
fun ClienteCard(
    nombre: String,
    id: Int,
    negocio: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Eliminamos el .clip() de aquí
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp), // <-- Lo agregamos como propiedad de la Card
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = if (seleccionado) 4.dp else 1.dp),
        border = if (seleccionado) BorderStroke(2.dp, AccentTeal) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (seleccionado) AccentTeal else AccentTealSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (seleccionado) SurfaceWhite else AccentTeal,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = nombre, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "$negocio ", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            if (seleccionado) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(26.dp))
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = "Seleccionar", tint = TextMuted)
            }
        }
    }
}
// ── DIÁLOGO DE SALIDA (Estilo Home) ──────────────────────────────────────────
@Composable
private fun ExitConfirmDialog(
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
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "¿Abandonar selección?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Ya tienes un cliente seleccionado.\nSi sales ahora, tendrás que volver a buscarlo.",
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
                Text("Sí, salir", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}