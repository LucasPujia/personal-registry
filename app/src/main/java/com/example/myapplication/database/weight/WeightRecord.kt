package com.example.myapplication.database.weight

import androidx.room.Entity
import androidx.room.PrimaryKey
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
        return DateTimeFormatter.ofPattern("MM/dd").format(ofEpochMilli(this.createdAt).atZone(UTC))
    }
}

