package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BackupRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Settings.
 * حالة واجهة المستخدم لإعدادات التطبيق.
 */
data class SettingsUiState(
    val isAppLockEnabled: Boolean = false,
    val lockType: String = "BIOMETRIC", // BIOMETRIC or PIN
    val isAutoBackupEnabled: Boolean = false,
    val isAutoSmsEnabled: Boolean = false,
    val currencySymbol: String = "ر.س",
    val isDarkMode: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for managing app settings and triggering backups.
 * مدير حالة واجهة المستخدم لضبط إعدادات التطبيق وإدارة النسخ الاحتياطي.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            settingsRepository.getBooleanSettingFlow("app_lock_enabled", false).collect { value ->
                _uiState.update { it.copy(isAppLockEnabled = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getSettingFlow("app_lock_type", "BIOMETRIC").collect { value ->
                _uiState.update { it.copy(lockType = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getBooleanSettingFlow("auto_backup_enabled", false).collect { value ->
                _uiState.update { it.copy(isAutoBackupEnabled = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getBooleanSettingFlow("auto_sms_enabled", false).collect { value ->
                _uiState.update { it.copy(isAutoSmsEnabled = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getSettingFlow("currency_symbol", "ر.س").collect { value ->
                _uiState.update { it.copy(currencySymbol = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getBooleanSettingFlow("dark_mode", false).collect { value ->
                _uiState.update { it.copy(isDarkMode = value) }
            }
        }
        
        _uiState.update { it.copy(isLoading = false) }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBooleanSetting("app_lock_enabled", enabled)
        }
    }

    fun setLockType(type: String) {
        viewModelScope.launch {
            settingsRepository.saveSetting("app_lock_type", type)
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBooleanSetting("auto_backup_enabled", enabled)
        }
    }

    fun setAutoSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBooleanSetting("auto_sms_enabled", enabled)
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            settingsRepository.saveSetting("currency_symbol", symbol)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBooleanSetting("dark_mode", enabled)
        }
    }

    fun triggerManualBackup() {
        // Here we would typically trigger the WorkManager or call Drive API
        // This is a placeholder for the UI trigger
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
