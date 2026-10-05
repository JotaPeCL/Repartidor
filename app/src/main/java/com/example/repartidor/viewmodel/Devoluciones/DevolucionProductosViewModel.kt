package com.example.repartidor.viewmodel.Devoluciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.ProductoConStock
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.data.repository.DevolucionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class DevolucionProductosViewModel(
    private val repository: DevolucionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ProductoTerminadoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoTerminadoEntity>> = _productos

    private var job: Job? = null
    private var miniBodegaId: Int? = null

    fun cargarProductos() {
        job?.cancel()

        job = viewModelScope.launch {

            val id = sessionManager.getMiniBodegaId()

            if (id == null) {
                _productos.value = emptyList()
                return@launch
            }

            miniBodegaId = id

            repository.getProductosParaDevolucion(id).collect { lista ->
                _productos.value = lista.toList()
            }
        }
    }

    fun getVariaciones(productoId: Int): Flow<List<ProductoConStock>> {
        val id = miniBodegaId ?: return flowOf(emptyList())
        return repository.getVariaciones(productoId, id)
    }

    fun getVariacionesParaDevolucion(
        productoId: Int
    ): Flow<List<ProductoConStock>> {
        val id = miniBodegaId ?: return flowOf(emptyList())

        return repository.getVariacionesParaDevolucion(
            productoId,
            id
        )
    }


    fun getVariacionesDisponiblesParaSustitucion(): Flow<List<ProductoSustitucion>> {
        val id = miniBodegaId ?: return flowOf(emptyList())
        return repository.getVariacionesDisponiblesParaSustitucion(id)
    }
}