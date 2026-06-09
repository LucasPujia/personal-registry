package com.lucaspujia.personalregistry.mainActivity.bottomSheet

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.extensionFunctions.selectedDateRange
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.defaultDatePickerFormatter
import com.lucaspujia.personalregistry.utils.resolveDatePickerText
import com.lucaspujia.personalregistry.utils.selectableDatesFromFunction
import com.lucaspujia.personalregistry.utils.todayForDatePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersContent(
    onDismissRequest: () -> Unit = {},
) {
    val viewModel = LocalMainActivityActions.current
    FiltersContentImpl(
        onDismissRequest = onDismissRequest,
        initialMinViewValue = viewModel.filters.minViewValue,
        initialMaxViewValue = viewModel.filters.maxViewValue,
        initialGoalValue = viewModel.filters.goalValue,
        initialDateRange = viewModel.filters.dateRange,
        onApplyFilters = { min, max, goal, range ->
            viewModel.applyFilters(min, max, goal, range)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersContentImpl(
    initialMinViewValue: Double,
    initialMaxViewValue: Double,
    initialGoalValue: Double?,
    initialDateRange: Pair<Long, Long>?,
    onApplyFilters: (Double?, Double?, Double?, Pair<Long, Long>?) -> Int?,
    onDismissRequest: () -> Unit = {},
) {
    var minVal by remember { mutableStateOf(initialMinViewValue.toString()) }
    var maxVal by remember { mutableStateOf(initialMaxViewValue.toString()) }
    var goalValue by remember { mutableStateOf(initialGoalValue?.toString() ?: "") }
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialDateRange?.first,
        initialSelectedEndDateMillis = initialDateRange?.second,
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
            color = MaterialTheme.colorScheme.onSurface,
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
            value = goalValue,
            onValueChange = { goalValue = it },
            label = { Text(stringResource(R.string.goal_value)) },
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
                    text = stringResource(R.string.date_range),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateRangeText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
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

        AcceptButton(minVal, maxVal, goalValue, dateRangePickerState, onApplyFilters, onDismissRequest)
    }
}

@Composable
private fun AcceptButton(
    minVal: String,
    maxVal: String,
    goalValue: String,
    dateRangePickerState: DateRangePickerState,
    onApplyFilters: (Double?, Double?, Double?, Pair<Long, Long>?) -> Int?,
    onDismissRequest: () -> Unit = {},
) {
    val responseMessage = remember { mutableStateOf<Int?>(null) }
    Button(
        onClick = {
            val response = onApplyFilters(
                minVal.toDoubleOrNull(),
                maxVal.toDoubleOrNull(),
                goalValue.toDoubleOrNull(),
                dateRangePickerState.selectedDateRange()
            )
            if (response != null) {
                responseMessage.value = response
            } else {
                onDismissRequest()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.apply))
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
                    Text(stringResource(R.string.warning_filters))
                }
            }
        )
    }
}

@ThemePreviews
@Composable
private fun FiltersContentPreview() {
    PersonalRegistryTheme {
        FiltersContentImpl(
            initialMinViewValue = 60.0,
            initialMaxViewValue = 80.0,
            initialGoalValue = 70.0,
            initialDateRange = null,
            onApplyFilters = { _, _, _, _ -> null }
        )
    }
}
