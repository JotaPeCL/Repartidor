package com.example.repartidor.ui.screens.Cliente

import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    onQrDetectado: (Int) -> Unit,
    onBack: () -> Unit // 🔥 Es importante poder cancelar y salir
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var escaneado by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context)
    }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val barcodeScanner = BarcodeScanning.getClient()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val mediaImage = imageProxy.image

                    if (mediaImage != null && !escaneado) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val clienteId = barcode.rawValue?.toIntOrNull()
                                    if (clienteId != null && !escaneado) {
                                        escaneado = true
                                        onQrDetectado(clienteId)
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🔥 CAJA PRINCIPAL QUE APILA TODO
    Box(modifier = Modifier.fillMaxSize()) {

        // 1. LA CÁMARA DE FONDO
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        // 2. EL OVERLAY (El diseño oscuro con el hueco en el centro)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Este graphicsLayer es el truco mágico para que el BlendMode.Clear funcione y "recorte" el hoyo
                .graphicsLayer { alpha = 0.99f }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculamos el tamaño del cuadrito (70% del ancho de la pantalla)
            val rectSize = canvasWidth * 0.7f
            val left = (canvasWidth - rectSize) / 2f
            val top = (canvasHeight - rectSize) / 2f

            // Dibujamos el fondo oscuro semitransparente sobre TODA la pantalla
            drawRect(color = Color.Black.copy(alpha = 0.6f))

            // "Recortamos" el hueco transparente en el centro
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(rectSize, rectSize),
                cornerRadius = CornerRadius(16.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            // 3. DIBUJAR LAS ESQUINAS BLANCAS (Para que se vea PRO)
            val strokeWidth = 4.dp.toPx()
            val cornerLength = 32.dp.toPx()
            val cornerColor = Color.White

            // Arriba - Izquierda
            drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
            drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)
            // Arriba - Derecha
            drawLine(cornerColor, Offset(left + rectSize, top), Offset(left + rectSize - cornerLength, top), strokeWidth)
            drawLine(cornerColor, Offset(left + rectSize, top), Offset(left + rectSize, top + cornerLength), strokeWidth)
            // Abajo - Izquierda
            drawLine(cornerColor, Offset(left, top + rectSize), Offset(left + cornerLength, top + rectSize), strokeWidth)
            drawLine(cornerColor, Offset(left, top + rectSize), Offset(left, top + rectSize - cornerLength), strokeWidth)
            // Abajo - Derecha
            drawLine(cornerColor, Offset(left + rectSize, top + rectSize), Offset(left + rectSize - cornerLength, top + rectSize), strokeWidth)
            drawLine(cornerColor, Offset(left + rectSize, top + rectSize), Offset(left + rectSize, top + rectSize - cornerLength), strokeWidth)
        }

        // 4. TEXTO DE INSTRUCCIÓN (Alineado arriba del cuadrito)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Centra el código QR en el recuadro",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .offset(y = (-180).dp) // Lo subimos para que quede arriba del cuadro
            )
        }

        // 5. BOTÓN DE CERRAR / VOLVER (Esquina superior izquierda)
        IconButton(
            onClick = onBack, // ¡Asegúrate de pasar onBack al llamar a esta pantalla!
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar Escáner",
                tint = Color.White
            )
        }
    }
}