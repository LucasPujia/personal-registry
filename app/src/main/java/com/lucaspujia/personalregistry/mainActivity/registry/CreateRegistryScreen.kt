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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
) {
    fun getCurrentEmoji(): String {
        return if (isEmojiMode) emoji else icon
    }
}


// TODO: mover cada componente a archivos separados para mejorar legibilidad
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRegistryScreenContent(
    onBack: () -> Unit,
    onCreateRegistry: (Registry) -> Unit,
    existingRegistries: List<Registry> = emptyList()
) {
    var registryName by remember { mutableStateOf("") }

    var emojiState by remember { mutableStateOf(EmojiState("", "",
        isEmojiMode = false,
        showIconSelector = false
    )) }
    val setEmojiState = { it: EmojiState -> emojiState = it }

    var unit1 by remember { mutableStateOf(MeasureUnitInput("", "", 1)) }
    var unit2Enabled by remember { mutableStateOf(false) }
    var unit2 by remember { mutableStateOf(MeasureUnitInput("", "", 1)) }
    val setUnit1 = { it: MeasureUnitInput -> unit1 = it }
    val setUnit2 = { it: MeasureUnitInput -> unit2 = it }

    var formula by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val isDuplicateName = remember(registryName, existingRegistries) {
        existingRegistries.any { it.name.equals(registryName.trim(), ignoreCase = true) }
    }
    val isDuplicateEmoji = remember(emojiState.emoji, existingRegistries) {
        existingRegistries.any { it.emoji == emojiState.emoji.trim() }
    }

    val suggestedWeightName = stringResource(R.string.weight)
    val suggestedWeightUnitName = stringResource(R.string.kilogram)
    val suggestedMoneyName = stringResource(R.string.money)
    val suggestedMoneyUnitName = stringResource(R.string.currency)

    if (emojiState.showIconSelector && emojiState.icon.isEmpty()) {
        IconSelectorDialog(
            selectedIcon = emojiState.icon,
            onIconSelected = {
                emojiState = emojiState.copy(icon = it, showIconSelector = false)
            },
            onDismissRequest = { emojiState = emojiState.copy(showIconSelector = false) }
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
                        registryName = suggestedWeightName
                        emojiState = emojiState.copy(icon = "Scale", isEmojiMode = false)
                        unit1 = MeasureUnitInput(suggestedWeightUnitName, "kg", 1)
                        unit2Enabled = false
                    }
                )
                SuggestionChip(
                    name = suggestedMoneyName,
                    emoji = "Money",
                    onClick = {
                        registryName = suggestedMoneyName
                        emojiState = emojiState.copy(icon = "Money", isEmojiMode = false)
                        unit1 = MeasureUnitInput(suggestedMoneyUnitName, "$", 2)
                        unit2Enabled = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = registryName,
                onValueChange = { registryName = it },
                label = { Text(stringResource(R.string.registry_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = isDuplicateName,
                supportingText = if (isDuplicateName) {
                    { Text(stringResource(R.string.duplicate_name_error)) }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            EmojiChipsSelector(emojiState, setEmojiState, coroutineScope, focusRequester)

            Spacer(modifier = Modifier.height(8.dp))

            val isEmojiTooLong = emojiState.isEmojiMode && emojiState.emoji.isNotEmpty() && run {
                try {
                    emojiState.emoji.codePointCount(0, emojiState.emoji.length) > 1
                } catch (_: Exception) {
                    false
                }
            }

            // TODO: simplificar parametros
            EmojiSelector(
                emojiState,
                setEmojiState,
                coroutineScope,
                shakeOffset,
                focusRequester,
                isDuplicateEmoji,
                isEmojiTooLong,
            )

            Spacer(modifier = Modifier.height(24.dp))
            MeasureUnitForm(unit1, setUnit1, 1)

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = unit2Enabled, onCheckedChange = { unit2Enabled = it })
                Text(stringResource(R.string.unit2_enabled))
            }

            if (unit2Enabled) {
                Spacer(modifier = Modifier.height(8.dp))
                MeasureUnitForm(unit2, setUnit2, 2)

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = formula,
                    onValueChange = { formula = it },
                    label = { Text(stringResource(R.string.formula_desc)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val registry = Registry(
                        name = registryName.trim(),
                        emoji = emojiState.emoji.trim(),
                        unit1 = unit1.toMeasureUnit(),
                        unit2 = if (unit2Enabled) unit2.toMeasureUnit() else null,
                        formula = if (unit2Enabled) formula.trim() else null
                    )
                    onCreateRegistry(registry)
                },
                modifier = Modifier.fillMaxWidth(),
                // TODO: extraer esta lógica a una función para mejorar legibilidad
                enabled = registryName.isNotBlank() && emojiState.getCurrentEmoji().isNotBlank() && unit1.hasValidData() && (!unit2Enabled || unit2.hasValidData())
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
private fun MeasureUnitForm(
    unit1: MeasureUnitInput,
    setUnit1: (MeasureUnitInput) -> Unit,
    unitNumber: Int
) {
    Text("${stringResource(R.string.unit)} $unitNumber", style = MaterialTheme.typography.titleMedium)

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

@Composable
private fun EmojiSelector(
    emojiState: EmojiState,
    setEmojiState: (EmojiState) -> Unit,
    coroutineScope: CoroutineScope,
    shakeOffset: Animatable<Float, AnimationVector1D>,
    focusRequester: FocusRequester,
    isDuplicateEmoji: Boolean,
    isEmojiTooLong: Boolean,
) {
    OutlinedTextField(
        value = emojiState.getCurrentEmoji(),
        onValueChange = { newValue ->
            if (emojiState.isEmojiMode) {
                val codePointCount = try {
                    newValue.codePointCount(0, newValue.length)
                } catch (_: Exception) {
                    0
                }

                if (codePointCount <= 1) {
                    setEmojiState(emojiState.copy(emoji = newValue))
                } else {
                    // Filtro automático: si pegan algo largo, tomamos solo el primer code point
                    try {
                        val firstCodePoint = newValue.codePointAt(0)
                        setEmojiState(
                            emojiState.copy(
                                emoji = String(Character.toChars(firstCodePoint))
                            )
                        )
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
        readOnly = !emojiState.isEmojiMode,
        label = { Text(stringResource(R.string.registry_icon)) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        isError = isDuplicateEmoji || isEmojiTooLong,
        placeholder = if (emojiState.isEmojiMode) {
            { Text(stringResource(R.string.emoji_hint)) }
        } else null,
        supportingText = when {
            isDuplicateEmoji -> {
                { Text(stringResource(R.string.duplicate_icon_error)) }
            }

            isEmojiTooLong -> {
                { Text(stringResource(R.string.emoji_length_error)) }
            }

            emojiState.isEmojiMode -> {
                { Text(stringResource(R.string.emoji_hint)) }
            }

            else -> null
        },
        trailingIcon = if (!emojiState.isEmojiMode) {
            {
                IconButton(onClick = {
                    setEmojiState(emojiState.copy(showIconSelector = true))
                }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.select_icon)
                    )
                }
            }
        } else null,
        leadingIcon = {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                RegistryIcon(iconIdentifier = emojiState.getCurrentEmoji(), contentDescription = null)
            }
        }
    )
}

@Composable
private fun EmojiChipsSelector(
    emojiState: EmojiState,
    setEmojiState: (EmojiState) -> Unit,
    coroutineScope: CoroutineScope,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip( // ICON CHIP
            selected = !emojiState.isEmojiMode,
            onClick = {
                if (emojiState.isEmojiMode) {
                    setEmojiState(emojiState.copy(isEmojiMode = false, showIconSelector = true))
                }
            },
            label = { Text(stringResource(R.string.use_icon)) },
            leadingIcon = if (!emojiState.isEmojiMode) {
                {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
        FilterChip( // EMOJI CHIP
            selected = emojiState.isEmojiMode,
            onClick = {
                if (!emojiState.isEmojiMode) {
                    setEmojiState(emojiState.copy(isEmojiMode = true, showIconSelector = false))
                    coroutineScope.launch {
                        // Pequeño delay para asegurar que el campo es editable antes de pedir el foco
                        focusRequester.requestFocus()
                    }
                }
            },
            label = { Text(stringResource(R.string.use_emoji)) },
            leadingIcon = if (emojiState.isEmojiMode) {
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
