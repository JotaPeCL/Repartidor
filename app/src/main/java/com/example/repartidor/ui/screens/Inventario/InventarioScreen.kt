package com.example.repartidor.ui.screens.Inventario

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.InventarioViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InventarioSreen(viewModel: InventarioViewModel,
                    sessionManager: SessionManager
){
    val lista = viewModel.inventario

    LaunchedEffect(Unit) {
        val username = sessionManager.getUser()
        username?.let {
            viewModel.cargarInventario(it)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            text = "Inventario actual de la camioneta",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(lista) { item ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {

                    Text(text = item.productoNombre)

                    Text(text = "Presentación: ${item.presentacion}")

                    Text(
                        text = "Stock: ${item.cantidadActual} / ${item.cantidadInicial}"
                    )

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}