package com.example.myapplication.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.database.weight.RoomWeightsStorage
import com.example.myapplication.mainActivity.bottomSheet.BottomSheetHandler


@Preview(showBackground = true)
@Composable
fun MyApplicationAppPreview() {
    MaterialTheme {
        val initialValues = listOf(61f, 60f, 58f, 62f)
        val memoryStorage = InMemoryWeightsStorage.fromFloats(initialValues)
        val mainActivityModel = MainActivityModel(memoryStorage)
        MyApplicationApp(MainActivityViewModel(mainActivityModel))
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel> {
        val database = AppDatabase.getInstance(applicationContext)
        val storage = RoomWeightsStorage(database.weightRecordDao())
        MainActivityViewModelFactory(MainActivityModel(storage))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme {
                MyApplicationApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp(viewModel: MainActivityViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .statusBarsPadding()
            .padding(16.dp),
    ) {
        WeightSelector(viewModel)
        if (viewModel.filters.weights.isNotEmpty()) WeightsViewer(viewModel)
    }

    BottomSheetHandler(viewModel)
}


