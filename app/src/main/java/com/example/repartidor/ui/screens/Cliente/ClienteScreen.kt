package com.example.repartidor.ui.screens.Cliente

import android.Manifest
import android.content.pm.PackageManager
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.repartidor.viewmodel.ClienteViewModel

@Composable
fun ClienteScreen(
    viewModel: ClienteViewModel,
    onClienteSeleccionado: (Int) -> Unit,
    onIrQrScanner: () -> Unit,
    onBack: () -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(16.dp)) {
        val context = LocalContext.current
        var textoBusqueda by remember { mutableStateOf("") }
        val cliente = viewModel.cliente
        val error = viewModel.error


        var tienePermiso by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        // 🔥 Launcher para pedir permiso
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            tienePermiso = granted

            if (granted) {
                onIrQrScanner() // 🔥 ahora sí entra al QR
            }
        }

        Text("Buscar cliente")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                viewModel.limpiarBusqueda()
            },
            label = { Text("ID del cliente") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val id = textoBusqueda.toIntOrNull()
                if (id != null) {
                    viewModel.buscarCliente(id)
                }
            }
        ) {
            Text("Buscar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔴 ERROR
        if (error != null) {
            Text(text = error)
        }

        if (cliente != null) {

            Spacer(modifier = Modifier.height(16.dp))

            Text("Cliente encontrado:")
            Text("ID: ${cliente.id}")
            Text("Nombre: ${cliente.nombre}") // ajusta

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onClienteSeleccionado(cliente.id)
                    viewModel.limpiarClienteSeleccionado()
                }
            ) {
                Text("Siguiente")
            }
        }

        Button(
            onClick = {
                if (tienePermiso) {
                    onIrQrScanner()
                } else {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }
        ) {
            Text("Escaner QR")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (cliente != null) {
                    mostrarDialogo = true
                } else {
                    viewModel.limpiarClienteSeleccionado()
                    onBack()
                }
            }
        ) {
            Text("Volver")
        }
    }

    if (mostrarDialogo) {
        Dialog(onDismissRequest = { mostrarDialogo = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text("¿Salir?", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Se perderá el cliente seleccionado")

                    Spacer(modifier = Modifier.height(20.dp))

                    Row {
                        Button(onClick = {
                            mostrarDialogo = false
                        }) {
                            Text("Cancelar")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(onClick = {
                            mostrarDialogo = false
                            viewModel.limpiarClienteSeleccionado()
                            onBack()
                        }) {
                            Text("Salir")
                        }
                    }
                }
            }
        }
    }

}