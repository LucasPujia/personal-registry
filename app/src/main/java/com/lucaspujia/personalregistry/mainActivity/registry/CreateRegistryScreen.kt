package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.IconCategory
import com.lucaspujia.personalregistry.utils.RegistryIcon
import com.lucaspujia.personalregistry.utils.RegistryIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CreateRegistryScreen() {
    val mainViewModel = LocalMainActivityActions.current
    val registries by mainViewModel.allRegistries.collectAsState(initial = emptyList())

    CreateRegistryScreenContent(
        onBack = { mainViewModel.createRegistryOpened = false },
        onCreateRegistry = { mainViewModel.createRegistry(it) },
        existingRegistries = registries
    )
}

data class MeasureUnitInput(
    val name: String,
    val symbol: String,
    val precision: Int
) {
    fun hasValidData(): Boolean {
        return name.isNotBlank() && symbol.isNotBlank() && precision >= 0
    }

    fun toMeasureUnit(): MeasureUnit {
        return MeasureUnit(name.trim(), symbol.trim(), precision)
    }
}

data class EmojiState(
    val emoji: String,
    val icon: String,
    val isEmojiMode: Boolean,
    val showIconSelector: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRegistryScreenContent(
    onBack: () -> Unit,
    onCreateRegistry: (Registry) -> Unit,
    existingRegistries: List<Registry> = emptyList()
) {
    var name by remember { mutableStateOf("") }

//    var emojiState by remember { mutableStateOf(EmojiState("Scale", "", false, false)) }
    var emoji by remember { mutableStateOf("Scale") }
    var isEmojiMode by remember { mutableStateOf(false) }
    var showIconSelector by remember { mutableStateOf(false) }
    val setEmoji = { it: String -> emoji = it}
    val toggleEmojiMode = { isEmojiMode = !isEmojiMode }
    val toggleIconSelector = { showIconSelector = !showIconSelector }

    var unit1 by remember { mutableStateOf(MeasureUnitInput("", "", 1)) }
    var unit2Enabled by remember { mutableStateOf(false) }
    var unit2 by remember { mutableStateOf(MeasureUnitInput("", "", 1)) }

    var formula by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val isDuplicateName = remember(name, existingRegistries) {
        existingRegistries.any { it.name.equals(name.trim(), ignoreCase = true) }
    }
    val isDuplicateEmoji = remember(emoji, existingRegistries) {
        existingRegistries.any { it.emoji == emoji.trim() }
    }

    val suggestedWeightName = stringResource(R.string.weight)
    val suggestedWeightUnitName = stringResource(R.string.kilogram)
    val suggestedMoneyName = stringResource(R.string.money)
    val suggestedMoneyUnitName = stringResource(R.string.currency)

    if (showIconSelector) {
        IconSelectorDialog(
            selectedIcon = emoji,
            onIconSelected = {
                emoji = it
                toggleIconSelector()
            },
            onDismissRequest = toggleIconSelector
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_registry)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.suggested_registries),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    name = suggestedWeightName,
                    emoji = "Scale",
                    onClick = {
                        name = suggestedWeightName
                        emoji = "Scale"
                        toggleEmojiMode()
                        unit1 = MeasureUnitInput(suggestedWeightUnitName, "kg", 1)
                        unit2Enabled = false
                    }
                )
                SuggestionChip(
                    name = suggestedMoneyName,
                    emoji = "Money",
                    onClick = {
                        name = suggestedMoneyName
                        emoji = "Money"
                        toggleEmojiMode()
                        unit1 = MeasureUnitInput(suggestedMoneyUnitName, "$", 2)
                        unit2Enabled = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.registry_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = isDuplicateName,
                supportingText = if (isDuplicateName) {
                    { Text(stringResource(R.string.duplicate_name_error)) }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            EmojiChipsSelector(
                isEmojiMode,
                toggleEmojiMode,
                toggleIconSelector,
                coroutineScope,
                focusRequester
            )

            Spacer(modifier = Modifier.height(8.dp))

            val isEmojiTooLong = isEmojiMode && emoji.isNotEmpty() && run {
                try {
                    emoji.codePointCount(0, emoji.length) > 1
                } catch (_: Exception) {
                    false
                }
            }

            EmojiSelector(
                emoji,
                isEmojiMode,
                setEmoji,
                coroutineScope,
                shakeOffset,
                focusRequester,
                isDuplicateEmoji,
                isEmojiTooLong,
                toggleIconSelector
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.unit) + " 1", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = unit1.name,
                onValueChange = { unit1 = unit1.copy(name = it) },
                label = { Text(stringResource(R.string.unit_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = unit1.symbol,
                    onValueChange = { unit1 = unit1.copy(symbol = it) },
                    label = { Text(stringResource(R.string.symbol)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = unit1.precision.toString(),
                    onValueChange = { if (it.all { c -> c.isDigit() }) unit1 = unit1.copy(precision = it.toIntOrNull() ?: 1) },
                    label = { Text(stringResource(R.string.precision)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = unit2Enabled, onCheckedChange = { unit2Enabled = it })
                Text(stringResource(R.string.unit2_enabled))
            }

            if (unit2Enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.unit) + " 2", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = unit2.name,
                    onValueChange = { unit2 = unit2.copy(name = it) },
                    label = { Text(stringResource(R.string.unit_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = unit2.symbol,
                        onValueChange = { unit2 = unit2.copy(symbol = it) },
                        label = { Text(stringResource(R.string.symbol)) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit2.precision.toString(),
                        onValueChange = { if (it.all { c -> c.isDigit() }) unit2 = unit2.copy(precision = it.toIntOrNull() ?: 1) },
                        label = { Text(stringResource(R.string.precision)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    label = { Text(stringResource(R.string.formula_desc)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val registry = Registry(
                        name = name.trim(),
                        emoji = emoji.trim(),
                        unit1 = unit1.toMeasureUnit(),
                        unit2 = if (unit2Enabled) unit2.toMeasureUnit() else null,
                        formula = if (unit2Enabled) formula.trim() else null
                    )
                    onCreateRegistry(registry)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && unit1.hasValidData() && (!unit2Enabled || unit2.hasValidData())
                        && !isDuplicateName && !isDuplicateEmoji && !isEmojiTooLong
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.create_registry))
            }
        }
    }
}

@Composable
private fun EmojiSelector(
    emoji: String,
    isEmojiMode: Boolean,
    setEmoji: (String) -> Unit,
    coroutineScope: CoroutineScope,
    shakeOffset: Animatable<Float, AnimationVector1D>,
    focusRequester: FocusRequester,
    isDuplicateEmoji: Boolean,
    isEmojiTooLong: Boolean,
    toggleIconSelector: () -> Unit
) {
    OutlinedTextField(
        value = emoji,
        onValueChange = { newValue ->
            if (isEmojiMode) {
                val codePointCount = try {
                    newValue.codePointCount(0, newValue.length)
                } catch (_: Exception) {
                    0
                }

                if (codePointCount <= 1) {
                    setEmoji(newValue)
                } else {
                    // Filtro automático: si pegan algo largo, tomamos solo el primer code point
                    try {
                        val firstCodePoint = newValue.codePointAt(0)
                        setEmoji(String(Character.toChars(firstCodePoint)))
                        // Trigger shake animation on automatic clipping
                        coroutineScope.launch {
                            shakeOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.2f, stiffness = 1000f),
                                initialVelocity = 100f
                            )
                        }
                    } catch (_: Exception) {
                        // Fallback
                    }
                }
            }
        },
        readOnly = !isEmojiMode,
        label = { Text(stringResource(R.string.registry_icon)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        isError = isDuplicateEmoji || isEmojiTooLong,
        placeholder = if (isEmojiMode) {
            { Text(stringResource(R.string.emoji_hint)) }
        } else null,
        supportingText = when {
            isDuplicateEmoji -> {
                { Text(stringResource(R.string.duplicate_icon_error)) }
            }

            isEmojiTooLong -> {
                { Text(stringResource(R.string.emoji_length_error)) }
            }

            isEmojiMode -> {
                { Text(stringResource(R.string.emoji_hint)) }
            }

            else -> null
        },
        trailingIcon = if (!isEmojiMode) {
            {
                IconButton(onClick = toggleIconSelector) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.select_icon)
                    )
                }
            }
        } else null,
        leadingIcon = {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                RegistryIcon(iconIdentifier = emoji, contentDescription = null)
            }
        }
    )
}

@Composable
private fun EmojiChipsSelector(
    isEmojiMode: Boolean,
    toggleEmojiMode: () -> Unit,
    toggleIconSelector: () -> Unit,
    coroutineScope: CoroutineScope,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = !isEmojiMode,
            onClick = {
                if (isEmojiMode) {
                    toggleEmojiMode()
//                            emoji = "Scale"
                    toggleIconSelector()
                }
            },
            label = { Text(stringResource(R.string.use_icon)) },
            leadingIcon = if (!isEmojiMode) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
        FilterChip(
            selected = isEmojiMode,
            onClick = {
                if (!isEmojiMode) {
                    toggleEmojiMode()
//                            emoji = ""
                    coroutineScope.launch {
                        // Pequeño delay para asegurar que el campo es editable antes de pedir el foco
                        focusRequester.requestFocus()
                    }
                }
            },
            label = { Text(stringResource(R.string.use_emoji)) },
            leadingIcon = if (isEmojiMode) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
    }
}

@Composable
private fun IconSelectorDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(IconCategory.ALL) }

    val filteredIcons = remember(searchQuery, selectedCategory) {
        RegistryIcons.icons.filter {
            (selectedCategory == IconCategory.ALL || it.category == selectedCategory) &&
            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.select_icon)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_icons)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedCategory.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    IconCategory.entries.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { Text(stringResource(category.labelRes)) }
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredIcons) { iconInfo ->
                        val isSelected = iconInfo.name == selectedIcon
                        Card(
                            onClick = { onIconSelected(iconInfo.name) },
                            modifier = Modifier.aspectRatio(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                               else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(MaterialTheme.colorScheme.primary)
                            ) else null
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                RegistryIcon(
                                    iconIdentifier = iconInfo.name, 
                                    contentDescription = iconInfo.name,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun SuggestionChip(
    name: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RegistryIcon(iconIdentifier = emoji, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@ThemePreviews
@Composable
fun CreateRegistryScreenPreview() {
    PersonalRegistryTheme {
        CreateRegistryScreenContent(
            onBack = {},
            onCreateRegistry = {},
            existingRegistries = emptyList()
        )
    }
}
