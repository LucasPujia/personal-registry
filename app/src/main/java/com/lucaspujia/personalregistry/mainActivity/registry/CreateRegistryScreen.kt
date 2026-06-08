package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.mainActivity.RegistryEditorState
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews
import com.lucaspujia.personalregistry.utils.RegistryIcon
import com.lucaspujia.personalregistry.utils.defaultMoneyRegistry
import com.lucaspujia.personalregistry.utils.defaultWeightRegistry

/**
 * Pantalla para la creación o edición de un registro.
 */
@Composable
fun CreateRegistryScreen() {
    val mainViewModel = LocalMainActivityActions.current
    val registries by mainViewModel.allRegistries.collectAsState(initial = emptyList())
    val editorState = mainViewModel.registryEditorState
    val registryToEdit = (editorState as? RegistryEditorState.Edit)?.registry

    CreateRegistryScreenContent(
        onBack = { mainViewModel.registryEditorState = RegistryEditorState.Closed },
        onSaveRegistry = { registry ->
            if (registryToEdit != null) {
                mainViewModel.updateRegistry(registry.copy(id = registryToEdit.id))
            } else {
                mainViewModel.createRegistry(registry)
            }
        },
        existingRegistries = registries,
        registryToEdit = registryToEdit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRegistryScreenContent(
    onBack: () -> Unit,
    onSaveRegistry: (Registry) -> Unit,
    existingRegistries: List<Registry> = emptyList(),
    registryToEdit: Registry? = null
) {
    var registryName by remember(registryToEdit) { mutableStateOf(registryToEdit?.name ?: "") }
    
    var emojiState by remember(registryToEdit) {
        val isIcon = registryToEdit?.emoji?.startsWith(":") == true
        mutableStateOf(EmojiState(
            emoji = if (!isIcon) registryToEdit?.emoji ?: "" else "",
            icon = if (isIcon) registryToEdit?.emoji ?: "" else "",
            isEmojiMode = !isIcon,
            showIconSelector = false
        ))
    }
    
    var unit1 by remember(registryToEdit) {
        mutableStateOf(registryToEdit?.unit1?.let { MeasureUnitInput(it.name, it.symbol, it.precision) } ?: MeasureUnitInput("", "", 1))
    }
    var unit2Enabled by remember(registryToEdit) { mutableStateOf(registryToEdit?.unit2 != null) }
    var unit2 by remember(registryToEdit) {
        mutableStateOf(registryToEdit?.unit2?.let { MeasureUnitInput(it.name, it.symbol, it.precision) } ?: MeasureUnitInput("", "", 1))
    }
    
    // Estado de la fórmula (manejado vía teclado personalizado)
    var formulaValue by remember(registryToEdit) { mutableStateOf(TextFieldValue(registryToEdit?.formula ?: "")) }
    var showFormulaKeyboard by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isDuplicateName = existingRegistries.any { it.id != registryToEdit?.id && it.name.equals(registryName.trim(), ignoreCase = true) }
    val isDuplicateEmoji = existingRegistries.any { it.id != registryToEdit?.id && it.emoji == emojiState.getCurrentEmoji().trim() }

    if (emojiState.showIconSelector && emojiState.icon.isEmpty()) {
        IconSelectorDialog(
            selectedIcon = emojiState.icon,
            onIconSelected = { emojiState = emojiState.copy(icon = it, showIconSelector = false) },
            onDismissRequest = { emojiState = emojiState.copy(showIconSelector = false) }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(if (registryToEdit != null) R.string.edit_registry else R.string.create_registry)) },
                    navigationIcon = {
                        if (existingRegistries.isNotEmpty()) IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                SuggestedRegistriesSection { suggestion ->
                    registryName = suggestion.name
                    emojiState = emojiState.copy(icon = suggestion.emoji, isEmojiMode = false)
                    unit1 = MeasureUnitInput(suggestion.unit1.name, suggestion.unit1.symbol, suggestion.unit1.precision)
                    unit2Enabled = false
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = registryName,
                    onValueChange = { registryName = it },
                    label = { Text(stringResource(R.string.registry_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isDuplicateName,
                    supportingText = if (isDuplicateName) { { Text(stringResource(R.string.duplicate_name_error)) } } else null
                )

                Spacer(modifier = Modifier.height(16.dp))

            // EmojiSelectorForm "absorbe" el focus y EmojiChipsSelector lo activa
                val focusRequester = remember { FocusRequester() }
                EmojiChipsSelector(emojiState, { emojiState = it }, focusRequester)
                Spacer(modifier = Modifier.height(8.dp))
                EmojiSelectorForm(emojiState, { emojiState = it }, focusRequester, isDuplicateEmoji)

                Spacer(modifier = Modifier.height(24.dp))
                
                MeasureUnitForm(unit1, { unit1 = it }, 1)

                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = unit2Enabled, onCheckedChange = { unit2Enabled = it })
                    Text(stringResource(R.string.unit2_enabled))
                }

                if (unit2Enabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MeasureUnitForm(unit2, { unit2 = it }, 2)

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    FormulaEntryField(
                        value = formulaValue,
                        u1Symbol = unit1.symbol,
                        u2Symbol = unit2.symbol,
                        onClick = { 
                            showFormulaKeyboard = true
                            focusManager.clearFocus() 
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                val isValid = !isDuplicateName && !isDuplicateEmoji && !emojiState.isEmojiTooLong() &&
                              unit1.hasValidData() && (!unit2Enabled || unit2.hasValidData()) && 
                              registryName.isNotBlank() && emojiState.getCurrentEmoji().isNotBlank()
                
                Button(
                    onClick = {
                        onSaveRegistry(Registry(
                            name = registryName.trim(),
                            emoji = emojiState.getCurrentEmoji().trim(),
                            unit1 = unit1.toMeasureUnit(),
                            unit2 = if (unit2Enabled) unit2.toMeasureUnit() else null,
                            formula = if (unit2Enabled) formulaValue.text.trim() else null
                        ))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isValid
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(if (registryToEdit != null) R.string.save_changes else R.string.create_registry))
                }
            }
        }

        FormulaKeyboardWrapper(
            visible = showFormulaKeyboard,
            value = formulaValue,
            onValueChange = { formulaValue = it },
            onClose = { showFormulaKeyboard = false },
            unit1Symbol = unit1.symbol,
            unit2Symbol = unit2.symbol
        )
    }
}

@Composable
private fun SuggestedRegistriesSection(onSelect: (Registry) -> Unit) {
    Text(stringResource(R.string.suggested_registries), style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(defaultWeightRegistry(), defaultMoneyRegistry()).forEach {
            SuggestionChip(name = it.name, emoji = it.emoji, onClick = { onSelect(it) })
        }
    }
}

@Composable
private fun FormulaEntryField(value: TextFieldValue, u1Symbol: String, u2Symbol: String, onClick: () -> Unit) {
    OutlinedTextField(
        value = value.copy(
            text = value.text
                .replace("v1", u1Symbol.ifBlank { "v1" })
                .replace("v2", u2Symbol.ifBlank { "v2" })
        ),
        onValueChange = { },
        label = { Text(stringResource(R.string.formula_desc)) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect { if (it is PressInteraction.Release) onClick() }
            }
        }
    )
}

@Composable
private fun SuggestionChip(name: String, emoji: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RegistryIcon(iconIdentifier = emoji, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@ThemePreviews
@Composable
fun CreateRegistryScreenPreview() {
    PersonalRegistryTheme {
        CreateRegistryScreenContent(onBack = {}, onSaveRegistry = {}, existingRegistries = emptyList())
    }
}
