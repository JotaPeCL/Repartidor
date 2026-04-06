package com.example.repartidor.ui.screens.Venta

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VentaScreen(onIrCarrito: () -> Unit) {


    val productos = listOf(
        "cacahuates",
        "papas",
        "lunetas",
        "Krankys"
    )


    var seleccionados by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Selecciona los productos", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(productos) { producto ->
                val seleccionado = seleccionados.contains(producto)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            seleccionados = if (seleccionado) {
                                seleccionados - producto
                            } else {
                                seleccionados + producto
                            }
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(producto)

                    if (seleccionado) {
                        Text("✔")
                    }
                }
            }
        }


        Button(
            onClick = {
                if(seleccionados.isNotEmpty()){
                    onIrCarrito()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir al carrito (\${seleccionados.size})")
        }
    }
}