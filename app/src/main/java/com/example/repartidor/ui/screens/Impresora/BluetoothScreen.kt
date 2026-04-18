package com.example.repartidor.ui.screens.Impresora

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.repartidor.utils.obtenerDispositivoGuardado
import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.repartidor.data.remote.BluetoothState
import com.example.repartidor.utils.getBluetoothState
import com.example.repartidor.utils.guardarImpresora



@Composable
fun BluetoothScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val activity = context as Activity

    var dispositivos by remember { mutableStateOf(listOf<BluetoothDevice>()) }
    var impresoraGuardada by remember { mutableStateOf<BluetoothDevice?>(null) }
    var mensaje by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<BluetoothState?>(null) }

    val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val adapter = bluetoothManager.adapter

    val isEmulator = Build.FINGERPRINT.contains("generic")

    // 🔥 PERMISSION LAUNCHER
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->

        if (granted) {

            mensaje = "Permiso Bluetooth concedido"

            state = getBluetoothState(context, activity)

            dispositivos = getBondedDevicesSafe(context, adapter)

        } else {
            mensaje = "Permiso Bluetooth denegado"
        }
    }

    // 🔥 INIT
    LaunchedEffect(Unit) {

        state = getBluetoothState(context, activity)

        impresoraGuardada = obtenerDispositivoGuardado(context)

        // 🔥 SI NO TIENE PERMISO → LO PEDIMOS
        if (state?.hasPermission == false) {
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }

        // 🔥 SI TODO OK → CARGAR DISPOSITIVOS
        if (state?.isSupported == true &&
            state?.isEnabled == true &&
            state?.hasPermission == true
        ) {
            dispositivos = getBondedDevicesSafe(context, adapter)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            text = "Configuración de impresora",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔵 ESTADO BLUETOOTH
        state?.let { st ->

            when {
                !st.isSupported -> Text("❌ Bluetooth no soportado")

                !st.isEnabled -> Text("⚠️ Activa Bluetooth")

                !st.hasPermission -> {

                    Text("⚠️ Permiso requerido")

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    }) {
                        Text("Dar permiso")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔵 IMPRESORA GUARDADA
        if (impresoraGuardada != null) {

            val nombre = obtenerNombreDispositivo(context, impresoraGuardada)

            Text("Impresora actual:")
            Text(nombre)
            Text(impresoraGuardada?.address ?: "")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                impresoraGuardada = null
            }) {
                Text("Cambiar impresora")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 🔵 LISTA DE DISPOSITIVOS
        if (impresoraGuardada == null) {

            Text("Selecciona impresora Bluetooth")

            Spacer(modifier = Modifier.height(12.dp))

            dispositivos.forEach { device ->

                val nombre = obtenerNombreDispositivo(context, device)

                Text(
                    text = "$nombre - ${device.address}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {

                            guardarImpresora(context, device)

                            impresoraGuardada = device

                            mensaje = "Impresora guardada"
                        }
                        .padding(8.dp)
                )
            }

            if (isEmulator) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("🧪 Emulador: Bluetooth simulado")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onBack) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(mensaje)
    }
}

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


fun obtenerNombreDispositivo(
    context: Context,
    device: BluetoothDevice?
): String {

    if (!tienePermisoBluetooth(context)) return "Sin permiso"

    return try {
        device?.name ?: "Sin nombre"
    } catch (e: SecurityException) {
        "Sin acceso"
    }
}