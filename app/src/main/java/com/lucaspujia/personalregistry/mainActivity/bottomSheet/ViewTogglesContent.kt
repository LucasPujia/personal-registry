package com.lucaspujia.personalregistry.mainActivity.bottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews

@Composable
fun ViewTogglesContent(
    onDismissRequest: () -> Unit,
) {
    val viewModel = LocalMainActivityActions.current
    ViewTogglesContentImpl(
        onDismissRequest = onDismissRequest,
        initialShowGraph = viewModel.viewToggles.graph,
        initialShowList = viewModel.viewToggles.list,
        onApplyViewToggles = { graph, list ->
            viewModel.applyViewToggles(graph, list)
        }
    )
}

@Composable
private fun ViewTogglesContentImpl(
    onDismissRequest: () -> Unit,
    initialShowGraph: Boolean,
    initialShowList: Boolean,
    onApplyViewToggles: (Boolean, Boolean) -> Unit
) {
    var showGraph by remember { mutableStateOf(initialShowGraph) }
    var showList by remember { mutableStateOf(initialShowList) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.view_graph), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = showGraph, onCheckedChange = { showGraph = it })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.view_list), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = showList, onCheckedChange = { showList = it })
        }

        Button(
            onClick = {
                onApplyViewToggles(showGraph, showList)
                onDismissRequest()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@ThemePreviews
@Composable
private fun ViewTogglesContentPreview() {
    PersonalRegistryTheme {
        ViewTogglesContentImpl(
            onDismissRequest = {},
            initialShowGraph = true,
            initialShowList = true,
            onApplyViewToggles = { _, _ -> }
        )
    }
}
