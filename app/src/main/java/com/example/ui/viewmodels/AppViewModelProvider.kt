package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer

/**
 * Factory class to instantiate ViewModels and inject dependencies from AppContainer.
 * مزود مصنع لإنشاء الـ ViewModels وحقن الاعتماديات من الـ AppContainer.
 */
class AppViewModelProvider(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    appContainer.employeeRepository,
                    appContainer.wageRepository,
                    appContainer.financeRepository
                ) as T
            }
            modelClass.isAssignableFrom(EmployeeViewModel::class.java) -> {
                EmployeeViewModel(appContainer.employeeRepository) as T
            }
            modelClass.isAssignableFrom(WageViewModel::class.java) -> {
                WageViewModel(appContainer.wageRepository, appContainer.employeeRepository) as T
            }
            modelClass.isAssignableFrom(FinanceViewModel::class.java) -> {
                FinanceViewModel(appContainer.financeRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(appContainer.settingsRepository, appContainer.backupRepository) as T
            }
            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(
                    appContainer.employeeRepository,
                    appContainer.wageRepository,
                    appContainer.financeRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
