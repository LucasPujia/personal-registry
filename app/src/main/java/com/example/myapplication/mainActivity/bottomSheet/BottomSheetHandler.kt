package com.example.myapplication.mainActivity.bottomSheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.example.myapplication.mainActivity.MainActivityViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetHandler(
    viewModel: MainActivityViewModel
) {
    if (viewModel.filtersOpened) {
        val filtersSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.filtersOpened = false },
            sheetState = filtersSheetState,
        ) {
            FiltersContent(
                viewModel = viewModel,
                onDismissRequest = { viewModel.filtersOpened = false }
            )
        }
    }

    if (viewModel.viewTogglesOpened) {
        val viewTogglesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.viewTogglesOpened = false },
            sheetState = viewTogglesSheetState,
        ) {
            ViewTogglesContent(
                viewModel = viewModel,
                onDismissRequest = { viewModel.viewTogglesOpened = false }
            )
        }
    }
}