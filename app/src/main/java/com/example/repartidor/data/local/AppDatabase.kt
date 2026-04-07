package com.example.repartidor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.repartidor.data.model.RolEntity
import com.example.repartidor.data.model.RutaEntity
import com.example.repartidor.data.model.UsuarioEntity
import com.example.repartidor.data.model.VehiculoEntity
import com.example.repartidor.data.model.ClienteEntity
import com.example.repartidor.data.model.ClienteDiasVisitaEntity
import com.example.repartidor.data.model.CategoriaProductoEntity
import com.example.repartidor.data.model.PresentacionProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoTerminadoEntity
import com.example.repartidor.data.model.ProductoVariacionEntity
import com.example.repartidor.data.model.MiniBodegaEntity
import com.example.repartidor.data.model.MiniBodegaDetalleEntity
import com.example.repartidor.data.model.PedidoReabastecimientoEntity
import com.example.repartidor.data.model.PedidoReabastecimientoDetalleEntity
import com.example.repartidor.data.model.VentaEntity
import com.example.repartidor.data.model.VentaDetalleEntity

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
        VentaDetalleEntity::class

    ],
    version = 5 // IMPORTANTE

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

}