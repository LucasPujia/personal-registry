package com.example.myapplication.mainActivity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.utils.pressedInteractionSource2
import kotlin.math.pow

@Composable
fun WeightSelector(
    viewModel: MainActivityViewModel,
) {
    val latestStoredWeight = viewModel.filters.weightsF.lastOrNull()
    var weight by remember(latestStoredWeight) {
        mutableFloatStateOf(latestStoredWeight ?: WEIGHT_DEFAULT_VALUE)
    }
    val weightStep = (10.0).pow(-WEIGHT_DECIMAL_PRECISION).toFloat()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Peso del día:",
                style = MaterialTheme.typography.titleMedium,
            )

            Row {
                val nextViewIconRes = if (viewModel.viewMode == ViewMode.CHART) {
                    android.R.drawable.ic_menu_agenda
                } else {
                    android.R.drawable.ic_menu_sort_by_size
                }


                FilledIconButton(
                    onClick = { viewModel.changeViewMode() },
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = nextViewIconRes),
                        contentDescription = "Cambiar vista",
                    )
                }

                FilledIconButton(
                    onClick = { viewModel.filtersOpened = true },
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_manage),
                        contentDescription = "Filtros",
                    )
                }
            }
        }

        VerticalNumberPicker(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledIconButton(
                onClick = { weight -= weightStep },
                interactionSource = pressedInteractionSource2 { weight -= weightStep },
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_down_float),
                    contentDescription = "Decrementar peso",
                )
            }

            Button(
                onClick = { viewModel.addWeight(weight) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { Text("Agregar") }

            FilledIconButton(
                onClick = { weight += weightStep },
                interactionSource = pressedInteractionSource2 { weight += weightStep },
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.arrow_up_float),
                    contentDescription = "Aumentar peso",
                )
            }
        }
    }
}