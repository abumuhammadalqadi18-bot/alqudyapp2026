import sys

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

target = """        ReportUiState(
            employees = employees,
            selectedEmployeeId = selectedEmpId,"""

replacement = """        ReportUiState(
            settings = settingsState,
            employees = employees,
            selectedEmployeeId = selectedEmpId,"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'w') as f:
        f.write(content)
    print("Fixed settings in ReportUiState")
else:
    print("Target not found")
