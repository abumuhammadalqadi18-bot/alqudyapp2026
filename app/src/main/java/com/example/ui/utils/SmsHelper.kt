package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsHelper {
    fun sendWageSms(
        context: Context,
        phone: String,
        employeeName: String,
        dateMillis: Long,
        dayTypeStr: String,
        amount: Double,
        currency: String,
        netPayable: Double
    ) {
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar"))
        val dateStr = dateFormatter.format(Date(dateMillis))
        val amountStr = String.format(Locale.US, "%,.2f", amount)
        val netPayableStr = String.format(Locale.US, "%,.2f", netPayable)

        val msg = """
            أخي العزيز/ $employeeName المحترم،
            نحيطكم علماً بأنه تم تقييد أجر يوم جديد في حسابكم لتاريخ $dateStr.
            الحالة: $dayTypeStr.
            المبلغ المضاف: $amountStr $currency.
            صافي مستحقاتكم الحالية: $netPayableStr $currency.
            تطبيق القاضي لإدارة الأجور والعمليات المالية.
        """.trimIndent()

        sendSms(context, phone, msg)
    }

    fun sendWithdrawalSms(
        context: Context,
        phone: String,
        employeeName: String,
        dateMillis: Long,
        isCash: Boolean,
        amount: Double,
        currency: String,
        notes: String?,
        netPayable: Double
    ) {
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar"))
        val dateStr = dateFormatter.format(Date(dateMillis))
        val typeStr = if (isCash) "نقدي" else "آجل"
        val amountStr = String.format(Locale.US, "%,.2f", amount)
        val netPayableStr = String.format(Locale.US, "%,.2f", netPayable)
        val notesStr = if (!notes.isNullOrBlank()) "\nالملاحظات: $notes." else ""

        val msg = """
            أخي العزيز/ $employeeName المحترم،
            نحيطكم علماً بأنه تم تسجيل عملية سحب من حسابكم بتاريخ $dateStr.
            نوع السحب: $typeStr.
            المبلغ المستقطع: $amountStr $currency.$notesStr
            صافي مستحقاتكم المتبقية: $netPayableStr $currency.
            تطبيق القاضي لإدارة الأجور والعمليات المالية.
        """.trimIndent()

        sendSms(context, phone, msg)
    }

    fun sendUpdateSms(
        context: Context,
        phone: String,
        employeeName: String,
        dateMillis: Long,
        currency: String,
        netPayable: Double
    ) {
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar"))
        val dateStr = dateFormatter.format(Date(dateMillis))
        val netPayableStr = String.format(Locale.US, "%,.2f", netPayable)

        val msg = """
            تنويه رسمي من إدارة الأجور:
            أخي العزيز/ $employeeName المحترم،
            تم إجراء تعديل/تحديث على السجل المالي الخاص بكم لتاريخ $dateStr.
            صافي مستحقاتكم المحدثة والنهائية: $netPayableStr $currency.
            تطبيق القاضي لإدارة الأجور والعمليات المالية.
        """.trimIndent()

        sendSms(context, phone, msg)
    }

    private fun sendSms(context: Context, phone: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", message)
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra("body", message)
            putExtra(Intent.EXTRA_SUBJECT, "إشعار من تطبيق القاضي")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
