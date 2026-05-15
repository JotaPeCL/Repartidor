package com.example.repartidor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "abono")
data class AbonoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val uuid: String, // 🔥

    val ventaId: Int,
    val ventaUuid: String, // 🔥

    val usuarioId: Int,
    val monto: Double,
    val fecha: String,
    val sincronizado: Boolean = false
)