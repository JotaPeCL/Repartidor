package com.example.repartidor.ui.screens.login

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.repartidor.viewmodel.SyncViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onSyncCompleto: () -> Unit
) {
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMensaje

    // Usamos el color de fondo del tema para mantener el contraste correcto
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // --- CONTENIDO PRINCIPAL ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícono principal minimalista
            Icon(
                imageVector = Icons.Rounded.CloudSync,
                contentDescription = "Sincronización en la nube",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sincronización de Datos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mantén tu ruta y ventas actualizadas con el servidor principal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Botón de acción principal, ancho completo
            Button(
                onClick = {
                    viewModel.sincronizar { onSyncCompleto() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Altura recomendada para accesibilidad
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sincronizar Ahora",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // --- OVERLAY DE CARGA ---
        // AnimatedVisibility le da una entrada y salida profesional
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // Tarjeta limpia en lugar de texto sobre fondo transparente
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sincronizando...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO DE ERROR ---
        if (error != null) {
            AlertDialog(
                onDismissRequest = { /* Previene cerrar tocando fuera */ },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(text = "Error de Sincronización")
                },
                text = {
                    Text(
                        text = error,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.limpiarError()
                            viewModel.sincronizar { onSyncCompleto() }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reintentar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.limpiarError() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cerrar")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}