package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.WageRecordEntity
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.WageRepository
import com.example.domain.model.DayType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * UI State for Wage Management.
 * حالة واجهة المستخدم لإدارة الأجور.
 */
data class WageUiState(
    val recentRecords: List<WageRecordEntity> = emptyList(),
    val activeEmployees: List<EmployeeEntity> = emptyList(),
    val hasAttendanceForSelectedDate: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for handling daily wage operations and calculations.
 * مدير الحالة لتسجيل الأجور اليومية وحساباتها.
 */
class WageViewModel(
    private val wageRepository: WageRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WageUiState())
    val uiState: StateFlow<WageUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun checkAttendanceForDate(dateMillis: Long) {
        viewModelScope.launch {
            try {
                val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = dateMillis
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendar.set(java.util.Calendar.MINUTE, 59)
                calendar.set(java.util.Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis

                val records = wageRepository.getRecordsInRange(startOfDay, endOfDay).firstOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(hasAttendanceForSelectedDate = records.isNotEmpty())
            } catch (e: Exception) {
                // Ignore errors for this check
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // تحميل الموظفين
            employeeRepository.getActiveEmployees()
                .catch { e -> handleError(e) }
                .collect { employees ->
                    _uiState.value = _uiState.value.copy(
                        activeEmployees = employees,
                        isLoading = false
                    )
                }
        }
    }

    /**
     * Record a wage for a specific employee and day.
     * تسجيل أجر يومي لموظف، ويقوم بالحساب التلقائي بناءً على نوع اليوم.
     */
    fun recordWage(
        employeeId: Long,
        date: Long,
        dayType: DayType,
        hoursWorked: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                // Fetch the current employee to get their daily wage
                val employee = employeeRepository.getEmployeeById(employeeId)
                if (employee == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "الموظف غير موجود")
                    return@launch
                }

                val currentWage = employee.currentDailyWage
                val calculatedAmount = calculateWageAmount(currentWage, dayType, hoursWorked)
                
                // For simplicity, final amount equals calculated amount initially (can be modified later)
                val finalAmount = calculatedAmount

                val record = WageRecordEntity(
                    employeeId = employeeId,
                    wageDate = date,
                    dayType = dayType,
                    hoursWorked = hoursWorked,
                    calculatedAmount = calculatedAmount,
                    finalAmount = finalAmount,
                    notes = notes,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                wageRepository.insertWageRecord(record)

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Calculate the wage based on the day type.
     * دالة حساب الأجر بناءً على نوع الدوام.
     */
    private fun calculateWageAmount(dailyWage: Double, dayType: DayType, hoursWorked: Double?): Double {
        return when (dayType) {
            DayType.FULL_DAY -> dailyWage
            DayType.HALF_DAY -> dailyWage / 2.0
            DayType.LATE -> dailyWage // Default to full daily wage for late, can be customized
            DayType.HOURS -> {
                // Assuming standard 8 hours work day for hourly calculation
                val hourlyRate = dailyWage / 8.0
                hourlyRate * (hoursWorked ?: 0.0)
            }
            DayType.ABSENT -> 0.0
            DayType.CUSTOM -> 0.0 // Requires manual input elsewhere
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun handleError(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = "حدث خطأ: ${error.message}"
        )
    }
}
