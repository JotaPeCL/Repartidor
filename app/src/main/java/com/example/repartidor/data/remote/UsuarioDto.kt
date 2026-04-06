package com.example.repartidor.data.remote

data class UsuarioDto(
    val id: Int,
    val username: String,
    val first_name: String,
    val last_name: String,
    val email: String?,
    val telefono: String?,
    val direccion: String?,
    val foto: String?,
    val rol: Int?, // 👈 IMPORTANTE (solo ID)
    val updated_at: String
)