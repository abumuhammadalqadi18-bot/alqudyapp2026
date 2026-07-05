package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.WageRepository
import com.example.domain.model.AdjustmentType
import com.example.domain.model.DayType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.TimeZone

data class EmployeeReportSummary(
    val employeeId: Long,
    val employeeName: String,
    val attendanceDays: Int,
    val totalEarned: Double,
    val totalWithdrawn: Double,
    val totalBonuses: Double,
    val totalDeductions: Double,
    val netPayable: Double,
    val isOverdrawn: Boolean
)

enum class TransactionType {
    WAGE, WITHDRAWAL, BONUS, DEDUCTION
}

data class TransactionItem(
    val id: String,
    val employeeId: Long,
    val employeeName: String,
    val amount: Double,
    val date: Long,
    val type: TransactionType,
    val description: String
)

data class ReportUiState(
    val summaries: List<EmployeeReportSummary> = emptyList(),
    val transactions: List<TransactionItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val reportPeriod = MutableStateFlow<Pair<Long, Long>>(getPeriodDates("يومي"))

    val uiState: StateFlow<ReportUiState> = reportPeriod.flatMapLatest { period ->
        val (startDate, endDate) = period
        combine(
            employeeRepository.getActiveEmployees(),
            wageRepository.getRecordsInRange(0L, Long.MAX_VALUE),
            financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE),
            financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE)
        ) { employees, allWages, allWithdrawals, allAdjustments ->
            val wages = allWages.filter { it.wageDate in startDate..endDate }
            val withdrawals = allWithdrawals.filter { it.withdrawalDate in startDate..endDate }
            val adjustments = allAdjustments.filter { it.adjustmentDate in startDate..endDate }

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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ReportUiState(isLoading = true)
    )

    private fun getPeriodDates(filter: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val endDate = Long.MAX_VALUE
        val startDate = when (filter) {
            "يومي" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            "أسبوعي" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            "شهري" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            else -> 0L // All time
        }
        return Pair(startDate, endDate)
    }

    fun loadReports(filter: String = "يومي") {
        reportPeriod.value = getPeriodDates(filter)
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
}
