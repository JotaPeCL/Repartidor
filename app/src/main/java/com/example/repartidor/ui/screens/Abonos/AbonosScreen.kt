package com.example.repartidor.ui.screens.Abonos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.data.model.VentaCredito
import com.example.repartidor.ui.screens.components.AccentBlue
import com.example.repartidor.ui.screens.components.SurfaceWhite
import com.example.repartidor.ui.screens.components.TextMuted
import com.example.repartidor.viewmodel.AbonosViewModel

@Composable
fun AbonosScreen(
    viewModel: AbonosViewModel,
    onIrForm: (Int) -> Unit,
    onBack: () -> Unit
) {

    val ventas = viewModel.ventas


    // 🔍 estado búsqueda (igual que reabastecimiento)
    var searchQuery by remember { mutableStateOf("") }
    var ventaSeleccionada by remember { mutableStateOf<VentaCredito?>(null) }

    val filteredVentas = remember(searchQuery, ventas) {
        if (searchQuery.isBlank()) {
            ventas
        } else {
            ventas.filter {
                (it.nombre ?: "").lowercase().contains(searchQuery.lowercase()) ||
                        (it.nombreNegocio ?: "").lowercase().contains(searchQuery.lowercase())
            }
        }
    }


    LaunchedEffect(Unit) {
        viewModel.cargarVentas()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // 🔎 BUSCADOR (MISMO ESTILO)
        if (ventas.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = {
                    Text("Buscar cliente...", color = TextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Borrar", tint = TextMuted)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = AccentBlue
                ),
                singleLine = true
            )
        }

        // 🔄 ESTADOS
        when {
            viewModel.isLoading -> {
                // 🔄 Cargando
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }

            ventas.isEmpty() -> {
                // 📭 NO HAY VENTAS A CRÉDITO
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay ventas a crédito")
                }
            }

            filteredVentas.isEmpty() -> {
                // 🔍 SIN RESULTADOS DE BÚSQUEDA
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron clientes")
                }
            }

            else -> {
                // 📋 LISTA
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVentas) { venta ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ventaSeleccionada = if (ventaSeleccionada == venta) null else venta
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (ventaSeleccionada == venta)
                                    AccentBlue.copy(alpha = 0.2f)
                                else
                                    SurfaceWhite
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {

                                Text(venta.nombre ?: "")
                                Text(venta.nombreNegocio ?: "")

                                Spacer(modifier = Modifier.height(6.dp))

                                Text("Total: $${venta.total}")
                                Text("Pendiente: $${venta.saldoCalculado}")

                            }
                        }
                    }
                }
                if (ventaSeleccionada != null) {
                    Button(
                        onClick = {
                            onIrForm(ventaSeleccionada!!.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Abonar")
                    }
                }

            }
        }
    }
}