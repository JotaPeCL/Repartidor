package com.example.repartidor.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.utils.SecureStorage
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.viewmodel.Login.DispositivoViewModel

@Composable
fun DispositivoScreen(
    viewModel: DispositivoViewModel,
    sessionManager: SessionManager,
    onActivacionExitosa: () -> Unit
) {
    val context = LocalContext.current

    val secureStorage = remember {
        SecureStorage(context)
    }

    var codigo by remember { mutableStateOf("") }
    var credencial by remember { mutableStateOf("") }
    var mostrarCredencial by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()


    LaunchedEffect(success) {
        if (success) {

            secureStorage.saveCredential(credencial)

            sessionManager.guardarCodigoDispositivo(codigo.trim())

            sessionManager.guardarDispositivoActivado()

            viewModel.limpiarSuccess()

            onActivacionExitosa()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFFF4F6FB)),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            // Ícono
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEBF0FC)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalShipping,
                        contentDescription = "Dispositivo",
                        modifier = Modifier.size(42.dp),
                        tint = Color(0xFF3A6FD8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Activar dispositivo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Este teléfono debe activarse antes de poder utilizar la aplicación.",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Código
            OutlinedTextField(
                value = codigo,
                onValueChange = {
                    codigo = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Código del dispositivo")
                },
                placeholder = {
                    Text("Ej. OSMIT-001")
                },
                singleLine = true,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Credencial
            OutlinedTextField(
                value = credencial,
                onValueChange = {
                    credencial = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Credencial")
                },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (mostrarCredencial) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            mostrarCredencial = !mostrarCredencial
                        }
                    ) {
                        Icon(
                            imageVector = if (mostrarCredencial) {
                                Icons.Rounded.VisibilityOff
                            } else {
                                Icons.Rounded.Visibility
                            },
                            contentDescription = if (mostrarCredencial) {
                                "Ocultar credencial"
                            } else {
                                "Mostrar credencial"
                            }
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Error
            if (error != null) {
                Text(
                    text = error!!,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFDC2626),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Botón
            Button(
                onClick = {
                    viewModel.activarDispositivo(
                        codigo = codigo.trim(),
                        credencial = credencial
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = codigo.isNotBlank() &&
                        credencial.isNotBlank() &&
                        !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3A6FD8)
                )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Text(
                        text = "Activando..."
                    )

                } else {

                    Text(
                        text = "Activar dispositivo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}