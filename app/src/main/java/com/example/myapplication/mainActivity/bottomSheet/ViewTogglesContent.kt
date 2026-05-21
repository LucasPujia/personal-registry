package com.example.myapplication.mainActivity.bottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.mainActivity.MainActivityModel
import com.example.myapplication.mainActivity.MainActivityViewModel
import com.example.myapplication.mainActivity.settings.SettingsRepository
import com.example.myapplication.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTogglesContent(
    viewModel: MainActivityViewModel,
    onDismissRequest: () -> Unit
) {
    var showGraph by remember { mutableStateOf(viewModel.viewToggles.graph) }
    var showList by remember { mutableStateOf(viewModel.viewToggles.list) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ver gráfico")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = showGraph, onCheckedChange = { showGraph = it })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ver lista")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = showList, onCheckedChange = { showList = it })
        }

        Button(
            onClick = {
                viewModel.applyViewToggles(showGraph, showList)
                onDismissRequest()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aplicar")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewTogglesContentPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    MyApplicationTheme {
        val settingsRepository = SettingsRepository(context)
        ViewTogglesContent(
            viewModel = MainActivityViewModel(
                MainActivityModel(InMemoryWeightsStorage(), settingsRepository)
            ),
            onDismissRequest = {}
        )
    }
}
