import os

content = """package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmployeeEntity
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.WageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val activeEmployeeCount: Int = 0,
    val totalPayableToday: Double = 0.0,
    val totalWithdrawalsToday: Double = 0.0,
    val totalPaymentsThisMonth: Double = 0.0,
    val recentEmployees: List<EmployeeEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    employeeRepository: EmployeeRepository,
    wageRepository: WageRepository,
    financeRepository: FinanceRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private val startOfDay = calendar.timeInMillis
    
    private val endOfDay = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private val monthCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private val startOfMonth = monthCalendar.timeInMillis
    
    private val endOfMonth = monthCalendar.apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private val activeEmployeesFlow = employeeRepository.getActiveEmployees()
    private val todayWagesFlow = wageRepository.getRecordsInRange(startOfDay, endOfDay)
    private val todayWithdrawalsFlow = financeRepository.getWithdrawalsInRange(startOfDay, endOfDay)
    private val monthWagesFlow = wageRepository.getRecordsInRange(startOfMonth, endOfMonth)

    val uiState: StateFlow<HomeUiState> = combine(
        activeEmployeesFlow,
        todayWagesFlow,
        todayWithdrawalsFlow,
        monthWagesFlow
    ) { employees, todayWages, todayWithdrawals, monthWages ->
        HomeUiState(
            activeEmployeeCount = employees.size,
            recentEmployees = employees.take(5),
            totalPayableToday = todayWages.sumOf { it.finalAmount },
            totalWithdrawalsToday = todayWithdrawals.sumOf { it.amount },
            totalPaymentsThisMonth = monthWages.sumOf { it.finalAmount },
            isLoading = false,
            errorMessage = null
        )
    }.catch { e ->
        emit(HomeUiState(isLoading = false, errorMessage = "حدث خطأ غير متوقع: ${e.message}"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun loadDashboardData() {
        // Now fully reactive, no manual trigger needed.
    }
}
"""
with open("app/src/main/java/com/example/ui/viewmodels/HomeViewModel.kt", "w", encoding="utf-8") as f:
    f.write(content)
