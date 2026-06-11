package com.example.repartidor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.repartidor.data.model.entity.AbonoEntity
import com.example.repartidor.data.model.entity.RolEntity
import com.example.repartidor.data.model.entity.RutaEntity
import com.example.repartidor.data.model.entity.UsuarioEntity
import com.example.repartidor.data.model.entity.VehiculoEntity
import com.example.repartidor.data.model.entity.ClienteEntity
import com.example.repartidor.data.model.entity.ClienteDiasVisitaEntity
import com.example.repartidor.data.model.entity.CategoriaProductoEntity
import com.example.repartidor.data.model.entity.PresentacionProductoTerminadoEntity
import com.example.repartidor.data.model.entity.ProductoTerminadoEntity
import com.example.repartidor.data.model.entity.ProductoVariacionEntity
import com.example.repartidor.data.model.entity.MiniBodegaEntity
import com.example.repartidor.data.model.entity.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.entity.MiniBodegaDetalleMermaEntity
import com.example.repartidor.data.model.entity.PedidoReabastecimientoEntity
import com.example.repartidor.data.model.entity.PedidoReabastecimientoDetalleEntity
import com.example.repartidor.data.model.entity.VentaEntity
import com.example.repartidor.data.model.entity.VentaDetalleEntity
import com.example.repartidor.data.model.entity.DevolucionEntity
import com.example.repartidor.data.model.entity.DevolucionDetalleEntity

@Database(
    entities = [
        RolEntity::class,
        UsuarioEntity::class,
        VehiculoEntity::class,
        RutaEntity::class,
        ClienteEntity::class,
        ClienteDiasVisitaEntity::class,
        CategoriaProductoEntity::class,
        PresentacionProductoTerminadoEntity::class,
        ProductoTerminadoEntity::class,
        ProductoVariacionEntity::class,
        MiniBodegaEntity::class,
        MiniBodegaDetalleEntity::class,
        PedidoReabastecimientoEntity::class,
        PedidoReabastecimientoDetalleEntity::class,
        VentaEntity::class,
        VentaDetalleEntity::class,
        DevolucionEntity::class,
        DevolucionDetalleEntity::class,
        MiniBodegaDetalleMermaEntity::class,
        AbonoEntity::class,

    ],
    version = 8 // IMPORTANTE

)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rolDao(): RolDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun rutaDao(): RutaDao
    abstract fun clienteDao(): ClienteDao
    abstract fun clienteDiasVisitaDao(): ClienteDiasVisitaDao
    abstract fun categoriaDao(): CategoriaProductoDao
    abstract fun presentacionDao(): PresentacionProductoTerminadoDao
    abstract fun productoDao(): ProductoTerminadoDao
    abstract fun variacionDao(): ProductoVariacionDao
    abstract fun miniBodegaDao(): MiniBodegaDao
    abstract fun miniBodegaDetalleDao(): MiniBodegaDetalleDao
    abstract fun pedidoReabastecimientoDao(): PedidoReabastecimientoDao
    abstract fun pedidoReabastecimientoDetalleDao(): PedidoReabastecimientoDetalleDao
    abstract fun ventaDao(): VentaDao
    abstract fun ventaDetalleDao(): VentaDetalleDao
    abstract fun devolucionDao(): DevolucionDao
    abstract fun devolucionDetalleDao(): DevolucionDetalleDao
    abstract fun mermaDao(): MiniBodegaDetalleMermaDao
    abstract fun abonoDao(): AbonoDao
}