package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.ui.utils.toFormattedDateString
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
        val dateStr = dateMillis.toFormattedDateString()
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
        val dateStr = dateMillis.toFormattedDateString()
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
        val dateStr = dateMillis.toFormattedDateString()
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

    private fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val encodedMessage = Uri.encode(message)
            val uri = Uri.parse("smsto:$phoneNumber?body=$encodedMessage")
            
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
