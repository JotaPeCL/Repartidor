package com.example.repartidor.ui.screens.login

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.repartidor.data.preferences.SyncPreferences
import com.example.repartidor.viewmodel.SyncViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onSyncCompleto: () -> Unit
) {

    val context = LocalContext.current
    /*
    val yaSync = remember {
        SyncPreferences.yaSincronizoHoy(context)
    }*/

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*
        if (yaSync) {
            Text("Ya sincronizaste hoy 👌")
        }*/

        Button(
            onClick = {
                viewModel.sincronizar {
                    onSyncCompleto()
                }
            },
            enabled = true//!yaSync
        ) {
            Text("Sincronizar")
        }
    }
}

