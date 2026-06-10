package com.lucaspujia.personalregistry.mainActivity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.utils.lastMonthRange
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

enum class TimeRange(val label: String, val apply: (LocalDate) -> LocalDate) {
    DAYS_7("7D", { it.minusDays(7) }),
    DAYS_15("15D", { it.minusDays(15) }),
    MONTH_1("1M", { it.minusMonths(1) }),
    MONTH_3("3M", { it.minusMonths(3) }),
    MONTH_6("6M", { it.minusMonths(6) }),
    YEAR_1("1A", { it.minusYears(1) })
}

data class ViewToggles(
    val graph: Boolean = true,
    val list: Boolean = true,
)

sealed class RegistryEditorState {
    data object Closed : RegistryEditorState()
    data object New : RegistryEditorState()
    data class Edit(val registry: Registry) : RegistryEditorState()

    fun isClosed() = this is Closed
}

// TODO: revisar
sealed interface RegistryToast {
    val id: String
    val icon: ImageVector
    val textRes: Int
    val containerColor: ColorScheme.() -> Color
    val contentColor: ColorScheme.() -> Color

    data class Success(
        override val textRes: Int,
        override val id: String = UUID.randomUUID().toString()
    ) : RegistryToast {
        override val icon = Icons.Default.CheckCircle
        override val containerColor: ColorScheme.() -> Color = { primaryContainer }
        override val contentColor: ColorScheme.() -> Color = { onPrimaryContainer }
    }

    data class Error(
        override val textRes: Int,
        override val id: String = UUID.randomUUID().toString()
    ) : RegistryToast {
        override val icon = Icons.Default.Error
        override val containerColor: ColorScheme.() -> Color = { errorContainer }
        override val contentColor: ColorScheme.() -> Color = { onErrorContainer }
    }
}

interface MainActivityActions {
    val activeRegistry: Registry?
    val allRegistries: Flow<List<Registry>>
    val filters: ActiveFilters
    val viewToggles: ViewToggles
    val currentTimeRange: TimeRange?
    var filtersOpened: Boolean
    var viewTogglesOpened: Boolean
    var settingsOpened: Boolean
    var registryEditorState: RegistryEditorState
    val toasts: List<RegistryToast>

    fun switchRegistry(registry: Registry)
    fun addRecord(value1: Double, value2: Double?, pickerMillis: Long?)
    fun removeRecord(recordItem: RecordItem)
    fun isSelectableDate(utcTimeMillis: Long): Boolean
    fun applyFilters(
        minViewValue: Double? = null,
        maxViewValue: Double? = null,
        goalValue: Double? = null,
        dateRange: Pair<Long, Long>? = lastMonthRange(),
    ): Int?
    fun applyViewToggles(showGraph: Boolean, showList: Boolean)
    fun updateTimeRange(range: TimeRange)
    fun createRegistry(registry: Registry)
    fun updateRegistry(registry: Registry)
    fun deleteRegistry(registry: Registry)
    fun showToast(toast: RegistryToast)
    fun dismissToast(id: String)
}

data class ActiveFilters(
    val minViewValue: Double = 0.0,
    val maxViewValue: Double = 100.0,
    val records: List<RecordItem> = emptyList(),
    val dateLabels: List<String> = emptyList(),
    val goalValue: Double? = null,
    val dateRange: Pair<Long, Long>? = null,
    val shouldAnimate: Boolean = true,
    val calculatedValues: List<Double> = emptyList()
) {
//    val calculatedFloats: List<Float> by lazy { calculatedValues.map { it.toFloat() } }
}
