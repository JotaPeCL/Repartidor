package com.example.repartidor.data.model.dclass

data class ResumenDiaState(

    val efectivoVentas: Double = 0.0,
    val dineroTotalVentas: Double = 0.0,
    val totalAbonos: Double = 0.0,
    val totalEfectivo: Double = 0.0,

    // DINERO DE ROTACIONES
    val cantidadRotaciones: Int = 0,
    val totalRotacionesRecibido: Double = 0.0,
    val totalRotacionesEntregado: Double = 0.0,
    val balanceRotaciones: Double = 0.0,

    val productosRecibidosRotacion: List<ProductoResumen> = emptyList(),
    val productosEntregadosRotacion: List<ProductoResumen> = emptyList(),

    val totalDevolucionesRecibido: Double = 0.0,
    val totalDevolucionesEntregado: Double = 0.0,
    val balanceDevoluciones: Double = 0.0,

    val cantidadCreditos: Int = 0,
    val totalPendiente: Double = 0.0,

    val productosVendidos: List<ProductoResumen> = emptyList(),
    val productosDevueltos: List<ProductoResumen> = emptyList(),
    val totalDevoluciones: Double = 0.0

)