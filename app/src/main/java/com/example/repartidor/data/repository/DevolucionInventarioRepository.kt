package com.example.repartidor.data.repository

import androidx.room.withTransaction
import com.example.repartidor.data.local.AppDatabase
import com.example.repartidor.data.local.DevolucionDao
import com.example.repartidor.data.local.DevolucionSustitucionDao
import com.example.repartidor.data.local.MiniBodegaDetalleDao
import com.example.repartidor.data.local.MiniBodegaDetalleMermaDao
import com.example.repartidor.data.model.dclass.CarritoItem
import com.example.repartidor.data.model.dclass.ProductoSustitucion
import com.example.repartidor.data.model.dclass.SustitucionSeleccionada
import com.example.repartidor.data.model.entity.DevolucionDetalleEntity
import com.example.repartidor.data.model.entity.DevolucionEntity
import com.example.repartidor.data.model.entity.DevolucionSustitucionEntity
import com.example.repartidor.data.model.entity.MiniBodegaDetalleMermaEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.math.roundToLong

class DevolucionInventarioRepository(
    private val database: AppDatabase,
    private val devolucionDao: DevolucionDao,
    private val devolucionSustitucionDao: DevolucionSustitucionDao,
    private val miniBodegaDetalleDao: MiniBodegaDetalleDao,
    private val mermaDao: MiniBodegaDetalleMermaDao
) {
    fun getVariacionesDisponiblesParaSustitucion(
        miniBodegaId: Int
    ): Flow<List<ProductoSustitucion>> {
        return miniBodegaDetalleDao.getVariacionesDisponiblesParaSustitucion(miniBodegaId)
    }

    suspend fun registrarDevolucion(
        devolucion: DevolucionEntity,
        carrito: List<CarritoItem>,
        sustitucionesPorProducto: Map<Int, List<SustitucionSeleccionada>>
    ): DevolucionEntity = database.withTransaction {
            require(carrito.isNotEmpty()) { "La devolución no contiene productos" }
            require(carrito.map { it.productoVariacionId }.distinct().size == carrito.size) {
                "Hay productos repetidos en la devolución"
            }

            val idsCarrito = carrito.map { it.productoVariacionId }.toSet()
            require(sustitucionesPorProducto.keys.all { it in idsCarrito }) {
                "Se encontraron sustituciones sin producto devuelto"
            }

            val descuentos = mutableMapOf<Int, Double>()
            var valorDevueltoCentavos = 0L
            var valorCambioCentavos = 0L

            // 1. Validar productos devueltos y sustituciones
            carrito.forEach { item ->
                val cantidad = item.cantidad.toDouble()
                val precio = item.precio
                require(cantidad.isFinite() && cantidad > 0 && precio.isFinite() && precio >= 0) {
                    "Cantidad o precio inválido para ${item.productoNombre}"
                }

                val sustituciones = sustitucionesPorProducto[item.productoVariacionId].orEmpty()

                if (sustituciones.isNotEmpty()) {
                    require(devolucion.tipo == "Devolución de cliente") {
                        "Solo se puede realizar un cambio en una devolución de cliente"
                    }

                    valorDevueltoCentavos += ((cantidad * precio) * 100).roundToLong()

                    sustituciones.forEach { sustitucion ->
                        val cantidadCambio = sustitucion.cantidad.toDouble()
                        val precioCambio = sustitucion.precio

                        require(
                            cantidadCambio.isFinite() && cantidadCambio > 0 &&
                                    precioCambio.isFinite() && precioCambio >= 0
                        ) {
                            "Cantidad o precio inválido para ${sustitucion.nombreProducto}"
                        }

                        valorCambioCentavos += ((cantidadCambio * precioCambio) * 100).roundToLong()

                        val id = sustitucion.productoVariacionId
                        descuentos[id] = (descuentos[id] ?: 0.0) + cantidadCambio
                    }
                } else {
                    // Sin cambio: se conserva el descuento normal de inventario.
                    val id = item.productoVariacionId
                    descuentos[id] = (descuentos[id] ?: 0.0) + cantidad
                }
            }

            // 2. Calcular diferencia económica de toda la operación
            val diferenciaCentavos = valorCambioCentavos - valorDevueltoCentavos
            val tipoDiferencia = when {
                diferenciaCentavos > 0 -> "CLIENTE_PAGO"
                diferenciaCentavos < 0 -> "REPARTIDOR_PAGO"
                else -> "SIN_DIFERENCIA"
            }

            val devolucionFinal = devolucion.copy(
                diferenciaDinero = kotlin.math.abs(diferenciaCentavos) / 100.0,
                tipoDiferencia = tipoDiferencia,
                formaLiquidacion = "EFECTIVO"
            )

            // 3. Validar el descuento total de cada variación
            descuentos.forEach { (productoId, cantidad) ->
                require(cantidad.isFinite() && cantidad > 0) {
                    "Cantidad inválida para el producto $productoId"
                }

                val stock = miniBodegaDetalleDao.obtenerStock(
                    devolucion.miniBodegaId, productoId
                ) ?: 0.0

                require(stock >= cantidad) {
                    "Stock insuficiente para el producto $productoId. Disponible: $stock, requerido: $cantidad"
                }
            }

            // 4. Guardar devolución con su diferencia económica
            val devolucionId = devolucionDao.insertarDevolucion(devolucionFinal).toInt()

            // 5. Guardar detalles
            val detalles = carrito.map { item ->
                DevolucionDetalleEntity(
                    uuid = UUID.randomUUID().toString(),
                    devolucionId = devolucionId,
                    devolucionUuid = devolucion.uuid,
                    productoVariacionId = item.productoVariacionId,
                    nombreProducto = item.productoNombre,
                    cantidad = item.cantidad.toDouble(),
                    precioUnitario = item.precio
                )
            }
            val detalleIds = devolucionDao.insertarDetalles(detalles)

            // 6. Guardar sustituciones
            val sustitucionesEntities = mutableListOf<DevolucionSustitucionEntity>()

            carrito.forEachIndexed { index, item ->
                val sustituciones = sustitucionesPorProducto[item.productoVariacionId].orEmpty()

                sustituciones.forEach { sustitucion ->
                    sustitucionesEntities.add(
                        DevolucionSustitucionEntity(
                            uuid = UUID.randomUUID().toString(),
                            devolucionId = devolucionId,
                            devolucionUuid = devolucion.uuid,
                            devolucionDetalleId = detalleIds[index].toInt(),
                            productoVariacionId = sustitucion.productoVariacionId,
                            nombreProducto = sustitucion.nombreProducto,
                            cantidad = sustitucion.cantidad.toDouble(),
                            precioUnitario = sustitucion.precio,
                            createdAt = devolucion.createdAt,
                            sincronizado = false
                        )
                    )
                }
            }

            if (sustitucionesEntities.isNotEmpty()) {
                devolucionSustitucionDao.insertarSustituciones(sustitucionesEntities)
            }

            // 7. Descontar inventario con validación de stock
            descuentos.forEach { (productoId, cantidad) ->
                val filas = miniBodegaDetalleDao.descontarStockValidado(
                    miniBodegaId = devolucion.miniBodegaId,
                    productoId = productoId,
                    cantidad = cantidad
                )

                check(filas == 1) {
                    "No se pudo descontar el producto $productoId por stock insuficiente"
                }
            }

            // 8. Registrar mermas de todos los productos devueltos
        carrito.forEach { item ->
            mermaDao.insertarMerma(
                MiniBodegaDetalleMermaEntity(
                    uuid = UUID.randomUUID().toString(),
                    miniBodegaId = devolucion.miniBodegaId,
                    productoVariacionId = item.productoVariacionId,
                    cantidad = item.cantidad.toDouble(),
                    devolucionId = devolucionId,
                    devolucionUuid = devolucion.uuid,
                    createdAt = devolucion.createdAt
                )
            )
        }

        devolucionFinal
    }
}
