package com.example.myapplication.mainActivity.bottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.extensionFunctions.selectedDateRange
import com.example.myapplication.mainActivity.MainActivityModel
import com.example.myapplication.mainActivity.MainActivityViewModel
import com.example.myapplication.mainActivity.settings.SettingsRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.defaultDatePickerFormatter
import com.example.myapplication.utils.resolveDatePickerText
import com.example.myapplication.utils.selectableDatesFromFunction
import com.example.myapplication.utils.todayForDatePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersContent(
    viewModel: MainActivityViewModel,
    onDismissRequest: () -> Unit
) {
    var minVal by remember { mutableStateOf(viewModel.filters.minViewValue.toString()) }
    var maxVal by remember { mutableStateOf(viewModel.filters.maxViewValue.toString()) }
    var goal by remember { mutableStateOf(viewModel.filters.goalWeight?.toString() ?: "") }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = viewModel.filters.dateRange?.first,
        initialSelectedEndDateMillis = viewModel.filters.dateRange?.second,
        selectableDates = selectableDatesFromFunction { it <= todayForDatePicker() }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.filters),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = minVal,
            onValueChange = { minVal = it },
            label = { Text("${stringResource(R.string.minimum)} (${stringResource(R.string.graph)})") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = maxVal,
            onValueChange = { maxVal = it },
            label = { Text("${stringResource(R.string.maximum)} (${stringResource(R.string.graph)})") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text(stringResource(R.string.weight_goal)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                val dateRangeText = dateRangePickerState.selectedDateRange()?.let { (start, end) ->
                    "${resolveDatePickerText(start)} - ${resolveDatePickerText(end)}"
                } ?: stringResource(R.string.no_range_selected)
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

            var datePickerOpened by remember { mutableStateOf(false) }
            var previousStartDateMillis by remember { mutableStateOf<Long?>(null) }
            var previousEndDateMillis by remember { mutableStateOf<Long?>(null) }
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
                        Button(onClick = { datePickerOpened = false }) { Text(stringResource(R.string.accept)) }
                    },
                    dismissButton = {
                        Button(onClick = {
                            datePickerOpened = false
                            dateRangePickerState.setSelection(null, null)
                        }) { Text(stringResource(R.string.clear)) }
                    }
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        dateFormatter = defaultDatePickerFormatter(),
                        title = null,
                    )
                }
            }
        }

        AcceptButton(viewModel, minVal, maxVal, goal, dateRangePickerState, onDismissRequest)
    }
}

@Composable
private fun AcceptButton(
    viewModel: MainActivityViewModel,
    minVal: String,
    maxVal: String,
    goal: String,
    dateRangePickerState: DateRangePickerState,
    onDismissRequest: () -> Unit
) {
    val responseMessage = remember { mutableStateOf<Int?>(null) }
    Button(
        onClick = {
            val response = viewModel.applyFilters(
                minViewValue = minVal.toIntOrNull(),
                maxViewValue = maxVal.toIntOrNull(),
                goalWeight = goal.toIntOrNull(),
                dateRange = dateRangePickerState.selectedDateRange()
            )
            if (response != null) {
                responseMessage.value = response
            } else {
                onDismissRequest()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Aplicar")
    }

    responseMessage.value?.let { messageID ->
        AlertDialog(
            onDismissRequest = { responseMessage.value = null },
            confirmButton = {
                Button(onClick = { responseMessage.value = null }) {
                    Text(stringResource(R.string.accept))
                }
            },
            title = {
                Text(stringResource(R.string.warning))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(messageID))
                    Text("Por favor, revisa los filtros seleccionados.")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FiltersContentPreview() {
    val context = LocalContext.current
    MyApplicationTheme {
        val initialValues: List<Float> = listOf()
        val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
        val settingsRepository = SettingsRepository(context)
        val viewModel = MainActivityViewModel(
            MainActivityModel(memoryStorage, settingsRepository)
        )
        FiltersContent(viewModel = viewModel, onDismissRequest = {})
    }
}
