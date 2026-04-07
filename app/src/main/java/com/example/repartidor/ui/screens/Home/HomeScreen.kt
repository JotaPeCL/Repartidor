package com.example.repartidor.ui.screens.Home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.HomeViewModel


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

}


@Composable
fun HomeScreen(
    onCerrarSesion: () -> Unit,
    onIrClientes: () -> Unit,
    onIrVenta: () -> Unit,
    onIrInventarios: () -> Unit,
    viewModel: HomeViewModel,
    sessionManager: SessionManager
) {
    val data = viewModel.homeData
    LaunchedEffect(Unit) {
        val username = sessionManager.getUser()
        username?.let {
            viewModel.cargarDatosPorUsername(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {



        Text(
            text = "Menu principal",
            style = MaterialTheme.typography.headlineMedium
        )
        Column {
            // 🔹 USUARIO
            Text(
                text = "Bienvenido ${data.usuario?.firstName ?: ""}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 RUTA
            Text(
                text = "Ruta: ${data.ruta?.nombre ?: "Sin ruta"}"
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 🔹 VEHICULO (desde la ruta 🔥)
            Text(
                text = "Vehículo: ${
                    if (data.vehiculo != null)
                        "${data.vehiculo.marca} ${data.vehiculo.placa}"
                    else "Sin vehículo"
                }"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 MENÚ

            Button(
                onClick = onIrClientes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) { Text("Buscar Cliente") }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onIrVenta,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) { Text("Venta Rapida") }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onIrInventarios,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) { Text("Inventario") }

        }

        Column() {

            Button(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesion")
            }
        }

    }

}