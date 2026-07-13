package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.AlQadiApplication

/**
 * Factory class to instantiate ViewModels and inject dependencies from AppContainer.
 * مزود مصنع لإنشاء الـ ViewModels وحقن الاعتماديات من الـ AppContainer.
 */
class AppViewModelProvider(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appContainer = (application as AlQadiApplication).container
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    appContainer.employeeRepository,
                    appContainer.wageRepository,
                    appContainer.financeRepository
                ) as T
            }
            modelClass.isAssignableFrom(EmployeeViewModel::class.java) -> {
                EmployeeViewModel(appContainer.employeeRepository, appContainer.wageRepository, appContainer.financeRepository) as T
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
