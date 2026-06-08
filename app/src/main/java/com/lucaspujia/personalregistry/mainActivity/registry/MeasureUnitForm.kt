package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun MeasureUnitForm(
    unit1: MeasureUnitInput,
    setUnit1: (MeasureUnitInput) -> Unit,
    unitNumber: Int
) {
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
            OutlinedTextField(
                value = unit1.precision.toString(),
                onValueChange = {
                    if (it.all { c -> c.isDigit() }) setUnit1(
                        unit1.copy(
                            precision = it.toIntOrNull() ?: 1
                        )
                    )
                },
                label = { Text(stringResource(R.string.precision)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
