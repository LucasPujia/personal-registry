package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun CreateRegistryScreen() {
    val mainViewModel = LocalMainActivityActions.current
    val registries by mainViewModel.allRegistries.collectAsState(initial = emptyList())
    val editorState = mainViewModel.registryEditorState

    val registryToEdit = (editorState as? RegistryEditorState.Edit)?.registry

    CreateRegistryScreenContent(
        onBack = { mainViewModel.registryEditorState = RegistryEditorState.Closed },
        onSaveRegistry = {
            if (registryToEdit != null) {
                mainViewModel.updateRegistry(it.copy(id = registryToEdit.id))
            } else {
                mainViewModel.createRegistry(it)
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
        val isIcon: Boolean? = registryToEdit?.emoji?.startsWith(":")
        mutableStateOf(EmojiState(
            emoji = if (isIcon == false) registryToEdit.emoji else "",
            icon = if (isIcon == true) registryToEdit.emoji else "",
            isEmojiMode = isIcon == false,
            showIconSelector = false
        ))
    }
    val setEmojiState = { it: EmojiState -> emojiState = it }

    var unit1 by remember(registryToEdit) {
        mutableStateOf(
            registryToEdit?.unit1?.let { MeasureUnitInput(it.name, it.symbol, it.precision) }
                ?: MeasureUnitInput("", "", 1)
        )
    }
    var unit2Enabled by remember(registryToEdit) { mutableStateOf(registryToEdit?.unit2 != null) }
    var unit2 by remember(registryToEdit) {
        mutableStateOf(
            registryToEdit?.unit2?.let { MeasureUnitInput(it.name, it.symbol, it.precision) }
                ?: MeasureUnitInput("", "", 1)
        )
    }
    val setUnit1 = { it: MeasureUnitInput -> unit1 = it }
    val setUnit2 = { it: MeasureUnitInput -> unit2 = it }

    var formula by remember(registryToEdit) { mutableStateOf(registryToEdit?.formula ?: "") }

    val isDuplicateName = remember(registryName, existingRegistries, registryToEdit) {
        existingRegistries.any {
            it.id != registryToEdit?.id && it.name.equals(registryName.trim(), ignoreCase = true)
        }
    }
    val isDuplicateEmoji = remember(emojiState.getCurrentEmoji(), existingRegistries, registryToEdit) {
        existingRegistries.any {
            it.id != registryToEdit?.id && it.emoji == emojiState.getCurrentEmoji().trim()
        }
    }

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
                title = {
                    Text(
                        if (registryToEdit != null) stringResource(R.string.edit_registry)
                        else stringResource(R.string.create_registry)
                    )
                },
                navigationIcon = {
                    if (existingRegistries.isNotEmpty()) IconButton(onClick = onBack) {
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
                listOf(defaultWeightRegistry(), defaultMoneyRegistry()).forEach {
                    SuggestionChip(
                        name = it.name,
                        emoji = it.emoji,
                        onClick = {
                            registryName = it.name
                            emojiState = emojiState.copy(icon = it.emoji, isEmojiMode = false)
                            unit1 = MeasureUnitInput(it.unit1.name, it.unit1.symbol, it.unit1.precision)
                            unit2Enabled = false
                        }
                    )
                }
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

            // EmojiSelectorForm "absorbe" el focus y EmojiChipsSelector lo activa
            val focusRequester = remember { FocusRequester() }
            EmojiChipsSelector(emojiState, setEmojiState, focusRequester)

            Spacer(modifier = Modifier.height(8.dp))

            EmojiSelectorForm(emojiState, setEmojiState, focusRequester, isDuplicateEmoji)

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

            val hasNoDuplicateData = !isDuplicateName && !isDuplicateEmoji && !emojiState.isEmojiTooLong()
            val hasValidUnits = unit1.hasValidData() && (!unit2Enabled || unit2.hasValidData())
            Button(
                onClick = {
                    val registry = Registry(
                        name = registryName.trim(),
                        emoji = emojiState.getCurrentEmoji().trim(),
                        unit1 = unit1.toMeasureUnit(),
                        unit2 = if (unit2Enabled) unit2.toMeasureUnit() else null,
                        formula = if (unit2Enabled) formula.trim() else null
                    )
                    onSaveRegistry(registry)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasNoDuplicateData && hasValidUnits && registryName.isNotBlank() && emojiState.getCurrentEmoji().isNotBlank()

            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (registryToEdit != null) stringResource(R.string.save_changes)
                    else stringResource(R.string.create_registry)
                )
            }
        }
    }
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
        CreateRegistryScreenContent(
            onBack = {},
            onSaveRegistry = {},
            existingRegistries = emptyList()
        )
    }
}
