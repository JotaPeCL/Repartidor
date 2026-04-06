package com.example.repartidor.data.repository

import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.model.CategoriaProductoEntity
import com.example.repartidor.data.model.ClienteDiasVisitaEntity
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.PresentacionProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoVariacionEntity
import com.example.repartidor.data.model.RolEntity
import com.example.repartidor.data.model.RutaEntity
import com.example.repartidor.data.model.UsuarioEntity
import com.example.repartidor.data.model.VehiculoEntity
import com.example.repartidor.data.remote.RetrofitClient

class SyncRepository(
    private val db: AppDatabase
) {

    suspend fun sincronizarTodo() {

        // 🔹 ROLES
        val rolesResponse = RetrofitClient.api.getRoles()
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
        val usuariosResponse = RetrofitClient.api.getUsuarios()
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
        val vehiculosResponse = RetrofitClient.api.getVehiculos()
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
        val rutasResponse = RetrofitClient.api.getRutas()
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
        val clientesResponse = RetrofitClient.api.getClientes()
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
        val diasResponse = RetrofitClient.api.getClienteDiasVisita()
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
        val catResponse = RetrofitClient.api.getCategorias()
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
        val presResponse = RetrofitClient.api.getPresentaciones()
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
        val prodResponse = RetrofitClient.api.getProductos()
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
        val varResponse = RetrofitClient.api.getVariaciones()
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


    }
}