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

data class SettingsUiState(
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val isDarkMode: Boolean = false, // backward compatibility
    val currencySymbol: String = "ريال يمني",
    val isAutoSmsEnabled: Boolean = false,
    val isPinLockEnabled: Boolean = false,
    val pinCode: String = "",
    val isBiometricEnabled: Boolean = false,
    val isAppLockEnabled: Boolean = false, // backward compatibility
    val lockType: String = "BIOMETRIC", // backward compatibility
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

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
            settingsRepository.getSettingFlow("theme_mode", "SYSTEM").collect { value ->
                val isDark = value == "DARK"
                _uiState.update { it.copy(themeMode = value, isDarkMode = isDark) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getSettingFlow("currency_symbol", "ريال يمني").collect { value ->
                _uiState.update { it.copy(currencySymbol = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getBooleanSettingFlow("auto_sms_enabled", false).collect { value ->
                _uiState.update { it.copy(isAutoSmsEnabled = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getBooleanSettingFlow("pin_lock_enabled", false).collect { value ->
                _uiState.update { 
                    it.copy(
                        isPinLockEnabled = value,
                        isAppLockEnabled = value || it.isBiometricEnabled,
                        lockType = if (value) "PIN" else if (it.isBiometricEnabled) "BIOMETRIC" else "BIOMETRIC"
                    ) 
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.getSettingFlow("pin_code", "").collect { value ->
                _uiState.update { it.copy(pinCode = value) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getBooleanSettingFlow("biometric_enabled", false).collect { value ->
                _uiState.update { 
                    it.copy(
                        isBiometricEnabled = value,
                        isAppLockEnabled = value || it.isPinLockEnabled,
                        lockType = if (value) "BIOMETRIC" else if (it.isPinLockEnabled) "PIN" else "BIOMETRIC"
                    ) 
                }
            }
        }

        _uiState.update { it.copy(isLoading = false) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.saveSetting("theme_mode", mode)
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            settingsRepository.saveSetting("currency_symbol", symbol)
        }
    }

    fun setAutoSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBooleanSetting("auto_sms_enabled", enabled)
        }
    }

    fun setPinLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBooleanSetting("pin_lock_enabled", enabled)
            if (enabled) {
                settingsRepository.saveBooleanSetting("biometric_enabled", false)
            }
        }
    }

    fun setPinCode(code: String) {
        viewModelScope.launch {
            settingsRepository.saveSetting("pin_code", code)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBooleanSetting("biometric_enabled", enabled)
            if (enabled) {
                settingsRepository.saveBooleanSetting("pin_lock_enabled", false)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
