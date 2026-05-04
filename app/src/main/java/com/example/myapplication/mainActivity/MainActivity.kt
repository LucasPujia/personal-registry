package com.example.myapplication.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.weight.InMemoryWeightsStorage
import com.example.myapplication.database.weight.RoomWeightsStorage

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

@Composable
fun MyApplicationApp(viewModel: MainActivityViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .background(color = Color.White),
    ) {
        WeightSelector(viewModel)

        WeightsViewer(viewModel)
    }
}

@Composable
private fun WeightSelector(viewModel: MainActivityViewModel) {
    var weight by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .height(64.dp)
    ) {
        Text(text = "Agregar peso:")

        BasicTextField(
            value = weight,
            onValueChange = { weight = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .width(64.dp)
                .height(40.dp),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraSmall
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    if (weight.isEmpty() && LocalInspectionMode.current) {
                        Text("25", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    innerTextField()
                }
            }
        )

        FilledIconButton(
            onClick = {
                weight.toIntOrNull()?.let {
                    viewModel.addWeight(it)
                    weight = ""
                }
            },
            shape = CircleShape
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_input_add),
                contentDescription = "Agregar peso",
            )
        }
    }
}

@Composable
private fun WeightsViewer(viewModel: MainActivityViewModel) {
    LazyColumn {
        val weightsList = viewModel.weightsList
        items(weightsList.size) { index ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    text = "Peso ${index + 1}: ${weightsList[index]} kg",
                )

                IconButton(
                    onClick = { viewModel.removeWeight(index) },
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_delete),
                        contentDescription = "Eliminar peso",
                        tint = Color.Red
                    )
                }
            }
            if (index != weightsList.size - 1) HorizontalDivider(thickness = 2.dp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyApplicationAppPreview() {
    MaterialTheme {
        val initialValues = listOf(25, 30, 35)
        val memoryStorage = InMemoryWeightsStorage(initialValues)
        val mainActivityModel = MainActivityModel(memoryStorage)
        MyApplicationApp(MainActivityViewModel(mainActivityModel))
    }
}

