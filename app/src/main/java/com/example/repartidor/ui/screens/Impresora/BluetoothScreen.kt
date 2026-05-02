package com.example.repartidor.ui.screens.Impresora

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.repartidor.data.remote.BluetoothState
import com.example.repartidor.utils.getBluetoothState
import com.example.repartidor.utils.guardarImpresora
import com.example.repartidor.utils.obtenerDispositivoGuardado
import kotlinx.coroutines.launch

// ── Paleta de colores (Soft UI) ──────────────────────────────────────────────
private val BackgroundLight  = Color(0xFFF4F6FB)
private val SurfaceWhite     = Color(0xFFFFFFFF)
private val AccentBlue       = Color(0xFF3A6FD8)
private val AccentBlueSoft   = Color(0xFFEBF0FC)
private val AccentTeal       = Color(0xFF0F9E82)
private val AccentTealSoft   = Color(0xFFE6F6F2)
private val TextPrimary      = Color(0xFF111827)
private val TextMuted        = Color(0xFF9CA3AF)
private val ErrorRed         = Color(0xFFDC2626)
private val ErrorRedSoft     = Color(0xFFFEF2F2)
private val WarningOrange    = Color(0xFFF59E0B)
private val WarningOrangeSoft= Color(0xFFFEF3C7)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var dispositivos by remember { mutableStateOf(listOf<BluetoothDevice>()) }
    var impresoraGuardada by remember { mutableStateOf<BluetoothDevice?>(null) }
    var state by remember { mutableStateOf<BluetoothState?>(null) }

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = bluetoothManager.adapter
    val isEmulator = Build.FINGERPRINT.contains("generic")

    // 🔥 PERMISSION LAUNCHER
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        state = getBluetoothState(context, activity)
        if (granted) {
            dispositivos = getBondedDevicesSafe(context, adapter)
            coroutineScope.launch { snackbarHostState.showSnackbar("Permiso Bluetooth concedido") }
        } else {
            coroutineScope.launch { snackbarHostState.showSnackbar("Permiso Bluetooth denegado") }
        }
    }

    // 🔥 INIT
    LaunchedEffect(Unit) {
        state = getBluetoothState(context, activity)
        impresoraGuardada = obtenerDispositivoGuardado(context)

        if (state?.hasPermission == false) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (state?.isSupported == true && state?.isEnabled == true && state?.hasPermission == true) {
            dispositivos = getBondedDevicesSafe(context, adapter)
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            BluetoothHeader(onBack = onBack)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 🔴 ESTADO BLUETOOTH (Alertas)
            state?.let { st ->
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    when {
                        !st.isSupported -> StatusCard(
                            icon = Icons.Default.BluetoothDisabled,
                            title = "Bluetooth no soportado",
                            message = "Este dispositivo no cuenta con tecnología Bluetooth.",
                            isError = true
                        )
                        !st.isEnabled -> StatusCard(
                            icon = Icons.Default.BluetoothDisabled,
                            title = "Bluetooth Apagado",
                            message = "Por favor, enciende el Bluetooth para conectar la impresora.",
                            isError = true
                        )
                        !st.hasPermission -> StatusCard(
                            icon = Icons.Default.WarningAmber,
                            title = "Permiso Requerido",
                            message = "La app necesita permisos para buscar la impresora.",
                            isError = false,
                            actionText = "Conceder Permiso",
                            onAction = { permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🟢 IMPRESORA GUARDADA (Activa)
            if (impresoraGuardada != null) {
                val nombre = obtenerNombreDispositivo(context, impresoraGuardada)

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionLabel("Impresora actual")
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AccentTealSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = AccentTeal,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = nombre,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = impresoraGuardada?.address ?: "MAC desconocida",
                                fontSize = 13.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { impresoraGuardada = null },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentBlueSoft,
                                    contentColor = AccentBlue
                                ),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Icon(Icons.Default.BluetoothDisabled, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Desvincular o cambiar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            // 🔵 LISTA DE DISPOSITIVOS VINCULADOS
            else if (state?.isEnabled == true && state?.hasPermission == true) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionLabel("Dispositivos vinculados")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selecciona tu impresora térmica. (Asegúrate de haberla vinculado antes en los ajustes de tu teléfono).",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dispositivos) { device ->
                        val nombre = obtenerNombreDispositivo(context, device)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(78.dp)
                                .clickable {
                                    guardarImpresora(context, device)
                                    impresoraGuardada = device
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("🖨 Impresora configurada")
                                    }
                                },
                            shape = RoundedCornerShape(20.dp),
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AccentBlueSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nombre,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = device.address,
                                        color = TextMuted,
                                        fontSize = 12.sp
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

                    if (isEmulator) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = AccentBlueSoft,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Print, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Emulador detectado: Bluetooth simulado",
                                        fontSize = 13.sp,
                                        color = AccentBlue,
                                        fontWeight = FontWeight.Medium
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

// ── COMPONENTES UI ADICIONALES ───────────────────────────────────────────────

@Composable
private fun BluetoothHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Impresora Térmica",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
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

// 🎨 COMPONENTE REUTILIZABLE PARA ALERTAS DE BLUETOOTH (Soft UI)
@Composable
fun StatusCard(
    icon: ImageVector,
    title: String,
    message: String,
    isError: Boolean,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val iconColor = if (isError) ErrorRed else WarningOrange
    val iconBg    = if (isError) ErrorRedSoft else WarningOrangeSoft

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = actionText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Funciones Helper Originales (Sin Modificar su lógica) ---

fun getBondedDevicesSafe(context: Context, adapter: BluetoothAdapter?): List<BluetoothDevice> {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return emptyList()
    }
    return try {
        adapter?.bondedDevices?.toList() ?: emptyList()
    } catch (e: SecurityException) {
        emptyList()
    }
}

fun tienePermisoBluetooth(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED
}

fun obtenerNombreDispositivo(context: Context, device: BluetoothDevice?): String {
    if (!tienePermisoBluetooth(context)) return "Sin permiso"
    return try {
        device?.name ?: "Sin nombre"
    } catch (e: SecurityException) {
        "Sin acceso"
    }
}