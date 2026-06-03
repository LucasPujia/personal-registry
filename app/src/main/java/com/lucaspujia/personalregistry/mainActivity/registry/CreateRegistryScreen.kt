package com.lucaspujia.personalregistry.mainActivity.registry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lucaspujia.personalregistry.R
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRegistryScreen() {
    val viewModel = LocalMainActivityActions.current
    
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📊") }
    
    var unit1Name by remember { mutableStateOf("") }
    var unit1Symbol by remember { mutableStateOf("") }
    var unit1Precision by remember { mutableStateOf("1") }
    
    var unit2Enabled by remember { mutableStateOf(false) }
    var unit2Name by remember { mutableStateOf("") }
    var unit2Symbol by remember { mutableStateOf("") }
    var unit2Precision by remember { mutableStateOf("1") }
    
    var formula by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_registry)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.createRegistryOpened = false }) {
                        // TODO: checkk
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.registry_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = emoji,
                onValueChange = { emoji = it },
                label = { Text(stringResource(R.string.registry_emoji)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.unit) + " 1", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = unit1Name,
                onValueChange = { unit1Name = it },
                label = { Text(stringResource(R.string.unit_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = unit1Symbol,
                    onValueChange = { unit1Symbol = it },
                    label = { Text(stringResource(R.string.symbol)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = unit1Precision,
                    onValueChange = { if (it.all { c -> c.isDigit() }) unit1Precision = it },
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
                    value = unit2Name,
                    onValueChange = { unit2Name = it },
                    label = { Text(stringResource(R.string.unit_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = unit2Symbol,
                        onValueChange = { unit2Symbol = it },
                        label = { Text(stringResource(R.string.symbol)) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = unit2Precision,
                        onValueChange = { if (it.all { c -> c.isDigit() }) unit2Precision = it },
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
                        name = name,
                        emoji = emoji,
                        unit1 = MeasureUnit(unit1Name, unit1Symbol, unit1Precision.toIntOrNull() ?: 1),
                        unit2 = if (unit2Enabled) MeasureUnit(unit2Name, unit2Symbol, unit2Precision.toIntOrNull() ?: 1) else null,
                        formula = if (unit2Enabled) formula else null
                    )
                    viewModel.createRegistry(registry)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && unit1Symbol.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.create_registry))
            }
        }
    }
}

@ThemePreviews
@Composable
fun CreateRegistryScreenPreview() {
    PersonalRegistryTheme {
        CreateRegistryScreen()
    }
}
