package com.example.repartidor.ui.screens.Inventario

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items

@Composable
fun ReabastecimientoScreen(
    onIrPedido: () -> Unit,
    onBack: () -> Unit
) {

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Reabastecimiento", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Aquí después pondrás lista de productos
        Text("Lista de productos (pendiente)")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onIrPedido) {
            Text("Ir a Pedido")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onBack) {
            Text("Volver")
        }
    }
}
