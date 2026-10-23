package com.example.repartidor.ui.screens.RotacionesDIa

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.entity.RotacionEntity
import com.example.repartidor.data.model.entity.RotacionDetalleEntity
import com.example.repartidor.data.model.entity.RotacionSustitucionEntity
import com.example.repartidor.ui.screens.components.*
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.viewmodel.Rotacion.RotacionesDiaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun RotacionesDiaScreen(
    onBack: () -> Unit,
    viewModel: RotacionesDiaViewModel,
    sessionManager: SessionManager
) {
    val rotaciones by viewModel.rotaciones.collectAsState()
    val seleccionada by viewModel.rotacionSeleccionada.collectAsState()
    val recibidos by viewModel.productosRecibidos.collectAsState()
    val entregados by viewModel.productosEntregados.collectAsState()
    val cargando by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val printResult by viewModel.printResult.collectAsState()

    val finalDia by sessionManager.finalDiaFlow.collectAsState(
        initial = false
    )

    var mostrarConfirmarImpresion by remember {
        mutableStateOf(false)
    }
    var mostrarConfirmarCancelacion by remember {
        mutableStateOf(false)
    }
    var motivoCancelacion by remember {
        mutableStateOf("")
    }
    var rotacionParaAccion by remember {
        mutableStateOf<Int?>(null)
    }
    var mensajeResultado by remember {
        mutableStateOf<String?>(null)
    }
    var resultadoExitoso by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.cargarRotaciones()
    }

    LaunchedEffect(success) {
        if (success != null) {
            mensajeResultado = success
            resultadoExitoso = true
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            mensajeResultado = error
            resultadoExitoso = false
        }
    }

    LaunchedEffect(printResult) {
        val resultado = printResult ?: return@LaunchedEffect

        resultadoExitoso = resultado is PrintResult.Success

        mensajeResultado = when (resultado) {
            is PrintResult.Success ->
                "Ticket impreso correctamente."

            is PrintResult.NoPrinter ->
                "No hay impresora configurada."

            is PrintResult.BluetoothOff ->
                "El Bluetooth está apagado."

            is PrintResult.Error ->
                "Error al imprimir: ${resultado.msg}"
        }
    }

    // DETALLE DE ROTACIÓN

    if (seleccionada != null) {
        DetalleRotacionDialog(
            rotacion = seleccionada!!,
            recibidos = recibidos,
            entregados = entregados,
            finalDia = finalDia,
            cargando = cargando,
            onDismiss = {
                viewModel.limpiarSeleccion()
            },
            onPrintRequest = {
                rotacionParaAccion = seleccionada!!.id
                mostrarConfirmarImpresion = true
            },
            onCancelRequest = {
                rotacionParaAccion = seleccionada!!.id
                motivoCancelacion = ""
                mostrarConfirmarCancelacion = true
            }
        )
    }

    // CONFIRMAR IMPRESIÓN

    if (mostrarConfirmarImpresion) {
        AlertDialog(
            onDismissRequest = {
                mostrarConfirmarImpresion = false
            },
            title = {
                Text("Imprimir Ticket")
            },
            text = {
                Text(
                    "¿Deseas imprimir nuevamente el ticket de esta rotación?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarImpresion = false

                        rotacionParaAccion?.let {
                            viewModel.reimprimirTicket(it)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    )
                ) {
                    Text("Sí, imprimir")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        mostrarConfirmarImpresion = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // CONFIRMAR CANCELACIÓN

    if (mostrarConfirmarCancelacion) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) {
                    mostrarConfirmarCancelacion = false
                }
            },
            icon = {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = ErrorRed
                )
            },
            title = {
                Text(
                    "Cancelar rotación",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¿Estás seguro de cancelar esta rotación? " +
                                "Se revertirán los movimientos de inventario.",
                        color = TextMuted
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = motivoCancelacion,
                        onValueChange = {
                            motivoCancelacion = it
                        },
                        label = {
                            Text("Motivo de cancelación")
                        },
                        placeholder = {
                            Text("Escribe el motivo...")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 4,
                        enabled = !cargando,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (motivoCancelacion.isBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "El motivo es obligatorio.",
                            fontSize = 12.sp,
                            color = ErrorRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = motivoCancelacion.isNotBlank() &&
                            !cargando && !finalDia,
                    onClick = {
                        if (!finalDia) {
                            mostrarConfirmarCancelacion = false

                            rotacionParaAccion?.let {
                                viewModel.cancelarRotacion(
                                    rotacionId = it,
                                    motivo = motivoCancelacion.trim()
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        mostrarConfirmarCancelacion = false
                    },
                    enabled = !cargando
                ) {
                    Text("Regresar")
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // RESULTADO

    if (mensajeResultado != null) {
        AlertDialog(
            onDismissRequest = {
                mensajeResultado = null
                viewModel.limpiarEstado()
            },
            icon = {
                Icon(
                    if (resultadoExitoso)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.Info,
                    contentDescription = null,
                    tint = if (resultadoExitoso)
                        SuccessGreen
                    else
                        ErrorRed
                )
            },
            title = {
                Text(
                    if (resultadoExitoso) "Éxito" else "Error",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(mensajeResultado.orEmpty())
            },
            confirmButton = {
                Button(
                    onClick = {
                        mensajeResultado = null
                        viewModel.limpiarEstado()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    )
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            StandardTopBar(
                title = "Rotaciones del Día",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (rotaciones.isEmpty() && !cargando) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = TextMuted.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Aún no hay rotaciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Las rotaciones que realices hoy aparecerán aquí.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = rotaciones,
                        key = { it.id }
                    ) { rotacion ->
                        RotacionDiaCard(
                            rotacion = rotacion,
                            onClick = {
                                viewModel.seleccionarRotacion(
                                    rotacion.id
                                )
                            }
                        )
                    }
                }
            }

            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentBlue
                )
            }
        }
    }
}

// ============================================================
// TARJETA DE ROTACIÓN
// ============================================================

@Composable
private fun RotacionDiaCard(
    rotacion: RotacionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = AccentBlue
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Rotación #${rotacion.id}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    formatearFechaRotacion(rotacion.fecha),
                    fontSize = 12.sp,
                    color = TextMuted
                )

                if (rotacion.descripcion.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        rotacion.descripcion,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 2
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "$${String.format(Locale.US, "%.2f", rotacion.diferenciaDinero)}",
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    when (rotacion.tipoDiferencia) {
                        "CLIENTE_PAGO" -> "Cliente paga"
                        "REPARTIDOR_PAGO" -> "Repartidor paga"
                        else -> "Sin diferencia"
                    },
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// ============================================================
// DETALLE DE ROTACIÓN
// ============================================================

@Composable
private fun DetalleRotacionDialog(
    rotacion: RotacionEntity,
    recibidos: List<RotacionDetalleEntity>,
    entregados: List<RotacionSustitucionEntity>,
    finalDia: Boolean,
    cargando: Boolean,
    onDismiss: () -> Unit,
    onPrintRequest: () -> Unit,
    onCancelRequest: () -> Unit
) {
    val totalRecibido = recibidos.sumOf {
        it.cantidad * it.precioUnitario
    }

    val totalEntregado = entregados.sumOf {
        it.cantidad * it.precioUnitario
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .heightIn(max = 650.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = AccentBlue
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Detalle de Rotación",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = BackgroundLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            "Rotación #${rotacion.id}",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            formatearFechaRotacion(rotacion.fecha),
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        if (rotacion.descripcion.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))

                            Text(
                                "Motivo: ${rotacion.descripcion}",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "PRODUCTOS RECIBIDOS",
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(8.dp))

                recibidos.forEach { producto ->
                    ProductoRotacionFila(
                        nombre = producto.nombreProducto,
                        cantidad = producto.cantidad,
                        precio = producto.precioUnitario
                    )
                }

                Spacer(Modifier.height(8.dp))

                TotalRotacionFila(
                    titulo = "Valor recibido",
                    valor = totalRecibido
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = BackgroundLight
                )

                Text(
                    "PRODUCTOS ENTREGADOS",
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(8.dp))

                entregados.forEach { producto ->
                    ProductoRotacionFila(
                        nombre = producto.nombreProducto,
                        cantidad = producto.cantidad,
                        precio = producto.precioUnitario
                    )
                }

                Spacer(Modifier.height(8.dp))

                TotalRotacionFila(
                    titulo = "Valor entregado",
                    valor = totalEntregado
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = BackgroundLight
                )

                Text(
                    "DIFERENCIA DE DINERO",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    when (rotacion.tipoDiferencia) {
                        "CLIENTE_PAGO" ->
                            "Cliente paga: ${dineroRotacion(rotacion.diferenciaDinero)}"

                        "REPARTIDOR_PAGO" ->
                            "Repartidor entrega: ${dineroRotacion(rotacion.diferenciaDinero)}"

                        else ->
                            "Sin diferencia de dinero"
                    },
                    color = AccentTeal,
                    fontWeight = FontWeight.SemiBold
                )

                if (rotacion.tipoDiferencia != "SIN_DIFERENCIA") {
                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Forma: ${rotacion.formaLiquidacion}",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onPrintRequest,
                    enabled = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    )
                ) {
                    Icon(
                        Icons.Default.Print,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "Imprimir Ticket",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!finalDia) {
                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onCancelRequest,
                        enabled = !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Cancelar Rotación",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// FILA DE PRODUCTO
// ============================================================

@Composable
private fun ProductoRotacionFila(
    nombre: String,
    cantidad: Double,
    precio: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            nombre,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${cantidadRotacion(cantidad)} x ${dineroRotacion(precio)}",
                fontSize = 13.sp,
                color = TextMuted
            )

            Text(
                dineroRotacion(cantidad * precio),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun TotalRotacionFila(
    titulo: String,
    valor: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            titulo,
            fontSize = 14.sp,
            color = TextMuted
        )

        Text(
            dineroRotacion(valor),
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

// ============================================================
// FORMATOS
// ============================================================

private fun dineroRotacion(valor: Double): String {
    return "$${String.format(Locale.US, "%.2f", valor)}"
}

private fun cantidadRotacion(cantidad: Double): String {
    return if (cantidad % 1.0 == 0.0) {
        cantidad.toInt().toString()
    } else {
        cantidad.toString()
    }
}

private fun formatearFechaRotacion(fecha: String): String {
    return try {
        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        formato.format(Date(fecha.toLong()))
    } catch (e: Exception) {
        fecha
    }
}
