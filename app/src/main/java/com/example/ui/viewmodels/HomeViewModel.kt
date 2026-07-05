package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmployeeEntity
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.WageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * UI State for the Home Screen.
 * حالة واجهة المستخدم للشاشة الرئيسية.
 */
data class HomeUiState(
    val activeEmployeeCount: Int = 0,
    val totalPayableToday: Double = 0.0,
    val totalWithdrawalsToday: Double = 0.0,
    val totalPaymentsThisMonth: Double = 0.0,
    val recentEmployees: List<EmployeeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for managing the Dashboard/Home screen data.
 * مدير حالة واجهة المستخدم للشاشة الرئيسية لتجميع الإحصائيات السريعة.
 */
class HomeViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Get start and end of today
                val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis

                // Get start and end of the current month
                val monthCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
                monthCalendar.set(Calendar.HOUR_OF_DAY, 0)
                monthCalendar.set(Calendar.MINUTE, 0)
                monthCalendar.set(Calendar.SECOND, 0)
                monthCalendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = monthCalendar.timeInMillis
                
                monthCalendar.set(Calendar.DAY_OF_MONTH, monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                monthCalendar.set(Calendar.HOUR_OF_DAY, 23)
                monthCalendar.set(Calendar.MINUTE, 59)
                monthCalendar.set(Calendar.SECOND, 59)
                monthCalendar.set(Calendar.MILLISECOND, 999)
                val endOfMonth = monthCalendar.timeInMillis

                // 1. Get active employee count and recent employees
                employeeRepository.getActiveEmployees()
                    .catch { e -> 
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "خطأ في تحميل الموظفين: ${e.message}"
                        )
                    }
                    .collect { employees ->
                        val activeCount = employees.size
                        val recent = employees.take(5) // Example logic for recent

                        // 2. Get today's wages
                        val todayWages = wageRepository.getRecordsInRange(startOfDay, endOfDay).firstOrNull() ?: emptyList()
                        val totalPayable = todayWages.sumOf { it.finalAmount }

                        // 3. Get today's withdrawals
                        val todayWithdrawals = financeRepository.getWithdrawalsInRange(startOfDay, endOfDay).firstOrNull() ?: emptyList()
                        val totalWithdrawals = todayWithdrawals.sumOf { it.amount }

                        // 4. Get this month's payments (wages)
                        val monthWages = wageRepository.getRecordsInRange(startOfMonth, endOfMonth).firstOrNull() ?: emptyList()
                        val totalPaymentsThisMonth = monthWages.sumOf { it.finalAmount }

                        _uiState.value = _uiState.value.copy(
                            activeEmployeeCount = activeCount,
                            recentEmployees = recent,
                            totalPayableToday = totalPayable,
                            totalWithdrawalsToday = totalWithdrawals,
                            totalPaymentsThisMonth = totalPaymentsThisMonth,
                            isLoading = false
                        )
                    }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "حدث خطأ غير متوقع: ${e.message}"
                )
            }
        }
    }
}
