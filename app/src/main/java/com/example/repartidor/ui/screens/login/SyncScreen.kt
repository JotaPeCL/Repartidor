package com.example.repartidor.ui.screens.login

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repartidor.data.preferences.SyncPreferences
import com.example.repartidor.viewmodel.SyncViewModel
import androidx.compose.ui.window.Dialog

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onSyncCompleto: () -> Unit
) {

    val isLoading = viewModel.isLoading
    val error = viewModel.errorMensaje
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = {
                    viewModel.sincronizar {
                        onSyncCompleto()
                    }
                },
                enabled = true//!yaSync
            ) {
                Text("Sincronizar")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)), // fondo oscuro
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sincronizando...", color = Color.White)
                }
            }
        }

        if (error != null) {
            Dialog(onDismissRequest = { }) { // no se cierra tocando afuera
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("Error", fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(error)

                        Spacer(modifier = Modifier.height(20.dp))

                        Row {
                            Button(onClick = {
                                viewModel.limpiarError()
                            }) {
                                Text("Cerrar")
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(onClick = {
                                viewModel.limpiarError()
                                viewModel.sincronizar {
                                    onSyncCompleto()
                                }
                            }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }


}

