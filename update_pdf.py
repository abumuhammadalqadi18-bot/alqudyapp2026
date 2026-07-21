import sys

with open('app/src/main/java/com/example/util/PdfHelper.kt', 'r') as f:
    content = f.read()

# Update 1: Change signature of generateEmployeeStatementPdf
target1 = """    fun generateEmployeeStatementPdf(
        context: Context,
        uri: Uri,
        employee: EmployeeDetail,
        summary: EmployeeReportSummary,
        transactions: List<TransactionItem>,
        currency: String
    ) {"""
replacement1 = """    fun generateEmployeeStatementPdf(
        context: Context,
        uri: Uri,
        employee: EmployeeDetail,
        summary: EmployeeReportSummary,
        transactions: List<TransactionItem>,
        currency: String,
        companyName: String = "",
        companyPhone: String = "",
        companyAddress: String = "",
        companyFooter: String = "",
        logoUriString: String? = null,
        sealUriString: String? = null,
        signatureUriString: String? = null
    ) {"""

if target1 in content:
    content = content.replace(target1, replacement1)
else:
    print("Could not find target1")

# Update 2: Header section of employee pdf
target2 = """        // Header
        drawArabicText(canvas, "وكالة القاضي لإدارة الأجور", pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f
        
        drawArabicText(canvas, "كشف حساب عامل", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f"""
replacement2 = """        // Header
        val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
        drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 30f
        
        if (companyPhone.isNotBlank() || companyAddress.isNotBlank()) {
            val subHeader = listOf(companyPhone, companyAddress).filter { it.isNotBlank() }.joinToString(" - ")
            drawArabicText(canvas, subHeader, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 20f
        }
        
        drawArabicText(canvas, "كشف حساب عامل", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f"""

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("Could not find target2")

# Update 3: Footer section of employee pdf
target3 = """        val netPaint = TextPaint(headerPaint).apply { color = if (summary.netPayable >= 0) Color.rgb(46, 125, 91) else Color.RED }
        drawArabicText(canvas, "الصافي المتبقي للاستلام: ${String.format(Locale.US, "%,.2f", summary.netPayable)} $currency", pageWidth - margin, yPos, contentWidth, netPaint)"""
replacement3 = """        val netPaint = TextPaint(headerPaint).apply { color = if (summary.netPayable >= 0) Color.rgb(46, 125, 91) else Color.RED }
        drawArabicText(canvas, "الصافي المتبقي للاستلام: ${String.format(Locale.US, "%,.2f", summary.netPayable)} $currency", pageWidth - margin, yPos, contentWidth, netPaint)
        
        yPos += 40f
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 30f
        }"""

if target3 in content:
    content = content.replace(target3, replacement3)
else:
    print("Could not find target3")

# Update 4: Change signature of generateGeneralReportPdf
target4 = """    fun generateGeneralReportPdf(
        context: Context,
        uri: Uri,
        records: List<GeneralEmployeeRecord>,
        summary: GeneralReportSummary,
        participatingCount: Int,
        filterName: String,
        currency: String
    ) {"""
replacement4 = """    fun generateGeneralReportPdf(
        context: Context,
        uri: Uri,
        records: List<GeneralEmployeeRecord>,
        summary: GeneralReportSummary,
        participatingCount: Int,
        filterName: String,
        currency: String,
        companyName: String = "",
        companyPhone: String = "",
        companyAddress: String = "",
        companyFooter: String = "",
        logoUriString: String? = null,
        sealUriString: String? = null,
        signatureUriString: String? = null
    ) {"""

if target4 in content:
    content = content.replace(target4, replacement4)
else:
    print("Could not find target4")

# Update 5: Header section of general pdf
target5 = """        drawArabicText(canvas, "وكالة القاضي لإدارة الأجور", pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 40f

        drawArabicText(canvas, "التقرير الدوري العام للتكاليف", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 25f"""
replacement5 = """        val headerTitle = if (companyName.isNotBlank()) companyName else "وكالة القاضي لإدارة الأجور"
        drawArabicText(canvas, headerTitle, pageWidth / 2f + contentWidth/2, yPos, contentWidth, titlePaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 30f
        
        if (companyPhone.isNotBlank() || companyAddress.isNotBlank()) {
            val subHeader = listOf(companyPhone, companyAddress).filter { it.isNotBlank() }.joinToString(" - ")
            drawArabicText(canvas, subHeader, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
            yPos += 20f
        }

        drawArabicText(canvas, "التقرير الدوري العام للتكاليف", pageWidth / 2f + contentWidth/2, yPos, contentWidth, headerPaint, Layout.Alignment.ALIGN_CENTER)
        yPos += 25f"""

if target5 in content:
    content = content.replace(target5, replacement5)
else:
    print("Could not find target5")

# Update 6: Footer section of general pdf
target6 = """        document.finishPage(page)

        try {"""
replacement6 = """
        yPos += 40f
        if (companyFooter.isNotBlank()) {
            drawArabicText(canvas, companyFooter, pageWidth / 2f + contentWidth/2, yPos, contentWidth, textPaint, Layout.Alignment.ALIGN_CENTER)
        }

        document.finishPage(page)

        try {"""

if target6 in content:
    content = content.replace(target6, replacement6)
else:
    print("Could not find target6")


with open('app/src/main/java/com/example/util/PdfHelper.kt', 'w') as f:
    f.write(content)

print("Updated PdfHelper")
