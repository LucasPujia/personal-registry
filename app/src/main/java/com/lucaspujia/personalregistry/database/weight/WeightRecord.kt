package com.lucaspujia.personalregistry.database.weight

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lucaspujia.personalregistry.mainActivity.weightItem.WeightItem
import com.lucaspujia.personalregistry.utils.dateKeyToLocalDate
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val WEIGHT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

/**
 * [dateKey] es la clave estable del día calendario ("YYYY-MM-DD").
 * Se fija en el momento del registro y NUNCA se recalcula a partir de la zona horaria.
 * Así, si el usuario viaja, sus registros siguen perteneciendo al día en que los creó.
 *
 * [createdAt] es solo para ordenación/auditoría, no se usa como identificador de día.
 */
@Entity(
    tableName = "weight_records",
    indices = [Index(value = ["dateKey"], unique = true)]
)
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Float,
    val dateKey: String = localDateToDateKey(now()),         // "YYYY-MM-DD" — zona local en el momento del alta
    val createdAt: Long = forDatePicker(LocalDate.now()),
) {
    fun localDate(): LocalDate = dateKeyToLocalDate(dateKey)

    fun formattedDate(): String = localDate().format(WEIGHT_DATE_FORMATTER)

    fun toWeightItem(): WeightItem = WeightItem(
        weight = weight.toDouble(),
        date = formattedDate(),
        dateKey = dateKey
    )
}
