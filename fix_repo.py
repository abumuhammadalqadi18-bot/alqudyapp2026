import sys

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

target = """class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {"""
replacement = """import com.example.data.repository.SettingsRepository

class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("could not find repo replacement")
    
with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'w') as f:
    f.write(content)
print("Updated ReportViewModel Repo")
