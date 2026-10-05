package com.example.repartidor.ui.screens.Devoluciones

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PrintDisabled
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.dclass.SustitucionSeleccionada
import com.example.repartidor.ui.screens.Cliente.ClienteCard
import com.example.repartidor.ui.screens.components.StandardTopBar
import com.example.repartidor.utils.PrintResult
import com.example.repartidor.viewmodel.Cliente.ClienteViewModel
import com.example.repartidor.viewmodel.Devoluciones.CarritoDevolucionViewModel
import com.example.repartidor.viewmodel.Devoluciones.DevolucionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.repartidor.ui.screens.components.* //Aqui estan los colores del tema


@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevolucionFormScreen(
    carritoViewModel: CarritoDevolucionViewModel,
    clienteViewModel: ClienteViewModel,
    devolucionViewModel: DevolucionViewModel,
    onBack: () -> Unit,
    onDevolucionExitosa: () -> Unit = {}
) {
    val items by carritoViewModel.items.collectAsState()
    val cliente by remember { derivedStateOf { clienteViewModel.cliente } }
    val resultados = clienteViewModel.resultados

    val error = devolucionViewModel.error
    val success = devolucionViewModel.success

    var motivo by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf("") }

    var clienteBuscado by remember { mutableStateOf("") }
    var clienteNulo by remember { mutableStateOf(false) }
    var esExito by remember { mutableStateOf(false) }

    var imprimir by remember { mutableStateOf(true) }
    var showPrinterDialog by remember { mutableStateOf(false) }
    var printerStatus by remember { mutableStateOf<PrintResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Estado para la animación de ocultar/mostrar detalles
    var showDetalles by remember { mutableStateOf(false) }

    // Estado para cambio de producto
    var itemParaCambio by remember { mutableStateOf<CarritoItem?>(null) }
    var showCambioDialog by remember { mutableStateOf(false) }

    var sustitucionesPorProducto by remember {
        mutableStateOf<Map<Int, List<SustitucionSeleccionada>>>(emptyMap())
    }

    val motivos = listOf(
        "Devolución de cliente",
        "Producto dañado",
        "Merma",
        "Producto roto",
        "Error de manejo",
        "Otro"
    )

    val totalProductos = remember(items) {
        items.sumOf { it.cantidad }
    }

    // Esta consulta solo devuelve variaciones con stock > 0.
    // Por eso, cuando ya cargó la información, que una variación
    // del carrito no aparezca significa que actualmente tiene 0 stock.
    val variacionesDisponibles by devolucionViewModel
        .getVariacionesDisponiblesParaSustitucion()
        .collectAsState(initial = null)

    val stockYaCargado = variacionesDisponibles != null

    val itemsSinStock = remember(items, variacionesDisponibles) {
        val disponibles =
            variacionesDisponibles ?: emptyList()

        if (variacionesDisponibles == null) {
            emptyList()
        } else {
            items.filter { item ->
                disponibles.none {
                    it.id == item.productoVariacionId
                }
            }
        }
    }

    val cambiosPendientes = remember(
        items,
        motivo,
        itemsSinStock,
        sustitucionesPorProducto
    ) {
        if (motivo == "Devolución de cliente") {
            items.filter { item ->
                itemsSinStock.any {
                    it.productoVariacionId == item.productoVariacionId
                } &&
                        sustitucionesPorProducto[
                            item.productoVariacionId
                        ].isNullOrEmpty()
            }
        } else {
            emptyList()
        }
    }

    val cambiosConValorIncorrecto = remember(
        items,
        motivo,
        sustitucionesPorProducto
    ) {
        if (motivo == "Devolución de cliente") {
            items.filter { item ->
                val sustituciones =
                    sustitucionesPorProducto[
                        item.productoVariacionId
                    ]

                !sustituciones.isNullOrEmpty() &&
                        dineroACentavos(
                            sustituciones.sumOf {
                                it.precio * it.cantidad
                            }
                        ) != dineroACentavos(
                    item.precio * item.cantidad
                )
            }
        } else {
            emptyList()
        }
    }

    val devolucionesSinStockBloqueadas =
        motivo.isNotBlank() &&
                motivo != "Devolución de cliente" &&
                itemsSinStock.isNotEmpty()

    val hayBloqueoDevolucion =
        devolucionesSinStockBloqueadas ||
                cambiosPendientes.isNotEmpty() ||
                cambiosConValorIncorrecto.isNotEmpty()

    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        error?.let {
            esExito = false
            mensajeResultado = it
            showResultDialog = true
            isProcessing = false
        }
    }

    LaunchedEffect(success) {
        if (success) {
            val print = devolucionViewModel.printResult

            esExito = true

            mensajeResultado = when (print) {
                is PrintResult.Success ->
                    "Devolución registrada e impresa correctamente"

                is PrintResult.NoPrinter ->
                    "Devolución registrada (sin impresora)"

                is PrintResult.BluetoothOff ->
                    "Devolución registrada (Bluetooth apagado)"

                is PrintResult.Error ->
                    "Devolución registrada pero error al imprimir"

                else ->
                    "Devolución registrada correctamente"
            }

            showResultDialog = true
            carritoViewModel.limpiar()
            sustitucionesPorProducto = emptyMap()
            devolucionViewModel.reset()
            isProcessing = false
        }
    }

    fun intentarRegistrar(imprimir: Boolean) {

        if (!stockYaCargado) {
            isProcessing = false
            mensajeResultado =
                "Espera un momento mientras se verifica el stock."
            esExito = false
            showResultDialog = true
            return
        }

        if (motivo.isBlank()) {
            isProcessing = false
            mensajeResultado = "Debes seleccionar un motivo."
            esExito = false
            showResultDialog = true
            showDetalles = true
            return
        }

        if (hayBloqueoDevolucion) {
            isProcessing = false
            mensajeResultado =
                when {
                    devolucionesSinStockBloqueadas ->
                        "No se puede realizar la devolución porque uno o más productos no tienen existencias."

                    cambiosPendientes.isNotEmpty() ->
                        "Hay productos sin existencias que requieren seleccionar un cambio."

                    else ->
                        "El valor de los productos de cambio debe ser exactamente igual al valor de la devolución."
                }

            esExito = false
            showResultDialog = true
            return
        }

        val clienteIdFinal =
            if (clienteNulo) null else cliente?.id

        isProcessing = true

        scope.launch {

            val result = withContext(Dispatchers.IO) {
                devolucionViewModel.verificarImpresora()
            }

            printerStatus = result

            when (result) {

                is PrintResult.Success -> {

                    devolucionViewModel.registrarDevolucion(
                        clienteId = clienteIdFinal,
                        clienteNombre = cliente?.nombre,
                        clienteNulo = clienteNulo,
                        motivo = motivo,
                        observacion = observacion,
                        carrito = items,
                        sustitucionesPorProducto = sustitucionesPorProducto,
                        imprimir = imprimir
                    )
                }

                else -> {
                    isProcessing = false
                    showPrinterDialog = true
                }
            }
        }
    }

    // ── DIÁLOGOS ESTILIZADOS ──────────────────────────────────────────────────

    if (showConfirmDialog) {

        CustomStyledDialog(
            title = "Confirmar devolución",
            message = "¿Deseas registrar esta devolución con los productos y motivos especificados?",
            icon = Icons.Default.Info,
            iconColor = AccentBlue,
            iconBgColor = AccentBlueSoft,
            confirmText = "Confirmar",
            onConfirm = {
                showConfirmDialog = false
                intentarRegistrar(imprimir = true)
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    if (showPrinterDialog) {

        val mensaje = when (printerStatus) {

            is PrintResult.BluetoothOff ->
                "El Bluetooth está apagado."

            is PrintResult.NoPrinter ->
                "No hay impresora configurada."

            is PrintResult.Error ->
                "No se pudo conectar a la impresora."

            else ->
                "Impresora no disponible."
        }

        CustomStyledDialog(
            title = "Impresora no disponible",
            message = "$mensaje\n\n¿Deseas continuar sin imprimir el ticket?",
            icon = Icons.Default.PrintDisabled,
            iconColor = ErrorRed,
            iconBgColor = ErrorRedSoft,
            confirmText = "Continuar sin imprimir",
            onConfirm = {

                showPrinterDialog = false

                val clienteIdFinal =
                    if (clienteNulo) null else cliente?.id

                devolucionViewModel.registrarDevolucion(
                    clienteId = clienteIdFinal,
                    clienteNombre = cliente?.nombre,
                    clienteNulo = clienteNulo,
                    motivo = motivo,
                    observacion = observacion,
                    carrito = items,
                    sustitucionesPorProducto = sustitucionesPorProducto,
                    imprimir = false
                )
            },
            onDismiss = {
                showPrinterDialog = false
            }
        )
    }

    if (showResultDialog) {

        CustomStyledDialog(
            title = "Resultado",
            message = mensajeResultado,
            icon =
                if (esExito)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.Warning,
            iconColor =
                if (esExito)
                    AccentTeal
                else
                    ErrorRed,
            iconBgColor =
                if (esExito)
                    Color(0xFFE6F6F2)
                else
                    ErrorRedSoft,
            confirmText = "Aceptar",
            onConfirm = {
                showResultDialog = false

                if (esExito) {
                    onDevolucionExitosa()
                }
            },
            onDismiss = {
                showResultDialog = false

                if (esExito) {
                    onDevolucionExitosa()
                }
            },
            showDismissButton = false
        )
    }

    // ── DIÁLOGO DE CAMBIO DE PRODUCTO ─────────────────────────────────────────

    if (showCambioDialog && itemParaCambio != null) {

        val item = itemParaCambio!!

        CambioProductoDialog(
            item = item,
            viewModel = devolucionViewModel,
            sustitucionesActuales =
                sustitucionesPorProducto[item.productoVariacionId]
                    ?: emptyList(),
            onDismiss = {
                showCambioDialog = false
                itemParaCambio = null
            },
            onConfirm = { sustituciones ->

                sustitucionesPorProducto =
                    sustitucionesPorProducto +
                            (
                                    item.productoVariacionId to
                                            sustituciones
                                    )

                showCambioDialog = false
                itemParaCambio = null
            }
        )
    }

    if (isProcessing) {

        Dialog(
            onDismissRequest = { }
        ) {

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceWhite,
                modifier = Modifier.padding(16.dp)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = AccentBlue
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        "Procesando devolución...",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {

            StandardTopBar(
                title = "Devolución",
                onBackClick = onBack
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── LISTA DE PRODUCTOS ──

            if (items.isEmpty()) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(42.dp),
                                tint = TextMuted.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text =
                                "No hay productos en devolución",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(items) { item ->

                        CarritoItemDevolucionCard(
                            item = item,
                            viewModel = carritoViewModel,
                            mostrarCambio =
                                motivo == "Devolución de cliente",
                            sustituciones =
                                sustitucionesPorProducto[
                                    item.productoVariacionId
                                ] ?: emptyList(),
                            onCambiar = {
                                itemParaCambio = item
                                showCambioDialog = true
                            },
                            onCantidadCambiada = {
                                // Al cambiar la cantidad, el valor de la
                                // sustitución anterior puede dejar de coincidir.
                                sustitucionesPorProducto =
                                    sustitucionesPorProducto -
                                            item.productoVariacionId
                            }
                        )
                    }
                }
            }

            // ── SECCIÓN DE DETALLES ANIMADA ──

            if (items.isNotEmpty()) {

                Surface(
                    color = SurfaceWhite,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp
                    )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {

                        val rotacion by animateFloatAsState(
                            targetValue =
                                if (showDetalles)
                                    0f
                                else
                                    180f
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDetalles =
                                        !showDetalles
                                }
                                .padding(
                                    horizontal = 24.dp,
                                    vertical = 16.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Column {

                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text =
                                            "Detalles de Devolución",
                                        fontWeight =
                                            FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(8.dp)
                                    )

                                    if (motivo.isBlank()) {

                                        Surface(
                                            shape =
                                                RoundedCornerShape(4.dp),
                                            color =
                                                ErrorRedSoft,
                                            contentColor =
                                                ErrorRed
                                        ) {

                                            Text(
                                                text =
                                                    "Obligatorio",
                                                fontSize = 10.sp,
                                                fontWeight =
                                                    FontWeight.Bold,
                                                modifier =
                                                    Modifier.padding(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp
                                                    )
                                            )
                                        }

                                    } else {

                                        Icon(
                                            imageVector =
                                                Icons.Default.CheckCircle,
                                            contentDescription =
                                                "Completado",
                                            tint = AccentTeal,
                                            modifier =
                                                Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text =
                                        "$totalProductos productos",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    modifier =
                                        Modifier.padding(top = 2.dp)
                                )
                            }

                            Icon(
                                imageVector =
                                    Icons.Default.ExpandMore,
                                contentDescription =
                                    "Expandir/Colapsar",
                                tint = TextMuted,
                                modifier =
                                    Modifier.rotate(rotacion)
                            )
                        }

                        AnimatedVisibility(
                            visible = showDetalles,
                            enter =
                                expandVertically(
                                    expandFrom = Alignment.Top
                                ) + fadeIn(),
                            exit =
                                shrinkVertically(
                                    shrinkTowards = Alignment.Top
                                ) + fadeOut()
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 350.dp)
                                    .verticalScroll(
                                        rememberScrollState()
                                    )
                                    .padding(
                                        horizontal = 20.dp
                                    )
                            ) {

                                HorizontalDivider(
                                    color = BackgroundLight
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(16.dp)
                                )

                                // ── 1. MOTIVO ──

                                Text(
                                    text =
                                        "Motivo de devolución *",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    modifier =
                                        Modifier.padding(
                                            start = 4.dp,
                                            bottom = 6.dp
                                        )
                                )

                                var expanded by remember {
                                    mutableStateOf(false)
                                }

                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = {
                                        expanded = !expanded
                                    }
                                ) {

                                    OutlinedTextField(
                                        value = motivo,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = {
                                            Text(
                                                "Selecciona un motivo",
                                                color = TextMuted
                                            )
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults
                                                .TrailingIcon(
                                                    expanded
                                                )
                                        },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape =
                                            RoundedCornerShape(12.dp),
                                        colors =
                                            defaultTextFieldColors()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = {
                                            expanded = false
                                        },
                                        modifier =
                                            Modifier.background(
                                                SurfaceWhite
                                            )
                                    ) {

                                        motivos.forEach { opcion ->

                                            DropdownMenuItem(
                                                text = {
                                                    Text(opcion)
                                                },
                                                onClick = {

                                                    motivo = opcion
                                                    expanded = false

                                                    if (
                                                        opcion !=
                                                        "Devolución de cliente"
                                                    ) {
                                                        sustitucionesPorProducto =
                                                            emptyMap()
                                                    }

                                                    if (
                                                        opcion !=
                                                        "Devolución de cliente"
                                                    ) {

                                                        clienteBuscado = ""
                                                        clienteNulo = false

                                                        clienteViewModel
                                                            .limpiarBusqueda()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(
                                    modifier =
                                        Modifier.height(16.dp)
                                )

                                // ── 2. CLIENTE ──

                                if (
                                    motivo ==
                                    "Devolución de cliente"
                                ) {

                                    Text(
                                        text =
                                            "Buscar cliente",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        modifier =
                                            Modifier.padding(
                                                start = 4.dp,
                                                bottom = 6.dp
                                            )
                                    )

                                    OutlinedTextField(
                                        value =
                                            clienteBuscado,
                                        onValueChange = {
                                            clienteBuscado = it
                                            clienteNulo = false
                                            clienteViewModel
                                                .limpiarBusqueda()
                                        },
                                        placeholder = {
                                            Text(
                                                "Nombre o ID del cliente",
                                                color = TextMuted
                                            )
                                        },
                                        modifier =
                                            Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape =
                                            RoundedCornerShape(12.dp),
                                        colors =
                                            defaultTextFieldColors()
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(8.dp)
                                    )

                                    Button(
                                        onClick = {
                                            clienteViewModel
                                                .buscarCliente(
                                                    clienteBuscado
                                                )
                                        },
                                        enabled =
                                            clienteBuscado.isNotBlank(),
                                        modifier =
                                            Modifier.fillMaxWidth(),
                                        shape =
                                            RoundedCornerShape(12.dp),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor =
                                                    AccentIndigo
                                            )
                                    ) {

                                        Text("Buscar")
                                    }

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically,
                                        modifier =
                                            Modifier.padding(
                                                vertical = 4.dp
                                            )
                                    ) {

                                        Checkbox(
                                            checked =
                                                clienteNulo,
                                            onCheckedChange = {

                                                clienteNulo = it

                                                if (it) {
                                                    clienteBuscado = ""
                                                    clienteViewModel
                                                        .limpiarBusqueda()
                                                }
                                            },
                                            colors =
                                                CheckboxDefaults.colors(
                                                    checkedColor =
                                                        AccentBlue
                                                )
                                        )

                                        Text(
                                            "Cliente nulo (venta rápida)",
                                            color = TextPrimary,
                                            fontSize = 14.sp
                                        )
                                    }

                                    cliente?.let {

                                        ClienteCard(
                                            nombre = it.nombre,
                                            id = it.id,
                                            negocio =
                                                it.nombreNegocio,
                                            seleccionado = true,
                                            onClick = {}
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(8.dp)
                                        )
                                    }

                                    resultados
                                        .filter {
                                            it.id != cliente?.id
                                        }
                                        .forEach { resultado ->

                                            ClienteCard(
                                                nombre =
                                                    resultado.nombre,
                                                id =
                                                    resultado.id,
                                                negocio =
                                                    resultado.nombreNegocio,
                                                seleccionado =
                                                    cliente?.id ==
                                                            resultado.id,
                                                onClick = {

                                                    clienteViewModel
                                                        .seleccionarCliente(
                                                            resultado
                                                        )

                                                    clienteNulo = false
                                                }
                                            )

                                            Spacer(
                                                modifier =
                                                    Modifier.height(8.dp)
                                            )
                                        }

                                    Spacer(
                                        modifier =
                                            Modifier.height(16.dp)
                                    )
                                }

                                // ── 3. OBSERVACIÓN ──

                                Text(
                                    text = "Observación",
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                    modifier =
                                        Modifier.padding(
                                            start = 4.dp,
                                            bottom = 6.dp
                                        )
                                )

                                OutlinedTextField(
                                    value = observacion,
                                    onValueChange = {
                                        observacion = it
                                    },
                                    placeholder = {
                                        Text(
                                            "Agrega detalles adicionales...",
                                            color = TextMuted
                                        )
                                    },
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    shape =
                                        RoundedCornerShape(12.dp),
                                    colors =
                                        defaultTextFieldColors()
                                )

                                if (stockYaCargado && devolucionesSinStockBloqueadas) {
                                    AdvertenciaDevolucion(
                                        mensaje =
                                            "No se puede realizar una devolución normal porque uno o más productos no tienen existencias."
                                    )
                                }

                                if (
                                    stockYaCargado &&
                                    motivo == "Devolución de cliente" &&
                                    cambiosPendientes.isNotEmpty()
                                ) {
                                    AdvertenciaDevolucion(
                                        mensaje =
                                            "Uno o más productos no tienen existencias. Debes seleccionar un cambio por otro producto."
                                    )
                                }

                                if (
                                    stockYaCargado &&
                                    cambiosConValorIncorrecto.isNotEmpty()
                                ) {
                                    AdvertenciaDevolucion(
                                        mensaje =
                                            "El valor de los productos de cambio debe ser exactamente igual al valor de la devolución."
                                    )
                                }

                                Spacer(
                                    modifier =
                                        Modifier.height(24.dp)
                                )
                            }
                        }

                        // ── BOTÓN CONFIRMAR FIJO ──

                        Button(
                            onClick = {

                                if (motivo.isBlank()) {

                                    showDetalles = true

                                    mensajeResultado =
                                        "Debes seleccionar un motivo"

                                    showResultDialog = true

                                } else if (!stockYaCargado) {

                                    showDetalles = true

                                    mensajeResultado =
                                        "Espera un momento mientras se verifica el stock."

                                    showResultDialog = true

                                } else if (hayBloqueoDevolucion) {

                                    showDetalles = true

                                    mensajeResultado =
                                        when {
                                            devolucionesSinStockBloqueadas ->
                                                "No se puede realizar la devolución porque uno o más productos no tienen existencias."

                                            cambiosPendientes.isNotEmpty() ->
                                                "Hay productos sin existencias que requieren seleccionar un cambio."

                                            else ->
                                                "El valor de los productos de cambio debe ser exactamente igual al valor de la devolución."
                                        }

                                    showResultDialog = true

                                } else {

                                    showConfirmDialog = true
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 16.dp
                                )
                                .height(56.dp),
                            shape =
                                RoundedCornerShape(16.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        AccentBlue
                                )
                        ) {

                            Text(
                                "Confirmar Devolución",
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


// ── COMPONENTES UI Y ESTILOS ──────────────────────────────────────────────────

@Composable
fun defaultTextFieldColors() =
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
fun CarritoItemDevolucionCard(
    item: CarritoItem,
    viewModel: CarritoDevolucionViewModel,
    mostrarCambio: Boolean,
    sustituciones: List<SustitucionSeleccionada>,
    onCambiar: () -> Unit,
    onCantidadCambiada: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = SurfaceWhite
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 20.dp
                )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

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
                        text =
                            "$${String.format(
                                "%.2f",
                                item.precio
                            )} c/u",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                // ── CONTROLES DE CANTIDAD ──

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    modifier = Modifier.background(
                        BackgroundLight,
                        RoundedCornerShape(12.dp)
                    )
                ) {

                    IconButton(
                        onClick = {

                            viewModel.actualizarCantidad(
                                item.productoVariacionId,
                                item.cantidad - 1
                            )
                            onCantidadCambiada()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {

                        Icon(
                            Icons.Default.Remove,
                            "Quitar",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = item.cantidad.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AccentBlue,
                        modifier =
                            Modifier.padding(
                                horizontal = 8.dp
                            )
                    )

                    IconButton(
                        onClick = {

                            viewModel.actualizarCantidad(
                                item.productoVariacionId,
                                item.cantidad + 1
                            )
                            onCantidadCambiada()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {

                        Icon(
                            Icons.Default.Add,
                            "Agregar",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (mostrarCambio) {

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Button(
                        onClick = onCambiar,
                        shape =
                            RoundedCornerShape(12.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 10.dp
                            ),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    AccentIndigo
                            )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier =
                                Modifier.size(17.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text(
                            text = "Cambiar",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── SUSTITUCIÓN CONFIGURADA ──

            if (
                mostrarCambio &&
                sustituciones.isNotEmpty()
            ) {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                HorizontalDivider(
                    color = BackgroundLight
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Cambio por:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentIndigo
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                sustituciones.forEach { sustitucion ->

                    Text(
                        text =
                            "${sustitucion.cantidad} × " +
                                    "${sustitucion.nombreProducto} " +
                                    "(${sustitucion.presentacionNombre}) " +
                                    "— $${String.format(
                                        "%.2f",
                                        sustitucion.precio
                                    )}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}


// ── DIÁLOGO DE CAMBIO DE PRODUCTO ─────────────────────────────────────────────

@Composable
fun CambioProductoDialog(
    item: CarritoItem,
    viewModel: DevolucionViewModel,
    sustitucionesActuales: List<SustitucionSeleccionada>,
    onDismiss: () -> Unit,
    onConfirm: (List<SustitucionSeleccionada>) -> Unit
) {
    val variaciones by viewModel
        .getVariacionesDisponiblesParaSustitucion()
        .collectAsState(initial = emptyList())

    val valorDevueltoCentavos =
        remember(item) {
            dineroACentavos(
                item.cantidad * item.precio
            )
        }

    var cantidades by remember(
        item.productoVariacionId,
        sustitucionesActuales
    ) {
        mutableStateOf(
            sustitucionesActuales.associate {
                it.productoVariacionId to it.cantidad
            }
        )
    }

    val valorSeleccionadoCentavos =
        remember(
            cantidades,
            variaciones
        ) {
            cantidades.entries.sumOf { (id, cantidad) ->
                val variacion =
                    variaciones.firstOrNull { it.id == id }

                if (variacion == null) {
                    0
                } else {
                    dineroACentavos(
                        variacion.precio
                    ) * cantidad
                }
            }
        }

    val restanteCentavos =
        valorDevueltoCentavos -
                valorSeleccionadoCentavos

    val candidatosValidos =
        remember(
            variaciones,
            cantidades,
            valorDevueltoCentavos
        ) {
            obtenerVariacionesValidas(
                variaciones = variaciones,
                cantidadesActuales = cantidades,
                objetivoCentavos = valorDevueltoCentavos
            )
        }

    val seleccionados =
        remember(
            cantidades,
            variaciones
        ) {
            cantidades
                .filter { it.value > 0 }
                .mapNotNull { (id, cantidad) ->
                    variaciones
                        .firstOrNull { it.id == id }
                        ?.let { variacion ->
                            SustitucionSeleccionada(
                                productoVariacionId =
                                    variacion.id,
                                nombreProducto =
                                    variacion.nombreProducto,
                                presentacionNombre =
                                    variacion.presentacionNombre,
                                precio =
                                    variacion.precio,
                                cantidad = cantidad
                            )
                        }
                }
        }

    val puedeConfirmar =
        valorSeleccionadoCentavos ==
                valorDevueltoCentavos &&
                seleccionados.isNotEmpty()

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

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        "${item.productoNombre} - " +
                                item.presentacionNombre,
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Valor a cambiar: $" +
                                centavosADinero(
                                    valorDevueltoCentavos
                                ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            }
        },
        text = {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 430.dp)
                        .verticalScroll(
                            rememberScrollState()
                        )
            ) {

                if (variaciones.isEmpty()) {

                    Text(
                        text =
                            "No hay producto que pueda cambiarse",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextMuted,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 24.dp
                                )
                    )

                } else {

                    if (seleccionados.isNotEmpty()) {

                        Text(
                            text = "Cambios seleccionados:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        seleccionados.forEach { seleccion ->

                            val variacion =
                                variaciones.firstOrNull {
                                    it.id ==
                                            seleccion.productoVariacionId
                                }

                            val puedeAumentar =
                                if (variacion == null) {
                                    false
                                } else {
                                    val prueba =
                                        cantidades +
                                                (
                                                        variacion.id to
                                                                (
                                                                        seleccion.cantidad + 1
                                                                        )
                                                        )

                                    puedeCompletarConSeleccionadas(
                                        variaciones =
                                            variaciones,
                                        objetivoCentavos =
                                            valorDevueltoCentavos,
                                        cantidadesActuales =
                                            prueba
                                    )
                                }

                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 3.dp
                                        ),
                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    ),
                                colors =
                                    CardDefaults
                                        .cardColors(
                                            containerColor =
                                                AccentBlueSoft
                                        )
                            ) {

                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = 10.dp,
                                                vertical = 6.dp
                                            ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Column(
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text =
                                                "${seleccion.nombreProducto} " +
                                                        "(${seleccion.presentacionNombre})",
                                            fontSize = 12.sp,
                                            fontWeight =
                                                FontWeight.SemiBold,
                                            color = TextPrimary
                                        )

                                        Text(
                                            text =
                                                "$${String.format(
                                                    "%.2f",
                                                    seleccion.precio
                                                )} c/u",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    IconButton(
                                        onClick = {

                                            val nuevaCantidad =
                                                seleccion.cantidad - 1

                                            cantidades =
                                                if (
                                                    nuevaCantidad <= 0
                                                ) {
                                                    cantidades -
                                                            seleccion.productoVariacionId
                                                } else {
                                                    cantidades +
                                                            (
                                                                    seleccion.productoVariacionId to
                                                                            nuevaCantidad
                                                                    )
                                                }
                                        },
                                        modifier =
                                            Modifier.size(34.dp)
                                    ) {

                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription =
                                                "Quitar",
                                            modifier =
                                                Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text =
                                            seleccion.cantidad.toString(),
                                        fontWeight =
                                            FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AccentBlue,
                                        modifier =
                                            Modifier.padding(
                                                horizontal = 4.dp
                                            )
                                    )

                                    IconButton(
                                        onClick = {

                                            if (
                                                puedeAumentar
                                            ) {
                                                cantidades =
                                                    cantidades +
                                                            (
                                                                    seleccion.productoVariacionId to
                                                                            (
                                                                                    seleccion.cantidad + 1
                                                                                    )
                                                                    )
                                            }
                                        },
                                        enabled =
                                            puedeAumentar,
                                        modifier =
                                            Modifier.size(34.dp)
                                    ) {

                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription =
                                                "Agregar",
                                            modifier =
                                                Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        HorizontalDivider(
                            color = BackgroundLight
                        )

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )
                    }

                    if (restanteCentavos > 0) {

                        Text(
                            text =
                                "Productos que pueden completar el cambio:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        if (candidatosValidos.isEmpty()) {

                            Text(
                                text =
                                    "No hay producto que pueda cambiarse",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                color = TextMuted,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 20.dp
                                        )
                            )

                        } else {

                            candidatosValidos.forEach { variacion ->

                                val cantidadActual =
                                    cantidades[
                                        variacion.id
                                    ] ?: 0

                                val stockMaximo =
                                    variacion.stockActual
                                        .toInt()

                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical = 3.dp
                                            ),
                                    shape =
                                        RoundedCornerShape(
                                            12.dp
                                        ),
                                    colors =
                                        CardDefaults
                                            .cardColors(
                                                containerColor =
                                                    BackgroundLight
                                            )
                                ) {

                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    10.dp
                                                ),
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Column(
                                            modifier =
                                                Modifier.weight(1f)
                                        ) {

                                            Text(
                                                text =
                                                    variacion
                                                        .nombreProducto,
                                                fontWeight =
                                                    FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color =
                                                    TextPrimary
                                            )

                                            Text(
                                                text =
                                                    variacion
                                                        .presentacionNombre,
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )

                                            Text(
                                                text =
                                                    "$${String.format(
                                                        "%.2f",
                                                        variacion.precio
                                                    )} • Stock: " +
                                                            "${variacion.stockActual}",
                                                fontSize = 11.sp,
                                                color = AccentBlue
                                            )
                                        }

                                        Button(
                                            onClick = {

                                                if (
                                                    cantidadActual == 0
                                                ) {
                                                    val prueba =
                                                        cantidades +
                                                                (
                                                                        variacion.id to 1
                                                                        )

                                                    if (
                                                        puedeCompletarConSeleccionadas(
                                                            variaciones =
                                                                variaciones,
                                                            objetivoCentavos =
                                                                valorDevueltoCentavos,
                                                            cantidadesActuales =
                                                                prueba
                                                        ) ||
                                                        candidatosValidos.any {
                                                            it.id ==
                                                                    variacion.id
                                                        }
                                                    ) {
                                                        cantidades =
                                                            cantidades +
                                                                    (
                                                                            variacion.id to 1
                                                                            )
                                                    }
                                                }
                                            },
                                            shape =
                                                RoundedCornerShape(
                                                    10.dp
                                                ),
                                            contentPadding =
                                                PaddingValues(
                                                    horizontal = 10.dp,
                                                    vertical = 4.dp
                                                ),
                                            colors =
                                                ButtonDefaults
                                                    .buttonColors(
                                                        containerColor =
                                                            AccentIndigo
                                                    )
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription =
                                                    null,
                                                modifier =
                                                    Modifier.size(16.dp)
                                            )
                                            Spacer(
                                                modifier =
                                                    Modifier.width(3.dp)
                                            )
                                            Text(
                                                "Agregar",
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text =
                            "Cambio seleccionado: $" +
                                    centavosADinero(
                                        valorSeleccionadoCentavos
                                    ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            if (puedeConfirmar)
                                AccentTeal
                            else
                                TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    if (restanteCentavos > 0) {

                        Text(
                            text =
                                "Faltan $" +
                                        centavosADinero(
                                            restanteCentavos
                                        ),
                            fontSize = 12.sp,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }

                    if (restanteCentavos < 0) {

                        Text(
                            text =
                                "El cambio excede $" +
                                        centavosADinero(
                                            -restanteCentavos
                                        ),
                            fontSize = 12.sp,
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }

                    if (puedeConfirmar) {

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                "El valor del cambio coincide exactamente.",
                            fontSize = 11.sp,
                            color = AccentTeal,
                            textAlign = TextAlign.Center,
                            modifier =
                                Modifier.fillMaxWidth()
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
                onClick = {
                    onConfirm(seleccionados)
                },
                enabled = puedeConfirmar,
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    ),
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    "Aceptar cambio",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}


// ── FUNCIONES PARA COMBINACIONES EXACTAS ──────────────────────────────────────

private fun dineroACentavos(
    valor: Double
): Int {
    return kotlin.math.round(
        valor * 100
    ).toInt()
}

private fun centavosADinero(
    valor: Int
): String {
    return String.format(
        "%.2f",
        valor / 100.0
    )
}

private fun obtenerVariacionesValidas(
    variaciones: List<ProductoSustitucion>,
    cantidadesActuales: Map<Int, Int>,
    objetivoCentavos: Int
): List<ProductoSustitucion> {

    val valorActual =
        cantidadesActuales.entries.sumOf { (id, cantidad) ->
            val variacion =
                variaciones.firstOrNull {
                    it.id == id
                }

            if (variacion == null) {
                0
            } else {
                dineroACentavos(
                    variacion.precio
                ) * cantidad
            }
        }

    val restanteCentavos =
        objetivoCentavos - valorActual

    if (restanteCentavos <= 0) {
        return emptyList()
    }

    return variaciones.filter { candidata ->

        if (
            cantidadesActuales.containsKey(
                candidata.id
            )
        ) {
            return@filter false
        }

        val precioCandidata =
            dineroACentavos(
                candidata.precio
            )

        val stockDisponible =
            candidata.stockActual.toInt()

        if (
            precioCandidata <= 0 ||
            stockDisponible <= 0 ||
            precioCandidata > restanteCentavos
        ) {
            return@filter false
        }

        val cantidadMaxima =
            minOf(
                stockDisponible,
                restanteCentavos /
                        precioCandidata
            )

        for (
        cantidad in
        1..cantidadMaxima
        ) {

            val prueba =
                cantidadesActuales +
                        (
                                candidata.id to
                                        cantidad
                                )

            if (
                puedeCompletarConSeleccionadas(
                    variaciones =
                        variaciones,
                    objetivoCentavos =
                        objetivoCentavos,
                    cantidadesActuales =
                        prueba
                )
            ) {
                return@filter true
            }
        }

        false
    }
}

private fun puedeCompletarConSeleccionadas(
    variaciones: List<ProductoSustitucion>,
    objetivoCentavos: Int,
    cantidadesActuales: Map<Int, Int>
): Boolean {

    val valorSeleccionado =
        cantidadesActuales.entries.sumOf { (id, cantidad) ->
            val variacion =
                variaciones.firstOrNull {
                    it.id == id
                }

            if (variacion == null) {
                Int.MAX_VALUE
            } else {
                dineroACentavos(
                    variacion.precio
                ) * cantidad
            }
        }

    if (valorSeleccionado > objetivoCentavos) {
        return false
    }

    val restantes =
        variaciones.mapNotNull { variacion ->

            val cantidadUsada =
                cantidadesActuales[
                    variacion.id
                ] ?: 0

            val stock =
                variacion.stockActual
                    .toInt()

            if (cantidadUsada > stock) {
                return false
            }

            val stockRestante =
                stock - cantidadUsada

            if (stockRestante <= 0) {
                null
            } else {
                ProductoSustitucion(
                    id = variacion.id,
                    productoId = variacion.productoId,
                    nombreProducto =
                        variacion.nombreProducto,
                    presentacionNombre =
                        variacion.presentacionNombre,
                    precio = variacion.precio,
                    stockActual =
                        stockRestante.toDouble()
                )
            }
        }

    val restanteCentavos =
        objetivoCentavos -
                valorSeleccionado

    if (restanteCentavos == 0) {
        return true
    }

    return puedeFormarValorExacto(
        variaciones = restantes,
        objetivo = restanteCentavos
    )
}

private fun puedeFormarValorExacto(
    variaciones: List<ProductoSustitucion>,
    objetivo: Int
): Boolean {

    if (objetivo == 0) {
        return true
    }

    if (objetivo < 0) {
        return false
    }

    val alcanzable =
        IntArray(
            objetivo + 1
        ) {
            -1
        }

    alcanzable[0] = 0

    variaciones.forEach { variacion ->

        val precio =
            dineroACentavos(
                variacion.precio
            )

        val stock =
            variacion.stockActual
                .toInt()

        if (
            precio <= 0 ||
            stock <= 0 ||
            precio > objetivo
        ) {
            return@forEach
        }

        for (
        suma in
        0..objetivo
        ) {

            if (
                alcanzable[suma] >= 0
            ) {
                alcanzable[suma] =
                    stock
            } else if (
                suma < precio ||
                alcanzable[
                    suma - precio
                ] <= 0
            ) {
                alcanzable[suma] =
                    -1
            } else {
                alcanzable[suma] =
                    alcanzable[
                        suma - precio
                    ] - 1
            }
        }
    }

    return alcanzable[objetivo] >= 0
}


@Composable
private fun AdvertenciaDevolucion(
    mensaje: String
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 12.dp
                ),
        shape =
            RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    ErrorRedSoft
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Default.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier =
                    Modifier.size(20.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Text(
                text = mensaje,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextPrimary
            )
        }
    }
}


// ── DIÁLOGO ESTILIZADO ────────────────────────────────────────────────────────

@Composable
fun CustomStyledDialog(
    title: String,
    message: String,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDismissButton: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier =
                        Modifier.size(26.dp)
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
        dismissButton =
            if (showDismissButton) {

                {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape =
                            RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    contentColor =
                                        TextPrimary
                                ),
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            "Cancelar",
                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }
                }

            } else {
                null
            },
        confirmButton = {

            Button(
                onClick = onConfirm,
                shape =
                    RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            iconColor,
                        contentColor =
                            Color.White
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    confirmText,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }
    )
}