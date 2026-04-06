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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onCerrarSesion = {},
        onIrClientes = {},
        onIrVenta = {},
        onIrInventarios = {}
    )
}

@Composable
fun HomeScreen(
    onCerrarSesion: () -> Unit,
    onIrClientes: () -> Unit,
    onIrVenta: () -> Unit,
    onIrInventarios: () -> Unit,
) {
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