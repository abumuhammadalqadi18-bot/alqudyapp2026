package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.ui.viewmodels.EmployeeReportSummary
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    fun generateAndSharePdf(context: Context, summaries: List<EmployeeReportSummary>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var yPosition = 50f
        canvas.drawText("التقرير المالي - نظام القاضي", pageInfo.pageWidth / 2f, yPosition, titlePaint)
        
        yPosition += 50f
        
        // Draw Table Header
        val startX = pageInfo.pageWidth - 20f
        canvas.drawText("الاسم", startX, yPosition, headerPaint)
        canvas.drawText("الحضور", startX - 150f, yPosition, headerPaint)
        canvas.drawText("الأجور", startX - 250f, yPosition, headerPaint)
        canvas.drawText("المسحوبات", startX - 350f, yPosition, headerPaint)
        canvas.drawText("الصافي", startX - 450f, yPosition, headerPaint)
        
        yPosition += 10f
        canvas.drawLine(20f, yPosition, pageInfo.pageWidth - 20f, yPosition, linePaint)
        yPosition += 20f

        for (summary in summaries) {
            // Check for new page
            if (yPosition > pageInfo.pageHeight - 50) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText(summary.employeeName, startX, yPosition, textPaint)
            canvas.drawText("${summary.attendanceDays}", startX - 150f, yPosition, textPaint)
            canvas.drawText(String.format("%.2f", summary.totalEarned), startX - 250f, yPosition, textPaint)
            canvas.drawText(String.format("%.2f", summary.totalWithdrawn), startX - 350f, yPosition, textPaint)
            canvas.drawText(String.format("%.2f", summary.netPayable), startX - 450f, yPosition, textPaint)

            yPosition += 15f
            canvas.drawLine(20f, yPosition, pageInfo.pageWidth - 20f, yPosition, linePaint)
            yPosition += 20f
        }

        pdfDocument.finishPage(page)

        try {
            val file = File(context.cacheDir, "Financial_Report.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            sharePdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "مشاركة التقرير عبر"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
