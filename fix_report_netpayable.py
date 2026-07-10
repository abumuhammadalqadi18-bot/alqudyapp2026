import re

with open("app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt", "r") as f:
    content = f.read()

replacement = """
    suspend fun getNetPayableForEmployee(employeeId: Long): Double {
        val wages = wageRepository.getRecordsInRange(0L, Long.MAX_VALUE).kotlinx.coroutines.flow.firstOrNull() ?: emptyList()
        val withdrawals = financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE).kotlinx.coroutines.flow.firstOrNull() ?: emptyList()
        val adjustments = financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE).kotlinx.coroutines.flow.firstOrNull() ?: emptyList()
"""

# Actually, I should just add import kotlinx.coroutines.flow.firstOrNull and use .firstOrNull()

content = content.replace("kotlinx.coroutines.flow.firstOrNull(wageRepository.getRecordsInRange(0L, Long.MAX_VALUE)) ?: emptyList()", "wageRepository.getRecordsInRange(0L, Long.MAX_VALUE).kotlinx.coroutines.flow.firstOrNull() ?: emptyList()")
content = content.replace("kotlinx.coroutines.flow.firstOrNull(financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE)) ?: emptyList()", "financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE).kotlinx.coroutines.flow.firstOrNull() ?: emptyList()")
content = content.replace("kotlinx.coroutines.flow.firstOrNull(financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE)) ?: emptyList()", "financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE).kotlinx.coroutines.flow.firstOrNull() ?: emptyList()")

content = content.replace(".kotlinx.coroutines.flow.firstOrNull()", ".firstOrNull()")

if "import kotlinx.coroutines.flow.firstOrNull" not in content:
    content = content.replace("import kotlinx.coroutines.flow.catch", "import kotlinx.coroutines.flow.catch\nimport kotlinx.coroutines.flow.firstOrNull")

with open("app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt", "w") as f:
    f.write(content)

