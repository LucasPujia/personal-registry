package com.lucaspujia.personalregistry.database.registry

data class MeasureUnit(
    val name: String,
    val symbol: String,
    val precision: Int = 1
)
