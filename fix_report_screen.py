import sys

with open('app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt', 'r') as f:
    content = f.read()

target1 = """            PdfHelper.generateGeneralReportPdf(
                context, uri, state.generalRecords, state.generalSummary, 
                state.generalSummary.participatingEmployeesCount, state.generalFilter, currency
            )"""

replacement1 = """            PdfHelper.generateGeneralReportPdf(
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

if target1 in content:
    content = content.replace(target1, replacement1)

target2 = """            PdfHelper.generateEmployeeReportPdf(
                context, uri, state.employeeRecords, state.employeeSummary, 
                state.employeeFilter, currency
            )"""

replacement2 = """            PdfHelper.generateEmployeeReportPdf(
                context = context,
                uri = uri,
                filterText = state.employeeFilter,
                summary = state.employeeSummary,
                records = state.employeeRecords,
                currency = currency,
                companyName = state.settings.companyName,
                companyPhone = state.settings.phoneNumbers,
                companyAddress = state.settings.address,
                companyFooter = state.settings.footerNote,
                logoUriString = state.settings.logoUri,
                sealUriString = state.settings.sealUri,
                signatureUriString = state.settings.signatureUri
            )"""

if target2 in content:
    content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt', 'w') as f:
    f.write(content)

print("Fixed ReportScreen calls")
