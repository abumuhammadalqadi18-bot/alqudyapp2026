package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.WageRepository
import com.example.domain.model.AdjustmentType
import com.example.domain.model.DayType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.TimeZone

data class EmployeeDetail(
    val id: Long,
    val name: String,
    val jobTitle: String,
    val dailyWage: Double,
    val hireDate: Long
)

data class GeneralReportSummary(
    val totalEarned: Double,
    val totalWithdrawn: Double,
    val netCost: Double,
    val participatingEmployeesCount: Int
)

data class GeneralEmployeeRecord(
    val employeeName: String,
    val attendanceDays: Int,
    val totalAmount: Double,
    val lastTransactionDate: Long
)

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
    val employees: List<EmployeeDetail> = emptyList(),
    val selectedEmployeeId: Long? = null,
    val employeeTransactions: List<TransactionItem> = emptyList(),
    val employeeSummary: EmployeeReportSummary? = null,
    
    val generalFilter: String = "آخر 30 يوم (تقرير شهري)",
    val generalSummary: GeneralReportSummary = GeneralReportSummary(0.0, 0.0, 0.0, 0),
    val generalRecords: List<GeneralEmployeeRecord> = emptyList(),
    
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val _selectedEmployeeId = MutableStateFlow<Long?>(null)
    private val _generalFilter = MutableStateFlow("آخر 30 يوم (تقرير شهري)")
    
    private val allEmployeesFlow = employeeRepository.getActiveEmployees().map { list ->
        list.map { EmployeeDetail(it.id, it.name, it.jobTitle, it.currentDailyWage, it.hireDate) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val allWagesFlow = wageRepository.getRecordsInRange(0L, Long.MAX_VALUE)
    private val allWithdrawalsFlow = financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE)
    private val allAdjustmentsFlow = financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE)
    
    private val transactionsFlow = combine(
        allWagesFlow,
        allWithdrawalsFlow,
        allAdjustmentsFlow
    ) { wages, withdrawals, adjustments ->
        Triple(wages, withdrawals, adjustments)
    }

    val uiState: StateFlow<ReportUiState> = combine(
        allEmployeesFlow,
        _selectedEmployeeId,
        _generalFilter,
        transactionsFlow
    ) { employees, selectedEmpId, generalFilter, txs ->
        
        val wages = txs.first
        val withdrawals = txs.second
        val adjustments = txs.third

        val (startDate, endDate) = getPeriodDates(generalFilter)
        
        val filteredWages = wages.filter { it.wageDate in startDate..endDate }
        val filteredWithdrawals = withdrawals.filter { it.withdrawalDate in startDate..endDate }
        val filteredAdjustments = adjustments.filter { it.adjustmentDate in startDate..endDate }
        
        val totalEarned = filteredWages.sumOf { it.finalAmount } + filteredAdjustments.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
        val totalWithdrawn = filteredWithdrawals.sumOf { it.amount } + filteredAdjustments.filter { it.type == AdjustmentType.DEDUCTION }.sumOf { it.amount }
        val netCost = totalEarned - totalWithdrawn
        
        val participatingEmpIds = (filteredWages.map { it.employeeId } + filteredWithdrawals.map { it.employeeId } + filteredAdjustments.map { it.employeeId }).distinct()
        
        val generalRecords = participatingEmpIds.mapNotNull { empId ->
            val emp = employees.find { it.id == empId } ?: return@mapNotNull null
            val empWages = filteredWages.filter { it.employeeId == empId }
            val empWithdrawals = filteredWithdrawals.filter { it.employeeId == empId }
            val empAdjs = filteredAdjustments.filter { it.employeeId == empId }
            
            val empEarned = empWages.sumOf { it.finalAmount } + empAdjs.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
            val attendanceDays = empWages.count { it.dayType == DayType.FULL_DAY || it.dayType == DayType.HALF_DAY }
            val lastTxDate = listOf(
                empWages.maxOfOrNull { it.wageDate } ?: 0L,
                empWithdrawals.maxOfOrNull { it.withdrawalDate } ?: 0L,
                empAdjs.maxOfOrNull { it.adjustmentDate } ?: 0L
            ).maxOrNull() ?: 0L
            
            GeneralEmployeeRecord(
                employeeName = emp.name,
                attendanceDays = attendanceDays,
                totalAmount = empEarned,
                lastTransactionDate = lastTxDate
            )
        }.sortedByDescending { it.lastTransactionDate }
        
        val generalSummary = GeneralReportSummary(totalEarned, totalWithdrawn, netCost, participatingEmpIds.size)
        
        var employeeSummary: EmployeeReportSummary? = null
        var employeeTransactions = emptyList<TransactionItem>()
        
        if (selectedEmpId != null) {
            val emp = employees.find { it.id == selectedEmpId }
            if (emp != null) {
                val empWages = wages.filter { it.employeeId == selectedEmpId }
                val empWithdrawals = withdrawals.filter { it.employeeId == selectedEmpId }
                val empAdjs = adjustments.filter { it.employeeId == selectedEmpId }
                
                val empTotalEarned = empWages.sumOf { it.finalAmount } + empAdjs.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
                val empTotalWithdrawn = empWithdrawals.sumOf { it.amount } + empAdjs.filter { it.type == AdjustmentType.DEDUCTION }.sumOf { it.amount }
                val empNetPayable = empTotalEarned - empTotalWithdrawn
                val attendanceDays = empWages.count { it.dayType == DayType.FULL_DAY || it.dayType == DayType.HALF_DAY }
                
                employeeSummary = EmployeeReportSummary(
                    employeeId = emp.id,
                    employeeName = emp.name,
                    attendanceDays = attendanceDays,
                    totalEarned = empTotalEarned,
                    totalWithdrawn = empTotalWithdrawn,
                    totalBonuses = empAdjs.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount },
                    totalDeductions = empAdjs.filter { it.type == AdjustmentType.DEDUCTION }.sumOf { it.amount },
                    netPayable = empNetPayable,
                    isOverdrawn = empNetPayable < 0
                )
                
                val txs = mutableListOf<TransactionItem>()
                empWages.forEach { w ->
                    txs.add(TransactionItem("w_${w.id}", w.employeeId, emp.name, w.finalAmount, w.wageDate, TransactionType.WAGE, w.notes ?: "تسجيل حضور وعمل يومي"))
                }
                empWithdrawals.forEach { w ->
                    txs.add(TransactionItem("with_${w.id}", w.employeeId, emp.name, w.amount, w.withdrawalDate, TransactionType.WITHDRAWAL, w.description ?: "سلفة على راتب"))
                }
                empAdjs.forEach { a ->
                    txs.add(TransactionItem("adj_${a.id}", a.employeeId, emp.name, a.amount, a.adjustmentDate, if (a.type == AdjustmentType.BONUS) TransactionType.BONUS else TransactionType.DEDUCTION, a.reason ?: "تعديل مالي"))
                }
                employeeTransactions = txs.sortedByDescending { it.date }
            }
        }
        
        ReportUiState(
            employees = employees,
            selectedEmployeeId = selectedEmpId,
            employeeTransactions = employeeTransactions,
            employeeSummary = employeeSummary,
            generalFilter = generalFilter,
            generalSummary = generalSummary,
            generalRecords = generalRecords,
            isLoading = false,
            errorMessage = null
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, ReportUiState(isLoading = true))

    fun selectEmployee(id: Long?) {
        _selectedEmployeeId.value = id
    }
    
    fun setGeneralFilter(filter: String) {
        _generalFilter.value = filter
    }

    private fun getPeriodDates(filter: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val endDate = Long.MAX_VALUE
        val startDate = when (filter) {
            "آخر 24 ساعة" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            "آخر 7 أيام" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            "آخر 30 يوم (تقرير شهري)" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            else -> 0L
        }
        return Pair(startDate, endDate)
    }

    suspend fun getNetPayableForEmployee(employeeId: Long): Double {
        val wages = wageRepository.getRecordsInRange(0L, Long.MAX_VALUE).firstOrNull() ?: emptyList()
        val withdrawals = financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE).firstOrNull() ?: emptyList()
        val adjustments = financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE).firstOrNull() ?: emptyList()
        
        val empWages = wages.filter { it.employeeId == employeeId }
        val empWithdrawals = withdrawals.filter { it.employeeId == employeeId }
        val empAdjs = adjustments.filter { it.employeeId == employeeId }
        
        val totalEarned = empWages.sumOf { it.finalAmount } + empAdjs.filter { it.type == com.example.domain.model.AdjustmentType.BONUS }.sumOf { it.amount }
        val totalWithdrawn = empWithdrawals.sumOf { it.amount } + empAdjs.filter { it.type == com.example.domain.model.AdjustmentType.DEDUCTION }.sumOf { it.amount }
        
        return totalEarned - totalWithdrawn
    }
}
