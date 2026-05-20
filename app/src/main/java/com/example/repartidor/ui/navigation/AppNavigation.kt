package com.example.repartidor.ui.navigation

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.local.DatabaseProvider
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.repository.ClienteRepository
import com.example.repartidor.data.repository.ClienteViewModelFactory
import com.example.repartidor.data.repository.DevolucionInventarioRepository
import com.example.repartidor.data.repository.DevolucionRepository
import com.example.repartidor.data.repository.HomeRepository
import com.example.repartidor.data.repository.HomeViewModelFactory
import com.example.repartidor.data.repository.InventarioRepository
import com.example.repartidor.data.repository.InventarioViewModelFactory
import com.example.repartidor.data.repository.LoginViewModelFactory
import com.example.repartidor.data.repository.MiniBodegaRepository
import com.example.repartidor.data.repository.MiniBodegaRepository2
import com.example.repartidor.data.repository.PrinterRepository
import com.example.repartidor.data.repository.ReabastecimientoRepository
import com.example.repartidor.data.repository.SyncRepository
import com.example.repartidor.data.repository.UsuarioRepository
import com.example.repartidor.data.repository.VentaLocalRepository
import com.example.repartidor.data.repository.VentaRepository
import com.example.repartidor.data.repository.VentasDiaRepository
import com.example.repartidor.data.repository.VentasDiaViewModelFactory
import com.example.repartidor.ui.screens.Cliente.ClienteScreen
import com.example.repartidor.ui.screens.Cliente.QrScannerScreen
import com.example.repartidor.ui.screens.Devoluciones.DevolucionFormScreen
import com.example.repartidor.ui.screens.Devoluciones.DevolucionesScreen
import com.example.repartidor.ui.screens.Home.HomeScreen
import com.example.repartidor.ui.screens.Impresora.BluetoothScreen
import com.example.repartidor.ui.screens.Inventario.InventarioScreen
import com.example.repartidor.ui.screens.Inventario.PedidoReabastecimientoScreen
import com.example.repartidor.ui.screens.Inventario.ReabastecimientoScreen
import com.example.repartidor.ui.screens.Venta.CarritosScreen
import com.example.repartidor.ui.screens.Venta.VentaScreen
import com.example.repartidor.ui.screens.VentasDia.VentaDiaScreen
import com.example.repartidor.ui.screens.login.LoginScreen
import com.example.repartidor.ui.screens.login.SyncScreen
import com.example.repartidor.utils.AppConfig
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.viewmodel.CarritoDevolucionViewModel
import com.example.repartidor.viewmodel.CarritoViewModel
import com.example.repartidor.viewmodel.CierreMiniBodegaViewModel
import com.example.repartidor.viewmodel.ClienteViewModel
import com.example.repartidor.viewmodel.DevolucionProductosViewModel
import com.example.repartidor.viewmodel.DevolucionViewModel
import com.example.repartidor.viewmodel.HomeViewModel
import com.example.repartidor.viewmodel.InventarioViewModel
import com.example.repartidor.viewmodel.LoginViewModel
import com.example.repartidor.viewmodel.ReabastecimientoCarritoViewModel
import com.example.repartidor.viewmodel.ReabastecimientoProcesoViewModel
import com.example.repartidor.viewmodel.ReabastecimientoViewModel
import com.example.repartidor.viewmodel.SyncViewModel
import com.example.repartidor.viewmodel.VentaProcesoViewModel
import com.example.repartidor.viewmodel.VentaViewModel
import com.example.repartidor.viewmodel.VentasDiaViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import com.example.repartidor.data.repository.AbonoRepository
import com.example.repartidor.data.repository.AbonosFormRepository
import com.example.repartidor.data.repository.AbonosRepository
import com.example.repartidor.data.repository.ResumenDiaRepository
import com.example.repartidor.ui.screens.Abonos.AbonosFormScreen
import com.example.repartidor.ui.screens.Abonos.AbonosScreen
import com.example.repartidor.ui.screens.Resumen.ResumenDiaScreen
import com.example.repartidor.viewmodel.AbonoViewModel
import com.example.repartidor.viewmodel.AbonosFormViewModel
import com.example.repartidor.viewmodel.AbonosViewModel
import com.example.repartidor.viewmodel.ResumenDiaViewModel

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // DB
    val db = DatabaseProvider.getDatabase(context)

    // ViewModels
    val repository = remember {
        SyncRepository(db)
    }
    val syncViewModel = remember {
        SyncViewModel(repository, sessionManager)
    }
    val usuarioRepository = remember { UsuarioRepository(db) }
    val miniBodegaRepository = remember { MiniBodegaRepository(db) }

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            usuarioRepository,
            miniBodegaRepository,
            sessionManager
        )
    )

    val homeRepository = remember { HomeRepository(db) }
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(homeRepository)
    )

    val inventarioRepository = remember { InventarioRepository(db) }
    val inventarioViewModel: InventarioViewModel = viewModel(
        factory = InventarioViewModelFactory(inventarioRepository)
    )

    val clienteRepository = remember { ClienteRepository(db.clienteDao()) }

    val clienteViewModel: ClienteViewModel = viewModel(
        factory = ClienteViewModelFactory(clienteRepository)
    )
    val ventaRepository = remember {
        VentaRepository(
            db.productoDao(),
            db.miniBodegaDetalleDao(),
            db.clienteDao()
        )
    }

    val ventaViewModel = remember {
        VentaViewModel(ventaRepository, sessionManager)
    }
    val carritoViewModel = remember { CarritoViewModel() }

    val ventaLocalRepository = remember {
        VentaLocalRepository(
            db.ventaDao(), // 🔥 importante
            db.clienteDao()
        )
    }

    val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val bluetoothAdapter = bluetoothManager.adapter

    val printerRepository = remember { PrinterRepository(context) }

    val printerManager = remember { PrinterManager(bluetoothAdapter) }

    val ventaProcesoViewModel = remember {
        VentaProcesoViewModel(
            ventaLocalRepository,
            sessionManager,
            printerRepository,
            printerManager,
            bluetoothAdapter
        )
    }

    val reabastecimientoRepository = remember {
        ReabastecimientoRepository(
            db.productoDao(),
            db.miniBodegaDetalleDao()
        )
    }

    val reabastecimientoViewModel = remember {
        ReabastecimientoViewModel(
            reabastecimientoRepository,
            sessionManager
        )
    }

    val reabastecimientoCarritoViewModel = remember {
        ReabastecimientoCarritoViewModel()
    }

    val reabastecimientoProcesoViewModel = remember {
        ReabastecimientoProcesoViewModel(
            sessionManager,
            homeRepository
        )
    }
    val miniBodegaRepository2 = remember {
        MiniBodegaRepository2(
            db.miniBodegaDao(),
            db.miniBodegaDetalleDao(),
            sessionManager
        )
    }
    val cierreMiniBodegaViewModel = remember {
        CierreMiniBodegaViewModel(miniBodegaRepository2)
    }
    val ventasDiaRepository = remember {
        VentasDiaRepository(db.ventaDao())
    }
    val ventasDiaViewModel: VentasDiaViewModel = viewModel(
        factory = VentasDiaViewModelFactory(
            repository = ventasDiaRepository,
            printerRepository = printerRepository,
            printerManager = printerManager,
            bluetoothAdapter = bluetoothAdapter,
            sessionManager=sessionManager
        )
    )
    val devolucionRepository = remember {
        DevolucionRepository(db.miniBodegaDetalleDao())
    }

    val devolucionViewModel = remember {
        DevolucionProductosViewModel(devolucionRepository, sessionManager)
    }

    val carritoDevolucionViewModel = remember {
        CarritoDevolucionViewModel()
    }
    val devolucionFormRepository = remember {
        DevolucionInventarioRepository(
            db.devolucionDao(),
            db.miniBodegaDetalleDao(),
            db.mermaDao()
        )
    }

    val devolucionFormViewModel = remember {
        DevolucionViewModel(
            devolucionFormRepository,
            sessionManager,
            printerRepository,
            printerManager,
            bluetoothAdapter
        )
    }

    val abonosRepository = remember {
        AbonosRepository(db.ventaDao())
    }

    val abonosViewModel = remember {
        AbonosViewModel(abonosRepository)
    }

    val abonosFormRepository = remember {
        AbonosFormRepository(
            db.ventaDao()
        )
    }

    val abonosFormViewModel = remember {
        AbonosFormViewModel(abonosFormRepository)
    }

    val abonoRepository = remember {
        AbonoRepository(db)
    }

    val abonoViewModel = remember {
        AbonoViewModel(
            abonoRepository,
            printerRepository,
            printerManager,
            bluetoothAdapter
        )
    }

    val resumenDiaRepository = remember {
        ResumenDiaRepository(
            db.ventaDao(),
            db.abonoDao(),
            db.ventaDetalleDao(),
            db.devolucionDetalleDao()
        )
    }

    val resumenDiaViewModel = remember {
        ResumenDiaViewModel(
            resumenDiaRepository,
            sessionManager,
            inventarioRepository
        )
    }

    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    //estados
    val lastSync by sessionManager.lastSyncFlow.collectAsState(initial = null)
    val userSession by sessionManager.userFlow.collectAsState(initial = null)
    val miniBodegaId by sessionManager.miniBodegaFlow.collectAsState(initial = null)

    LaunchedEffect(userSession) {
        println("USER SESSION CAMBIÓ: $userSession")
        println("ultima sincronizacion: $lastSync")
        println("MINI BODEGA ID: $miniBodegaId")
    }
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            println("📍 Pantalla actual: ${destination.route}")
        }
    }

    val startDestination = remember(lastSync, userSession) {
        when {
            !yaSincronizoHoy(lastSync) -> Routes.Sync.route
            userSession.isNullOrEmpty() -> Routes.Login.route
            else -> Routes.Home.route
        }
    }
    LaunchedEffect(lastSync, userSession) {
        when {
            !yaSincronizoHoy(lastSync) -> {
                navController.navigate(Routes.Sync.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            userSession.isNullOrEmpty() -> {
                navController.navigate(Routes.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            else -> {
                navController.navigate(Routes.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,

        // 🚀 AQUÍ SE ELIMINAN LAS ANIMACIONES Y SE HACE INSTANTÁNEO
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }

    ) {

        composable(Routes.Sync.route) {
            SyncScreen(
                viewModel = syncViewModel,
                onSyncCompleto = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Sync.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onCerrarSesion = {
                    coroutineScope.launch {
                        sessionManager.clearSession()
                        loginViewModel.logout()
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true } // limpia toda la pila
                            launchSingleTop = true
                        }
                    }
                },
                onIrClientes = {
                    navController.navigate(Routes.Cliente.route)
                },
                onIrVenta = {
                    navController.navigate(Routes.Venta.route)
                },
                onIrInventarios = {
                    navController.navigate(Routes.Inventario.route)
                },
                viewModel = homeViewModel,
                sessionManager = sessionManager,
                onIrBluetooth = {
                    navController.navigate(Routes.Bluetooth.route)
                },
                onIrVentasDia = {
                    navController.navigate(Routes.VentasDia.route)
                },
                onIrDevoluciones = {
                    navController.navigate(Routes.Devolucion.route)
                },
                onIrAbonos = {
                    navController.navigate(Routes.Abonos.route)
                },
                onIrResumenDia = {
                    navController.navigate(Routes.ResumenDia.route)
                }
            )
        }

        composable(Routes.Cliente.route) {
            ClienteScreen(
                viewModel = clienteViewModel,
                onClienteSeleccionado = { clienteId ->
                    navController.navigate("venta?clienteId=$clienteId")
                },
                onIrQrScanner = {
                    navController.navigate(Routes.QrScanner.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.QrScanner.route) {
            QrScannerScreen(
                onQrDetectado = { clienteId ->
                    clienteViewModel.buscarCliente(clienteId.toString())
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack() // cierra el escáner si se arrepiente
                }
            )
        }

        composable(
            route = Routes.Venta.route,
            arguments = listOf(
                navArgument("clienteId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->

            val clienteId = backStackEntry.arguments?.getInt("clienteId")

            VentaScreen(
                clienteId = if (clienteId == -1) null else clienteId,
                onIrCarrito = {
                    navController.navigate(Routes.Carrito.route)
                },
                viewModel = ventaViewModel,
                carritoViewModel=carritoViewModel,
                ventaProcesoViewModel = ventaProcesoViewModel,
                onBack = {
                    navController.popBackStack() //
                }
            )
        }

        composable(Routes.Carrito.route) {
            CarritosScreen(
                carritoViewModel = carritoViewModel,
                ventaProcesoViewModel = ventaProcesoViewModel,
                onVolver = {
                    navController.popBackStack() // regresa a la pantalla anterior (VentaScreen)
                },
                onVentaExitosa = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.Inventario.route) {
            InventarioScreen(
                viewModel = inventarioViewModel,
                sessionManager = sessionManager,
                onIrReabastecimiento = {
                    navController.navigate(Routes.Reabastecimiento.route)
                },
                onBack = {
                    navController.popBackStack() // 🔥 clave
                }
            )
        }

        composable(Routes.Reabastecimiento.route) {
            ReabastecimientoScreen(
                onIrPedido = {
                    navController.navigate(Routes.PedidoReabastecimiento.route)
                },
                onBack = {
                    navController.popBackStack()
                },
                viewModel = reabastecimientoViewModel,
                carritoViewModel = reabastecimientoCarritoViewModel
            )
        }

        composable(Routes.PedidoReabastecimiento.route) {
            PedidoReabastecimientoScreen(
                carritoViewModel = reabastecimientoCarritoViewModel,
                onVolver = {
                    navController.popBackStack()
                },
                reabastecimientoProcesoViewModel = reabastecimientoProcesoViewModel,
                cierreMiniBodegaViewModel = cierreMiniBodegaViewModel,
                onPedidoCompleto = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.Bluetooth.route) {
            BluetoothScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.VentasDia.route) {
            VentaDiaScreen(
                onBack = {
                    navController.popBackStack()
                },
                viewModel = ventasDiaViewModel
            )
        }

        composable(Routes.Devolucion.route) {
            DevolucionesScreen(
                onIrCarrito = {
                    navController.navigate(Routes.CarritoDevolucion.route)
                },
                viewModel = devolucionViewModel,
                carritoViewModel = carritoDevolucionViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CarritoDevolucion.route) {
            DevolucionFormScreen(
                carritoViewModel = carritoDevolucionViewModel,
                clienteViewModel = clienteViewModel,
                devolucionViewModel = devolucionFormViewModel,
                onBack = { navController.popBackStack() },
                onDevolucionExitosa = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Abonos.route) {
            AbonosScreen(
                viewModel = abonosViewModel,
                onIrForm = { ventaId ->
                    navController.navigate(Routes.AbonoForm.createRoute(ventaId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.AbonoForm.route,
            arguments = listOf(
                navArgument("ventaId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val ventaId = backStackEntry.arguments?.getInt("ventaId") ?: 0

            AbonosFormScreen(
                ventaId = ventaId,
                viewModel = abonosFormViewModel,
                abonoViewModel = abonoViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.ResumenDia.route) {
            ResumenDiaScreen(
                viewModel = resumenDiaViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onGoInventario = {
                    navController.navigate(Routes.Inventario.route)
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun yaSincronizoHoy(lastSync: String?): Boolean {

    if (AppConfig.FORCE_SYNC) return false // en pruebas SIEMPRE entra a Sync

    if (lastSync == null) return false

    val now = LocalDateTime.now()
    val reset = now.toLocalDate().atTime(5, 0)

    val effectiveToday = if (now.isBefore(reset)) {
        now.toLocalDate().minusDays(1)
    } else {
        now.toLocalDate()
    }

    val last = LocalDate.parse(lastSync)

    return last == effectiveToday
}