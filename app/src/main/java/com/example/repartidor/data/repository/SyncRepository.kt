package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.entity.CategoriaProductoEntity
import com.example.repartidor.data.model.entity.ClienteDiasVisitaEntity
import com.example.repartidor.data.model.entity.ClienteEntity
import com.example.repartidor.data.model.entity.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.entity.MiniBodegaEntity
import com.example.repartidor.data.model.entity.PedidoReabastecimientoDetalleEntity
import com.example.repartidor.data.model.entity.PedidoReabastecimientoEntity
import com.example.repartidor.data.model.entity.PresentacionProductoTerminadoEntity
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.data.model.entity.ProductoVariacionEntity
import com.example.repartidor.data.model.entity.RolEntity
import com.example.repartidor.data.model.entity.RutaEntity
import com.example.repartidor.data.model.entity.UsuarioEntity
import com.example.repartidor.data.model.entity.VehiculoEntity
import com.example.repartidor.data.remote.RetrofitClient
import com.example.repartidor.utils.AppConfig

class SyncRepository(
    private val db: AppDatabase
) {

    private fun verificarRespuesta(
        respuesta: retrofit2.Response<*>,
        nombre: String
    ) {
        if (!respuesta.isSuccessful) {
            throw Exception(
                "HTTP_${respuesta.code()}: Error al sincronizar $nombre"
            )
        }
    }

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

        verificarRespuesta(rolesResponse, "roles")

        val roles = rolesResponse.body() ?: emptyList()

        val rolesEntity = roles.map {
            RolEntity(
                it.id,
                it.nombre,
                it.descripcion,
                it.estado,
                it.updated_at
            )
        }

        syncTable(
            rolesEntity,
            { it.id },
            { db.rolDao().getAllIds() },
            { db.rolDao().deleteByIds(it) },
            { db.rolDao().insertAll(it) }
        )

        println("ROLES OK")


        // 🔹 USUARIOS
        val usuariosResponse = RetrofitClient.api.getUsuarios(updatedAfter)

        verificarRespuesta(usuariosResponse, "usuarios")

        val usuarios = usuariosResponse.body() ?: emptyList()

        val usuariosEntity = usuarios.map {
            UsuarioEntity(
                it.id,
                it.username,
                it.first_name,
                it.last_name,
                it.email,
                it.telefono,
                it.direccion,
                it.foto,
                it.rol,
                it.updated_at
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


        // 🔹 VEHICULOS
        val vehiculosResponse = RetrofitClient.api.getVehiculos(updatedAfter)

        verificarRespuesta(vehiculosResponse, "vehículos")

        val vehiculos = vehiculosResponse.body() ?: emptyList()

        val vehiculosEntity = vehiculos.map {
            VehiculoEntity(
                it.id,
                it.marca,
                it.color,
                it.placa,
                it.kilometraje,
                it.ultimo_servicio,
                it.observaciones,
                it.imagen,
                it.estado,
                it.updated_at
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


        // 🔹 RUTAS
        val rutasResponse = RetrofitClient.api.getRutas(updatedAfter)

        verificarRespuesta(rutasResponse, "rutas")

        val rutas = rutasResponse.body() ?: emptyList()

        val rutasEntity = rutas.map {
            RutaEntity(
                it.id,
                it.nombre,
                it.descripcion,
                it.usuario,
                it.vehiculo,
                it.estado,
                it.updated_at
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


        // 🔹 CLIENTES
        val clientesResponse = RetrofitClient.api.getClientes(updatedAfter)

        verificarRespuesta(clientesResponse, "clientes")

        val clientes = clientesResponse.body() ?: emptyList()

        val clientesEntity = clientes.map {
            ClienteEntity(
                it.id,
                it.nombre,
                it.nombre_negocio,
                it.giro,
                it.tipo_exhibidor,
                it.direccion,
                it.localidad,
                it.colonia,
                it.telefono,
                it.porcentaje_descuento,
                it.limite_credito,
                it.saldo_adeudo,
                it.imagen,
                it.observaciones,
                it.ruta,
                it.estado,
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


        // 🔹 CLIENTE DIAS VISITA
        val diasResponse = RetrofitClient.api.getClienteDiasVisita(updatedAfter)

        verificarRespuesta(diasResponse, "cliente-dias")

        val dias = diasResponse.body() ?: emptyList()

        val diasEntity = dias.map {
            ClienteDiasVisitaEntity(
                it.id,
                it.cliente,
                it.dia_semana,
                it.updated_at
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


        // 🔹 CATEGORIAS
        val categoriasResponse = RetrofitClient.api.getCategorias(updatedAfter)

        verificarRespuesta(categoriasResponse, "categorías")

        val categorias = categoriasResponse.body() ?: emptyList()

        val categoriasEntity = categorias.map {
            CategoriaProductoEntity(
                it.id,
                it.nombre,
                it.descripcion,
                it.imagen,
                it.estado,
                it.updated_at
            )
        }

        syncTable(
            categoriasEntity,
            { it.id },
            { db.categoriaDao().getAllIds() },
            { db.categoriaDao().deleteByIds(it) },
            { db.categoriaDao().insertAll(it) }
        )

        println("CATEGORIAS OK")


        // 🔹 PRESENTACIONES
        val presentacionesResponse = RetrofitClient.api.getPresentaciones(updatedAfter)

        verificarRespuesta(presentacionesResponse, "presentaciones")

        val presentaciones = presentacionesResponse.body() ?: emptyList()

        val presentacionesEntity = presentaciones.map {
            PresentacionProductoTerminadoEntity(
                it.id,
                it.nombre,
                it.descripcion,
                it.imagen,
                it.estado,
                it.updated_at
            )
        }

        syncTable(
            presentacionesEntity,
            { it.id },
            { db.presentacionDao().getAllIds() },
            { db.presentacionDao().deleteByIds(it) },
            { db.presentacionDao().insertAll(it) }
        )

        println("PRESENTACIONES OK")


        // 🔹 PRODUCTOS
        val productosResponse = RetrofitClient.api.getProductos(updatedAfter)

        verificarRespuesta(productosResponse, "productos")

        val productos = productosResponse.body() ?: emptyList()

        val productosEntity = productos.map {
            ProductoTerminadoEntity(
                it.id,
                it.nombre,
                it.categoria_producto,
                it.imagen,
                it.estado,
                it.updated_at
            )
        }

        syncTable(
            productosEntity,
            { it.id },
            { db.productoDao().getAllIds() },
            { db.productoDao().deleteByIds(it) },
            { db.productoDao().insertAll(it) }
        )

        println("PRODUCTOS OK")


        // 🔹 VARIACIONES
        val variacionesResponse = RetrofitClient.api.getVariaciones(updatedAfter)

        verificarRespuesta(variacionesResponse, "variaciones")

        val variaciones = variacionesResponse.body() ?: emptyList()

        val variacionesEntity = variaciones.map {
            ProductoVariacionEntity(
                it.id,
                it.producto,
                it.presentacion,
                it.costo,
                it.precio,
                it.stock,
                it.stock_min,
                it.codigo_barras,
                it.updated_at
            )
        }

        syncTable(
            variacionesEntity,
            { it.id },
            { db.variacionDao().getAllIds() },
            { db.variacionDao().deleteByIds(it) },
            { db.variacionDao().insertAll(it) }
        )

        println("VARIACIONES OK")


        // 🔹 MINI BODEGA
        val miniResponse = RetrofitClient.api.getMiniBodegas(updatedAfter)

        verificarRespuesta(miniResponse, "mini-bodegas")

        val miniBodegas = miniResponse.body() ?: emptyList()

        val miniEntity = miniBodegas.map {
            MiniBodegaEntity(
                it.id,
                it.ruta,
                it.fecha,
                it.usuario,
                it.vehiculo,
                it.estado,
                it.updated_at
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


        // 🔹 MINI BODEGA DETALLE
        val detalleResponse =
            RetrofitClient.api.getMiniBodegaDetalles(updatedAfter)

        verificarRespuesta(detalleResponse, "mini-bodega-detalles")

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


        // 🔹 LIMPIEZA DE DATOS SINCRONIZADOS

        db.mermaDao().deleteSincronizados()

        println("MERMAS SINCRONIZADAS ELIMINADAS")


        val limite =
            System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        // 🔥 VENTAS
        val ventasIds =
            db.ventaDao().getVentasParaEliminar(limite)

        if (ventasIds.isNotEmpty()) {
            db.ventaDetalleDao().deleteDetallesByVentaIds(ventasIds)
            db.abonoDao().deleteAbonosByVentaIds(ventasIds)
            db.ventaDao().deleteVentasByIds(ventasIds)
        }


        // 🔥 DEVOLUCIONES
        val devolucionesIds =
            db.devolucionDao().getDevolucionesParaEliminar()

        if (devolucionesIds.isNotEmpty()) {
            db.devolucionDetalleDao()
                .deleteDetallesDevolucionByIds(devolucionesIds)

            db.devolucionDao()
                .deleteDevolucionesByIds(devolucionesIds)
        }

        println("LIMPIEZA DE DATOS ANTIGUOS COMPLETADA")


        /*
        // 🔹 PEDIDOS
        val pedidosResponse =
            RetrofitClient.api.getPedidosReabastecimiento(updatedAfter)

        verificarRespuesta(pedidosResponse, "pedidos")

        val pedidos = pedidosResponse.body() ?: emptyList()

        val pedidosEntity = pedidos.map {
            PedidoReabastecimientoEntity(
                it.id,
                it.ruta,
                it.fecha,
                it.estado,
                it.updated_at,
                it.usuario
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


        // 🔹 PEDIDOS DETALLE
        val detallePedidoResponse =
            RetrofitClient.api.getPedidosReabastecimientoDetalle(updatedAfter)

        verificarRespuesta(
            detallePedidoResponse,
            "pedidos-detalle"
        )

        val detallesPedido =
            detallePedidoResponse.body() ?: emptyList()

        val detallePedidoEntity = detallesPedido.map {
            PedidoReabastecimientoDetalleEntity(
                it.id,
                it.pedido,
                it.producto_variacion,
                it.cantidad,
                it.updated_at
            )
        }

        syncTable(
            detallePedidoEntity,
            { it.id },
            { db.pedidoReabastecimientoDetalleDao().getAllIds() },
            { db.pedidoReabastecimientoDetalleDao().deleteByIds(it) },
            { db.pedidoReabastecimientoDetalleDao().insertAll(it) }
        )

        println("PEDIDOS DETALLE OK")
        */
    }
}