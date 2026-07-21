package com.example.util

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

    private object ArabicNumberToWords {
        private val units = arrayOf("", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة")
        private val teens = arrayOf("عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر")
        private val tens = arrayOf("", "عشرة", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون")
        private val hundreds = arrayOf("", "مائة", "مائتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة")

        fun convert(amount: Double): String {
            val longVal = amount.toLong()
            if (longVal == 0L) return "صفر"
            val words = convertGroup(longVal).trim()
            return "فقط $words لا غير"
        }

        private fun convertGroup(number: Long): String {
            if (number == 0L) return ""
            if (number < 10) return units[number.toInt()]
            if (number < 20) return teens[(number - 10).toInt()]
            if (number < 100) {
                val unit = number % 10
                val ten = number / 10
                if (unit == 0L) return tens[ten.toInt()]
                return "${units[unit.toInt()]} و${tens[ten.toInt()]}"
            }
            if (number < 1000) {
                val hundred = number / 100
                val rest = number % 100
                if (rest == 0L) return hundreds[hundred.toInt()]
                return "${hundreds[hundred.toInt()]} و${convertGroup(rest)}"
            }
            if (number < 1000000) {
                val thousand = number / 1000
                val rest = number % 1000
                val thousandStr = when (thousand) {
                    1L -> "ألف"
                    2L -> "ألفان"
                    in 3..10 -> "${convertGroup(thousand)} آلاف"
                    else -> "${convertGroup(thousand)} ألف"
                }
                if (rest == 0L) return thousandStr
                return "$thousandStr و${convertGroup(rest)}"
            }
            if (number < 1000000000) {
                val million = number / 1000000
                val rest = number % 1000000
                val millionStr = when (million) {
                    1L -> "مليون"
                    2L -> "مليونان"
                    in 3..10 -> "${convertGroup(million)} ملايين"
                    else -> "${convertGroup(million)} مليون"
                }
                if (rest == 0L) return millionStr
                return "$millionStr و${convertGroup(rest)}"
            }
            return number.toString()
        }
    }

    private fun formatAmount(amount: Double): String {
        return String.format(Locale.US, "%,.2f", amount).replace(".00", "")
    }

    private fun drawArabicText(
        canvas: Canvas, text: String, x: Float, y: Float, width: Int, paint: TextPaint
    ): Float {
        // Adjust x to be the right edge since we are using RTL alignment explicitly
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        // Align RIGHT: start drawing from (x - width) so the right edge of text bounds is at x
        canvas.translate(x - width, y)
        staticLayout.draw(canvas)
        canvas.restore()
        return staticLayout.height.toFloat()
    }

    private fun drawCenteredText(
        canvas: Canvas, text: String, centerX: Float, y: Float, width: Int, paint: TextPaint
    ): Float {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .build()
        canvas.save()
        canvas.translate(centerX - width / 2f, y)
        staticLayout.draw(canvas)
        canvas.restore()
        return staticLayout.height.toFloat()
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

    private fun drawHeader(
        context: Context, canvas: Canvas, pageWidth: Int, margin: Float, contentWidth: Int,
        companyName: String, companyPhone: String, companyAddress: String, logoUriString: String?,
        documentTitle: String, docNo: String
    ): Float {
        var yPos = margin
        val headerTitlePaint = TextPaint().apply { color = Color.BLACK; textSize = 16f; isFakeBoldText = true }
        val headerDetailPaint = TextPaint().apply { color = Color.DKGRAY; textSize = 12f }
        val docInfoPaint = TextPaint().apply { color = Color.DKGRAY; textSize = 12f }

        val rightAlignX = pageWidth - margin
        var rightY = yPos
        rightY += drawArabicText(canvas, companyName.ifBlank { "وكالة القاضي لإدارة الأجور" }, rightAlignX, rightY, contentWidth / 3, headerTitlePaint) + 5f
        if (companyAddress.isNotBlank()) rightY += drawArabicText(canvas, "العنوان: $companyAddress", rightAlignX, rightY, contentWidth / 3, headerDetailPaint) + 5f
        if (companyPhone.isNotBlank()) rightY += drawArabicText(canvas, "هاتف: $companyPhone", rightAlignX, rightY, contentWidth / 3, headerDetailPaint) + 5f

        if (!logoUriString.isNullOrBlank()) {
            drawBitmapSafe(context, canvas, logoUriString, pageWidth / 2f - 30f, yPos, 60f, 60f)
        }

        val leftAlignX = margin + (contentWidth / 3f)
        var leftY = yPos
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
        leftY += drawArabicText(canvas, "التاريخ: ${dateFormat.format(Date())}", leftAlignX, leftY, contentWidth / 3, docInfoPaint) + 5f
        leftY += drawArabicText(canvas, "رقم المرجع: $docNo", leftAlignX, leftY, contentWidth / 3, docInfoPaint) + 5f

        yPos = maxOf(rightY, leftY, yPos + 60f) + 15f
        
        val linePaint = Paint().apply { color = Color.parseColor("#D4AF37"); strokeWidth = 2f }
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        val mainTitlePaint = TextPaint().apply { color = Color.BLACK; textSize = 22f; isFakeBoldText = true }
        yPos += drawCenteredText(canvas, documentTitle, pageWidth / 2f, yPos, contentWidth, mainTitlePaint) + 25f

        return yPos
    }

    private fun drawFooter(
        context: Context, canvas: Canvas, pageWidth: Int, pageHeight: Int, margin: Float,
        companyFooter: String, sealUriString: String?, signatureUriString: String?
    ) {
        var yPos = pageHeight - margin - 80f
        val contentWidth = (pageWidth - 2 * margin).toInt()
        val textPaint = TextPaint().apply { color = Color.BLACK; textSize = 12f; isFakeBoldText = true }
        
        val thirdWidth = contentWidth / 3f
        
        val linePaint = Paint().apply { color = Color.BLACK; strokeWidth = 1f }
        
        // Employee / Receiver (Left)
        val leftCenterX = margin + thirdWidth / 2f
        canvas.drawLine(margin + 20f, yPos, margin + thirdWidth - 20f, yPos, linePaint)
        drawCenteredText(canvas, "توقيع المستلم / الموظف", leftCenterX, yPos + 10f, thirdWidth.toInt(), textPaint)
        
        // Manager Signature (Center)
        val middleCenterX = pageWidth / 2f
        canvas.drawLine(middleCenterX - thirdWidth / 2f + 20f, yPos, middleCenterX + thirdWidth / 2f - 20f, yPos, linePaint)
        drawCenteredText(canvas, "توقيع المدير العام", middleCenterX, yPos + 10f, thirdWidth.toInt(), textPaint)
        drawBitmapSafe(context, canvas, signatureUriString, middleCenterX - 40f, yPos - 50f, 80f, 40f)

        // Seal (Right)
        val rightCenterX = pageWidth - margin - thirdWidth / 2f
        canvas.drawLine(pageWidth - margin - thirdWidth + 20f, yPos, pageWidth - margin - 20f, yPos, linePaint)
        drawCenteredText(canvas, "ختم المؤسسة الرسمي", rightCenterX, yPos + 10f, thirdWidth.toInt(), textPaint)
        drawBitmapSafe(context, canvas, sealUriString, rightCenterX - 40f, yPos - 80f, 80f, 80f)

        if (companyFooter.isNotBlank()) {
            val notePaint = TextPaint().apply { color = Color.DKGRAY; textSize = 10f; isFakeBoldText = false }
            drawCenteredText(canvas, companyFooter, pageWidth / 2f, pageHeight - margin - 20f, contentWidth, notePaint)
        }
    }

    private fun writeAndCloseDoc(context: Context, uri: Uri, document: PdfDocument) {
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

    fun generatePaymentReceiptPdf(
        context: Context, uri: Uri,
        employeeName: String, amount: Double, reason: String, txId: String,
        currency: String, companyName: String, companyPhone: String,
        companyAddress: String, companyFooter: String, logoUriString: String?,
        sealUriString: String?, signatureUriString: String?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var yPos = drawHeader(context, canvas, pageWidth, margin, contentWidth, companyName, companyPhone, companyAddress, logoUriString, "سند صرف نقدي رسمي", txId)

        val labelPaint = TextPaint().apply { color = Color.GRAY; textSize = 14f; isFakeBoldText = true }
        val valuePaint = TextPaint().apply { color = Color.BLACK; textSize = 16f; isFakeBoldText = true }
        
        val boxPaint = Paint().apply { color = Color.parseColor("#F8F9FA"); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 1f }

        val startY = yPos
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 220f, boxPaint)
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 220f, borderPaint)
        
        yPos += 20f
        
        val rightAlign = pageWidth - margin - 20f
        drawArabicText(canvas, "يُصرف للأخ/الأخت:", rightAlign, yPos, 150, labelPaint)
        drawArabicText(canvas, employeeName, rightAlign - 160f, yPos, contentWidth - 200, valuePaint)
        yPos += 45f

        drawArabicText(canvas, "مبلغاً وقدره (بالأرقام):", rightAlign, yPos, 150, labelPaint)
        drawArabicText(canvas, "${formatAmount(amount)} $currency", rightAlign - 160f, yPos, contentWidth - 200, valuePaint)
        yPos += 45f

        val tafqeet = ArabicNumberToWords.convert(amount)
        drawArabicText(canvas, "المبلغ (بالحروف):", rightAlign, yPos, 150, labelPaint)
        drawArabicText(canvas, "$tafqeet $currency", rightAlign - 160f, yPos, contentWidth - 200, valuePaint)
        yPos += 45f

        drawArabicText(canvas, "وذلك عبارة عن:", rightAlign, yPos, 150, labelPaint)
        drawArabicText(canvas, reason, rightAlign - 160f, yPos, contentWidth - 200, valuePaint)

        drawFooter(context, canvas, pageWidth, pageHeight, margin, companyFooter, sealUriString, signatureUriString)

        document.finishPage(page)
        writeAndCloseDoc(context, uri, document)
    }

    fun generateShiftApprovalPdf(
        context: Context, uri: Uri,
        employeeName: String, amount: Double, totalAccumulated: Double, dateStr: String, note: String, txId: String,
        currency: String, companyName: String, companyPhone: String,
        companyAddress: String, companyFooter: String, logoUriString: String?,
        sealUriString: String?, signatureUriString: String?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var yPos = drawHeader(context, canvas, pageWidth, margin, contentWidth, companyName, companyPhone, companyAddress, logoUriString, "إشعار إثبات حضور واعتماد أجر يومي", txId)

        val labelPaint = TextPaint().apply { color = Color.GRAY; textSize = 14f; isFakeBoldText = true }
        val valuePaint = TextPaint().apply { color = Color.BLACK; textSize = 16f; isFakeBoldText = true }
        
        val boxPaint = Paint().apply { color = Color.parseColor("#F8F9FA"); style = Paint.Style.FILL }
        val borderPaint = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 1f }

        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 260f, boxPaint)
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 260f, borderPaint)
        
        yPos += 20f
        
        val rightAlign = pageWidth - margin - 20f
        drawArabicText(canvas, "اسم الموظف:", rightAlign, yPos, 150, labelPaint)
        drawArabicText(canvas, employeeName, rightAlign - 160f, yPos, contentWidth - 200, valuePaint)
        yPos += 45f

        drawArabicText(canvas, "تاريخ العمل:", rightAlign, yPos, 150, labelPaint)
        drawArabicText(canvas, dateStr, rightAlign - 160f, yPos, contentWidth - 200, valuePaint)
        yPos += 45f

        drawArabicText(canvas, "الأجر اليومي المعتمد:", rightAlign, yPos, 150, labelPaint)
        drawArabicText(canvas, "${formatAmount(amount)} $currency", rightAlign - 160f, yPos, contentWidth - 200, valuePaint)
        yPos += 45f
        
        drawArabicText(canvas, "إجمالي المستحقات المتراكمة:", rightAlign, yPos, 220, labelPaint)
        val positiveGreen = TextPaint(valuePaint).apply { color = Color.parseColor("#2E7D5B") }
        drawArabicText(canvas, "${formatAmount(totalAccumulated)} $currency", rightAlign - 230f, yPos, contentWidth - 270, positiveGreen)
        yPos += 45f

        if (note.isNotBlank()) {
            drawArabicText(canvas, "ملاحظات إدارية:", rightAlign, yPos, 150, labelPaint)
            drawArabicText(canvas, note, rightAlign - 160f, yPos, contentWidth - 200, valuePaint)
        }

        drawFooter(context, canvas, pageWidth, pageHeight, margin, companyFooter, sealUriString, signatureUriString)

        document.finishPage(page)
        writeAndCloseDoc(context, uri, document)
    }

    fun generateEmployeeStatementPdf(
        context: Context, uri: Uri,
        employee: EmployeeDetail, summary: EmployeeReportSummary, transactions: List<TransactionItem>,
        currency: String, companyName: String, companyPhone: String,
        companyAddress: String, companyFooter: String, logoUriString: String?,
        sealUriString: String?, signatureUriString: String?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        val margin = 36f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        val textPaint = TextPaint().apply { color = Color.BLACK; textSize = 11f }
        val tableHeaderPaint = TextPaint().apply { color = Color.WHITE; textSize = 12f; isFakeBoldText = true }
        val borderPaint = Paint().apply { color = Color.BLACK; strokeWidth = 1f; style = Paint.Style.STROKE }
        val linePaint = Paint().apply { color = Color.BLACK; strokeWidth = 1f }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#0F1B2B"); style = Paint.Style.FILL } // PrimaryTeal

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = margin

        fun drawCurrentHeader() {
            yPos = drawHeader(context, canvas, pageWidth, margin, contentWidth, companyName, companyPhone, companyAddress, logoUriString, "كشف حساب تفصيلي", "EMP-${employee.id}")
        }

        fun startNewPage() {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPos = margin
            drawCurrentHeader()
        }

        drawCurrentHeader()

        val infoPaint = TextPaint().apply { color = Color.BLACK; textSize = 14f; isFakeBoldText = true }
        drawArabicText(canvas, "الاسم: ${employee.name}", pageWidth - margin, yPos, contentWidth / 2, infoPaint)
        drawArabicText(canvas, "المسمى الوظيفي: ${employee.jobTitle}", pageWidth / 2f, yPos, contentWidth / 2, infoPaint)
        yPos += 25f

        val summaryBg = Paint().apply { color = Color.parseColor("#F5F5F5") }
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 40f, summaryBg)
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 40f, borderPaint)
        val summaryY = yPos + 10f
        
        val summaryTextPaint = TextPaint(infoPaint).apply { textSize = 12f }
        val thirdW = contentWidth / 3
        drawArabicText(canvas, "إجمالي الأجور: ${formatAmount(summary.totalEarned)}", pageWidth - margin - 10f, summaryY, thirdW, summaryTextPaint)
        drawArabicText(canvas, "إجمالي السحوبات: ${formatAmount(summary.totalWithdrawn)}", pageWidth - margin - thirdW - 10f, summaryY, thirdW, summaryTextPaint)
        
        val netColor = if (summary.netPayable >= 0) Color.parseColor("#2E7D5B") else Color.RED
        val netPaint = TextPaint(summaryTextPaint).apply { color = netColor }
        drawArabicText(canvas, "الصافي: ${formatAmount(summary.netPayable)} $currency", pageWidth - margin - 2 * thirdW - 10f, summaryY, thirdW, netPaint)
        yPos += 60f

        // Table Header
        // 20% Date, 40% Desc, 20% Debit, 20% Credit
        val colDateW = contentWidth * 0.20f
        val colDescW = contentWidth * 0.40f
        val colDebitW = contentWidth * 0.20f
        val colCreditW = contentWidth * 0.20f
        
        val startXDate = pageWidth - margin
        val startXDesc = startXDate - colDateW
        val startXDebit = startXDesc - colDescW
        val startXCredit = startXDebit - colDebitW

        val rowHeight = 25f

        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, headerBgPaint)
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, borderPaint)
        
        drawArabicText(canvas, "التاريخ", startXDate - 5f, yPos + 5f, colDateW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "البيان (نوع العملية)", startXDesc - 5f, yPos + 5f, colDescW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "مدين (مسحوبات)", startXDebit - 5f, yPos + 5f, colDebitW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "دائن (أجور)", startXCredit - 5f, yPos + 5f, colCreditW.toInt(), tableHeaderPaint)
        
        // Vertical lines for header
        canvas.drawLine(startXDesc, yPos, startXDesc, yPos + rowHeight, linePaint)
        canvas.drawLine(startXDebit, yPos, startXDebit, yPos + rowHeight, linePaint)
        canvas.drawLine(startXCredit, yPos, startXCredit, yPos + rowHeight, linePaint)
        
        yPos += rowHeight

        val rowFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        for (tx in transactions) {
            if (yPos > pageHeight - margin - 150f) {
                startNewPage()
                // Re-draw header on new page
                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, headerBgPaint)
                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, borderPaint)
                drawArabicText(canvas, "التاريخ", startXDate - 5f, yPos + 5f, colDateW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "البيان", startXDesc - 5f, yPos + 5f, colDescW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "مدين (مسحوبات)", startXDebit - 5f, yPos + 5f, colDebitW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "دائن (أجور)", startXCredit - 5f, yPos + 5f, colCreditW.toInt(), tableHeaderPaint)
                canvas.drawLine(startXDesc, yPos, startXDesc, yPos + rowHeight, linePaint)
                canvas.drawLine(startXDebit, yPos, startXDebit, yPos + rowHeight, linePaint)
                canvas.drawLine(startXCredit, yPos, startXCredit, yPos + rowHeight, linePaint)
                yPos += rowHeight
            }

            val dateStr = rowFormat.format(Date(tx.date))
            var debitStr = ""
            var creditStr = ""
            var descStr = tx.description
            
            when(tx.type) {
                TransactionType.WAGE -> {
                    creditStr = formatAmount(tx.amount)
                }
                TransactionType.WITHDRAWAL -> {
                    debitStr = formatAmount(tx.amount)
                }
                TransactionType.BONUS -> {
                    creditStr = formatAmount(tx.amount)
                    descStr = "مكافأة: $descStr"
                }
                TransactionType.DEDUCTION -> {
                    debitStr = formatAmount(tx.amount)
                    descStr = "خصم: $descStr"
                }
            }

            // Measure height needed for description (could be multi-line)
            val staticLayout = StaticLayout.Builder.obtain(descStr, 0, descStr.length, textPaint, colDescW.toInt() - 10)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setTextDirection(TextDirectionHeuristics.RTL)
                .build()
            val cellHeight = maxOf(rowHeight, staticLayout.height.toFloat() + 10f)

            // Draw horizontal row boundaries
            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + cellHeight, borderPaint)
            
            // Vertical lines
            canvas.drawLine(startXDesc, yPos, startXDesc, yPos + cellHeight, linePaint)
            canvas.drawLine(startXDebit, yPos, startXDebit, yPos + cellHeight, linePaint)
            canvas.drawLine(startXCredit, yPos, startXCredit, yPos + cellHeight, linePaint)

            drawArabicText(canvas, dateStr, startXDate - 5f, yPos + (cellHeight - 15f)/2f, colDateW.toInt(), textPaint)
            drawArabicText(canvas, debitStr, startXDebit - 5f, yPos + (cellHeight - 15f)/2f, colDebitW.toInt(), textPaint)
            drawArabicText(canvas, creditStr, startXCredit - 5f, yPos + (cellHeight - 15f)/2f, colCreditW.toInt(), textPaint)
            
            // Draw multi-line description
            canvas.save()
            canvas.translate(startXDesc - colDescW + 5f, yPos + 5f)
            staticLayout.draw(canvas)
            canvas.restore()

            yPos += cellHeight
        }

        drawFooter(context, canvas, pageWidth, pageHeight, margin, companyFooter, sealUriString, signatureUriString)

        document.finishPage(page)
        writeAndCloseDoc(context, uri, document)
    }

    fun generateGeneralReportPdf(
        context: Context, uri: Uri,
        filterText: String, summary: GeneralReportSummary, records: List<GeneralEmployeeRecord>,
        currency: String, companyName: String, companyPhone: String,
        companyAddress: String, companyFooter: String, logoUriString: String?,
        sealUriString: String?, signatureUriString: String?
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        val margin = 36f
        val contentWidth = (pageWidth - 2 * margin).toInt()

        val textPaint = TextPaint().apply { color = Color.BLACK; textSize = 11f }
        val tableHeaderPaint = TextPaint().apply { color = Color.WHITE; textSize = 12f; isFakeBoldText = true }
        val borderPaint = Paint().apply { color = Color.BLACK; strokeWidth = 1f; style = Paint.Style.STROKE }
        val linePaint = Paint().apply { color = Color.BLACK; strokeWidth = 1f }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#0F1B2B"); style = Paint.Style.FILL } 

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = margin

        fun drawCurrentHeader() {
            yPos = drawHeader(context, canvas, pageWidth, margin, contentWidth, companyName, companyPhone, companyAddress, logoUriString, "التقرير الختامي العام", "GEN-REP")
        }

        fun startNewPage() {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPos = margin
            drawCurrentHeader()
        }

        drawCurrentHeader()

        val infoPaint = TextPaint().apply { color = Color.BLACK; textSize = 14f; isFakeBoldText = true }
        drawArabicText(canvas, "الفترة: $filterText", pageWidth - margin, yPos, contentWidth, infoPaint)
        yPos += 25f

        val summaryBg = Paint().apply { color = Color.parseColor("#F5F5F5") }
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 40f, summaryBg)
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 40f, borderPaint)
        val summaryY = yPos + 10f
        
        val summaryTextPaint = TextPaint(infoPaint).apply { textSize = 12f }
        val thirdW = contentWidth / 3
        drawArabicText(canvas, "إجمالي الأجور: ${formatAmount(summary.totalEarned)}", pageWidth - margin - 10f, summaryY, thirdW, summaryTextPaint)
        drawArabicText(canvas, "إجمالي السحوبات: ${formatAmount(summary.totalWithdrawn)}", pageWidth - margin - thirdW - 10f, summaryY, thirdW, summaryTextPaint)
        
        val netColor = if (summary.netCost >= 0) Color.parseColor("#2E7D5B") else Color.RED
        val netPaint = TextPaint(summaryTextPaint).apply { color = netColor }
        drawArabicText(canvas, "الصافي: ${formatAmount(summary.netCost)} $currency", pageWidth - margin - 2 * thirdW - 10f, summaryY, thirdW, netPaint)
        yPos += 60f

        // Table Header
        // 5% ID, 35% Name, 15% Days, 15% Earned, 15% Withdrawn, 15% Net
        val colIdW = contentWidth * 0.05f
        val colNameW = contentWidth * 0.35f
        val colDaysW = contentWidth * 0.15f
        val colEarnedW = contentWidth * 0.15f
        val colWithdW = contentWidth * 0.15f
        val colNetW = contentWidth * 0.15f

        val startXId = pageWidth - margin
        val startXName = startXId - colIdW
        val startXDays = startXName - colNameW
        val startXEarned = startXDays - colDaysW
        val startXWithd = startXEarned - colEarnedW
        val startXNet = startXWithd - colWithdW

        val rowHeight = 25f

        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, headerBgPaint)
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, borderPaint)
        
        drawArabicText(canvas, "م", startXId - 2f, yPos + 5f, colIdW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "اسم الموظف", startXName - 5f, yPos + 5f, colNameW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "العمل", startXDays - 5f, yPos + 5f, colDaysW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "الأجور", startXEarned - 5f, yPos + 5f, colEarnedW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "السحوبات", startXWithd - 5f, yPos + 5f, colWithdW.toInt(), tableHeaderPaint)
        drawArabicText(canvas, "الصافي", startXNet - 5f, yPos + 5f, colNetW.toInt(), tableHeaderPaint)
        
        canvas.drawLine(startXName, yPos, startXName, yPos + rowHeight, linePaint)
        canvas.drawLine(startXDays, yPos, startXDays, yPos + rowHeight, linePaint)
        canvas.drawLine(startXEarned, yPos, startXEarned, yPos + rowHeight, linePaint)
        canvas.drawLine(startXWithd, yPos, startXWithd, yPos + rowHeight, linePaint)
        canvas.drawLine(startXNet, yPos, startXNet, yPos + rowHeight, linePaint)
        
        yPos += rowHeight

        var totalDays = 0
        var sumEarned = 0.0
        var sumWithdrawn = 0.0

        for ((index, rec) in records.withIndex()) {
            if (yPos > pageHeight - margin - 150f) {
                startNewPage()
                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, headerBgPaint)
                canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, borderPaint)
                drawArabicText(canvas, "م", startXId - 2f, yPos + 5f, colIdW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "اسم الموظف", startXName - 5f, yPos + 5f, colNameW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "العمل", startXDays - 5f, yPos + 5f, colDaysW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "الأجور", startXEarned - 5f, yPos + 5f, colEarnedW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "السحوبات", startXWithd - 5f, yPos + 5f, colWithdW.toInt(), tableHeaderPaint)
                drawArabicText(canvas, "الصافي", startXNet - 5f, yPos + 5f, colNetW.toInt(), tableHeaderPaint)
                canvas.drawLine(startXName, yPos, startXName, yPos + rowHeight, linePaint)
                canvas.drawLine(startXDays, yPos, startXDays, yPos + rowHeight, linePaint)
                canvas.drawLine(startXEarned, yPos, startXEarned, yPos + rowHeight, linePaint)
                canvas.drawLine(startXWithd, yPos, startXWithd, yPos + rowHeight, linePaint)
                canvas.drawLine(startXNet, yPos, startXNet, yPos + rowHeight, linePaint)
                yPos += rowHeight
            }

            val cellHeight = rowHeight

            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + cellHeight, borderPaint)
            
            canvas.drawLine(startXName, yPos, startXName, yPos + cellHeight, linePaint)
            canvas.drawLine(startXDays, yPos, startXDays, yPos + cellHeight, linePaint)
            canvas.drawLine(startXEarned, yPos, startXEarned, yPos + cellHeight, linePaint)
            canvas.drawLine(startXWithd, yPos, startXWithd, yPos + cellHeight, linePaint)
            canvas.drawLine(startXNet, yPos, startXNet, yPos + cellHeight, linePaint)

            drawArabicText(canvas, "${index + 1}", startXId - 2f, yPos + 5f, colIdW.toInt(), textPaint)
            drawArabicText(canvas, rec.employeeName, startXName - 5f, yPos + 5f, colNameW.toInt(), textPaint)
            drawArabicText(canvas, "${rec.attendanceDays}", startXDays - 5f, yPos + 5f, colDaysW.toInt(), textPaint)
            drawArabicText(canvas, formatAmount(rec.totalAmount), startXEarned - 5f, yPos + 5f, colEarnedW.toInt(), textPaint)
            drawArabicText(canvas, formatAmount(rec.totalWithdrawn), startXWithd - 5f, yPos + 5f, colWithdW.toInt(), textPaint)
            drawArabicText(canvas, formatAmount(rec.netPayable), startXNet - 5f, yPos + 5f, colNetW.toInt(), textPaint)
            
            totalDays += rec.attendanceDays
            sumEarned += rec.totalAmount
            sumWithdrawn += rec.totalWithdrawn

            yPos += cellHeight
        }

        // Totals Row
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, Paint().apply { color = Color.parseColor("#E0E0E0") })
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, borderPaint)
        
        canvas.drawLine(startXDays, yPos, startXDays, yPos + rowHeight, linePaint)
        canvas.drawLine(startXEarned, yPos, startXEarned, yPos + rowHeight, linePaint)
        canvas.drawLine(startXWithd, yPos, startXWithd, yPos + rowHeight, linePaint)
        canvas.drawLine(startXNet, yPos, startXNet, yPos + rowHeight, linePaint)

        val totalPaint = TextPaint(textPaint).apply { isFakeBoldText = true }
        drawArabicText(canvas, "الإجمالي", startXId - 10f, yPos + 5f, (colIdW + colNameW).toInt(), totalPaint)
        drawArabicText(canvas, "$totalDays", startXDays - 5f, yPos + 5f, colDaysW.toInt(), totalPaint)
        drawArabicText(canvas, formatAmount(sumEarned), startXEarned - 5f, yPos + 5f, colEarnedW.toInt(), totalPaint)
        drawArabicText(canvas, formatAmount(sumWithdrawn), startXWithd - 5f, yPos + 5f, colWithdW.toInt(), totalPaint)
        drawArabicText(canvas, formatAmount(sumEarned - sumWithdrawn), startXNet - 5f, yPos + 5f, colNetW.toInt(), totalPaint)
        
        drawFooter(context, canvas, pageWidth, pageHeight, margin, companyFooter, sealUriString, signatureUriString)

        document.finishPage(page)
        writeAndCloseDoc(context, uri, document)
    }
}
