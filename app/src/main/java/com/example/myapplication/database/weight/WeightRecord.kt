package com.example.myapplication.database.weight

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.mainActivity.WeightItem
import java.time.Instant.ofEpochMilli
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

@Entity(tableName = "weight_records")
data class WeightRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Float,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun formattedDate(): String {
        return DateTimeFormatter.ofPattern("dd/MM").format(ofEpochMilli(this.createdAt).atZone(UTC))
    }

    fun toWeightItem(): WeightItem {
        return WeightItem(weight.toDouble(), formattedDate())
    }
}

