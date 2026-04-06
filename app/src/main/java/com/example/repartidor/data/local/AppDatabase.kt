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
        ProductoVariacionEntity::class

    ],
    version = 3 // IMPORTANTE

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

}