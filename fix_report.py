import sys

with open('app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt', 'r') as f:
    content = f.read()

target = """                PdfHelper.generateEmployeeStatementPdf(context, uri, empDetail, state.employeeSummary, state.employeeTransactions, currency)"""
replacement = """                PdfHelper.generateEmployeeStatementPdf(
                    context = context,
                    uri = uri,
                    employee = empDetail,
                    summary = state.employeeSummary,
                    transactions = state.employeeTransactions,
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

target2 = """            PdfHelper.generateGeneralReportPdf(context, uri, state.generalRecords, state.generalSummary, state.participatingCount, state.filterName, currency)"""
replacement2 = """            PdfHelper.generateGeneralReportPdf(
                context = context,
                uri = uri,
                records = state.generalRecords,
                summary = state.generalSummary,
                participatingCount = state.participatingCount,
                filterName = state.filterName,
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
print("Updated ReportScreen.kt")
