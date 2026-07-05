package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AdjustmentEntity
import com.example.data.local.entity.WithdrawalEntity
import com.example.data.repository.FinanceRepository
import com.example.domain.model.AdjustmentType
import com.example.domain.model.WithdrawalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Financial Operations.
 * حالة واجهة المستخدم للعمليات المالية.
 */
data class FinanceUiState(
    val withdrawals: List<WithdrawalEntity> = emptyList(),
    val adjustments: List<AdjustmentEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for managing Withdrawals and Adjustments.
 * مدير حالة واجهة المستخدم للسحوبات والمكافآت/الخصومات.
 */
class FinanceViewModel(
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    /**
     * Add a withdrawal for an employee.
     * إضافة سلفة أو سحب نقدي للموظف.
     */
    fun addWithdrawal(
        employeeId: Long,
        amount: Double,
        date: Long,
        type: WithdrawalType,
        description: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val withdrawal = WithdrawalEntity(
                    employeeId = employeeId,
                    withdrawalDate = date,
                    amount = amount,
                    withdrawalType = type,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                financeRepository.insertWithdrawal(withdrawal)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "خطأ في تسجيل السحب: ${e.message}"
                )
            }
        }
    }

    /**
     * Add an adjustment (Bonus or Deduction) for an employee.
     * إضافة مكافأة أو خصم للموظف.
     */
    fun addAdjustment(
        employeeId: Long,
        amount: Double,
        date: Long,
        type: AdjustmentType,
        reason: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val adjustment = AdjustmentEntity(
                    employeeId = employeeId,
                    adjustmentDate = date,
                    amount = amount,
                    type = type,
                    reason = reason,
                    createdAt = System.currentTimeMillis()
                )
                financeRepository.insertAdjustment(adjustment)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "خطأ في تسجيل التعديل: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
