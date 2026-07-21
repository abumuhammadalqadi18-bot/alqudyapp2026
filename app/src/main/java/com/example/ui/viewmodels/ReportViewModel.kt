package com.example.ui.viewmodels
import com.example.data.repository.SettingsRepository
import com.example.data.repository.CompanyProfileRepository
import com.example.ui.viewmodels.SettingsUiState

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
import kotlinx.coroutines.launch
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
    val totalWithdrawn: Double,
    val netPayable: Double,
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
    val errorMessage: String? = null,
    val settings: SettingsUiState = SettingsUiState()
)


class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository,
    private val settingsRepository: SettingsRepository,
    private val companyProfileRepository: CompanyProfileRepository
) : ViewModel() {

    private val _selectedEmployeeId = MutableStateFlow<Long?>(null)
    private val _generalFilter = MutableStateFlow("آخر 30 يوم (تقرير شهري)")

    private val allEmployeesFlow = employeeRepository.getActiveEmployees().map { list ->
        list.map { EmployeeDetail(it.id, it.name, it.jobTitle, it.currentDailyWage, it.hireDate) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Reactive transactions flow: now depends on _generalFilter to limit data fetched
    // This avoids loading ALL records into memory - only the selected period is fetched
    private val transactionsFlow = combine(
        _generalFilter
    ) { filter ->
        val (startDate, endDate) = getPeriodDates(filter[0])
        val wages = wageRepository.getRecordsInRange(startDate, endDate).firstOrNull() ?: emptyList()
        val withdrawals = financeRepository.getWithdrawalsInRange(startDate, endDate).firstOrNull() ?: emptyList()
        val adjustments = financeRepository.getAdjustmentsInRange(startDate, endDate).firstOrNull() ?: emptyList()
        Triple(wages, withdrawals, adjustments)
    }

    private val settingsFlow = companyProfileRepository.getProfileFlow().map { profile ->
        if (profile != null) {
            SettingsUiState(
                companyName = profile.companyName,
                phoneNumbers = profile.phoneNumbers,
                address = profile.address,
                footerNote = profile.footerNote,
                logoUri = profile.logoUri,
                sealUri = profile.sealUri,
                signatureUri = profile.signatureUri
            )
        } else {
            SettingsUiState()
        }
    }

    val uiState: StateFlow<ReportUiState> = combine(
        allEmployeesFlow,
        _selectedEmployeeId,
        _generalFilter,
        transactionsFlow,
        settingsFlow
    ) { employees, selectedEmpId, generalFilter, txs, settingsState ->

        val wages = txs.first
        val withdrawals = txs.second
        val adjustments = txs.third

        // Data is already filtered by period from the reactive flow
        val totalEarned = wages.sumOf { it.finalAmount } + adjustments.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
        val totalWithdrawn = withdrawals.sumOf { it.amount } + adjustments.filter { it.type == AdjustmentType.DEDUCTION }.sumOf { it.amount }
        val netCost = totalEarned - totalWithdrawn

        val participatingEmpIds = (wages.map { it.employeeId } + withdrawals.map { it.employeeId } + adjustments.map { it.employeeId }).distinct()

        val generalRecords = participatingEmpIds.mapNotNull { empId ->
            val emp = employees.find { it.id == empId } ?: return@mapNotNull null
            val empWages = wages.filter { it.employeeId == empId }
            val empWithdrawals = withdrawals.filter { it.employeeId == empId }
            val empAdjs = adjustments.filter { it.employeeId == empId }

            val empEarned = empWages.sumOf { it.finalAmount } + empAdjs.filter { it.type == AdjustmentType.BONUS }.sumOf { it.amount }
            val empWithdrawn = empWithdrawals.sumOf { it.amount } + empAdjs.filter { it.type == AdjustmentType.DEDUCTION }.sumOf { it.amount }
            val empNetPayable = empEarned - empWithdrawn
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
                totalWithdrawn = empWithdrawn,
                netPayable = empNetPayable,
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
            settings = settingsState,
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
        // Set endDate to end of today (23:59:59.999)
        val endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)
        val endDate = endCalendar.timeInMillis
        return Pair(startDate, endDate)
    }

    /**
     * Uses aggregate DB queries (SUM) instead of loading all records.
     */
    suspend fun getNetPayableForEmployee(employeeId: Long): Double {
        val totalWages = wageRepository.getTotalWageForEmployee(employeeId)
        val totalBonuses = financeRepository.getTotalBonusesForEmployee(employeeId)
        val totalWithdrawals = financeRepository.getTotalWithdrawalForEmployee(employeeId)
        val totalDeductions = financeRepository.getTotalDeductionsForEmployee(employeeId)
        return (totalWages + totalBonuses) - (totalWithdrawals + totalDeductions)
    }

    /**
     * Uses aggregate DB queries (SUM) instead of loading all records.
     */
    suspend fun getEmployeeFinancialSummary(employeeId: Long): FinancialSummary {
        val totalWages = wageRepository.getTotalWageForEmployee(employeeId)
        val totalBonuses = financeRepository.getTotalBonusesForEmployee(employeeId)
        val totalWithdrawals = financeRepository.getTotalWithdrawalForEmployee(employeeId)
        val totalDeductions = financeRepository.getTotalDeductionsForEmployee(employeeId)
        val totalEarned = totalWages + totalBonuses
        val totalWithdrawn = totalWithdrawals + totalDeductions
        return FinancialSummary(totalEarned, totalWithdrawn, totalEarned - totalWithdrawn)
    }
}

// Extensions for Payment Screen
data class FinancialSummary(
    val totalEarned: Double,
    val totalWithdrawn: Double,
    val netPayable: Double
)
