import sys

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

target = """data class ReportUiState(
    val selectedTab: Int = 0,
    
    // General Report"""

replacement = """data class ReportUiState(
    val selectedTab: Int = 0,
    val settings: SettingsUiState = SettingsUiState(),
    
    // General Report"""

if target in content:
    content = content.replace(target, replacement)
    
target2 = """            ReportUiState(
                generalSummary = generalSummary,
                generalRecords = generalRecords,
                employeeSummary = employeeSummary,
                employeeRecords = employeeRecords,
                isLoading = false
            )"""
            
replacement2 = """            ReportUiState(
                settings = settings,
                generalSummary = generalSummary,
                generalRecords = generalRecords,
                employeeSummary = employeeSummary,
                employeeRecords = employeeRecords,
                isLoading = false
            )"""

if target2 in content:
    content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'w') as f:
    f.write(content)

print("Fixed ReportUiState")
