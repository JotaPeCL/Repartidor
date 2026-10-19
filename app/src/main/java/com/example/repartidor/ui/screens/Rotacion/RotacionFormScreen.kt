
package com.example.repartidor.ui.screens.Rotacion

import android.annotation.SuppressLint
import com.example.repartidor.utils.PrintResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.dclass.SustitucionRotacionSeleccionada
import com.example.repartidor.ui.screens.Cliente.ClienteCard
import com.example.repartidor.ui.screens.components.AccentBlue
import com.example.repartidor.ui.screens.components.AccentBlueSoft
import com.example.repartidor.ui.screens.components.AccentIndigo
import com.example.repartidor.ui.screens.components.AccentTeal
import com.example.repartidor.ui.screens.components.BackgroundLight
import com.example.repartidor.ui.screens.components.ErrorRed
import com.example.repartidor.ui.screens.components.ErrorRedSoft
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.ui.screens.components.SurfaceWhite
import com.example.repartidor.ui.screens.components.TextMuted
import com.example.repartidor.ui.screens.components.TextPrimary
import com.example.repartidor.viewmodel.Cliente.ClienteViewModel

import com.example.repartidor.viewmodel.Rotacion.CarritoRotacionViewModel
import com.example.repartidor.viewmodel.Rotacion.RotacionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round


@SuppressLint("MissingPermission")
@Composable
fun RotacionFormScreen(
    carritoViewModel: CarritoRotacionViewModel,
    clienteViewModel: ClienteViewModel,
    rotacionViewModel: RotacionViewModel,
    onBack: () -> Unit,
    onRotacionExitosa: () -> Unit = {}
) {
    val items by carritoViewModel.items.collectAsState()
    val variacionesSustitucion by rotacionViewModel.variacionesSustitucion.collectAsState()

    val cliente by remember {
        derivedStateOf { clienteViewModel.cliente }
    }

    val resultados = clienteViewModel.resultados
    val error by rotacionViewModel.error.collectAsState()
    val success by rotacionViewModel.success.collectAsState()
    val isLoading by rotacionViewModel.isLoading.collectAsState()
    val printResult by rotacionViewModel.printResult.collectAsState()
    val scope = rememberCoroutineScope()
    var verificandoImpresora by remember { mutableStateOf(false) }
    var procesandoRotacion by remember { mutableStateOf(false) }

    var clienteBuscado by remember { mutableStateOf("") }
    var clienteNulo by remember { mutableStateOf(false) }
    var descripcion by remember { mutableStateOf("") }

    var showDetalles by remember { mutableStateOf(false) }
    var intentoConfirmar by remember { mutableStateOf(false) }
    var showCambioDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSinImpresoraDialog by remember { mutableStateOf(false) }
    var detalleImpresora by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }

    var itemParaCambio by remember { mutableStateOf<CarritoItem?>(null) }
    var mensajeResultado by remember { mutableStateOf("") }
    var esExito by remember { mutableStateOf(false) }

    var sustitucionesPorProducto by remember {
        mutableStateOf<Map<Int, List<SustitucionRotacionSeleccionada>>>(emptyMap())
    }

    LaunchedEffect(Unit) {
        rotacionViewModel.cargarVariacionesParaSustitucion()
    }

    LaunchedEffect(error) {
        error?.let {
            procesandoRotacion = false
            esExito = false
            mensajeResultado = it
            showResultDialog = true
        }
    }

    // Esperamos a que termine también la impresión antes de mostrar el resultado.
    LaunchedEffect(success, isLoading, printResult) {
        if (success && !isLoading) {
            procesandoRotacion = false
            esExito = true
            mensajeResultado = when {
                printResult is PrintResult.Success ->
                    "Rotación registrada correctamente. Ticket enviado a la impresora."
                printResult is PrintResult.NoPrinter ->
                    "Rotación registrada correctamente, sin imprimir ticket."
                else ->
                    "Rotación registrada correctamente, pero hubo un problema al imprimir el ticket."
            }
            showResultDialog = true

            carritoViewModel.limpiar()
            sustitucionesPorProducto = emptyMap()
            clienteNulo = false
            clienteBuscado = ""
            descripcion = ""
            intentoConfirmar = false
            clienteViewModel.limpiarBusqueda()
            rotacionViewModel.limpiarEstado()
        }
    }

    val cambiosPendientes = remember(items, sustitucionesPorProducto) {
        items.filter { item ->
            sustitucionesPorProducto[item.productoVariacionId].isNullOrEmpty()
        }
    }

    val totalProductos = remember(items) {
        items.sumOf { it.cantidad }
    }

    val valorRecibido = remember(items) {
        items.sumOf {
            dineroACentavosRotacion(it.precio * it.cantidad)
        }
    }

    val valorEntregado = remember(sustitucionesPorProducto) {
        sustitucionesPorProducto.values.flatten().sumOf {
            dineroACentavosRotacion(it.precio * it.cantidad)
        }
    }

    Scaffold(
        topBar = {
            StandardTopBar(
                title = "Rotación",
                onBackClick = onBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$totalProductos producto(s)",
                            fontSize = 14.sp,
                            color = TextMuted
                        )

                        Text(
                            text = "$${centavosADineroRotacion(valorRecibido)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            intentoConfirmar = true
                            showDetalles = true

                            when {
                                !clienteNulo && cliente == null -> {
                                    mensajeResultado = "Debes seleccionar un cliente o marcar Cliente nulo (venta rápida)."
                                    esExito = false
                                    showResultDialog = true
                                }

                                descripcion.isBlank() -> {
                                    mensajeResultado = "Debes indicar el motivo o descripción de la rotación."
                                    esExito = false
                                    showResultDialog = true
                                }

                                cambiosPendientes.isNotEmpty() -> {
                                    mensajeResultado = "Todos los productos deben tener un producto de cambio."
                                    esExito = false
                                    showResultDialog = true
                                }

                                else -> showConfirmDialog = true
                            }
                        },
                        enabled = items.isNotEmpty() && !isLoading && !procesandoRotacion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue
                        )
                    ) {
                        Text(
                            text = "Confirmar Rotacion",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Productos a rotar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BackgroundLight
                        )
                    ) {
                        Text(
                            text = "No hay productos seleccionados.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(
                    items,
                    key = { it.productoVariacionId }
                ) { item ->
                    CarritoItemRotacionCard(
                        item = item,
                        carritoViewModel = carritoViewModel,
                        sustituciones = sustitucionesPorProducto[item.productoVariacionId] ?: emptyList(),
                        onCambiar = {
                            itemParaCambio = item
                            showCambioDialog = true
                        },
                        onCantidadCambiada = {
                            sustitucionesPorProducto =
                                sustitucionesPorProducto - item.productoVariacionId
                            intentoConfirmar = false
                        }
                    )
                }
            }
            item {
                RotacionResumenCard(
                    valorRecibido = valorRecibido,
                    valorEntregado = valorEntregado
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDetalles = !showDetalles }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detalles de la rotación",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = when {
                                clienteNulo -> "Cliente nulo (venta rápida)"
                                cliente != null -> "Cliente seleccionado"
                                else -> "Selecciona un cliente"
                            },
                            fontSize = 12.sp,
                            color = if (clienteNulo || cliente != null) TextMuted else TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.rotate(if (showDetalles) 180f else 0f)
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = showDetalles,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = clienteNulo,
                                onCheckedChange = { marcado ->
                                    clienteNulo = marcado
                                    intentoConfirmar = false

                                    if (marcado) {
                                        clienteViewModel.limpiarBusqueda()
                                        clienteBuscado = ""
                                    }
                                }
                            )

                            Text(
                                text = "Cliente nulo (venta rápida)",
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }

                        if (!clienteNulo) {
                            Text(
                                text = "Buscar cliente",
                                fontSize = 13.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    bottom = 6.dp
                                )
                            )

                            OutlinedTextField(
                                value = clienteBuscado,
                                onValueChange = {
                                    clienteBuscado = it
                                    clienteViewModel.limpiarBusqueda()
                                },
                                placeholder = {
                                    Text(
                                        "Nombre o ID del cliente",
                                        color = TextMuted
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = defaultTextFieldColorsRotacion()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    clienteViewModel.buscarCliente(clienteBuscado)
                                },
                                enabled = clienteBuscado.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentIndigo
                                )
                            ) {
                                Text("Buscar")
                            }

                            cliente?.let {
                                ClienteCard(
                                    nombre = it.nombre,
                                    id = it.id,
                                    negocio = it.nombreNegocio,
                                    seleccionado = true,
                                    direccion = it.direccion,
                                    onClick = {}
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            resultados
                                .filter { it.id != cliente?.id }
                                .forEach { resultado ->
                                    ClienteCard(
                                        nombre = resultado.nombre,
                                        id = resultado.id,
                                        negocio = resultado.nombreNegocio,
                                        seleccionado = cliente?.id == resultado.id,
                                        direccion = resultado.direccion,
                                        onClick = {
                                            clienteViewModel.seleccionarCliente(resultado)
                                            clienteBuscado = resultado.nombre
                                            intentoConfirmar = false
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = {
                                descripcion = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            label = {
                                Text("Motivo / descripción *")
                            },
                            placeholder = {
                                Text("Ej. Rotación de producto con baja venta")
                            },
                            colors = defaultTextFieldColorsRotacion()
                        )

                        if (intentoConfirmar && cambiosPendientes.isNotEmpty()) {
                            AdvertenciaRotacion(
                                texto = "Hay productos que todavía no tienen seleccionado su cambio."
                            )
                        }

                    }
                }
            }




        }
    }

    if (showCambioDialog && itemParaCambio != null) {
        CambioProductoRotacionDialog(
            item = itemParaCambio!!,
            variaciones = variacionesSustitucion,
            seleccionActual = sustitucionesPorProducto[itemParaCambio!!.productoVariacionId] ?: emptyList(),
            onDismiss = {
                showCambioDialog = false
                itemParaCambio = null
            },
            onConfirm = { seleccion ->
                sustitucionesPorProducto =
                    sustitucionesPorProducto + (itemParaCambio!!.productoVariacionId to seleccion)

                showCambioDialog = false
                itemParaCambio = null
                intentoConfirmar = false
            }
        )
    }

    if (showConfirmDialog) {
        CustomStyledDialogRotacion(
            title = "Confirmar rotación",
            message = buildString {
                append(
                    if (clienteNulo)
                        "¿Deseas registrar esta rotación como venta rápida sin cliente?"
                    else
                        "¿Deseas registrar esta rotación con el cliente seleccionado?"
                )
                append("\n\n")
                append("Valor recibido: $${centavosADineroRotacion(valorRecibido)}")
                append("\n")
                append("Valor entregado: $${centavosADineroRotacion(valorEntregado)}")
                append("\n\n")
                append(mensajeDiferenciaRotacion(valorRecibido, valorEntregado))
                append("\n\nConfirma que la diferencia se liquidó en efectivo.")
            },
            icon = Icons.Default.Info,
            iconColor = AccentBlue,
            iconBgColor = AccentBlueSoft,
            confirmText = "Confirmar",
            onConfirm = {
                if (!procesandoRotacion && !isLoading) {
                    procesandoRotacion = true
                    verificandoImpresora = true
                    showConfirmDialog = false

                    scope.launch {
                        val verificacion = try {
                            withContext(Dispatchers.IO) {
                                rotacionViewModel.verificarImpresora()
                            }
                        } catch (e: Exception) {
                            detalleImpresora = e.message ?: "No se pudo verificar la impresora."
                            null
                        } finally {
                            verificandoImpresora = false
                        }

                        if (verificacion is PrintResult.Success) {
                            rotacionViewModel.registrarRotacion(
                                clienteId = if (clienteNulo) null else cliente?.id,
                                carrito = items,
                                sustitucionesPorProducto = sustitucionesPorProducto,
                                descripcion = descripcion,
                                clienteNombre = if (clienteNulo) "Venta rápida" else cliente?.nombre,
                                imprimir = true
                            )
                        } else {
                            if (verificacion != null) detalleImpresora = "La impresora no está disponible."
                            showSinImpresoraDialog = true
                        }
                    }
                }
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    if (showSinImpresoraDialog) {
        CustomStyledDialogRotacion(
            title = "Impresora no disponible",
            message = "$detalleImpresora\n\n¿Deseas registrar la rotación sin imprimir ticket?",
            icon = Icons.Default.Warning,
            iconColor = ErrorRed,
            iconBgColor = ErrorRedSoft,
            confirmText = "Registrar sin imprimir",
            onConfirm = {
                showSinImpresoraDialog = false
                rotacionViewModel.registrarRotacion(
                    clienteId = if (clienteNulo) null else cliente?.id,
                    carrito = items,
                    sustitucionesPorProducto = sustitucionesPorProducto,
                    descripcion = descripcion,
                    clienteNombre = if (clienteNulo) "Venta rápida" else cliente?.nombre,
                    imprimir = false
                )
            },
            onDismiss = { showSinImpresoraDialog = false
                procesandoRotacion = false }
        )
    }

    if (showResultDialog) {
        CustomStyledDialogRotacion(
            title = "Resultado",
            message = mensajeResultado,
            icon = if (esExito) Icons.Default.CheckCircle else Icons.Default.Warning,
            iconColor = if (esExito) AccentTeal else ErrorRed,
            iconBgColor = if (esExito) Color(0xFFE6F6F2) else ErrorRedSoft,
            confirmText = "Aceptar",
            onConfirm = {
                showResultDialog = false
                if (esExito) onRotacionExitosa()
            },
            onDismiss = {
                showResultDialog = false
                if (esExito) onRotacionExitosa()
            },
            showDismissButton = false
        )
    }
    if (verificandoImpresora || isLoading) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceWhite
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentBlue)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (verificandoImpresora)
                            "Conectando con la impresora..."
                        else
                            "Registrando rotación e imprimiendo tickets...",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun CarritoItemRotacionCard(
    item: CarritoItem,
    carritoViewModel: CarritoRotacionViewModel,
    sustituciones: List<SustitucionRotacionSeleccionada>,
    onCambiar: () -> Unit,
    onCantidadCambiada: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productoNombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = item.presentacionNombre,
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Text(
                        text = "$${String.format("%.2f", item.precio)} c/u",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                // ── CONTROLES DE CANTIDAD ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(
                        BackgroundLight,
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    IconButton(
                        onClick = {
                            carritoViewModel.actualizarCantidad(
                                item.productoVariacionId,
                                item.cantidad - 1
                            )
                            onCantidadCambiada()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Quitar",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = item.cantidad.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AccentBlue,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = {
                            carritoViewModel.actualizarCantidad(
                                item.productoVariacionId,
                                item.cantidad + 1
                            )
                            onCantidadCambiada()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ── BOTÓN DE CAMBIO ──
                Button(
                    onClick = onCambiar,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentIndigo
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Cambiar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── SUSTITUCIÓN CONFIGURADA ──
            if (sustituciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = BackgroundLight)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cambio por:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentIndigo
                )

                Spacer(modifier = Modifier.height(2.dp))

                sustituciones.forEach { sustitucion ->
                    Text(
                        text = "${sustitucion.cantidad} × " +
                                "${sustitucion.nombreProducto} " +
                                "(${sustitucion.presentacionNombre}) " +
                                "— $${String.format("%.2f", sustitucion.precio)}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}



@Composable
private fun CambioProductoRotacionDialog(
    item: CarritoItem,
    variaciones: List<ProductoSustitucion>,
    seleccionActual: List<SustitucionRotacionSeleccionada>,
    onDismiss: () -> Unit,
    onConfirm: (List<SustitucionRotacionSeleccionada>) -> Unit
) {
    val objetivoCentavos = remember(item) {
        dineroACentavosRotacion(item.precio * item.cantidad)
    }

    val variacionesDisponibles = remember(variaciones, item.productoVariacionId) {
        variaciones.filter {
            it.id != item.productoVariacionId && it.stockActual >= 1.0
        }
    }

    var cantidades by remember(item.productoVariacionId, seleccionActual) {
        mutableStateOf(
            seleccionActual.associate { it.productoVariacionId to it.cantidad }
        )
    }

    val seleccionadas = remember(cantidades, variacionesDisponibles) {
        cantidades.mapNotNull { (id, cantidad) ->
            val variacion = variacionesDisponibles.firstOrNull { it.id == id }
            if (variacion != null && cantidad > 0) {
                SustitucionRotacionSeleccionada(
                    productoVariacionId = variacion.id,
                    nombreProducto = variacion.nombreProducto,
                    presentacionNombre = variacion.presentacionNombre,
                    precio = variacion.precio,
                    cantidad = cantidad
                )
            } else null
        }
    }

    val valorSeleccionado = seleccionadas.sumOf {
        dineroACentavosRotacion(it.precio * it.cantidad)
    }

    val puedeConfirmar = seleccionadas.isNotEmpty() &&
            seleccionadas.size == cantidades.size &&
            seleccionadas.all { seleccion ->
                val variacion = variacionesDisponibles.firstOrNull {
                    it.id == seleccion.productoVariacionId
                }
                variacion != null &&
                        seleccion.cantidad > 0 &&
                        seleccion.cantidad <= variacion.stockActual.toInt()
            }

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {

                // ==========================================
                // TITULO Y BOTON X
                // ==========================================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Cambiar producto",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar cambio",
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${item.productoNombre} - ${item.presentacionNombre}",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Valor a cambiar: $${centavosADineroRotacion(objetivoCentavos)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )

                // ==========================================
                // RESUMEN DE DINERO
                // ==========================================

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentBlueSoft
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Cambio seleccionado: $${centavosADineroRotacion(valorSeleccionado)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = mensajeDiferenciaRotacion(
                                objetivoCentavos,
                                valorSeleccionado
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (objetivoCentavos == valorSeleccionado)
                                AccentTeal
                            else
                                TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (variacionesDisponibles.isEmpty()) {
                    Text(
                        text = "No hay productos de cambio con stock disponible",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                } else {
                    if (seleccionadas.isNotEmpty()) {
                        Text(
                            text = "Cambios seleccionados:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        seleccionadas.forEach { seleccion ->
                            val variacion = variacionesDisponibles.firstOrNull {
                                it.id == seleccion.productoVariacionId
                            }
                            val puedeAumentar = variacion != null &&
                                    seleccion.cantidad < variacion.stockActual.toInt()

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = AccentBlueSoft
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${seleccion.nombreProducto} (${seleccion.presentacionNombre})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "$${String.format("%.2f", seleccion.precio)} c/u",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val nuevaCantidad = seleccion.cantidad - 1
                                            cantidades = if (nuevaCantidad <= 0) {
                                                cantidades - seleccion.productoVariacionId
                                            } else {
                                                cantidades +
                                                        (seleccion.productoVariacionId to nuevaCantidad)
                                            }
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Quitar",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = seleccion.cantidad.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (puedeAumentar) {
                                                cantidades = cantidades +
                                                        (seleccion.productoVariacionId to seleccion.cantidad + 1)
                                            }
                                        },
                                        enabled = puedeAumentar,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Agregar",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BackgroundLight)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = "Productos disponibles para el cambio:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    variacionesDisponibles.filter { it.id !in cantidades }.forEach { variacion ->
                        val stockMaximo = variacion.stockActual.toInt()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BackgroundLight
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = variacion.nombreProducto,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = variacion.presentacionNombre,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", variacion.precio)} • Stock: ${formatearStockRotacion(variacion.stockActual)}",
                                        fontSize = 11.sp,
                                        color = AccentBlue
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (stockMaximo > 0) {
                                            cantidades = cantidades + (variacion.id to 1)
                                        }
                                    },
                                    enabled = stockMaximo > 0,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentIndigo
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "Agregar", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cambio seleccionado: $${centavosADineroRotacion(valorSeleccionado)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = mensajeDiferenciaRotacion(objetivoCentavos, valorSeleccionado),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (objetivoCentavos == valorSeleccionado) AccentTeal else TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        dismissButton = {},
        confirmButton = {
            Button(
                onClick = { onConfirm(seleccionadas) },
                enabled = puedeConfirmar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aceptar cambio", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun RotacionResumenCard(
    valorRecibido: Int,
    valorEntregado: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Valor recibido",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Text(
                        text =
                            "$${centavosADineroRotacion(valorRecibido)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Valor entregado",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Text(
                        text =
                            "$${centavosADineroRotacion(valorEntregado)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (valorRecibido == valorEntregado)
                                AccentBlue
                            else
                                ErrorRed
                    )
                }
            }
            Text(
                text = mensajeDiferenciaRotacion(valorRecibido, valorEntregado),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (valorRecibido == valorEntregado) AccentTeal else TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun AdvertenciaRotacion(
    texto: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ErrorRedSoft
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = ErrorRed
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = texto,
                fontSize = 12.sp,
                color = ErrorRed,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun defaultTextFieldColorsRotacion() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = BackgroundLight,
        unfocusedContainerColor = BackgroundLight,
        focusedBorderColor = AccentBlue,
        unfocusedBorderColor = Color.Transparent,
        cursorColor = AccentBlue,
        focusedLabelColor = AccentBlue,
        unfocusedLabelColor = TextMuted
    )

@Composable
private fun CustomStyledDialogRotacion(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDismissButton: Boolean = true
) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        dismissButton = if (showDismissButton) {
            {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        } else null,
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconColor,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

private fun dineroACentavosRotacion(
    valor: Double
): Int = round(valor * 100).toInt()

private fun centavosADineroRotacion(
    valor: Int
): String =
    String.format("%.2f", valor / 100.0)

private fun formatearStockRotacion(
    stock: Double
): String =
    if (stock % 1.0 == 0.0) {
        stock.toInt().toString()
    } else {
        String.format("%.2f", stock)
    }

private fun mensajeDiferenciaRotacion(
    valorRecibidoCentavos: Int,
    valorEntregadoCentavos: Int
): String {
    val diferencia = valorEntregadoCentavos - valorRecibidoCentavos
    return when {
        diferencia > 0 -> "EL CLIENTE PAGA $${centavosADineroRotacion(diferencia)} MÁS"
        diferencia < 0 -> "EL REPARTIDOR ENTREGA $${centavosADineroRotacion(-diferencia)}"
        else -> "ROTACIÓN SIN DIFERENCIA"
    }
}
