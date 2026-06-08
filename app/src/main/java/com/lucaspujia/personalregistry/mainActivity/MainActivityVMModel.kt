package com.lucaspujia.personalregistry.mainActivity

import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.utils.lastMonthRange
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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

    fun switchRegistry(registry: Registry)
    fun addRecord(value1: Double, value2: Double?, pickerMillis: Long?)
    fun removeRecord(recordItem: RecordItem)
    fun isSelectableDate(utcTimeMillis: Long): Boolean
    fun applyFilters(
        minViewValue: Int? = null,
        maxViewValue: Int? = null,
        goalValue: Int? = null,
        dateRange: Pair<Long, Long>? = lastMonthRange(),
    ): Int?
    fun applyViewToggles(showGraph: Boolean, showList: Boolean)
    fun updateTimeRange(range: TimeRange)
    fun createRegistry(registry: Registry)
    fun updateRegistry(registry: Registry)
    fun deleteRegistry(registry: Registry)
}

data class ActiveFilters(
    val minViewValue: Int = 0,
    val maxViewValue: Int = 100,
    val records: List<RecordItem> = emptyList(),
    val dateLabels: List<String> = emptyList(),
    val goalValue: Int? = null,
    val dateRange: Pair<Long, Long>? = null,
    val shouldAnimate: Boolean = true,
) {
    val values1F: List<Float> by lazy { records.map { it.value1.toFloat() } }
    val values1D: List<Double> by lazy { records.map { it.value1 } }
    val values2F: List<Float> by lazy { records.mapNotNull { it.value2?.toFloat() } }
}