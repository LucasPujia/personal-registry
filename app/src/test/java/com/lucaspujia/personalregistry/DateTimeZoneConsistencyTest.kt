package com.lucaspujia.personalregistry

import com.lucaspujia.personalregistry.database.registry.InMemoryRecordsStorage
import com.lucaspujia.personalregistry.database.registry.InMemoryRegistriesStorage
import com.lucaspujia.personalregistry.database.registry.MeasureUnit
import com.lucaspujia.personalregistry.database.registry.Record
import com.lucaspujia.personalregistry.database.registry.Registry
import com.lucaspujia.personalregistry.mainActivity.MainActivityModel
import com.lucaspujia.personalregistry.mainActivity.MainActivityViewModel
import com.lucaspujia.personalregistry.mainActivity.recordItem.RecordItem
import com.lucaspujia.personalregistry.utils.dateKeyToLocalDate
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.fromDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.resolveDatePickerMonthYearText
import com.lucaspujia.personalregistry.utils.resolveDatePickerText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class DateTimeZoneConsistencyTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── dateKey es inmune a cambios de zona horaria ──────────────────────────

    @Test
    fun dateKey_isTheSameRegardlessOfTimezone() {
        val date = LocalDate.of(2030, 1, 15)
        val key = localDateToDateKey(date)

        withTimeZone("America/Los_Angeles") { assertEquals("2030-01-15", key) }
        withTimeZone("Asia/Tokyo") { assertEquals("2030-01-15", key) }
        withTimeZone("UTC") { assertEquals("2030-01-15", key) }
    }

    @Test
    fun dateKey_roundtrips_correctly() {
        val original = LocalDate.of(2030, 6, 15)
        val key = localDateToDateKey(original)
        val restored = dateKeyToLocalDate(key)

        assertEquals(original, restored)
    }

    // ─── Formateo del DatePicker ───────────────────────────────────────────────

    @Test
    fun datePickerText_keepsCalendarDayInNegativeOffsetZone() {
        withTimeZone("America/Los_Angeles") {
            val selected = LocalDate.of(2030, 1, 15)
            val pickerMillis = forDatePicker(selected)

            assertEquals(selected, fromDatePicker(pickerMillis))
            assertEquals("15/01", resolveDatePickerText(null, pickerMillis))
        }
    }

    @Test
    fun datePickerMonthText_keepsSelectedMonthInNegativeOffsetZone() {
        withTimeZone("America/Los_Angeles") {
            val selected = LocalDate.of(2030, 3, 1)
            val pickerMillis = forDatePicker(selected)
            val expected = selected.format(DateTimeFormatter.ofPattern("MMMM, yyyy"))
                .replaceFirstChar { it.uppercase() }

            assertEquals(expected, resolveDatePickerMonthYearText(null, pickerMillis))
        }
    }

    // ─── formattedDate usa dateKey, no zona horaria ───────────────────────────

    @Test
    fun formattedDate_isStableAcrossTimezones() {
        val record = RecordItem(
            value1 = 72.0,
            dateKey = "2030-01-15",
        )

        withTimeZone("America/Los_Angeles") { assertEquals("15/01", record.formattedDate()) }
        withTimeZone("Asia/Tokyo") { assertEquals("15/01", record.formattedDate()) }
        withTimeZone("UTC") { assertEquals("15/01", record.formattedDate()) }
    }

    // ─── isSelectableDate compara contra dateKey, no timestamps ───────────────

    @Test
    fun isSelectableDate_returnsFalse_whenDateKeyMatches() = runTest {
        withTimeZone("America/Los_Angeles") {
            val date = LocalDate.of(2030, 1, 15)
            val registry = Registry(id = 1, name = "Test", emoji = "⚖️", unit1 = MeasureUnit("kg", "kg"))
            
            val registriesStorage = InMemoryRegistriesStorage(listOf(registry))
            val recordsStorage = InMemoryRecordsStorage(listOf(
                Record(registryId = 1, value1 = 71.0, dateKey = "2030-01-15")
            ))

            val viewModel = MainActivityViewModel(MainActivityModel(registriesStorage, recordsStorage))

            assertFalse(viewModel.isSelectableDate(forDatePicker(date)))
        }
    }

    @Test
    fun isSelectableDate_returnsTrue_afterTravelingToAnotherTimezone() = runTest {
        // El usuario guardó un registro ayer estando en LA.
        // Al día siguiente viaja a Tokyo: ayer sigue siendo ayer (dateKey).
        val yesterday = nowTZ().minusDays(1)
        val today = nowTZ()

        val registry = Registry(id = 1, name = "Test", emoji = "⚖️", unit1 = MeasureUnit("kg", "kg"))
        val registriesStorage = InMemoryRegistriesStorage(listOf(registry))
        val recordsStorage = InMemoryRecordsStorage(listOf(
            Record(registryId = 1, value1 = 70.0, dateKey = localDateToDateKey(yesterday))
        ))

        withTimeZone("Asia/Tokyo") {
            val viewModel = MainActivityViewModel(MainActivityModel(registriesStorage, recordsStorage))

            // ayer sigue ocupado — no importa la zona
            assertFalse(viewModel.isSelectableDate(forDatePicker(yesterday)))
            // hoy está disponible
            assertTrue(viewModel.isSelectableDate(forDatePicker(today)))
        }
    }

    // ─── Filtrado por rango usa dateKey ────────────────────────────────────────

    @Test
    fun filterByRange_includesRecordsByDateKey() = runTest {
        val registry = Registry(id = 1, name = "Test", emoji = "⚖️", unit1 = MeasureUnit("kg", "kg"))
        val registriesStorage = InMemoryRegistriesStorage(listOf(registry))
        val recordsStorage = InMemoryRecordsStorage(listOf(
            Record(registryId = 1, value1 = 70.0, dateKey = "2030-01-14"),
            Record(registryId = 1, value1 = 71.0, dateKey = "2030-01-15"),
            Record(registryId = 1, value1 = 72.0, dateKey = "2030-01-16"),
        ))

        val viewModel = MainActivityViewModel(MainActivityModel(registriesStorage, recordsStorage))

        viewModel.applyFilters(
            minViewValue = 0,
            maxViewValue = 100,
            dateRange = forDatePicker(LocalDate.of(2030, 1, 14)) to
                        forDatePicker(LocalDate.of(2030, 1, 15))
        )

        assertEquals(listOf(70.0, 71.0), viewModel.filters.values1D)
    }

    @Test
    fun filterByRange_isStableAcrossTimezones() = runTest {
        val registry = Registry(id = 1, name = "Test", emoji = "⚖️", unit1 = MeasureUnit("kg", "kg"))
        val registriesStorage = InMemoryRegistriesStorage(listOf(registry))
        val recordsStorage = InMemoryRecordsStorage(listOf(
            Record(registryId = 1, value1 = 70.0, dateKey = "2030-01-14"),
            Record(registryId = 1, value1 = 71.0, dateKey = "2030-01-15"),
        ))

        val range = forDatePicker(LocalDate.of(2030, 1, 15)) to
                    forDatePicker(LocalDate.of(2030, 1, 15))

        listOf("America/Los_Angeles", "Asia/Tokyo", "UTC").forEach { tz ->
            withTimeZone(tz) {
                val viewModel = MainActivityViewModel(MainActivityModel(registriesStorage, recordsStorage))
                viewModel.applyFilters(minViewValue = 0, maxViewValue = 100, dateRange = range)
                assertEquals("Fallo en zona $tz", listOf(71.0), viewModel.filters.values1D)
            }
        }
    }

    // ─── addRecord sin fecha explícita guarda el día local correcto ────────────

    @Test
    fun addRecord_noDate_storesLocalTodayDateKey() = runTest {
        withTimeZone("Asia/Tokyo") {
            val todayInTokyo = nowTZ()
            val registryId = 1L
            val registriesStorage = InMemoryRegistriesStorage()
            val recordsStorage = InMemoryRecordsStorage()
            val model = MainActivityModel(registriesStorage, recordsStorage)
            
            runBlocking { 
                model.addRecord(registryId, 69.0, null, todayInTokyo)
                val records = recordsStorage.getRecordsByRegistry(registryId)
                assertEquals(1, records.size)
                assertEquals(localDateToDateKey(todayInTokyo), records.first().dateKey)
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun withTimeZone(zoneId: String, block: () -> Unit) {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
            block()
        } finally {
            TimeZone.setDefault(previous)
        }
    }

    private fun nowTZ(): LocalDate = LocalDate.now(java.time.ZoneId.systemDefault())
}
