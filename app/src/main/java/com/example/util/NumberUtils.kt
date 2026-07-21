package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun String.safeToDouble(): Double {
    if (this.isBlank()) return 0.0
    // تنظيف شامل وكامل من كل الفواصل العربية والأجنبية والإبقاء على النقطة العشرية الوحيدة
    val cleanInput = this.replace("،", "")
        .replace(",", "")
        .replace(" ", "")
        .trim()
    return cleanInput.toDoubleOrNull() ?: 0.0
}

fun Double.toCurrencyFormat(): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ',' // فاصل الآلاف إنجليزي قياسي لمنع التداخل مع الفاصلة العربية
        decimalSeparator = '.'  // الفاصل العشري نقطة
    }
    val formatter = DecimalFormat("#,##0.00", symbols)
    return formatter.format(this)
}
