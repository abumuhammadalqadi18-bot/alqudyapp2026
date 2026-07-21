import re

content = """package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmployeeEntity
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.WageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val allWagesFlow = wageRepository.getRecordsInRange(0L, Long.MAX_VALUE)
    private val allWithdrawalsFlow = financeRepository.getWithdrawalsInRange(0L, Long.MAX_VALUE)
    private val allAdjustmentsFlow = financeRepository.getAdjustmentsInRange(0L, Long.MAX_VALUE)

    private val netPayablesFlow = combine(
        allWagesFlow,
        allWithdrawalsFlow,
        allAdjustmentsFlow
    ) { wages, withdrawals, adjustments ->
        val map = mutableMapOf<Long, Double>()
        val employeeIds = (wages.map { it.employeeId } + withdrawals.map { it.employeeId } + adjustments.map { it.employeeId }).distinct()
        
        for (id in employeeIds) {
            val empWages = wages.filter { it.employeeId == id }
            val empWithdrawals = withdrawals.filter { it.employeeId == id }
            val empAdjs = adjustments.filter { it.employeeId == id }
            
            val totalEarned = empWages.sumOf { it.finalAmount } + empAdjs.filter { it.type == com.example.domain.model.AdjustmentType.BONUS }.sumOf { it.amount }
            val totalWithdrawn = empWithdrawals.sumOf { it.amount } + empAdjs.filter { it.type == com.example.domain.model.AdjustmentType.DEDUCTION }.sumOf { it.amount }
            
            map[id] = totalEarned - totalWithdrawn
        }
        map
    }

    val uiState: StateFlow<EmployeeUiState> = combine(
        employeeRepository.getActiveEmployees(),
        employeeRepository.getArchivedEmployees(),
        netPayablesFlow
    ) { active, archived, payables ->
        EmployeeUiState(
            activeEmployees = active,
            archivedEmployees = archived,
            netPayables = payables,
            isLoading = false,
            errorMessage = null
        )
    }.catch { e ->
        emit(EmployeeUiState(isLoading = false, errorMessage = e.message))
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
            }
        }
    }

    fun updateEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            try {
                employeeRepository.updateEmployee(employee)
            } catch (e: Exception) {
            }
        }
    }

    fun archiveEmployee(id: Long) {
        viewModelScope.launch {
            try {
                employeeRepository.archiveEmployee(id)
            } catch (e: Exception) { 
            }
        }
    }

    fun deleteEmployee(employee: EmployeeEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                employeeRepository.deleteEmployee(employee)
                onSuccess()
            } catch (e: Exception) {
            }
        }
    }

    fun clearError() {}
}
"""

with open("app/src/main/java/com/example/ui/viewmodels/EmployeeViewModel.kt", "w", encoding="utf-8") as f:
    f.write(content)

