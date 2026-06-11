package com.example.repartidor.data.remote.request

data class BluetoothState(
    val isSupported: Boolean,
    val isEnabled: Boolean,
    val hasPermission: Boolean
)