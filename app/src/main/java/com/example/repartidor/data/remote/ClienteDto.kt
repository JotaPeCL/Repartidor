package com.example.repartidor.data.remote

data class ClienteDto(
    val id: Int,
    val nombre: String,
    val nombre_negocio: String,
    val giro: String?,
    val tipo_exhibidor: String?,
    val direccion: String,
    val localidad: String?,
    val colonia: String?,
    val telefono: String?,
    val credito: Double?,
    val porcentaje_descuento: Double,
    val imagen: String?,
    val observaciones: String?,
    val ruta: Int?,
    val estado: Boolean,
    val updated_at: String
)