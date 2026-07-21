import sys

content = """package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.example.ui.viewmodels.EmployeeDetail
import com.example.ui.viewmodels.GeneralEmployeeRecord
import com.example.ui.viewmodels.TransactionItem
import com.example.ui.viewmodels.TransactionType
import com.example.ui.viewmodels.EmployeeReportSummary
import com.example.ui.viewmodels.GeneralReportSummary
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {

    private fun drawArabicText(canvas: Canvas, text: String, x: Float, y: Float, width: Int, paint: TextPaint, alignment: Layout.Alignment = Layout.Alignment.ALIGN_OPPOSITE) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        val drawX = if (alignment == Layout.Alignment.ALIGN_OPPOSITE) x - width else x
        canvas.translate(drawX, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawBitmapSafe(context: Context, canvas: Canvas, uriString: String?, x: Float, y: Float, width: Float, height: Float) {
        if (uriString.isNullOrBlank()) return
        try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (bitmap != null) {
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), true)
                    canvas.drawBitmap(scaledBitmap, x, y, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateEmployeeStatementPdf(
        context: Context,
        uri: Uri,
        employee: EmployeeDetail,
        summary: EmployeeReportSummary,
        transactions: List<TransactionItem>,
        currency: String,
        companyName: String,
        companyPhone: String,
        companyAddress: String,
        companyFooter: String,
        logoUriString: String?,
        sealUriString: String?,
        signatureUriString: String?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var yPos = 40f
        val margin = 40f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }

        val headerPaint = TextPaint().apply {
            color = Color.DKGRAY
            textSize = 14f
        }

        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        val tableHeaderPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 12f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeader() {
            // Header: Logo on right (RTL context), text on left/center
            drawBitmapSafe(context, canvas, logoUriString, pageWidth - margin - 60f, yPos - 10f, 60f, 60f)

            val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
            drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth / 2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f

            if (companyPhone.isNotBlank() || companyAddress.isNotBlank()) {
                val subHeader = "${if (companyAddress.isNotBlank()) "العنوان: $companyAddress" else ""} ${if (companyPhone.isNotBlank()) " | هاتف: $companyPhone" else ""}"
                drawArabicText(canvas, subHeader, pageWidth / 2f + contentWidth / 2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
                yPos += 20f
            }

            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 20f
        }

        fun startNewPage() {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPos = 40f
            drawHeader()
        }

        drawHeader()

        drawArabicText(canvas, "كشف حساب موظف", pageWidth / 2f + contentWidth / 2, yPos, contentWidth, titlePaint.apply { textSize = 20f }, Layout.Alignment.ALIGN_CENTER)
        yPos += 30f

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val printDate = dateFormat.format(Date())

        drawArabicText(canvas, "تاريخ الطباعة: $printDate", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "الاسم: ${employee.name}", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "المسمى الوظيفي: ${employee.jobTitle}", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 30f

        // Summary Box
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 60f, Paint().apply { color = Color.parseColor("#F5F5F5") })
        val summaryY = yPos + 10f
        drawArabicText(canvas, "إجمالي المستحق: ${summary.totalEarned} $currency", pageWidth - margin - 10f, summaryY, contentWidth / 2, textPaint)
        drawArabicText(canvas, "إجمالي المسحوب: ${summary.totalWithdrawn} $currency", pageWidth / 2f, summaryY, contentWidth / 2, textPaint)
        val netColor = if (summary.netBalance >= 0) Color.parseColor("#2E7D5B") else Color.RED
        drawArabicText(canvas, "الرصيد الصافي: ${summary.netBalance} $currency", pageWidth - margin - 10f, summaryY + 20f, contentWidth / 2, textPaint.apply { color = netColor; isFakeBoldText = true })
        textPaint.apply { color = Color.BLACK; isFakeBoldText = false }
        yPos += 80f

        // Table Header
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 30f, Paint().apply { color = Color.parseColor("#2E7D5B") })
        val colWidths = floatArrayOf(80f, 100f, 100f, 100f, 135f)
        var x = pageWidth - margin
        val headers = listOf("التاريخ", "النوع", "المبلغ", "البيان", "ملاحظات")
        headers.forEachIndexed { i, title ->
            drawArabicText(canvas, title, x - 5f, yPos + 5f, colWidths[i].toInt(), tableHeaderPaint)
            x -= colWidths[i]
        }
        yPos += 30f

        // Table Rows
        val rowFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        for (tx in transactions) {
            if (yPos > pageHeight - 150f) startNewPage()

            x = pageWidth - margin
            val dateStr = rowFormat.format(Date(tx.date))
            val typeStr = when(tx.type) {
                TransactionType.WAGE -> "أجرة"
                TransactionType.WITHDRAWAL -> "سحب"
                TransactionType.BONUS -> "مكافأة"
                TransactionType.DEDUCTION -> "خصم"
            }

            drawArabicText(canvas, dateStr, x - 5f, yPos + 5f, colWidths[0].toInt(), textPaint)
            x -= colWidths[0]
            drawArabicText(canvas, typeStr, x - 5f, yPos + 5f, colWidths[1].toInt(), textPaint)
            x -= colWidths[1]
            drawArabicText(canvas, "${tx.amount}", x - 5f, yPos + 5f, colWidths[2].toInt(), textPaint)
            x -= colWidths[2]
            drawArabicText(canvas, tx.description, x - 5f, yPos + 5f, colWidths[3].toInt(), textPaint)
            x -= colWidths[3]
            drawArabicText(canvas, tx.note, x - 5f, yPos + 5f, colWidths[4].toInt(), textPaint)

            yPos += 30f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        }

        // Footer Section
        if (yPos > pageHeight - 200f) startNewPage()
        yPos += 40f

        val footerY = yPos
        drawArabicText(canvas, "توقيع المحاسب", pageWidth - margin - 50f, footerY, 100, textPaint)
        drawArabicText(canvas, "الختم الرسمي", pageWidth / 2f + 50f, footerY, 100, textPaint, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, "توقيع الإدارة", margin + 150f, footerY, 100, textPaint)

        // Draw Signature and Seal
        drawBitmapSafe(context, canvas, signatureUriString, margin + 50f, footerY - 10f, 100f, 50f)
        drawBitmapSafe(context, canvas, sealUriString, pageWidth / 2f - 40f, footerY - 20f, 80f, 80f)

        yPos += 80f

        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth / 2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
        }

        document.finishPage(page)

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    fun generateGeneralReportPdf(
        context: Context,
        uri: Uri,
        filterText: String,
        summary: GeneralReportSummary,
        records: List<GeneralEmployeeRecord>,
        currency: String,
        companyName: String,
        companyPhone: String,
        companyAddress: String,
        companyFooter: String,
        logoUriString: String?,
        sealUriString: String?,
        signatureUriString: String?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var yPos = 40f
        val margin = 40f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }
        val headerPaint = TextPaint().apply { color = Color.DKGRAY; textSize = 14f }
        val textPaint = TextPaint().apply { color = Color.BLACK; textSize = 12f }
        val tableHeaderPaint = TextPaint().apply { color = Color.WHITE; textSize = 12f; isFakeBoldText = true }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeader() {
            drawBitmapSafe(context, canvas, logoUriString, pageWidth - margin - 60f, yPos - 10f, 60f, 60f)

            val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
            drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth / 2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f

            if (companyPhone.isNotBlank() || companyAddress.isNotBlank()) {
                val subHeader = "${if (companyAddress.isNotBlank()) "العنوان: $companyAddress" else ""} ${if (companyPhone.isNotBlank()) " | هاتف: $companyPhone" else ""}"
                drawArabicText(canvas, subHeader, pageWidth / 2f + contentWidth / 2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
                yPos += 20f
            }

            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 20f
        }

        fun startNewPage() {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPos = 40f
            drawHeader()
        }

        drawHeader()

        drawArabicText(canvas, "التقرير المالي العام", pageWidth / 2f + contentWidth / 2, yPos, contentWidth, titlePaint.apply { textSize = 20f }, Layout.Alignment.ALIGN_CENTER)
        yPos += 30f

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val printDate = dateFormat.format(Date())

        drawArabicText(canvas, "تاريخ الطباعة: $printDate", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "الفترة المحددة: $filterText", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 30f

        // Summary Box
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 60f, Paint().apply { color = Color.parseColor("#F5F5F5") })
        val summaryY = yPos + 10f
        drawArabicText(canvas, "إجمالي الأجور المستحقة: ${summary.totalEarned} $currency", pageWidth - margin - 10f, summaryY, contentWidth / 2, textPaint)
        drawArabicText(canvas, "إجمالي المبالغ المسحوبة: ${summary.totalWithdrawn} $currency", pageWidth / 2f, summaryY, contentWidth / 2, textPaint)
        val netColor = if (summary.netCost >= 0) Color.parseColor("#2E7D5B") else Color.RED
        drawArabicText(canvas, "التكلفة الصافية: ${summary.netCost} $currency", pageWidth - margin - 10f, summaryY + 20f, contentWidth / 2, textPaint.apply { color = netColor; isFakeBoldText = true })
        textPaint.apply { color = Color.BLACK; isFakeBoldText = false }
        yPos += 80f

        // Table Header
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 30f, Paint().apply { color = Color.parseColor("#2E7D5B") })
        val colWidths = floatArrayOf(150f, 100f, 130f, 135f)
        var x = pageWidth - margin
        val headers = listOf("الموظف", "أيام العمل", "إجمالي المستحق", "آخر حركة")
        headers.forEachIndexed { i, title ->
            drawArabicText(canvas, title, x - 5f, yPos + 5f, colWidths[i].toInt(), tableHeaderPaint)
            x -= colWidths[i]
        }
        yPos += 30f

        // Table Rows
        val rowFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        for (rec in records) {
            if (yPos > pageHeight - 150f) startNewPage()

            x = pageWidth - margin
            val lastDateStr = if (rec.lastTransactionDate > 0) rowFormat.format(Date(rec.lastTransactionDate)) else "-"

            drawArabicText(canvas, rec.employeeName, x - 5f, yPos + 5f, colWidths[0].toInt(), textPaint)
            x -= colWidths[0]
            drawArabicText(canvas, "${rec.attendanceDays}", x - 5f, yPos + 5f, colWidths[1].toInt(), textPaint)
            x -= colWidths[1]
            drawArabicText(canvas, "${rec.totalAmount}", x - 5f, yPos + 5f, colWidths[2].toInt(), textPaint)
            x -= colWidths[2]
            drawArabicText(canvas, lastDateStr, x - 5f, yPos + 5f, colWidths[3].toInt(), textPaint)

            yPos += 30f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        }

        if (yPos > pageHeight - 200f) startNewPage()
        yPos += 40f

        val footerY = yPos
        drawArabicText(canvas, "توقيع المحاسب", pageWidth - margin - 50f, footerY, 100, textPaint)
        drawArabicText(canvas, "الختم الرسمي", pageWidth / 2f + 50f, footerY, 100, textPaint, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, "توقيع الإدارة", margin + 150f, footerY, 100, textPaint)

        // Draw Signature and Seal
        drawBitmapSafe(context, canvas, signatureUriString, margin + 50f, footerY - 10f, 100f, 50f)
        drawBitmapSafe(context, canvas, sealUriString, pageWidth / 2f - 40f, footerY - 20f, 80f, 80f)

        yPos += 80f

        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth / 2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
        }

        document.finishPage(page)

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }
}
"""

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'w') as f:
    f.write(content)

print("Created PdfHelper.kt")
