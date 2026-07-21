import sys

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'r') as f:
    content = f.read()

content = content.replace("summary.netBalance", "summary.netPayable")

target_note = """            drawArabicText(canvas, tx.description, x - 5f, yPos + 5f, colWidths[3].toInt(), textPaint)
            x -= colWidths[3]
            drawArabicText(canvas, tx.note, x - 5f, yPos + 5f, colWidths[4].toInt(), textPaint)"""

replacement_note = """            drawArabicText(canvas, tx.description, x - 5f, yPos + 5f, colWidths[3].toInt(), textPaint)
            x -= colWidths[3]
            drawArabicText(canvas, "", x - 5f, yPos + 5f, colWidths[4].toInt(), textPaint)"""

content = content.replace(target_note, replacement_note)

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'w') as f:
    f.write(content)

print("Fixed PdfHelper")
