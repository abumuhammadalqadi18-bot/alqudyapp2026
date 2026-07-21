import sys

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'r') as f:
    content = f.read()

# Fix 1: Header of general report
target1 = """        val contentWidth = (pageWidth - 2 * margin).toInt()

        drawArabicText(canvas, "وكالة القاضي لإدارة الأجور", pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f

        drawArabicText(canvas, "التقرير الدوري العام للتكاليف", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)"""

replacement1 = """        val contentWidth = (pageWidth - 2 * margin).toInt()

        val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
        drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 30f
        
        if (companyPhone.isNotBlank() || companyAddress.isNotBlank()) {
            val subHeader = listOf(companyPhone, companyAddress).filter { it.isNotBlank() }.joinToString(" - ")
            drawArabicText(canvas, subHeader, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 20f
        }

        drawArabicText(canvas, "التقرير الدوري العام للتكاليف", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)"""

if target1 in content:
    content = content.replace(target1, replacement1)
    
# Fix 2: Remove duplicated footer lines from target3 replacement issue
target2 = """        yPos += 40f
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f
        }
        
        yPos += 40f
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
        }"""
        
replacement2 = """        yPos += 40f
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f
        }"""
        
if target2 in content:
    content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'w') as f:
    f.write(content)

print("Fixed PdfHelper")
