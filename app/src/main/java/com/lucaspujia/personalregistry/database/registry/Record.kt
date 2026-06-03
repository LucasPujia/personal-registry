package com.lucaspujia.personalregistry.database.registry

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lucaspujia.personalregistry.utils.forDatePicker
import com.lucaspujia.personalregistry.utils.localDateToDateKey
import com.lucaspujia.personalregistry.utils.now
import java.time.LocalDate

@Entity(
    tableName = "records",
    indices = [Index(value = ["registryId", "dateKey"], unique = true)]
)
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val registryId: Long,
    val value1: Double,
    val value2: Double? = null,
    val dateKey: String = localDateToDateKey(now()),
    val createdAt: Long = forDatePicker(LocalDate.now())
)
