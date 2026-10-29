package com.example.repartidor.data.remote.request

data class PinAdminRequest(val pin: String)

data class PinAdminResponse(
    val success: Boolean,
    val message: String
)