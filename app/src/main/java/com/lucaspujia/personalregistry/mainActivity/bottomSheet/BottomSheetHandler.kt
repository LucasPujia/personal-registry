package com.lucaspujia.personalregistry.mainActivity.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.lucaspujia.personalregistry.mainActivity.LocalMainActivityActions
import com.lucaspujia.personalregistry.ui.theme.PersonalRegistryTheme
import com.lucaspujia.personalregistry.ui.theme.ThemePreviews


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetHandler() {
    val viewModel = LocalMainActivityActions.current
    BottomSheetHandlerContent(
        filtersOpened = viewModel.filtersOpened,
        onFiltersDismissRequest = { viewModel.filtersOpened = false },
        viewTogglesOpened = viewModel.viewTogglesOpened,
        onViewTogglesDismissRequest = { viewModel.viewTogglesOpened = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetHandlerContent(
    filtersOpened: Boolean,
    onFiltersDismissRequest: () -> Unit,
    viewTogglesOpened: Boolean,
    onViewTogglesDismissRequest: () -> Unit,
) {
    if (filtersOpened) {
        val filtersSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onFiltersDismissRequest,
            sheetState = filtersSheetState,
        ) {
            FiltersContent(
                onDismissRequest = onFiltersDismissRequest
            )
        }
    }

    if (viewTogglesOpened) {
        val viewTogglesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onViewTogglesDismissRequest,
            sheetState = viewTogglesSheetState,
        ) {
            ViewTogglesContent(
                onDismissRequest = onViewTogglesDismissRequest
            )
        }
    }
}

@ThemePreviews
@Composable
private fun FiltersBottomSheetPreview() {
    PersonalRegistryTheme {
        BottomSheetHandlerContent(
            filtersOpened = true,
            onFiltersDismissRequest = {},
            viewTogglesOpened = false,
            onViewTogglesDismissRequest = {},
        )
    }
}

@ThemePreviews
@Composable
private fun ViewTogglesBottomSheetPreview() {
    PersonalRegistryTheme {
        BottomSheetHandlerContent(
            filtersOpened = false,
            onFiltersDismissRequest = {},
            viewTogglesOpened = true,
            onViewTogglesDismissRequest = {},
        )
    }
}
