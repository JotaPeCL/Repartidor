package com.example.repartidor.viewmodel.Inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.data.model.dclass.ProductoConStock
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.data.repository.ReabastecimientoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class ReabastecimientoViewModel(
    private val repository: ReabastecimientoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ProductoTerminadoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoTerminadoEntity>> = _productos

    private val miniBodegaIdFlow = MutableStateFlow<Int?>(null)

    init {

        // 🔹 Obtener miniBodegaId
        viewModelScope.launch {
            miniBodegaIdFlow.value = sessionManager.getMiniBodegaId()
        }

        // 🔹 Escuchar productos
        viewModelScope.launch {
            repository.getProductos().collect {
                _productos.value = it
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getVariaciones(productoId: Int): Flow<List<ProductoConStock>> {
        return miniBodegaIdFlow.flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                repository.getVariaciones(productoId, id)
            }
        }
    }
}