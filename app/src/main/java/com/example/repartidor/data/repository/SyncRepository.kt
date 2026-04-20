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

    suspend fun sincronizarTodo(lastSync: String?) {
        val updatedAfter =
            if (AppConfig.FORCE_SYNC) null else lastSync//Se hizo este cambio por si acaso en pruebas
        println("FORCE_SYNC: ${AppConfig.FORCE_SYNC}")
        println("lastSync: $lastSync")
        println("updatedAfter enviado: $updatedAfter")

        // 🔹 ROLES
        val rolesResponse = RetrofitClient.api.getRoles(updatedAfter)
        if (rolesResponse.isSuccessful) {

            val roles = rolesResponse.body() ?: emptyList()

            val rolesEntity = roles.map {
                RolEntity(
                    id = it.id,
                    nombre = it.nombre,
                    descripcion = it.descripcion,
                    estado = it.estado,
                    updated_at = it.updated_at
                )
            }

            db.rolDao().insertAll(rolesEntity)
            println("ROLES OK")
        }

        // 🔹 USUARIOS
        val usuariosResponse = RetrofitClient.api.getUsuarios(updatedAfter)
        if (usuariosResponse.isSuccessful) {

            val usuarios = usuariosResponse.body() ?: emptyList()

            val usuariosEntity = usuarios.map {
                UsuarioEntity(
                    id = it.id,
                    username = it.username,
                    firstName = it.first_name,
                    lastName = it.last_name,
                    email = it.email,
                    telefono = it.telefono,
                    direccion = it.direccion,
                    foto = it.foto,
                    rolId = it.rol,
                    updatedAt = it.updated_at
                )
            }
            db.usuarioDao().insertAll(usuariosEntity)
            println("USUARIOS OK")
        }
        //vehiculos
        val vehiculosResponse = RetrofitClient.api.getVehiculos(updatedAfter)
        if (vehiculosResponse.isSuccessful) {

            val vehiculos = vehiculosResponse.body() ?: emptyList()

            val vehiculosEntity = vehiculos.map {
                VehiculoEntity(
                    id = it.id,
                    marca = it.marca,
                    color = it.color,
                    placa = it.placa,
                    kilometraje = it.kilometraje,
                    ultimoServicio = it.ultimo_servicio,
                    observaciones = it.observaciones,
                    imagen = it.imagen,
                    estado = it.estado,
                    updatedAt = it.updated_at
                )
            }

            db.vehiculoDao().insertAll(vehiculosEntity)
            println("VEHICULOS OK")
        }

        //RUTAS
        val rutasResponse = RetrofitClient.api.getRutas(updatedAfter)
        if (rutasResponse.isSuccessful) {

            val rutas = rutasResponse.body() ?: emptyList()

            val rutasEntity = rutas.map {
                RutaEntity(
                    id = it.id,
                    nombre = it.nombre,
                    descripcion = it.descripcion,
                    usuarioId = it.usuario,
                    vehiculoId = it.vehiculo,
                    estado = it.estado,
                    updatedAt = it.updated_at
                )
            }

            db.rutaDao().insertAll(rutasEntity)
            println("RUTAS OK")
        }

        // 🔹 CLIENTES
        val clientesResponse = RetrofitClient.api.getClientes(updatedAfter)
        if (clientesResponse.isSuccessful) {

            val clientes = clientesResponse.body() ?: emptyList()

            val clientesEntity = clientes.map {
                ClienteEntity(
                    id = it.id,
                    nombre = it.nombre,
                    nombreNegocio = it.nombre_negocio,
                    giro = it.giro,
                    tipoExhibidor = it.tipo_exhibidor,
                    direccion = it.direccion,
                    localidad = it.localidad,
                    colonia = it.colonia,
                    telefono = it.telefono,
                    credito = it.credito,
                    imagen = it.imagen,
                    observaciones = it.observaciones,
                    rutaId = it.ruta,
                    estado = it.estado,
                    updatedAt = it.updated_at
                )
            }

            db.clienteDao().insertAll(clientesEntity)
            println("CLIENTES OK")
        }

// 🔹 CLIENTE DIAS VISITA
        val diasResponse = RetrofitClient.api.getClienteDiasVisita(updatedAfter)
        if (diasResponse.isSuccessful) {

            val dias = diasResponse.body() ?: emptyList()

            val diasEntity = dias.map {
                ClienteDiasVisitaEntity(
                    id = it.id,
                    clienteId = it.cliente,
                    diaSemana = it.dia_semana,
                    updatedAt = it.updated_at
                )
            }

            db.clienteDiasVisitaDao().insertAll(diasEntity)
            println("CLIENTE DIAS OK")
        }

        // 🔹 CATEGORIAS
        val catResponse = RetrofitClient.api.getCategorias(updatedAfter)
        if (catResponse.isSuccessful) {

            val categorias = catResponse.body() ?: emptyList()

            val categoriasEntity = categorias.map {
                CategoriaProductoEntity(
                    id = it.id,
                    nombre = it.nombre,
                    descripcion = it.descripcion,
                    imagen = it.imagen,
                    estado = it.estado,
                    updatedAt = it.updated_at
                )
            }

            db.categoriaDao().insertAll(categoriasEntity)
            println("CATEGORIAS OK")
        }

        // 🔹 PRESENTACIONES
        val presResponse = RetrofitClient.api.getPresentaciones(updatedAfter)
        if (presResponse.isSuccessful) {

            val presentaciones = presResponse.body() ?: emptyList()

            val presEntity = presentaciones.map {
                PresentacionProductoTerminadoEntity(
                    id = it.id,
                    nombre = it.nombre,
                    descripcion = it.descripcion,
                    imagen = it.imagen,
                    estado = it.estado,
                    updatedAt = it.updated_at
                )
            }

            db.presentacionDao().insertAll(presEntity)
            println("PRESENTACIONES OK")
        }

        // 🔹 PRODUCTOS
        val prodResponse = RetrofitClient.api.getProductos(updatedAfter)
        if (prodResponse.isSuccessful) {

            val productos = prodResponse.body() ?: emptyList()

            val prodEntity = productos.map {
                ProductoTerminadoEntity(
                    id = it.id,
                    nombre = it.nombre,
                    categoriaId = it.categoria_producto,
                    imagen = it.imagen,
                    estado = it.estado,
                    updatedAt = it.updated_at
                )
            }

            db.productoDao().insertAll(prodEntity)
            println("PRODUCTOS OK")
        }

        // 🔹 VARIACIONES
        val varResponse = RetrofitClient.api.getVariaciones(updatedAfter)
        if (varResponse.isSuccessful) {

            val variaciones = varResponse.body() ?: emptyList()

            val varEntity = variaciones.map {
                ProductoVariacionEntity(
                    id = it.id,
                    producto = it.producto,
                    presentacion = it.presentacion,
                    costo = it.costo,
                    precio = it.precio,
                    stock = it.stock,
                    stock_min = it.stock_min,
                    codigo_barras = it.codigo_barras,
                    updatedAt = it.updated_at
                )
            }

            db.variacionDao().insertAll(varEntity)
            println("VARIACIONES OK")
        }

        // Mini bodega
        val miniResponse = RetrofitClient.api.getMiniBodegas(updatedAfter)
        if (miniResponse.isSuccessful) {

            val miniBodegas = miniResponse.body() ?: emptyList()

            val miniEntity = miniBodegas.map {
                MiniBodegaEntity(
                    id = it.id,
                    rutaId = it.ruta,
                    fecha = it.fecha,
                    usuarioId = it.usuario,
                    vehiculoId = it.vehiculo,
                    estado = it.estado,
                    updatedAt = it.updated_at
                )
            }

            db.miniBodegaDao().insertAll(miniEntity)
            println("MINI BODEGAS OK")
        }

        // 🔹 MINI BODEGA DETALLE
        val detalleResponse = RetrofitClient.api.getMiniBodegaDetalles(updatedAfter)
        if (detalleResponse.isSuccessful) {

            val detalles = detalleResponse.body() ?: emptyList()

            val detalleEntity = detalles.map {
                MiniBodegaDetalleEntity(
                    id = it.id,
                    miniBodegaId = it.mini_bodega,
                    productoVariacionId = it.producto_variacion,
                    cantidadInicial = it.cantidad_inicial,
                    cantidadActual = it.cantidad_actual,
                    updatedAt = it.updated_at
                )
            }

            db.miniBodegaDetalleDao().insertAll(detalleEntity)
            println("MINI BODEGA DETALLE OK")
        }

        // 🔹 PEDIDOS
        val pedidosResponse = RetrofitClient.api.getPedidosReabastecimiento(updatedAfter)
        if (pedidosResponse.isSuccessful) {

            val pedidos = pedidosResponse.body() ?: emptyList()

            val pedidosEntity = pedidos.map {
                PedidoReabastecimientoEntity(
                    id = it.id,
                    rutaId = it.ruta,
                    fecha = it.fecha,
                    estado = it.estado,
                    updatedAt = it.updated_at,
                    usuario = it.usuario
                )
            }

            db.pedidoReabastecimientoDao().insertAll(pedidosEntity)
            println("PEDIDOS OK")
        }

        val pedidodetalleResponse =
            RetrofitClient.api.getPedidosReabastecimientoDetalle(updatedAfter)
        if (pedidodetalleResponse.isSuccessful) {

            val detalles = pedidodetalleResponse.body() ?: emptyList()

            val detalleEntity = detalles.map {
                PedidoReabastecimientoDetalleEntity(
                    id = it.id,
                    pedidoId = it.pedido,
                    productoVariacionId = it.producto_variacion,
                    cantidad = it.cantidad,
                    updatedAt = it.updated_at
                )
            }

            db.pedidoReabastecimientoDetalleDao().insertAll(detalleEntity)
            println("PEDIDOS DETALLE OK")
        }


    }
}