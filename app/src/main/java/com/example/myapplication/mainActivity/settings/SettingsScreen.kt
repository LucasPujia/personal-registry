package com.example.myapplication.mainActivity.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R
import com.example.myapplication.mainActivity.MainActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainActivityViewModel,
    modifier: Modifier = Modifier
) {
    // Manejo del botón atrás del sistema
    BackHandler {
        viewModel.settingsOpened = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    // TODO: Localizar texto "Ajustes"
                    Text("Ajustes") 
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.settingsOpened = false }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retroceder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var offsetX = 0f
                detectHorizontalDragGestures(
                    onDragEnd = { offsetX = 0f },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                        if (offsetX > 200) { // Umbral para detectar el slide de salida
                            viewModel.settingsOpened = false
                        }
                    }
                )
            }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                ListItem(
                    headlineContent = { 
                        // TODO: Localizar texto "Unidades"
                        Text("Unidades") 
                    },
                    supportingContent = { 
                        // TODO: Localizar texto "Métrica (kg, cm)"
                        Text("Métrica (kg, cm)") 
                    },
                    leadingContent = { Icon(Icons.Default.Scale, contentDescription = null) }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.weight_goal)) },
                    supportingContent = { 
                        // TODO: Placeholder dinámico para peso objetivo
                        Text("70 kg") 
                    },
                    leadingContent = { Icon(Icons.Default.Flag, contentDescription = null) }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
            item {
                ListItem(
                    headlineContent = { 
                        // TODO: Localizar texto "Notificaciones"
                        Text("Notificaciones") 
                    },
                    supportingContent = { 
                        // TODO: Localizar texto "Recordatorio diario"
                        Text("Recordatorio diario") 
                    },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    trailingContent = { Switch(checked = true, onCheckedChange = {}) }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
            item {
                ListItem(
                    headlineContent = { 
                        // TODO: Localizar texto "Tema"
                        Text("Tema") 
                    },
                    supportingContent = { 
                        // TODO: Localizar texto "Seguir sistema"
                        Text("Seguir sistema") 
                    },
                    leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
            item {
                ListItem(
                    headlineContent = { 
                        // TODO: Localizar texto "Acerca de"
                        Text("Acerca de") 
                    },
                    supportingContent = { 
                        // TODO: Localizar texto "Versión 1.0.0"
                        Text("Versión 1.0.0") 
                    },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
        }
    }
}
