package com.example.repartidor.ui.screens.components
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: () -> Unit,
    containerColor: Color = Color(0xFFFFFFFF),
    contentColor: Color = Color(0xFF111827)
) {
    TopAppBar(
        title = {
            // Cambiamos Column por Row para ponerlos uno al lado del otro
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 20.sp, // Mantenemos el tamaño en 20sp para el título principal
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.width(8.dp)) // Agregamos un pequeño margen de separación
                    Text(
                        text = " $subtitle", // Opcional: puedes agregar un guion para separarlos visualmente
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF) // Equivalente a TextMuted
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = contentColor
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        )
    )
}