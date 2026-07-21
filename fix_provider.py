import sys

with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'r') as f:
    content = f.read()

target = """            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(
                    appContainer.employeeRepository,
                    appContainer.wageRepository,
                    appContainer.financeRepository
                ) as T
            }"""
replacement = """            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(
                    appContainer.employeeRepository,
                    appContainer.wageRepository,
                    appContainer.financeRepository,
                    appContainer.settingsRepository
                ) as T
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'w') as f:
        f.write(content)
    print("Updated AppViewModelProvider.kt")
else:
    print("could not find target in AppViewModelProvider")
