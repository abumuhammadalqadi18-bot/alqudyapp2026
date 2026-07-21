package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmployeeEntity
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.WageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EmployeeUiState(
    val activeEmployees: List<EmployeeEntity> = emptyList(),
    val archivedEmployees: List<EmployeeEntity> = emptyList(),
    val netPayables: Map<Long, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EmployeeViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)

    // Pre-computed net payables map, refreshed when employee list changes
    // Uses aggregate DB queries (SUM) instead of loading all rows into memory
    private val _netPayables = MutableStateFlow<Map<Long, Double>>(emptyMap())

    init {
        // Recompute net payables whenever the employee list changes
        viewModelScope.launch {
            employeeRepository.getActiveEmployees().collect { employees ->
                val map = mutableMapOf<Long, Double>()
                for (emp in employees) {
                    map[emp.id] = computeNetPayable(emp.id)
                }
                _netPayables.value = map
            }
        }
    }

    /**
     * Computes net payable for a single employee using aggregate DB queries.
     * No full table scan - uses SUM() at the database level.
     */
    private suspend fun computeNetPayable(employeeId: Long): Double {
        val totalWages = wageRepository.getTotalWageForEmployee(employeeId)
        val totalBonuses = financeRepository.getTotalBonusesForEmployee(employeeId)
        val totalWithdrawals = financeRepository.getTotalWithdrawalForEmployee(employeeId)
        val totalDeductions = financeRepository.getTotalDeductionsForEmployee(employeeId)
        return (totalWages + totalBonuses) - (totalWithdrawals + totalDeductions)
    }

    val uiState: StateFlow<EmployeeUiState> = combine(
        employeeRepository.getActiveEmployees(),
        employeeRepository.getArchivedEmployees(),
        _netPayables,
        _errorMessage
    ) { active, archived, payables, errorMsg ->
        EmployeeUiState(
            activeEmployees = active,
            archivedEmployees = archived,
            netPayables = payables,
            isLoading = false,
            errorMessage = errorMsg
        )
    }.catch { e ->
        _errorMessage.value = e.message
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EmployeeUiState(isLoading = true)
    )

    fun addEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            try {
                employeeRepository.insertEmployee(employee)
            } catch (e: Exception) {
                _errorMessage.value = "فشل إضافة العامل: ${e.message}"
            }
        }
    }

    fun updateEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            try {
                employeeRepository.updateEmployee(employee)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تعديل بيانات العامل: ${e.message}"
            }
        }
    }

    fun archiveEmployee(id: Long) {
        viewModelScope.launch {
            try {
                employeeRepository.archiveEmployee(id)
            } catch (e: Exception) {
                _errorMessage.value = "فشل أرشفة العامل: ${e.message}"
            }
        }
    }

    fun deleteEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            try {
                employeeRepository.deleteEmployee(employee)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف العامل: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
