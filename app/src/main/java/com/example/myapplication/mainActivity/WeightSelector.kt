package com.example.myapplication.mainActivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.setSelectedDate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.utils.pressedInteractionSource
import com.example.myapplication.utils.resolveDatePickerText
import com.example.myapplication.utils.selectableDatesFromFunction
import com.example.myapplication.utils.todayForDatePicker
import java.time.LocalDate
import kotlin.math.pow

@Composable
fun WeightSelector(
    viewModel: MainActivityViewModel,
) {
    val latestStoredWeight = viewModel.filters.weightsF.lastOrNull()
    val weightStep = remember { (10.0).pow(-WEIGHT_DECIMAL_PRECISION).toFloat() }
    var weight by remember(latestStoredWeight) {
        mutableFloatStateOf(latestStoredWeight ?: WEIGHT_DEFAULT_VALUE)
    }
    // TODO: definir qué valores son válidos y cuáles no
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayForDatePicker(),
        selectableDates = selectableDatesFromFunction { viewModel.isSelectableDate(it) },
        yearRange = IntRange(LocalDate.now().year - 1, LocalDate.now().year),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilterControls(datePickerState, viewModel)

        if (!viewModel.isSelectableDate(datePickerState.selectedDateMillis ?: todayForDatePicker())) {
            Text(
                text = "Ya hay un peso registrado para esta fecha",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            VerticalNumberPicker(
                value = weight,
                onValueChange = { weight = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledIconButton(
                    onClick = { weight -= weightStep },
                    interactionSource = pressedInteractionSource { weight -= weightStep },
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Decrementar peso",
                    )
                }

                Button(
                    onClick = { viewModel.addWeight(weight, datePickerState.selectedDateMillis) },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) { Text("Agregar") }

                FilledIconButton(
                    onClick = { weight += weightStep },
                    interactionSource = pressedInteractionSource { weight += weightStep },
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_up_float),
                        contentDescription = "Aumentar peso",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterControls(
    datePickerState: DatePickerState,
    viewModel: MainActivityViewModel
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Peso del día:",
                style = MaterialTheme.typography.titleMedium,
            )

            var openedDatePicker by remember { mutableStateOf(false) }
            Text(
                text = resolveDatePickerText(datePickerState.selectedDateMillis),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            )
            FilledIconButton(
                modifier = Modifier.size(32.dp),
                onClick = { openedDatePicker = true },
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Seleccionar fecha",
                )
            }
            if (openedDatePicker) DatePickerDialog(
                modifier = Modifier.padding(start = 8.dp),
                onDismissRequest = { openedDatePicker = false },
                dismissButton = {
                    Button(onClick = {
                        openedDatePicker = false
                        datePickerState.setSelectedDate(null)
                    }) { Text("Limpiar") }
                },
                confirmButton = {
                    Button(onClick = { openedDatePicker = false }) { Text("Aceptar") }
                },
                content = {
                    DatePicker(state = datePickerState)
                }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
                onClick = { viewModel.viewTogglesOpened = true },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "Abrir Controles de vista",
                )
            }

            FilledIconButton(
                onClick = { viewModel.filtersOpened = true },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Abrir Filtros",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeightSelectorPreview() {
    MaterialTheme {
        val initialValues: List<Float> = listOf()
        val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
        val viewModel = MainActivityViewModel(MainActivityModel(memoryStorage))
        WeightSelector(viewModel)
    }
}