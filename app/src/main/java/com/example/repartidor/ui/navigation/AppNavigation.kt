package com.example.repartidor.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.repository.ClienteRepository
import com.example.repartidor.data.repository.HomeRepository
import com.example.repartidor.data.repository.InventarioRepository
import com.example.repartidor.data.repository.MiniBodegaRepository
import com.example.repartidor.data.repository.SyncRepository
import com.example.repartidor.data.repository.UsuarioRepository
import com.example.repartidor.data.repository.VentaRepository

import com.example.repartidor.ui.screens.Cliente.ClienteScreen
import com.example.repartidor.ui.screens.Cliente.QrScannerScreen
import com.example.repartidor.ui.screens.Home.HomeScreen
import com.example.repartidor.ui.screens.Inventario.InventarioSreen
import com.example.repartidor.ui.screens.Venta.CarritosScreen
import com.example.repartidor.ui.screens.Venta.VentaScreen
import com.example.repartidor.ui.screens.login.LoginScreen
import com.example.repartidor.ui.screens.login.SyncScreen
import com.example.repartidor.utils.AppConfig
import com.example.repartidor.viewmodel.CarritoViewModel
import com.example.repartidor.viewmodel.ClienteViewModel
import com.example.repartidor.viewmodel.HomeViewModel
import com.example.repartidor.viewmodel.InventarioViewModel
import com.example.repartidor.viewmodel.LoginViewModel
import com.example.repartidor.viewmodel.SyncViewModel
import com.example.repartidor.viewmodel.VentaViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // DB
    val db = remember {
        androidx.room.Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app-db"
        )
            //.fallbackToDestructiveMigration(true)// quitar esto despues, esto es para pruebas
            .build()
    }

    // ViewModels
    val repository = remember {
        SyncRepository(db)
    }
    val syncViewModel = remember {
        SyncViewModel(repository, sessionManager)
    }
    val usuarioRepository = remember { UsuarioRepository(db) }
    val miniBodegaRepository = remember { MiniBodegaRepository(db) }

    val loginViewModel = remember { LoginViewModel(usuarioRepository,miniBodegaRepository, sessionManager) }

    val homeRepository = remember { HomeRepository(db) }
    val homeViewModel = remember { HomeViewModel(homeRepository) }

    val inventarioRepository = remember { InventarioRepository(db) }
    val inventarioViewModel = remember { InventarioViewModel(inventarioRepository) }

    val clienteRepository = remember { ClienteRepository(db.clienteDao()) }

    val clienteViewModel = remember {
        ClienteViewModel(clienteRepository)
    }
    val ventaRepository = remember {
        VentaRepository(
            db.productoDao(),
            db.miniBodegaDetalleDao()
        )
    }

    val ventaViewModel = remember {
        VentaViewModel(ventaRepository, sessionManager)
    }
    val carritoViewModel = remember { CarritoViewModel() }


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
        //startDestination = Routes.Sync.route
        startDestination = startDestination
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
                sessionManager = sessionManager
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
                }
            )
        }

        composable(Routes.QrScanner.route) {
            QrScannerScreen(
                onQrDetectado = { clienteId ->
                    clienteViewModel.buscarCliente(clienteId) // 🔥 reutilizas lógica
                    navController.popBackStack() // 🔥 regresas a ClienteScreen
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
                carritoViewModel=carritoViewModel
            )
        }

        composable(Routes.Carrito.route) {
            CarritosScreen(
                carritoViewModel = carritoViewModel,
                onVolver = {
                    navController.popBackStack() // regresa a la pantalla anterior (VentaScreen)
                }
            )

        }

        composable(Routes.Inventario.route) {
            InventarioSreen(
                viewModel = inventarioViewModel,
                sessionManager = sessionManager
            )
        }


    }


}

@RequiresApi(Build.VERSION_CODES.O)
fun yaSincronizoHoy(lastSync: String?): Boolean {

    if (AppConfig.FORCE_SYNC) return false // 🔥 en pruebas SIEMPRE entra a Sync

    if (lastSync == null) return false

    val now = LocalDateTime.now()
    val reset = now.toLocalDate().atTime(7, 0)

    val effectiveToday = if (now.isBefore(reset)) {
        now.toLocalDate().minusDays(1)
    } else {
        now.toLocalDate()
    }

    val last = LocalDate.parse(lastSync)

    return last == effectiveToday
}

