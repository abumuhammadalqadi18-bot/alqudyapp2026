package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun String.safeToDouble(): Double {
    if (this.isBlank()) return 0.0
    val cleanInput = this.replace("،", ".")
        .replace(",", ".")
        .replace("٫", ".")
        .trim()
    return cleanInput.toDoubleOrNull() ?: 0.0
}

fun Double.toCurrencyFormat(): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatter = DecimalFormat("#,##0.00", symbols)
    return formatter.format(this)
}
