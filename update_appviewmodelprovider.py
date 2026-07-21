import sys

with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'r') as f:
    content = f.read()

target = """        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlQadiApplication)
            val container = application.container
            ReportViewModel(
                container.employeeRepository,
                container.wageRepository,
                container.financeRepository
            )
        }"""
replacement = """        initializer {
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AlQadiApplication)
            val container = application.container
            ReportViewModel(
                container.employeeRepository,
                container.wageRepository,
                container.financeRepository,
                container.settingsRepository
            )
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'w') as f:
        f.write(content)
    print("Updated AppViewModelProvider.kt")
else:
    print("could not find target in AppViewModelProvider")
