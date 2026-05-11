package com.example.myapplication.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun now(): Long {
    return LocalDateTime.now()
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun nowUTC(): Long {
    return LocalDateTime.now()
        .atZone(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
}

fun longUTCToLDT(milliseconds: Long): LocalDateTime {
    return Instant
        .ofEpochMilli(milliseconds)
        .atZone(ZoneId.of("UTC"))
        .toLocalDateTime()
}

fun longToLDT(milliseconds: Long): LocalDateTime {
    return Instant
        .ofEpochMilli(milliseconds)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}