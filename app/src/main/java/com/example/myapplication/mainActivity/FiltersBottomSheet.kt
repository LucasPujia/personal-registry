package com.example.myapplication.mainActivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBottomSheet(
    viewModel: MainActivityViewModel,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    var minVal by remember { mutableStateOf(viewModel.filters.minViewValue.toString()) }
    var maxVal by remember { mutableStateOf(viewModel.filters.maxViewValue.toString()) }
    var goal by remember { mutableStateOf(viewModel.filters.goalWeight?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = minVal,
                onValueChange = { minVal = it },
                label = { Text("Mínimo (Gráfico)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = maxVal,
                onValueChange = { maxVal = it },
                label = { Text("Máximo (Gráfico)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text("Peso Objetivo") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    viewModel.applyFilters(
                        Filters(
                            minViewValue = minVal.toInt(),
                            maxViewValue = maxVal.toInt(),
                            goalWeight = goal.toIntOrNull()
                        )
                    )
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aplicar")
            }
        }
    }
}