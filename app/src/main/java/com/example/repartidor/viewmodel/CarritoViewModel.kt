package com.example.repartidor.viewmodel

import androidx.lifecycle.ViewModel
import com.example.repartidor.data.model.CarritoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CarritoViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<CarritoItem>>(emptyList())
    val items: StateFlow<List<CarritoItem>> = _items

    // 🔹 Agregar productos (desde dialog)
    fun agregarProductos(nuevos: List<CarritoItem>) {
        val actual = _items.value.toMutableList()

        nuevos.forEach { nuevo ->
            val index = actual.indexOfFirst {
                it.productoVariacionId == nuevo.productoVariacionId
            }

            if (index >= 0) {
                // 🔥 ya existe → sumar cantidad
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

    // 🔹 actualizar cantidad (+ / -)
    fun actualizarCantidad(id: Int, nuevaCantidad: Int) {
        _items.value = _items.value.map {
            if (it.productoVariacionId == id) {
                it.copy(cantidad = nuevaCantidad)
            } else it
        }.filter { it.cantidad > 0 } // 🔥 elimina si llega a 0
    }

    // 🔹 eliminar producto
    fun eliminar(id: Int) {
        _items.value = _items.value.filter {
            it.productoVariacionId != id
        }
    }

    // 🔹 limpiar carrito
    fun limpiar() {
        _items.value = emptyList()
    }

    // 🔹 total
    fun total(): Double {
        return _items.value.sumOf { it.precio * it.cantidad }
    }
}