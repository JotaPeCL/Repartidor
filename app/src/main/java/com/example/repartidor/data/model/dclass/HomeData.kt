package com.example.repartidor.data.model.dclass

import com.example.repartidor.data.model.entity.RutaEntity
import com.example.repartidor.data.model.entity.UsuarioEntity
import com.example.repartidor.data.model.entity.VehiculoEntity

data class HomeData(
    val usuario: UsuarioEntity?,
    val ruta: RutaEntity?,
    val vehiculo: VehiculoEntity?
)