package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.EmployeeEntity
import com.example.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UI State for Employee Management.
 * حالة واجهة المستخدم لإدارة الموظفين.
 */
data class EmployeeUiState(
    val activeEmployees: List<EmployeeEntity> = emptyList(),
    val archivedEmployees: List<EmployeeEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for managing Employees logic.
 * مدير الحالة والمنطق الخاص بشاشة الموظفين.
 */
class EmployeeViewModel(
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeUiState())
    val uiState: StateFlow<EmployeeUiState> = _uiState.asStateFlow()

    init {
        loadEmployees()
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // جلب الموظفين النشطين
                employeeRepository.getActiveEmployees()
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "حدث خطأ أثناء تحميل بيانات الموظفين: ${e.message}"
                        )
                    }
                    .collect { activeList ->
                        _uiState.value = _uiState.value.copy(
                            activeEmployees = activeList,
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

        viewModelScope.launch {
            try {
                // جلب الموظفين المؤرشفين (لشاشة الأرشيف إن لزم الأمر)
                employeeRepository.getArchivedEmployees()
                    .collect { archivedList ->
                        _uiState.value = _uiState.value.copy(
                            archivedEmployees = archivedList
                        )
                    }
            } catch (e: Exception) {
                // Ignore or log error
            }
        }
    }

    fun addEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            try {
                employeeRepository.insertEmployee(employee)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "فشل في إضافة الموظف: ${e.message}"
                )
            }
        }
    }

    fun updateEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            try {
                employeeRepository.updateEmployee(employee)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "فشل في تحديث بيانات الموظف: ${e.message}"
                )
            }
        }
    }

    fun archiveEmployee(id: Long) {
        viewModelScope.launch {
            try {
                employeeRepository.archiveEmployee(id)
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(
                    errorMessage = "فشل في أرشفة الموظف: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
