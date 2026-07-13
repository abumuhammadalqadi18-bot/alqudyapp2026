content = """package com.example.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.AlQadiApplication
import com.example.data.local.AppDatabase
import com.example.data.repository.BackupRepository
import com.example.data.repository.SettingsRepository
import com.example.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val isDarkMode: Boolean = false,
    val currencySymbol: String = "ريال يمني",
    val isAutoSmsEnabled: Boolean = false,
    val isPinLockEnabled: Boolean = false,
    val pinCode: String = "",
    val isBiometricEnabled: Boolean = false,
    val isAppLockEnabled: Boolean = false,
    val lockType: String = "BIOMETRIC",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isLocalSaving: Boolean = false,
    val isLocalRestoring: Boolean = false,
    val isCloudSaving: Boolean = false,
    val isCloudRestoring: Boolean = false,
    val actionMessage: String? = null
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
                        isAppLockEnabled = value || it.isBiometricEnabled
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
                        isAppLockEnabled = value || it.isPinLockEnabled
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
        }
    }

    fun backupToLocal(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocalSaving = true) }
            try {
                withContext(Dispatchers.IO) {
                    val dbFile = context.getDatabasePath("alqadi_database.db")
                    if (dbFile.exists()) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            FileInputStream(dbFile).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    } else {
                        throw Exception("ملف قاعدة البيانات غير موجود.")
                    }
                }
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم حفظ النسخة الاحتياطية بنجاح.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "فشل النسخ الاحتياطي: ${e.message}") }
            }
        }
    }

    fun restoreFromLocal(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocalRestoring = true) }
            try {
                withContext(Dispatchers.IO) {
                    val app = context.applicationContext as AlQadiApplication
                    // Defensive DB shutdown
                    app.container.database.close()
                    AppDatabase.resetInstance()
                    
                    val dbFile = context.getDatabasePath("alqadi_database.db")
                    val walFile = context.getDatabasePath("alqadi_database.db-wal")
                    val shmFile = context.getDatabasePath("alqadi_database.db-shm")
                    
                    if (walFile.exists()) walFile.delete()
                    if (shmFile.exists()) shmFile.delete()
                    
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    
                    // Re-instantiate AppContainer
                    app.container = AppContainer(app)
                }
                _uiState.update { it.copy(isLocalRestoring = false, actionMessage = "تم استعادة البيانات بنجاح.") }
                // Trigger live UI reload without killing process
                if (context is Activity) {
                    context.runOnUiThread {
                        context.recreate()
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalRestoring = false, actionMessage = "فشل استعادة البيانات: ${e.message}") }
            }
        }
    }

    fun backupToCloud(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudSaving = true) }
            try {
                withContext(Dispatchers.IO) {
                    val dbFile = context.getDatabasePath("alqadi_database.db")
                    if (dbFile.exists()) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            FileInputStream(dbFile).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    } else {
                        throw Exception("ملف قاعدة البيانات غير موجود.")
                    }
                }
                _uiState.update { it.copy(isCloudSaving = false, actionMessage = "تم رفع النسخة الاحتياطية إلى التخزين السحابي بنجاح.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCloudSaving = false, actionMessage = "فشل النسخ السحابي: ${e.message}") }
            }
        }
    }

    fun restoreFromCloud(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudRestoring = true) }
            try {
                withContext(Dispatchers.IO) {
                    val app = context.applicationContext as AlQadiApplication
                    // Defensive DB shutdown
                    app.container.database.close()
                    AppDatabase.resetInstance()
                    
                    val dbFile = context.getDatabasePath("alqadi_database.db")
                    val walFile = context.getDatabasePath("alqadi_database.db-wal")
                    val shmFile = context.getDatabasePath("alqadi_database.db-shm")
                    
                    if (walFile.exists()) walFile.delete()
                    if (shmFile.exists()) shmFile.delete()
                    
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    
                    // Re-instantiate AppContainer
                    app.container = AppContainer(app)
                }
                _uiState.update { it.copy(isCloudRestoring = false, actionMessage = "تم استعادة النسخة السحابية بنجاح.") }
                // Trigger live UI reload without killing process
                if (context is Activity) {
                    context.runOnUiThread {
                        context.recreate()
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCloudRestoring = false, actionMessage = "فشل الاستعادة السحابية: ${e.message}") }
            }
        }
    }

    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
"""
with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w", encoding="utf-8") as f:
    f.write(content)
