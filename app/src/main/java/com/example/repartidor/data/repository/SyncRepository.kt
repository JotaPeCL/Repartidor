package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.CategoriaProductoEntity
import com.example.repartidor.data.model.ClienteDiasVisitaEntity
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.MiniBodegaEntity
import com.example.repartidor.data.model.PedidoReabastecimientoDetalleEntity
import com.example.repartidor.data.model.PedidoReabastecimientoEntity
import com.example.repartidor.data.model.PresentacionProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoVariacionEntity
import com.example.repartidor.data.model.RolEntity
import com.example.repartidor.data.model.RutaEntity
import com.example.repartidor.data.model.UsuarioEntity
import com.example.repartidor.data.model.VehiculoEntity
import com.example.repartidor.data.remote.RetrofitClient
import com.example.repartidor.utils.AppConfig

class SyncRepository(
    private val db: AppDatabase
) {

    private suspend fun <T> syncTable(
        dataServidor: List<T>,
        getId: (T) -> Int,
        getAllIds: suspend () -> List<Int>,
        deleteByIds: suspend (List<Int>) -> Unit,
        insertAll: suspend (List<T>) -> Unit
    ) {
        val idsServidor = dataServidor.map { getId(it) }.toSet()
        val idsLocales = getAllIds()

        val idsAEliminar = idsLocales.filter { it !in idsServidor }

        if (idsAEliminar.isNotEmpty()) {
            deleteByIds(idsAEliminar)
        }

        insertAll(dataServidor)
    }

    suspend fun sincronizarTodo(lastSync: String?) {

        val updatedAfter = null
        // val updatedAfter = if (AppConfig.FORCE_SYNC) null else lastSync

        println("FORCE_SYNC: ${AppConfig.FORCE_SYNC}")
        println("lastSync: $lastSync")
        println("updatedAfter enviado: $updatedAfter")

        // 🔹 ROLES
        val rolesResponse = RetrofitClient.api.getRoles(updatedAfter)
        if (rolesResponse.isSuccessful) {
            val roles = rolesResponse.body() ?: emptyList()

            val rolesEntity = roles.map {
                RolEntity(it.id, it.nombre, it.descripcion, it.estado, it.updated_at)
            }

            syncTable(
                rolesEntity,
                { it.id },
                { db.rolDao().getAllIds() },
                { db.rolDao().deleteByIds(it) },
                { db.rolDao().insertAll(it) }
            )
            println("ROLES OK")
        }

        // 🔹 USUARIOS
        val usuariosResponse = RetrofitClient.api.getUsuarios(updatedAfter)
        if (usuariosResponse.isSuccessful) {
            val usuarios = usuariosResponse.body() ?: emptyList()

            val usuariosEntity = usuarios.map {
                UsuarioEntity(
                    it.id, it.username, it.first_name, it.last_name,
                    it.email, it.telefono, it.direccion, it.foto,
                    it.rol, it.updated_at
                )
            }

            syncTable(
                usuariosEntity,
                { it.id },
                { db.usuarioDao().getAllIds() },
                { db.usuarioDao().deleteByIds(it) },
                { db.usuarioDao().insertAll(it) }
            )
            println("USUARIOS OK")
        }

        // 🔹 VEHICULOS
        val vehiculosResponse = RetrofitClient.api.getVehiculos(updatedAfter)
        if (vehiculosResponse.isSuccessful) {
            val vehiculos = vehiculosResponse.body() ?: emptyList()

            val vehiculosEntity = vehiculos.map {
                VehiculoEntity(
                    it.id, it.marca, it.color, it.placa,
                    it.kilometraje, it.ultimo_servicio,
                    it.observaciones, it.imagen,
                    it.estado, it.updated_at
                )
            }

            syncTable(
                vehiculosEntity,
                { it.id },
                { db.vehiculoDao().getAllIds() },
                { db.vehiculoDao().deleteByIds(it) },
                { db.vehiculoDao().insertAll(it) }
            )
            println("VEHICULOS OK")
        }

        // 🔹 RUTAS
        val rutasResponse = RetrofitClient.api.getRutas(updatedAfter)
        if (rutasResponse.isSuccessful) {
            val rutas = rutasResponse.body() ?: emptyList()

            val rutasEntity = rutas.map {
                RutaEntity(
                    it.id, it.nombre, it.descripcion,
                    it.usuario, it.vehiculo,
                    it.estado, it.updated_at
                )
            }

            syncTable(
                rutasEntity,
                { it.id },
                { db.rutaDao().getAllIds() },
                { db.rutaDao().deleteByIds(it) },
                { db.rutaDao().insertAll(it) }
            )
            println("RUTAS OK")
        }

        // 🔹 CLIENTES
        val clientesResponse = RetrofitClient.api.getClientes(updatedAfter)
        if (clientesResponse.isSuccessful) {
            val clientes = clientesResponse.body() ?: emptyList()

            val clientesEntity = clientes.map {
                ClienteEntity(
                    it.id, it.nombre, it.nombre_negocio,
                    it.giro, it.tipo_exhibidor,
                    it.direccion, it.localidad,
                    it.colonia, it.telefono,
                    it.porcentaje_descuento, it.credito,
                    it.imagen, it.observaciones,
                    it.ruta, it.estado,
                    it.updated_at
                )
            }

            syncTable(
                clientesEntity,
                { it.id },
                { db.clienteDao().getAllIds() },
                { db.clienteDao().deleteByIds(it) },
                { db.clienteDao().insertAll(it) }
            )
            println("CLIENTES OK")
        }

        // 🔹 CLIENTE DIAS VISITA
        val diasResponse = RetrofitClient.api.getClienteDiasVisita(updatedAfter)
        if (diasResponse.isSuccessful) {
            val dias = diasResponse.body() ?: emptyList()

            val diasEntity = dias.map {
                ClienteDiasVisitaEntity(
                    it.id, it.cliente, it.dia_semana, it.updated_at
                )
            }

            syncTable(
                diasEntity,
                { it.id },
                { db.clienteDiasVisitaDao().getAllIds() },
                { db.clienteDiasVisitaDao().deleteByIds(it) },
                { db.clienteDiasVisitaDao().insertAll(it) }
            )
            println("CLIENTE DIAS OK")
        }

        // 🔹 MINI BODEGA
        val miniResponse = RetrofitClient.api.getMiniBodegas(updatedAfter)
        if (miniResponse.isSuccessful) {
            val miniBodegas = miniResponse.body() ?: emptyList()

            val miniEntity = miniBodegas.map {
                MiniBodegaEntity(
                    it.id, it.ruta, it.fecha,
                    it.usuario, it.vehiculo,
                    it.estado, it.updated_at
                )
            }

            syncTable(
                miniEntity,
                { it.id },
                { db.miniBodegaDao().getAllIds() },
                { db.miniBodegaDao().deleteByIds(it) },
                { db.miniBodegaDao().insertAll(it) }
            )
            println("MINI BODEGAS OK")
        }

        // 🔹 MINI BODEGA DETALLE 🔥
        val detalleResponse = RetrofitClient.api.getMiniBodegaDetalles(updatedAfter)
        if (detalleResponse.isSuccessful) {
            val detalles = detalleResponse.body() ?: emptyList()

            val detalleEntity = detalles.map {
                MiniBodegaDetalleEntity(
                    it.id,
                    it.mini_bodega,
                    it.producto_variacion,
                    it.cantidad_inicial,
                    it.cantidad_actual,
                    it.updated_at
                )
            }

            syncTable(
                detalleEntity,
                { it.id },
                { db.miniBodegaDetalleDao().getAllIds() },
                { db.miniBodegaDetalleDao().deleteByIds(it) },
                { db.miniBodegaDetalleDao().insertAll(it) }
            )
            println("MINI BODEGA DETALLE OK")
        }

        // 🔹 PEDIDOS
        val pedidosResponse = RetrofitClient.api.getPedidosReabastecimiento(updatedAfter)
        if (pedidosResponse.isSuccessful) {
            val pedidos = pedidosResponse.body() ?: emptyList()

            val pedidosEntity = pedidos.map {
                PedidoReabastecimientoEntity(
                    it.id, it.ruta, it.fecha,
                    it.estado, it.updated_at, it.usuario
                )
            }

            syncTable(
                pedidosEntity,
                { it.id },
                { db.pedidoReabastecimientoDao().getAllIds() },
                { db.pedidoReabastecimientoDao().deleteByIds(it) },
                { db.pedidoReabastecimientoDao().insertAll(it) }
            )
            println("PEDIDOS OK")
        }

        // 🔹 PEDIDOS DETALLE
        val detallePedidoResponse = RetrofitClient.api.getPedidosReabastecimientoDetalle(updatedAfter)
        if (detallePedidoResponse.isSuccessful) {
            val detalles = detallePedidoResponse.body() ?: emptyList()

            val detalleEntity = detalles.map {
                PedidoReabastecimientoDetalleEntity(
                    it.id, it.pedido,
                    it.producto_variacion,
                    it.cantidad,
                    it.updated_at
                )
            }

            syncTable(
                detalleEntity,
                { it.id },
                { db.pedidoReabastecimientoDetalleDao().getAllIds() },
                { db.pedidoReabastecimientoDetalleDao().deleteByIds(it) },
                { db.pedidoReabastecimientoDetalleDao().insertAll(it) }
            )
            println("PEDIDOS DETALLE OK")
        }
    }
}