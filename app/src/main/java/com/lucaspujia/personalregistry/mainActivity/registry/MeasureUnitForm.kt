package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.MeasureUnit

data class MeasureUnitInput(
    val name: String,
    val symbol: String,
    val precision: Int
) {
    fun hasValidData() = name.isNotBlank() && symbol.isNotBlank() && precision >= 0

    fun toMeasureUnit() = MeasureUnit(name.trim(), symbol.trim(), precision)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureUnitForm(
    unit1: MeasureUnitInput,
    setUnit1: (MeasureUnitInput) -> Unit,
    unitNumber: Int
) {
    var expanded by remember { mutableStateOf(false) }
    val precisionOptions = listOf("1.0" to 0, "0.1" to 1, "0.01" to 2, "0.001" to 3)

    Column {
        Text(
            "${stringResource(R.string.unit)} $unitNumber",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = unit1.name,
            onValueChange = { setUnit1(unit1.copy(name = it)) },
            label = { Text(stringResource(R.string.unit_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = unit1.symbol,
                onValueChange = { setUnit1(unit1.copy(symbol = it)) },
                label = { Text(stringResource(R.string.symbol)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = precisionOptions.find { it.second == unit1.precision }?.first ?: "0.1",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.precision)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    precisionOptions.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                setUnit1(unit1.copy(precision = value))
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}
