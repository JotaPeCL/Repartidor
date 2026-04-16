package com.example.repartidor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.ProductoConStock
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.data.repository.ReabastecimientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ReabastecimientoViewModel(
    private val repository: ReabastecimientoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ProductoTerminadoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoTerminadoEntity>> = _productos

    private var miniBodegaId: Int? = null

    init {
        viewModelScope.launch {

            miniBodegaId = sessionManager.getMiniBodegaId()

            repository.getProductos().collect {
                _productos.value = it
            }
        }
    }

    fun getVariaciones(productoId: Int): Flow<List<ProductoConStock>> {

        val id = miniBodegaId ?: return flowOf(emptyList())

        return repository.getVariaciones(productoId, id)
    }
}
