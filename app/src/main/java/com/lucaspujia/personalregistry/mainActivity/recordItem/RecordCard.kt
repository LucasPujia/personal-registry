package com.lucaspujia.personalregistry.mainActivity.recordItem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordCard(
    recordItem: RecordItem,
    registry: Registry,
    onDelete: (RecordItem) -> Unit,
    variation: Double? = null,
) {
    // TODO: checkk
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(recordItem)
            }
            false
        },
        positionalThreshold = { it * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                }, label = "dismiss_color"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = registry.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recordItem.getFullDateText(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = recordItem.getDayOfWeekText(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = recordItem.formattedValue1(registry),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = registry.unit1.symbol,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }

                    recordItem.formattedValue2(registry)?.let { v2 ->
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = v2,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = registry.unit2?.symbol ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                            )
                        }
                    }

                    variation?.let { v ->
                        val color = if (v > 0) {
                            MaterialTheme.extendedColors.trendIncrease
                        } else if (v < 0) {
                            MaterialTheme.extendedColors.trendDecrease
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = recordItem.formattedVariation(registry, v) ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun RecordCardPreview() {
    val registry = Registry(
        name = "Peso",
        emoji = "⚖️",
        unit1 = MeasureUnit("Kilo", "kg", 1)
    )
    val record = RecordItem(value1 = 70.5, dateKey = "2024-03-20")
    PersonalRegistryTheme {
        RecordCard(
            recordItem = record,
            registry = registry,
            onDelete = {},
            variation = -0.5
        )
    }
}

@ThemePreviews
@Composable
fun RecordCardDoublePreview() {
    val registry = Registry(
        name = "Running",
        emoji = "🏃",
        unit1 = MeasureUnit("Distancia", "km", 2),
        unit2 = MeasureUnit("Tiempo", "min", 0)
    )
    val record = RecordItem(value1 = 5.23, value2 = 25.0, dateKey = "2024-03-20")
    PersonalRegistryTheme {
        RecordCard(
            recordItem = record,
            registry = registry,
            onDelete = {},
            variation = 0.1
        )
    }
}
