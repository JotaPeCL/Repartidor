package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.InventarioItem

class InventarioRepository(private val db: AppDatabase) {

    suspend fun obtenerInventario(username: String): List<InventarioItem> {

        // 1. Usuario
        val usuario = db.usuarioDao().getByUsername(username)
            ?: return emptyList()

        // 2. Ruta
        val ruta = db.rutaDao().getByUsuarioId(usuario.id)
            ?: return emptyList()

        // 3. MiniBodega (última)
        val miniBodega = db.miniBodegaDao().getUltimaPorRuta(ruta.id)
            ?: return emptyList()

        // 4. Detalles
        val detalles = db.miniBodegaDetalleDao()
            .getByMiniBodega(miniBodega.id)

        // 5. Armar lista
        return detalles.mapNotNull { detalle ->

            val variacion = db.variacionDao()
                .getById(detalle.productoVariacionId)
                ?: return@mapNotNull null

            val producto = db.productoDao()
                .getById(variacion.producto)
                ?: return@mapNotNull null

            val presentacion = db.presentacionDao()
                .getById(variacion.presentacion)
                ?: return@mapNotNull null


            InventarioItem(
                productoNombre = producto.nombre,
                presentacion = presentacion.nombre, // o como lo tengas
                cantidadActual = detalle.cantidadActual,
                cantidadInicial = detalle.cantidadInicial
            )
        }.sortedWith(
            compareBy(
                { it.productoNombre },
                { it.presentacion }
            )
        )
    }
}