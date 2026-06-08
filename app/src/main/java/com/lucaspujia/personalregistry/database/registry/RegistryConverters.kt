package com.lucaspujia.personalregistry.database.registry

import androidx.room.TypeConverter
import com.google.gson.Gson

class RegistryConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMeasureUnit(unit: MeasureUnit?): String? {
        return unit?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMeasureUnit(value: String?): MeasureUnit? {
        return value?.let { gson.fromJson(it, MeasureUnit::class.java) }
    }
}
