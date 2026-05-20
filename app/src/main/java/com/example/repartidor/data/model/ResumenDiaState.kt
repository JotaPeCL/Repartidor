package com.example.repartidor.data.model

data class ResumenDiaState(

    val efectivoVentas: Double = 0.0,
    val totalAbonos: Double = 0.0,
    val totalEfectivo: Double = 0.0,

    val cantidadCreditos: Int = 0,
    val totalPendiente: Double = 0.0,

    val productosVendidos: List<ProductoResumen> = emptyList(),
    val productosDevueltos: List<ProductoResumen> = emptyList()

)