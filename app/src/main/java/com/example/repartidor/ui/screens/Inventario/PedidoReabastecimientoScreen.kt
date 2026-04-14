package com.example.repartidor.ui.screens.Inventario

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun PedidoReabastecimientoScreen(
    onConfirmar: () -> Unit,
    onVolver: () -> Unit
) {

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Pedido de Reabastecimiento", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Aquí irá el resumen del pedido
        Text("Resumen del pedido (pendiente)")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onConfirmar) {
            Text("Confirmar Pedido")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onVolver) {
            Text("Volver")
        }
    }
}