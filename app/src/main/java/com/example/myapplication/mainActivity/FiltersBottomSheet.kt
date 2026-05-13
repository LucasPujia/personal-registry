package com.example.myapplication.mainActivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.extensionFunctions.selectedDateRange
import com.example.myapplication.utils.defaultDatePickerFormatter
import com.example.myapplication.utils.resolveDateText
import com.example.myapplication.utils.selectableDatesTilNow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBottomSheet(
    viewModel: MainActivityViewModel,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var minVal by remember { mutableStateOf(viewModel.filters.minViewValue.toString()) }
    var maxVal by remember { mutableStateOf(viewModel.filters.maxViewValue.toString()) }
    var goal by remember { mutableStateOf(viewModel.filters.goalWeight?.toString() ?: "") }
    // TODO: definir qué valores son válidos y cuáles no
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = viewModel.filters.dateRange?.first,
        initialSelectedEndDateMillis = viewModel.filters.dateRange?.second,
        selectableDates = selectableDatesTilNow()
    )

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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                var datePickerOpened by remember { mutableStateOf(false) }
                var previousStartDateMillis by remember { mutableStateOf<Long?>(null) }
                var previousEndDateMillis by remember { mutableStateOf<Long?>(null) }
                Column {
                    val dateRangeText = dateRangePickerState.selectedDateRange()?.let { (start, end) ->
                        "${resolveDateText(start)} - ${resolveDateText(end)}"
                    } ?: "Sin rango seleccionado"
                    Text(
                        text = "Rango de fechas",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateRangeText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                FilledIconButton(
                    onClick = {
                        datePickerOpened = true
                        previousStartDateMillis = dateRangePickerState.selectedStartDateMillis
                        previousEndDateMillis = dateRangePickerState.selectedEndDateMillis
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Seleccionar rango de fechas",
                    )
                }

                if (datePickerOpened) {
                    DatePickerDialog(
                        onDismissRequest = {
                            datePickerOpened = false
                            dateRangePickerState.setSelection(previousStartDateMillis, previousEndDateMillis)
                        },
                        confirmButton = {
                            Button(onClick = { datePickerOpened = false }) { Text("Aceptar") }
                        },
                        dismissButton = {
                            Button(onClick = {
                                datePickerOpened = false
                                dateRangePickerState.setSelection(null, null)
                            }) { Text("Limpiar") }
                        }
                    ) { DateRangePicker(
                        state = dateRangePickerState,
                        dateFormatter = defaultDatePickerFormatter(),
                        title = null,
                    ) }
                }
            }


            Button(
                onClick = {
                    viewModel.applyFilters(
                        minViewValue = minVal.toIntOrNull(),
                        maxViewValue = maxVal.toIntOrNull(),
                        goalWeight = goal.toIntOrNull(),
                        dateRange = dateRangePickerState.selectedDateRange()
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
