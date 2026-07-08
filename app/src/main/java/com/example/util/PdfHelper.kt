package com.example.util

import android.content.Context
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
import java.util.TimeZone

object PdfHelper {

    private fun drawArabicText(canvas: Canvas, text: String, x: Float, y: Float, width: Int, paint: TextPaint, alignment: Layout.Alignment = Layout.Alignment.ALIGN_OPPOSITE) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        // If aligning opposite (RTL), StaticLayout draws from x=0 to x=width, meaning the text is aligned to the right edge of 'width'.
        // So we translate to x - width, so the right edge of the text is exactly at 'x'.
        val drawX = if (alignment == Layout.Alignment.ALIGN_OPPOSITE) x - width else x
        canvas.translate(drawX, y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    fun generateEmployeeStatementPdf(
        context: Context,
        uri: Uri,
        employee: EmployeeDetail,
        summary: EmployeeReportSummary,
        transactions: List<TransactionItem>,
        currency: String
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply { color = Color.BLACK; textSize = 22f; isFakeBoldText = true }
        val headerPaint = TextPaint().apply { color = Color.DKGRAY; textSize = 14f; isFakeBoldText = true }
        val textPaint = TextPaint().apply { color = Color.BLACK; textSize = 12f }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var yPos = 50f
        val pageWidth = pageInfo.pageWidth.toFloat()
        val margin = 40f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        // Header
        drawArabicText(canvas, "وكالة القاضي لإدارة الأجور", pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f
        
        drawArabicText(canvas, "كشف حساب عامل", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f

        // Info
        drawArabicText(canvas, "اسم العامل: ${employee.name}", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "المهنة: ${employee.jobTitle.ifEmpty { "غير محدد" }}", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "معدل الأجرة: ${String.format(Locale.US, "%,.2f", employee.dailyWage)} $currency", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 30f

        // Table Header
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 10f
        drawArabicText(canvas, "التاريخ", pageWidth - margin, yPos, 100, headerPaint)
        drawArabicText(canvas, "البيان والتفاصيل", pageWidth - margin - 100, yPos, 250, headerPaint)
        drawArabicText(canvas, "الحركة المالية", pageWidth - margin - 350, yPos, 150, headerPaint)
        yPos += 25f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 15f

        // Transactions
        for (tx in transactions) {
            if (yPos > pageInfo.pageHeight - 150f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
            val isIncome = tx.type == TransactionType.WAGE || tx.type == TransactionType.BONUS
            val prefix = if (isIncome) "+" else "-"
            val amountStr = "$prefix${String.format(Locale.US, "%,.2f", tx.amount)} $currency"

            drawArabicText(canvas, tx.date.toUtcDateString(), pageWidth - margin, yPos, 100, textPaint)
            drawArabicText(canvas, tx.description, pageWidth - margin - 100, yPos, 250, textPaint)
            drawArabicText(canvas, amountStr, pageWidth - margin - 350, yPos, 150, textPaint)
            
            yPos += 25f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 10f
        }

        yPos += 20f

        // Footer Summary
        if (yPos > pageInfo.pageHeight - 150f) {
            document.finishPage(page)
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPos = 50f
        }
        
        drawArabicText(canvas, "الخلاصة:", pageWidth - margin, yPos, contentWidth, headerPaint)
        yPos += 25f
        drawArabicText(canvas, "إجمالي الأجور المستحقة: ${String.format(Locale.US, "%,.2f", summary.totalEarned)} $currency", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "إجمالي السلف المقتطعة: ${String.format(Locale.US, "%,.2f", summary.totalWithdrawn)} $currency", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        
        val netPaint = TextPaint(headerPaint).apply { color = if (summary.netPayable >= 0) Color.rgb(46, 125, 91) else Color.RED }
        drawArabicText(canvas, "الصافي المتبقي للاستلام: ${String.format(Locale.US, "%,.2f", summary.netPayable)} $currency", pageWidth - margin, yPos, contentWidth, netPaint)

        document.finishPage(page)

        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                document.writeTo(out)
            }
        } finally {
            document.close()
        }
    }

    fun generateGeneralReportPdf(
        context: Context,
        uri: Uri,
        records: List<GeneralEmployeeRecord>,
        summary: GeneralReportSummary,
        participatingCount: Int,
        filterName: String,
        currency: String
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply { color = Color.BLACK; textSize = 22f; isFakeBoldText = true }
        val headerPaint = TextPaint().apply { color = Color.DKGRAY; textSize = 14f; isFakeBoldText = true }
        val textPaint = TextPaint().apply { color = Color.BLACK; textSize = 12f }
        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var yPos = 50f
        val pageWidth = pageInfo.pageWidth.toFloat()
        val margin = 40f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        drawArabicText(canvas, "وكالة القاضي لإدارة الأجور", pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f
        drawArabicText(canvas, "التقرير الدوري العام للتكاليف", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 25f
        drawArabicText(canvas, "الفترة: $filterName", pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f

        // Summary
        drawArabicText(canvas, "ملخص الالتزامات والسيولة:", pageWidth - margin, yPos, contentWidth, headerPaint)
        yPos += 25f
        drawArabicText(canvas, "إجمالي الرواتب المحتسبة: ${String.format(Locale.US, "%,.2f", summary.totalEarned)} $currency", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "إجمالي السلفيات والمقتطعات: ${String.format(Locale.US, "%,.2f", summary.totalWithdrawn)} $currency", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "صافي التكلفة التشغيلية المتبقية: ${String.format(Locale.US, "%,.2f", summary.netCost)} $currency", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 20f
        drawArabicText(canvas, "تعداد العمال النشطين: $participatingCount عامل", pageWidth - margin, yPos, contentWidth, textPaint)
        yPos += 30f

        // Table Header
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 10f
        drawArabicText(canvas, "اسم العامل", pageWidth - margin, yPos, 150, headerPaint)
        drawArabicText(canvas, "أيام الحضور", pageWidth - margin - 150, yPos, 100, headerPaint)
        drawArabicText(canvas, "الإجمالي المستحق", pageWidth - margin - 250, yPos, 150, headerPaint)
        yPos += 25f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 15f

        // Records
        for (record in records) {
            if (yPos > pageInfo.pageHeight - 100f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
            
            drawArabicText(canvas, record.employeeName, pageWidth - margin, yPos, 150, textPaint)
            drawArabicText(canvas, record.attendanceDays.toString(), pageWidth - margin - 150, yPos, 100, textPaint)
            drawArabicText(canvas, "${String.format(Locale.US, "%,.2f", record.totalAmount)} $currency", pageWidth - margin - 250, yPos, 150, textPaint)
            
            yPos += 25f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 10f
        }

        document.finishPage(page)

        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                document.writeTo(out)
            }
        } finally {
            document.close()
        }
    }

    private fun Long.toUtcDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return format.format(Date(this))
    }
}
