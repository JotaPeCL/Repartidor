package com.example.repartidor.ui.screens.Cliente

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ClienteScreen(onClienteSeleccionado: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Buscar cliente")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Codigo o nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClienteSeleccionado) { Text("Buscar") }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {/*QR despues*/}) {
            Text("Escaner QR")
        }
    }

}