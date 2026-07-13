import re

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

# Replace the generation logic with a Flow based logic
imports_to_add = """
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import com.example.domain.model.DayType
"""

if 'import kotlinx.coroutines.flow.combine' not in content:
    content = content.replace('import kotlinx.coroutines.flow.firstOrNull', 'import kotlinx.coroutines.flow.firstOrNull\n' + imports_to_add)

new_viewmodel_body = """class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState(isLoading = true))
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val reportPeriod = MutableStateFlow<Pair<Long, Long>?>(null)

    init {
        viewModelScope.launch {
            reportPeriod.flatMapLatest { period ->
                if (period == null) {
                    flowOf(ReportUiState(isLoading = false))
                } else {
                    val (startDate, endDate) = period
                    combine(
                        employeeRepository.getActiveEmployees(),
                        wageRepository.getRecordsInRange(startDate, endDate),
                        financeRepository.getWithdrawalsInRange(startDate, endDate),
                        financeRepository.getAdjustmentsInRange(startDate, endDate)
                    ) { employees, wages, withdrawals, adjustments ->
                        val summaries = mutableListOf<EmployeeReportSummary>()
                        val allTransactions = mutableListOf<TransactionItem>()

                        for (employee in employees) {
                            val empWages = wages.filter { it.employeeId == employee.id }
                            val totalEarned = empWages.sumOf { it.finalAmount }
                            val attendanceDays = empWages.count { it.dayType == DayType.FULL_DAY || it.dayType == DayType.HALF_DAY }

                            val empWithdrawals = withdrawals.filter { it.employeeId == employee.id }
                            val totalWithdrawn = empWithdrawals.sumOf { it.amount }

                            empWages.forEach { w ->
                                allTransactions.add(
                                    TransactionItem(
                                        id = "w_${w.id}",
                                        employeeId = employee.id,
                                        employeeName = employee.name,
                                        amount = w.finalAmount,
                                        date = w.wageDate,
                                        type = TransactionType.WAGE,
                                        description = w.notes ?: "حضور"
                                    )
                                )
                            }

                            empWithdrawals.forEach { w ->
                                allTransactions.add(
                                    TransactionItem(
                                        id = "with_${w.id}",
                                        employeeId = employee.id,
                                        employeeName = employee.name,
                                        amount = w.amount,
                                        date = w.withdrawalDate,
                                        type = TransactionType.WITHDRAWAL,
                                        description = w.description ?: "سحب"
                                    )
                                )
                            }

                            val empAdjustments = adjustments.filter { it.employeeId == employee.id }
                            val totalBonuses = empAdjustments.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
                            val totalDeductions = empAdjustments.filter { it.type == AdjustmentType.DEDUCTION }.sumOf { it.amount }

                            empAdjustments.forEach { a ->
                                allTransactions.add(
                                    TransactionItem(
                                        id = "adj_${a.id}",
                                        employeeId = employee.id,
                                        employeeName = employee.name,
                                        amount = a.amount,
                                        date = a.adjustmentDate,
                                        type = if (a.type == AdjustmentType.BONUS) TransactionType.BONUS else TransactionType.DEDUCTION,
                                        description = a.reason ?: "تعديل"
                                    )
                                )
                            }

                            val netPayable = (totalEarned + totalBonuses) - (totalWithdrawn + totalDeductions)
                            val isOverdrawn = netPayable < 0

                            summaries.add(
                                EmployeeReportSummary(
                                    employeeId = employee.id,
                                    employeeName = employee.name,
                                    attendanceDays = attendanceDays,
                                    totalEarned = totalEarned,
                                    totalWithdrawn = totalWithdrawn,
                                    totalBonuses = totalBonuses,
                                    totalDeductions = totalDeductions,
                                    netPayable = netPayable,
                                    isOverdrawn = isOverdrawn
                                )
                            )
                        }

                        ReportUiState(
                            summaries = summaries,
                            transactions = allTransactions.sortedByDescending { it.date },
                            isLoading = false
                        )
                    }
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun generateReportForPeriod(startDate: Long, endDate: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        reportPeriod.value = Pair(startDate, endDate)
    }

    suspend fun getNetPayableForEmployee(employeeId: Long): Double {
        val wages = wageRepository.getRecordsForEmployee(employeeId).firstOrNull() ?: emptyList()
        val totalEarned = wages.sumOf { it.finalAmount }
        
        val withdrawals = financeRepository.getWithdrawalsForEmployee(employeeId).firstOrNull() ?: emptyList()
        val totalWithdrawn = withdrawals.sumOf { it.amount }
        
        val adjustments = financeRepository.getAdjustmentsForEmployee(employeeId).firstOrNull() ?: emptyList()
        val totalBonuses = adjustments.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
        val totalDeductions = adjustments.filter { it.type == AdjustmentType.DEDUCTION }.sumOf { it.amount }
        
        return (totalEarned + totalBonuses) - (totalWithdrawn + totalDeductions)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
"""

# Now replace everything from "class ReportViewModel(" to the end of the file
pattern = r'class ReportViewModel\(.*'
content = re.sub(pattern, new_viewmodel_body, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'w') as f:
    f.write(content)

