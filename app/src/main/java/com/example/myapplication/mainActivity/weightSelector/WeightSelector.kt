package com.example.myapplication.mainActivity.weightSelector

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.mainActivity.MainActivityModel
import com.example.myapplication.mainActivity.MainActivityViewModel
import com.example.myapplication.mainActivity.WEIGHT_DECIMAL_PRECISION
import com.example.myapplication.mainActivity.WEIGHT_DEFAULT_VALUE
import com.example.myapplication.utils.OUTER_PADDING
import com.example.myapplication.utils.resolveDatePickerText
import com.example.myapplication.utils.selectableDatesFromFunction
import com.example.myapplication.utils.todayForDatePicker
import java.time.LocalDate
import kotlin.math.pow

@Composable
fun WeightSelector(
    viewModel: MainActivityViewModel,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .fillMaxWidth()
            .padding(OUTER_PADDING),
    ) {
        FilterControls(datePickerState, viewModel)

        Spacer(Modifier.height(8.dp))

        VerticalNumberPicker(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.addWeight(weight, datePickerState.selectedDateMillis) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar peso", style = MaterialTheme.typography.titleMedium)
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
        modifier = Modifier.fillMaxWidth(),
    ) {
        var openedDatePicker by remember { mutableStateOf(false) }
        
        Text(
            text = resolveDatePickerText(datePickerState.selectedDateMillis),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
        
        Surface(
            onClick = { openedDatePicker = true },
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            modifier = Modifier.size(32.dp),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Seleccionar fecha",
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        FilledIconButton(
            onClick = { viewModel.viewTogglesOpened = true },
            shape = RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = Color(0xFF6750A4)),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RemoveRedEye,
                contentDescription = "Abrir Controles de vista",
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        FilledIconButton(
            onClick = { viewModel.filtersOpened = true },
            shape = RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = Color(0xFF6750A4)),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = "Abrir Filtros",
                modifier = Modifier.size(20.dp)
            )
        }

        if (openedDatePicker) DatePickerDialog(
            onDismissRequest = { openedDatePicker = false },
            confirmButton = {
                TextButton(onClick = { openedDatePicker = false }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    openedDatePicker = false
                    datePickerState.setSelectedDate(null)
                }) { Text("Limpiar") }
            }
        ) {
            DatePicker(state = datePickerState)
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