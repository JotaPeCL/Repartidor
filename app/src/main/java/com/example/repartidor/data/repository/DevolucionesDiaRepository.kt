package com.example.repartidor.data.repository

import com.example.repartidor.data.local.ClienteDao
import com.example.repartidor.data.local.DevolucionDao
import com.example.repartidor.data.local.DevolucionDetalleDao
import com.example.repartidor.data.local.DevolucionSustitucionDao
import com.example.repartidor.data.local.PresentacionProductoTerminadoDao
import com.example.repartidor.data.local.ProductoVariacionDao
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.DetalleDevolucionUI
import com.example.repartidor.data.model.dclass.DevolucionUI
import com.example.repartidor.data.model.dclass.SustitucionDevolucionUI
import com.example.repartidor.data.model.dclass.SustitucionSeleccionada
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DevolucionesDiaRepository(
    private val devolucionDao: DevolucionDao,
    private val devolucionDetalleDao: DevolucionDetalleDao,
    private val devolucionSustitucionDao: DevolucionSustitucionDao,
    private val clienteDao: ClienteDao,
    private val productoVariacionDao: ProductoVariacionDao,
    private val presentacionDao: PresentacionProductoTerminadoDao
) {

    suspend fun getDevolucionesDelDia(
        usuarioId: Int
    ): List<DevolucionUI> {

        val inicio = getInicioDelDia()
        val fin = getFinDelDia()

        val devoluciones = devolucionDao.getDevolucionesDelDia(
            inicio = inicio,
            fin = fin,
            usuarioId = usuarioId
        )

        return devoluciones.map { devolucion ->

            val cliente = devolucion.clienteId?.let {
                clienteDao.getClienteById(it)
            }

            val detalles = devolucionDetalleDao.getByDevolucion(
                devolucion.id
            )

            val totalDevuelto = detalles.sumOf {
                it.cantidad * it.precioUnitario
            }

            DevolucionUI(
                id = devolucion.id,
                clienteId = devolucion.clienteId,
                nombreCliente = cliente?.nombre ?: "Sin cliente",
                nombreNegocio = cliente?.nombreNegocio,
                tipo = devolucion.tipo,
                fecha = parseFecha(devolucion.fecha),
                total = totalDevuelto,
                descripcion = devolucion.descripcion
            )
        }
    }

    suspend fun getDetalleDevolucion(
        devolucionId: Int
    ): Pair<List<DetalleDevolucionUI>, List<SustitucionDevolucionUI>> {

        val detalles = devolucionDetalleDao.getByDevolucion(
            devolucionId
        )

        val sustituciones =
            devolucionSustitucionDao.obtenerPorDevolucion(
                devolucionId
            )

        val detallesUI = detalles.map { detalle ->

            val variacion =
                productoVariacionDao.getById(
                    detalle.productoVariacionId
                )

            val presentacion = variacion?.presentacion?.let {
                presentacionDao.getById(it)
            }

            val nombreCompleto = buildString {
                append(detalle.nombreProducto)

                presentacion?.nombre?.let {
                    append(" ")
                    append(it)
                }
            }

            DetalleDevolucionUI(
                nombreCompleto = nombreCompleto,
                cantidad = detalle.cantidad,
                precioUnitario = detalle.precioUnitario,
                subtotal = detalle.cantidad * detalle.precioUnitario
            )
        }

        val sustitucionesUI = sustituciones.map { sustitucion ->

            val variacion =
                productoVariacionDao.getById(
                    sustitucion.productoVariacionId
                )

            val presentacion = variacion?.presentacion?.let {
                presentacionDao.getById(it)
            }

            val nombreCompleto = buildString {
                append(sustitucion.nombreProducto)

                presentacion?.nombre?.let {
                    append(" ")
                    append(it)
                }
            }

            SustitucionDevolucionUI(
                nombreCompleto = nombreCompleto,
                cantidad = sustitucion.cantidad,
                precioUnitario = sustitucion.precioUnitario,
                subtotal =
                    sustitucion.cantidad *
                            sustitucion.precioUnitario
            )
        }

        return Pair(
            detallesUI,
            sustitucionesUI
        )
    }

    suspend fun getDatosParaImpresion(
        devolucionId: Int
    ): Pair<
            List<CarritoItem>,
            Map<Int, List<SustitucionSeleccionada>>
            > {

        val detalles =
            devolucionDetalleDao.getByDevolucion(devolucionId)

        val sustituciones =
            devolucionSustitucionDao.obtenerPorDevolucion(
                devolucionId
            )

        val items = detalles.map { detalle ->

            val variacion =
                productoVariacionDao.getById(
                    detalle.productoVariacionId
                )

            val presentacion =
                variacion?.presentacion?.let {
                    presentacionDao.getById(it)
                }

            CarritoItem(
                productoVariacionId =
                    detalle.productoVariacionId,
                productoNombre =
                    detalle.nombreProducto,
                presentacionNombre =
                    presentacion?.nombre ?: "",
                precio =
                    detalle.precioUnitario,
                cantidad =
                    detalle.cantidad.toInt()
            )
        }

        val sustitucionesPorProducto =
            mutableMapOf<Int, MutableList<SustitucionSeleccionada>>()

        sustituciones.forEach { sustitucion ->

            val detalle =
                detalles.firstOrNull {
                    it.id == sustitucion.devolucionDetalleId
                } ?: return@forEach

            val variacion =
                productoVariacionDao.getById(
                    sustitucion.productoVariacionId
                )

            val presentacion =
                variacion?.presentacion?.let {
                    presentacionDao.getById(it)
                }

            val cambio = SustitucionSeleccionada(
                productoVariacionId =
                    sustitucion.productoVariacionId,
                nombreProducto =
                    sustitucion.nombreProducto,
                presentacionNombre =
                    presentacion?.nombre ?: "",
                precio =
                    sustitucion.precioUnitario,
                cantidad =
                    sustitucion.cantidad.toInt()
            )

            sustitucionesPorProducto
                .getOrPut(detalle.productoVariacionId) {
                    mutableListOf()
                }
                .add(cambio)
        }

        return Pair(
            items,
            sustitucionesPorProducto
        )
    }

    private fun getInicioDelDia(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

    private fun getFinDelDia(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return calendar.timeInMillis
    }

    private fun parseFecha(fecha: String): Long {
        return fecha.toLongOrNull()
            ?: try {
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    Locale.getDefault()
                ).parse(fecha)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
    }
}