package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Long.toUtcDateString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return format.format(Date(this))
}
