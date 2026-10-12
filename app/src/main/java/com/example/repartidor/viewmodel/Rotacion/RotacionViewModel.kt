package com.example.repartidor.viewmodel.Rotacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.ProductoConStock
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.dclass.SustitucionRotacionSeleccionada
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.data.model.entity.RotacionEntity
import com.example.repartidor.data.repository.RotacionInventarioRepository
import com.example.repartidor.data.repository.RotacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RotacionViewModel(
    private val repository: RotacionRepository,
    private val inventarioRepository: RotacionInventarioRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    private val _productos =
        MutableStateFlow<List<ProductoTerminadoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoTerminadoEntity>> = _productos

    private val _variaciones =
        MutableStateFlow<List<ProductoConStock>>(emptyList())
    val variaciones: StateFlow<List<ProductoConStock>> = _variaciones

    private val _variacionesSustitucion =
        MutableStateFlow<List<ProductoSustitucion>>(emptyList())

    val variacionesSustitucion: StateFlow<List<ProductoSustitucion>> =
        _variacionesSustitucion

    fun cargarProductos() {
        viewModelScope.launch {

            val miniBodegaId =
                sessionManager.getMiniBodegaId()
                    ?: return@launch

            repository.getProductos(miniBodegaId)
                .collect {
                    _productos.value = it
                }
        }
    }

    fun cargarVariaciones(productoId: Int) {
        viewModelScope.launch {

            val miniBodegaId =
                sessionManager.getMiniBodegaId()
                    ?: return@launch

            repository.getVariaciones(
                productoId = productoId,
                miniBodegaId = miniBodegaId
            ).collect {
                _variaciones.value = it
            }
        }
    }

    fun cargarVariacionesParaSustitucion() {
        viewModelScope.launch {
            val miniBodegaId =
                sessionManager.getMiniBodegaId()
                    ?: return@launch

            repository.getVariacionesParaSustitucion(
                miniBodegaId = miniBodegaId
            ).collect {
                _variacionesSustitucion.value = it
            }
        }
    }

    fun registrarRotacion(
        clienteId: Int?,
        carrito: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionRotacionSeleccionada>>,
        descripcion: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = false

            try {
                if (carrito.isEmpty()) {
                    throw Exception("Debes seleccionar al menos un producto para rotar")
                }

                val usuarioId = sessionManager.getUserId()
                    ?: throw Exception("No se encontró el usuario de la sesión")

                val miniBodegaId = sessionManager.getMiniBodegaId()
                    ?: throw Exception("No se encontró la MiniBodega de la sesión")

                val fecha = System.currentTimeMillis().toString()

                val rotacion = RotacionEntity(
                    uuid = UUID.randomUUID().toString(),
                    clienteId = clienteId,
                    usuarioId = usuarioId,
                    miniBodegaId = miniBodegaId,
                    fecha = fecha,
                    descripcion = descripcion,
                    sincronizado = false,
                    createdAt = fecha
                )

                inventarioRepository.registrarRotacion(
                    rotacion = rotacion,
                    carrito = carrito,
                    sustitucionesPorProducto = sustitucionesPorProducto
                )

                _success.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al registrar la rotación"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarEstado() {
        _error.value = null
        _success.value = false
    }
}