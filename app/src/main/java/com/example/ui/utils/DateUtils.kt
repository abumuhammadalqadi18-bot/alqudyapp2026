package com.example.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar

fun Long.toFormattedDateString(pattern: String = "yyyy-MM-dd"): String {
    val date = Date(this)
    val format = SimpleDateFormat(pattern, Locale.ENGLISH)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(date)
}

fun Long.toArabicFormattedDateString(pattern: String = "EEEE، d MMMM yyyy"): String {
    val date = Date(this)
    val format = SimpleDateFormat(pattern, Locale.forLanguageTag("ar"))
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(date)
}

fun getUtcMidnight(): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
