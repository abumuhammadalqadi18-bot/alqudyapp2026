import re

with open("app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt", "r") as f:
    content = f.read()

replacement = """
    suspend fun getNetPayableForEmployee(employeeId: Long): Double {
        val wages = kotlinx.coroutines.flow.firstOrNull(wageRepository.getRecordsInRange(0L, Long.MAX_VALUE)) ?: emptyList()
        val withdrawals = kotlinx.coroutines.flow.firstOrNull(financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE)) ?: emptyList()
        val adjustments = kotlinx.coroutines.flow.firstOrNull(financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE)) ?: emptyList()
        
        val empWages = wages.filter { it.employeeId == employeeId }
        val empWithdrawals = withdrawals.filter { it.employeeId == employeeId }
        val empAdjs = adjustments.filter { it.employeeId == employeeId }
        
        val totalEarned = empWages.sumOf { it.finalAmount } + empAdjs.filter { it.type == com.example.domain.model.AdjustmentType.BONUS }.sumOf { it.amount }
        val totalWithdrawn = empWithdrawals.sumOf { it.amount } + empAdjs.filter { it.type == com.example.domain.model.AdjustmentType.DEDUCTION }.sumOf { it.amount }
        
        return totalEarned - totalWithdrawn
    }
"""

content = re.sub(
    r"suspend fun getNetPayableForEmployee\(employeeId: Long\): Double \{.*?return 0\.0.*?\}",
    replacement.strip(),
    content,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt", "w") as f:
    f.write(content)
