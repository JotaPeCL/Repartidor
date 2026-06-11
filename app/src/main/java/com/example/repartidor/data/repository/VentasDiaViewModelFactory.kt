package com.example.repartidor.data.repository

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.repartidor.data.local.SessionManager
import com.example.repartidor.utils.PrinterManager
import com.example.repartidor.viewmodel.VentasDia.VentasDiaViewModel

class VentasDiaViewModelFactory(
    private val repository: VentasDiaRepository,
    private val printerRepository: PrinterRepository,
    private val printerManager: PrinterManager,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VentasDiaViewModel(
            repository,
            printerRepository,
            printerManager,
            bluetoothAdapter,
            sessionManager
        ) as T
    }
}