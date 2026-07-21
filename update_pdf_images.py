import sys

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'r') as f:
    content = f.read()

imports = """import android.graphics.BitmapFactory
import android.graphics.Bitmap
"""

if "import android.graphics.BitmapFactory" not in content:
    content = content.replace("import android.graphics.Canvas\n", "import android.graphics.Canvas\n" + imports)

draw_bitmap_func = """
    private fun drawBitmap(context: Context, canvas: Canvas, uriString: String?, x: Float, y: Float, width: Float, height: Float) {
        if (uriString.isNullOrBlank()) return
        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), true)
                    canvas.drawBitmap(scaledBitmap, x, y, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
"""

if "private fun drawBitmap" not in content:
    content = content.replace("    private fun drawArabicText", draw_bitmap_func + "    private fun drawArabicText")


# In generateEmployeeStatementPdf
target_emp_header = """        val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
        drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 30f"""

replacement_emp_header = """        val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
        drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        
        // Draw Logo
        drawBitmap(context, canvas, logoUriString, margin, yPos - 20f, 60f, 60f)
        
        yPos += 30f"""

if target_emp_header in content:
    content = content.replace(target_emp_header, replacement_emp_header)

target_emp_footer = """        yPos += 40f
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f
        }"""

replacement_emp_footer = """        yPos += 40f
        
        val footerY = yPos
        // Draw Signature and Seal
        drawBitmap(context, canvas, signatureUriString, margin + 50f, footerY, 100f, 50f)
        drawBitmap(context, canvas, sealUriString, pageWidth - margin - 120f, footerY, 80f, 80f)
        
        yPos += 80f
        
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f
        }"""

if target_emp_footer in content:
    content = content.replace(target_emp_footer, replacement_emp_footer)

# In generateGeneralReportPdf
target_gen_header = """        val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
        drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 30f"""

replacement_gen_header = """        val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
        drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        
        // Draw Logo
        drawBitmap(context, canvas, logoUriString, margin, yPos - 20f, 60f, 60f)
        
        yPos += 30f"""

if target_gen_header in content:
    content = content.replace(target_gen_header, replacement_gen_header)

target_gen_footer = """        yPos += 40f
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f
        }"""

replacement_gen_footer = """        yPos += 40f
        
        val footerY = yPos
        // Draw Signature and Seal
        drawBitmap(context, canvas, signatureUriString, margin + 50f, footerY, 100f, 50f)
        drawBitmap(context, canvas, sealUriString, pageWidth - margin - 120f, footerY, 80f, 80f)
        
        yPos += 80f
        
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f
        }"""

if target_gen_footer in content:
    content = content.replace(target_gen_footer, replacement_gen_footer)

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'w') as f:
    f.write(content)

print("Added images support to PdfHelper")
