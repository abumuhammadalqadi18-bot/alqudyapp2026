import re

with open("app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt", "r") as f:
    content = f.read()

start_marker = "@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun GeneralReportsTab"
end_marker = "    LazyColumn("

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)

if start_idx != -1 and end_idx != -1:
    new_func = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReportsTab(
    state: ReportUiState,
    viewModel: ReportViewModel,
    context: Context,
    currency: String
) {
    val filters = listOf("آخر 24 ساعة", "آخر 7 أيام", "آخر 30 يوم (تقرير شهري)")
    
    var previewPdfFile by remember { mutableStateOf<java.io.File?>(null) }
    var pendingExportName by remember { mutableStateOf("") }
    
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null && previewPdfFile != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    previewPdfFile!!.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    if (previewPdfFile != null) {
        com.example.ui.components.PdfPreviewDialog(
            pdfFile = previewPdfFile!!,
            onDismiss = { previewPdfFile = null },
            onExport = { pdfLauncher.launch(pendingExportName) }
        )
    }

    val onGeneratePdf = { name: String ->
        val tempFile = java.io.File(context.cacheDir, name)
        PdfHelper.generateGeneralReportPdf(
            context = context,
            uri = android.net.Uri.fromFile(tempFile),
            filterText = state.generalFilter,
            summary = state.generalSummary,
            records = state.generalRecords,
            currency = currency,
            companyName = state.settings.companyName,
            companyPhone = state.settings.phoneNumbers,
            companyAddress = state.settings.address,
            companyFooter = state.settings.footerNote,
            logoUriString = state.settings.logoUri,
            sealUriString = state.settings.sealUri,
            signatureUriString = state.settings.signatureUri
        )
        pendingExportName = name
        previewPdfFile = tempFile
    }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            CsvExportHelper.exportGeneralReportCsv(context, uri, state.generalRecords, state.generalFilter, currency)
        }
    }

"""
    new_content = content[:start_idx] + new_func + content[end_idx:]
    with open("app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt", "w") as f:
        f.write(new_content)
    print("Fixed successfully")
else:
    print("Markers not found")
