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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.repartidor.data.remote.BluetoothState
import com.example.repartidor.utils.getBluetoothState
import com.example.repartidor.utils.guardarImpresora
import com.example.repartidor.utils.obtenerDispositivoGuardado
import kotlinx.coroutines.launch

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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Impresora Térmica",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 🔴 ESTADO BLUETOOTH (Alertas)
            state?.let { st ->
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
                        message = "Por favor, enciende el Bluetooth de tu dispositivo para conectar la impresora.",
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

            Spacer(modifier = Modifier.height(16.dp))

            // 🟢 IMPRESORA GUARDADA (Activa)
            if (impresoraGuardada != null) {
                val nombre = obtenerNombreDispositivo(context, impresoraGuardada)

                Text(
                    text = "Impresora Actual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = impresoraGuardada?.address ?: "MAC desconocida",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { impresoraGuardada = null },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.BluetoothDisabled, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Cambiar Impresora")
                        }
                    }
                }
            }
            // 🔵 LISTA DE DISPOSITIVOS VINCULADOS
            else if (state?.isEnabled == true && state?.hasPermission == true) {
                Text(
                    text = "Dispositivos Vinculados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Selecciona tu impresora térmica de la lista. (Asegúrate de haberla vinculado antes en los ajustes de Android).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dispositivos) { device ->
                        val nombre = obtenerNombreDispositivo(context, device)

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    guardarImpresora(context, device)
                                    impresoraGuardada = device
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("🖨 Impresora $nombre seleccionada")
                                    }
                                },
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = device.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.BluetoothConnected,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    if (isEmulator) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🧪 Emulador: Bluetooth simulado",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 🎨 COMPONENTE REUTILIZABLE PARA ALERTAS DE BLUETOOTH
@Composable
fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    isError: Boolean,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = contentColor, contentColor = containerColor)
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