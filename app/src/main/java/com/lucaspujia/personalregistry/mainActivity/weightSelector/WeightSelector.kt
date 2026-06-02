package com.lucaspujia.personalregistry.mainActivity.weightSelector

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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_DECIMAL_PRECISION
import com.lucaspujia.personalregistry.mainActivity.WEIGHT_DEFAULT_VALUE
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.OUTER_PADDING
import com.lucaspujia.personalregistry.utils.now
import com.lucaspujia.personalregistry.utils.nowMillis
import com.lucaspujia.personalregistry.utils.pressedInteractionSource
import com.lucaspujia.personalregistry.utils.resolveDatePickerText
import com.lucaspujia.personalregistry.utils.selectableDatesFromFunction
import com.lucaspujia.personalregistry.utils.todayForDatePicker
import java.time.LocalDate
import kotlin.math.pow

@Composable
fun WeightSelector(
    modifier: Modifier = Modifier,
) {
    val viewModel = LocalMainActivityActions.current
    WeightSelectorContent(
        modifier = modifier,
        latestStoredWeight = viewModel.filters.weightsF.lastOrNull(),
        isSelectableDate = { viewModel.isSelectableDate(it) },
        onAddWeight = { weight, date -> viewModel.addWeight(weight, date) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightSelectorContent(
    modifier: Modifier = Modifier,
    latestStoredWeight: Float?,
    isSelectableDate: (Long) -> Boolean,
    onAddWeight: (Float, Long?) -> Unit
) {
    val weightStep = remember { (10.0).pow(-WEIGHT_DECIMAL_PRECISION).toFloat() }
    var weight by remember(latestStoredWeight) {
        mutableFloatStateOf(latestStoredWeight ?: WEIGHT_DEFAULT_VALUE)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayForDatePicker(),
        selectableDates = selectableDatesFromFunction { isSelectableDate(it) },
        yearRange = IntRange(LocalDate.now().year - 1, LocalDate.now().year),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(OUTER_PADDING),
    ) {
        FilterControls(
            datePickerState = datePickerState
        )

        Spacer(Modifier.height(8.dp))

        if (isSelectableDate(datePickerState.selectedDateMillis ?: nowMillis())) {
            VerticalNumberPicker(
                value = weight,
                onValueChange = { weight = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilledIconButton(
                    onClick = { weight -= weightStep },
                    interactionSource = pressedInteractionSource { weight -= weightStep }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Disminuir peso")
                }
                Button(
                    onClick = { onAddWeight(weight, datePickerState.selectedDateMillis) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_weight), style = MaterialTheme.typography.titleMedium)
                }
                FilledIconButton(
                    onClick = { weight += weightStep },
                    interactionSource = pressedInteractionSource { weight += weightStep }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar peso")
                }
            }
        } else {
            Text(
                text = stringResource(R.string.date_already_registered),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterControls(
    datePickerState: DatePickerState,
) {
    val viewModel = LocalMainActivityActions.current
    FilterControlsContent(
        datePickerState = datePickerState,
        onViewTogglesOpened = { viewModel.viewTogglesOpened = true },
        onFiltersOpened = { viewModel.filtersOpened = true },
        onSettingsOpened = { viewModel.settingsOpened = true }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterControlsContent(
    datePickerState: DatePickerState,
    onViewTogglesOpened: () -> Unit,
    onFiltersOpened: () -> Unit,
    onSettingsOpened: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        var openedDatePicker by remember { mutableStateOf(false) }
        
        Text(
            text = resolveDatePickerText(datePickerState.selectedDateMillis),
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
        
        Surface(
            onClick = { openedDatePicker = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(32.dp),
            shadowElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Seleccionar fecha",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        FilledIconButton(
            onClick = onViewTogglesOpened,
            shape = RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ),
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
            onClick = onFiltersOpened,
            shape = RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ),
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
                TextButton(onClick = { openedDatePicker = false }) { Text(stringResource(R.string.accept)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    openedDatePicker = false
                    datePickerState.setSelectedDate(now())
                }) { Text(stringResource(R.string.clear)) }
            }
        ) {
            DatePicker(state = datePickerState, title = null)
        }

        Spacer(Modifier.width(12.dp))

        FilledIconButton(
            onClick = onSettingsOpened,
            shape = RoundedCornerShape(12.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Abrir Configuración",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FilterControlsPreview() {
    PersonalRegistryTheme {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayForDatePicker()
        )
        FilterControlsContent(
            datePickerState = datePickerState,
            onViewTogglesOpened = {},
            onFiltersOpened = {},
            onSettingsOpened = {}
        )
    }
}

@ThemePreviews
@Composable
private fun WeightSelectorPreview() {
    PersonalRegistryTheme {
        WeightSelectorContent(
            latestStoredWeight = 70.5f,
            isSelectableDate = { true },
            onAddWeight = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
private fun DatePickerPreview() {
    MaterialTheme {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayForDatePicker(),
            selectableDates = selectableDatesFromFunction { true },
            yearRange = IntRange(LocalDate.now().year - 1, LocalDate.now().year),
        )
        DatePickerDialog(
            onDismissRequest = { },
            confirmButton = { TextButton(onClick = { }) { Text(stringResource(R.string.accept)) } },
            dismissButton = { TextButton(onClick = {}) { Text(stringResource(R.string.clear)) } }
        ) {
            DatePicker(state = datePickerState, title = null)
        }
    }
}
