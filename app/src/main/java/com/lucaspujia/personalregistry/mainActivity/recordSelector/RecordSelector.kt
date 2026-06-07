package com.lucaspujia.personalregistry.mainActivity.recordSelector

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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.mainActivity.RECORD_DEFAULT_VALUE
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
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
fun RecordSelector(
    modifier: Modifier = Modifier,
) {
    val viewModel = LocalMainActivityActions.current
    val registry = viewModel.activeRegistry ?: return

    RecordSelectorContent(
        modifier = modifier,
        registry = registry,
        latestRecord = viewModel.filters.records.lastOrNull(),
        isSelectableDate = { viewModel.isSelectableDate(it) },
        onAddRecord = { v1, v2, date -> viewModel.addRecord(v1, v2, date) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordSelectorContent(
    modifier: Modifier = Modifier,
    registry: Registry,
    latestRecord: RecordItem?,
    isSelectableDate: (Long) -> Boolean,
    onAddRecord: (Double, Double?, Long?) -> Unit = {_, _, _ -> }
) {
    // TODO: checkk
    val step1 = remember(registry.unit1.precision) { (10.0).pow(-registry.unit1.precision) }
    val step2 = remember(registry.unit2?.precision) { registry.unit2?.let { (10.0).pow(-it.precision) } }

    var value1 by remember(latestRecord, registry.id) {
        mutableDoubleStateOf(latestRecord?.value1 ?: RECORD_DEFAULT_VALUE.toDouble())
    }
    var value2 by remember(latestRecord, registry.id) {
        mutableDoubleStateOf(latestRecord?.value2 ?: RECORD_DEFAULT_VALUE.toDouble())
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

        val isSmall = registry.unit2 != null
        Spacer(Modifier.height(8.dp))

        if (isSelectableDate(datePickerState.selectedDateMillis ?: nowMillis())) {
            Row(modifier = Modifier.fillMaxWidth()) {
                VerticalNumberPicker(
                    value = value1.toFloat(),
                    onValueChange = { value1 = it.toDouble() },
                    unit = registry.unit1.symbol,
                    precision = registry.unit1.precision,
                    label = registry.unit1.name,
                    isSmall = isSmall,
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSmall) 90.dp else 120.dp)
                )

                registry.unit2?.let { u2 ->
                    Spacer(Modifier.width(8.dp))
                    VerticalNumberPicker(
                        value = value2.toFloat(),
                        onValueChange = { value2 = it.toDouble() },
                        unit = u2.symbol,
                        precision = u2.precision,
                        label = u2.name,
                        isSmall = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val buttonHeight = if (isSmall) 36.dp else 40.dp
                FilledIconButton(
                    onClick = { value1 -= step1 },
                    interactionSource = pressedInteractionSource { value1 -= step1 },
                    modifier = Modifier.size(buttonHeight)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease value 1", )
                }
                Button(
                    onClick = { onAddRecord(value1, if (registry.unit2 != null) value2 else null, datePickerState.selectedDateMillis) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .size(buttonHeight),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "${stringResource(R.string.add)} ${registry.name}",
                        style = if (isSmall) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
                    )
                }
                FilledIconButton(
                    onClick = { value1 += step1 },
                    interactionSource = pressedInteractionSource { value1 += step1 },
                    modifier = Modifier.size(buttonHeight)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase value 1")
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
                    contentDescription = "Select date",
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
                contentDescription = "Open View Controls",
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
                contentDescription = "Open Filters",
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
                contentDescription = "Open Settings",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@ThemePreviews
@Composable
fun RecordSelectorPreview() {
    val registry = Registry(
        name = "Peso",
        emoji = "⚖️",
        unit1 = MeasureUnit("Kilo", "kg", 1)
    )
    PersonalRegistryTheme {
        RecordSelectorContent(
            registry = registry,
            latestRecord = null,
            isSelectableDate = { true },
        )
    }
}

@ThemePreviews
@Composable
fun TwoRecordSelectorsPreview() {
    val registry = Registry(
        name = "Money",
        emoji = "Money",
        unit1 = MeasureUnit("Dollar", "$", 1),
        unit2 = MeasureUnit("Cent", "¢", 0)
    )
    PersonalRegistryTheme {
        RecordSelectorContent(
            registry = registry,
            latestRecord = null,
            isSelectableDate = { true },
        )
    }
}
