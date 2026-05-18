package com.example.repartidor.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repartidor.R
import com.example.repartidor.viewmodel.LoginViewModel
import com.example.repartidor.ui.screens.components.* //Aqui estan los colores del tema


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    val loginState = viewModel.loginState
    val error = viewModel.error

    LaunchedEffect(loginState) {
        if (loginState != null) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 🌟 CAPA DE FONDO (Detrás de todo el contenido)
        Column(modifier = Modifier.fillMaxSize()) {
            // DEGRADADO SUPERIOR (Estilo Header del Home) - Ocupa la parte superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp) // Altura fija de lo azul
                    .background(Brush.linearGradient(listOf(HeaderGradStart, HeaderGradEnd)))
            )

            // IMAGEN DE FONDO (Debajo de lo azul, detrás de la tarjeta)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa el espacio restante hacia abajo
                    .background(BackgroundLight) // Mantenemos el color de fondo base
            ) {

                Image(
                    painter = painterResource(id = R.drawable.pistache2m), // USANDO EL LOGO COMO PLACEHOLDER. Reemplázalo por tu imagen real.
                    contentDescription = "Fondo de login sutil",
                    contentScale = ContentScale.Crop, // O 'Fit' según el diseño de tu imagen
                    modifier = Modifier.fillMaxSize()
                        // 👇 Ajusta la opacidad para que la imagen sea muy sutil (0.0 a 1.0)
                        .alpha(0.1f)
                )
            }
        }

        // 🌟 CAPA DE CONTENIDO (En primer plano)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding() // Soporte para el teclado
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_reparto_camioneta),
                contentDescription = "Logo de la empresa",
                // ContentScale.Fit asegura que el rectángulo completo se vea sin deformarse ni cortarse
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    // 👇 Ajusta estos valores según el tamaño que quieras que ocupe en la pantalla
                    .width(300.dp)
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(35.dp))

            // 🌟 PANEL FLOTANTE DE INICIO DE SESIÓN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bienvenido",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingresa tu usuario para comenzar tu ruta de hoy.",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Input de Usuario
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        placeholder = { Text("Usuario", color = TextMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Icono Usuario",
                                tint = AccentBlue
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BackgroundLight,
                            unfocusedContainerColor = BackgroundLight,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de Entrar
                    Button(
                        onClick = {
                            if (usuario.isNotBlank()) {
                                viewModel.login(usuario)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White,
                            disabledContainerColor = AccentBlueSoft,
                            disabledContentColor = AccentBlue.copy(alpha = 0.5f)
                        ),
                        enabled = usuario.isNotBlank()
                    ) {
                        Text(
                            text = "Entrar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔴 MANEJO DE ERROR
            if (error != null) {
                Surface(
                    color = ErrorRedSoft,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = error,
                            color = ErrorRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pie de página
            Text(
                text = "Sistema de Reparto v1.0",
                fontSize = 12.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 24.dp, top = 24.dp)
            )
        }
    }
}