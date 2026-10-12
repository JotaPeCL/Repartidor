package com.example.repartidor.ui.screens.Rotacion

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
import kotlin.math.round


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

    var clienteBuscado by remember { mutableStateOf("") }
    var clienteNulo by remember { mutableStateOf(false) }
    var descripcion by remember { mutableStateOf("") }

    var showDetalles by remember { mutableStateOf(false) }
    var intentoConfirmar by remember { mutableStateOf(false) }
    var showCambioDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
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
            esExito = false
            mensajeResultado = it
            showResultDialog = true
        }
    }

    LaunchedEffect(success) {
        if (success) {
            esExito = true
            mensajeResultado = "Rotación registrada correctamente"
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

    val cambiosConValorIncorrecto = remember(items, sustitucionesPorProducto) {
        items.filter { item ->
            val sustituciones = sustitucionesPorProducto[item.productoVariacionId]

            !sustituciones.isNullOrEmpty() &&
                    dineroACentavosRotacion(
                        sustituciones.sumOf { it.precio * it.cantidad }
                    ) != dineroACentavosRotacion(item.precio * item.cantidad)
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

                                cambiosConValorIncorrecto.isNotEmpty() -> {
                                    mensajeResultado = "El valor de cada cambio debe ser exactamente igual al valor del producto rotado."
                                    esExito = false
                                    showResultDialog = true
                                }

                                else -> showConfirmDialog = true
                            }
                        },
                        enabled = items.isNotEmpty() && !isLoading,
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

                        if (intentoConfirmar && cambiosConValorIncorrecto.isNotEmpty()) {
                            AdvertenciaRotacion(
                                texto = "El valor del cambio debe ser exactamente igual al valor del producto rotado."
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
            message = if (clienteNulo) {
                "¿Deseas registrar esta rotación como venta rápida sin cliente?"
            } else {
                "¿Deseas registrar esta rotación con el cliente seleccionado?"
            },
            icon = Icons.Default.Info,
            iconColor = AccentBlue,
            iconBgColor = AccentBlueSoft,
            confirmText = "Confirmar",
            onConfirm = {
                showConfirmDialog = false
                rotacionViewModel.registrarRotacion(
                    clienteId = if (clienteNulo) null else cliente?.id,
                    carrito = items,
                    sustitucionesPorProducto = sustitucionesPorProducto,
                    descripcion = descripcion
                )
            },
            onDismiss = { showConfirmDialog = false }
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

    if (isLoading) {
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
                        text = "Registrando rotación...",
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
        variaciones.filter { it.id != item.productoVariacionId }
    }

    var cantidades by remember(item.productoVariacionId, seleccionActual) {
        mutableStateOf(
            seleccionActual.associate {
                it.productoVariacionId to it.cantidad
            }
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
            } else {
                null
            }
        }
    }

    val valorSeleccionado = seleccionadas.sumOf {
        dineroACentavosRotacion(it.precio) * it.cantidad
    }

    val restante = objetivoCentavos - valorSeleccionado

    val candidatosValidos = remember(
        variacionesDisponibles,
        cantidades,
        objetivoCentavos
    ) {
        obtenerVariacionesValidasRotacion(
            variaciones = variacionesDisponibles,
            cantidadesActuales = cantidades,
            objetivoCentavos = objetivoCentavos
        ).filter { it.id !in cantidades }
    }

    val puedeConfirmar =
        valorSeleccionado == objetivoCentavos &&
                seleccionadas.isNotEmpty() &&
                seleccionadas.size == cantidades.size &&
                seleccionadas.all { seleccion ->
                    val variacion = variacionesDisponibles.firstOrNull {
                        it.id == seleccion.productoVariacionId
                    }

                    variacion != null &&
                            seleccion.cantidad <= variacion.stockActual.toInt()
                }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Cambiar producto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = TextPrimary
                )

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
                        text = "No hay producto que pueda cambiarse",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                } else {
                    // ── CAMBIOS SELECCIONADOS ──
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
                                    seleccion.cantidad < variacion.stockActual.toInt() &&
                                    puedeCompletarValorRotacion(
                                        variaciones = variacionesDisponibles,
                                        objetivoCentavos = objetivoCentavos,
                                        cantidadesActuales = cantidades +
                                                (seleccion.productoVariacionId to seleccion.cantidad + 1)
                                    )

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

                    // ── PRODUCTOS DISPONIBLES PARA COMPLETAR EL CAMBIO ──
                    if (restante > 0) {
                        Text(
                            text = "Productos que pueden completar el cambio:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (candidatosValidos.isEmpty()) {
                            Text(
                                text = "No hay producto que pueda cambiarse",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = TextMuted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp)
                            )
                        } else {
                            candidatosValidos.forEach { variacion ->
                                val cantidadActual = cantidades[variacion.id] ?: 0
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
                                                if (cantidadActual == 0 && stockMaximo > 0) {
                                                    cantidades = cantidades + (variacion.id to 1)
                                                }
                                            },
                                            enabled = cantidadActual < stockMaximo,
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

                                            Text(
                                                text = "Agregar",
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── RESUMEN DEL CAMBIO ──
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Cambio seleccionado: $${centavosADineroRotacion(valorSeleccionado)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (puedeConfirmar) AccentTeal else TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (restante > 0) {
                        Text(
                            text = "Faltan $${centavosADineroRotacion(restante)}",
                            fontSize = 12.sp,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (restante < 0) {
                        Text(
                            text = "El cambio excede $${centavosADineroRotacion(-restante)}",
                            fontSize = 12.sp,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (puedeConfirmar) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "El valor del cambio coincide exactamente.",
                            fontSize = 11.sp,
                            color = AccentTeal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(seleccionadas) },
                enabled = puedeConfirmar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aceptar cambio",
                    fontWeight = FontWeight.SemiBold
                )
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

private fun obtenerVariacionesValidasRotacion(
    variaciones: List<ProductoSustitucion>,
    cantidadesActuales: Map<Int, Int>,
    objetivoCentavos: Int
): List<ProductoSustitucion> {
    val valorActual = cantidadesActuales.entries.sumOf { (id, cantidad) ->
        val variacion = variaciones.firstOrNull { it.id == id }
        if (variacion == null) 0
        else dineroACentavosRotacion(variacion.precio) * cantidad
    }

    val restanteCentavos = objetivoCentavos - valorActual
    if (restanteCentavos <= 0) return emptyList()

    return variaciones.filter { candidata ->
        val cantidadActual = cantidadesActuales[candidata.id] ?: 0
        val stockDisponible = candidata.stockActual.toInt() - cantidadActual
        val precioCentavos = dineroACentavosRotacion(candidata.precio)

        if (precioCentavos <= 0 || stockDisponible <= 0 || precioCentavos > restanteCentavos) {
            return@filter false
        }

        val cantidadMaxima = minOf(
            stockDisponible,
            restanteCentavos / precioCentavos
        )

        (1..cantidadMaxima).any { cantidad ->
            puedeCompletarValorRotacion(
                variaciones = variaciones,
                objetivoCentavos = objetivoCentavos,
                cantidadesActuales = cantidadesActuales +
                        (candidata.id to cantidadActual + cantidad)
            )
        }
    }
}

private fun puedeCompletarValorRotacion(
    variaciones: List<ProductoSustitucion>,
    objetivoCentavos: Int,
    cantidadesActuales: Map<Int, Int>
): Boolean {
    var valorSeleccionado = 0

    for ((id, cantidad) in cantidadesActuales) {
        val variacion = variaciones.firstOrNull { it.id == id } ?: return false
        if (cantidad < 0 || cantidad > variacion.stockActual.toInt()) return false

        valorSeleccionado += dineroACentavosRotacion(variacion.precio) * cantidad
        if (valorSeleccionado > objetivoCentavos) return false
    }

    val restantes = variaciones.mapNotNull { variacion ->
        val stockRestante = variacion.stockActual.toInt() -
                (cantidadesActuales[variacion.id] ?: 0)

        if (stockRestante <= 0) null
        else variacion.copy(stockActual = stockRestante.toDouble())
    }

    val objetivoRestante = objetivoCentavos - valorSeleccionado
    if (objetivoRestante == 0) return true

    return puedeFormarValorExactoRotacion(
        variaciones = restantes,
        objetivo = objetivoRestante
    )
}

private fun puedeFormarValorExactoRotacion(
    variaciones: List<ProductoSustitucion>,
    objetivo: Int
): Boolean {
    if (objetivo == 0) return true
    if (objetivo < 0) return false

    val alcanzable = BooleanArray(objetivo + 1)
    alcanzable[0] = true

    variaciones.forEach { variacion ->
        val precio = dineroACentavosRotacion(variacion.precio)
        val stock = variacion.stockActual.toInt()

        if (precio <= 0 || stock <= 0 || precio > objetivo) {
            return@forEach
        }

        repeat(stock) {
            for (suma in objetivo downTo precio) {
                if (alcanzable[suma - precio]) {
                    alcanzable[suma] = true
                }
            }
        }
    }

    return alcanzable[objetivo]
}