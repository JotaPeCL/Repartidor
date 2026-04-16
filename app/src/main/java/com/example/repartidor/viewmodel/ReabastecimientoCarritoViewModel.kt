package com.example.repartidor.viewmodel

import androidx.lifecycle.ViewModel
import com.example.repartidor.data.model.ReabastecimientoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReabastecimientoCarritoViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<ReabastecimientoItem>>(emptyList())
    val items: StateFlow<List<ReabastecimientoItem>> = _items

    // 🔹 Agregar producto
    fun agregarItem(nuevo: ReabastecimientoItem) {

        val listaActual = _items.value.toMutableList()

        val index = listaActual.indexOfFirst {
            it.productoVariacionId == nuevo.productoVariacionId
        }

        if (index >= 0) {
            // 🔥 Ya existe → sumar cantidad
            val existente = listaActual[index]
            listaActual[index] = existente.copy(
                cantidad = existente.cantidad + nuevo.cantidad
            )
        } else {
            // 🔥 Nuevo item
            listaActual.add(nuevo)
        }

        _items.value = listaActual
    }

    // 🔹 Actualizar cantidad
    fun actualizarCantidad(productoVariacionId: Int, nuevaCantidad: Int) {

        val listaActual = _items.value.toMutableList()

        val index = listaActual.indexOfFirst {
            it.productoVariacionId == productoVariacionId
        }

        if (index >= 0) {
            if (nuevaCantidad <= 0) {
                // 🔥 eliminar si llega a 0
                listaActual.removeAt(index)
            } else {
                listaActual[index] = listaActual[index].copy(
                    cantidad = nuevaCantidad
                )
            }
        }

        _items.value = listaActual
    }

    // 🔹 Eliminar item directo
    fun eliminarItem(productoVariacionId: Int) {
        _items.value = _items.value.filter {
            it.productoVariacionId != productoVariacionId
        }
    }

    // 🔹 Limpiar carrito
    fun limpiar() {
        _items.value = emptyList()
    }
}