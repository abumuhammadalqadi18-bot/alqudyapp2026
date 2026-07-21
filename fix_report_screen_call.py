import sys

with open('app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt', 'r') as f:
    content = f.read()

target = """            PdfHelper.generateGeneralReportPdf(
                context, uri, state.generalRecords, state.generalSummary, 
                state.generalSummary.participatingEmployeesCount, state.generalFilter, currency
            )"""

replacement = """            PdfHelper.generateGeneralReportPdf(
                context = context,
                uri = uri,
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
            )"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt', 'w') as f:
        f.write(content)
    print("Fixed ReportScreen")
else:
    print("Target not found in ReportScreen")
