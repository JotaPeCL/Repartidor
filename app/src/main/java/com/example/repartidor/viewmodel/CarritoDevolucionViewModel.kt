package com.example.repartidor.viewmodel

import androidx.lifecycle.ViewModel
import com.example.repartidor.data.model.CarritoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class CarritoDevolucionViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<CarritoItem>>(emptyList())
    val items: StateFlow<List<CarritoItem>> = _items

    fun agregarProductos(nuevos: List<CarritoItem>) {
        val actual = _items.value.toMutableList()

        nuevos.forEach { nuevo ->
            val index = actual.indexOfFirst {
                it.productoVariacionId == nuevo.productoVariacionId
            }

            if (index >= 0) {
                val existente = actual[index]
                actual[index] = existente.copy(
                    cantidad = existente.cantidad + nuevo.cantidad
                )
            } else {
                actual.add(nuevo)
            }
        }

        _items.value = actual
    }

    fun actualizarCantidad(id: Int, nuevaCantidad: Int) {
        _items.value = _items.value.map {
            if (it.productoVariacionId == id) {
                it.copy(cantidad = nuevaCantidad)
            } else it
        }.filter { it.cantidad > 0 }
    }

    fun eliminar(id: Int) {
        _items.value = _items.value.filter {
            it.productoVariacionId != id
        }
    }

    fun limpiar() {
        _items.value = emptyList()
    }
}