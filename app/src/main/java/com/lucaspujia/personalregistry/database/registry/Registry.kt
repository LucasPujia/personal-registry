package com.lucaspujia.personalregistry.database.registry

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registries")
data class Registry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val unit1: MeasureUnit,
    val unit2: MeasureUnit? = null,
    val formula: String? = null
)
