import sys

with open('app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt', 'w') as f:
    f.write("""package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CompanyProfileEntity
import com.example.data.repository.BackupRepository
import com.example.data.repository.CompanyProfileRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

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
    val backupSchedule: String = "MANUAL",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    
    // Company Profile Settings
    val companyName: String = "",
    val phoneNumbers: String = "",
    val address: String = "",
    val services: String = "",
    val footerNote: String = "",
    val logoUri: String? = null,
    val sealUri: String? = null,
    val signatureUri: String? = null
)

class SettingsViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val companyProfileRepository: CompanyProfileRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                launch {
                    settingsRepository.getSettingFlow("theme_mode", "SYSTEM").collect { value ->
                        _uiState.update { it.copy(themeMode = value) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("is_dark_mode", "false").collect { value ->
                        _uiState.update { it.copy(isDarkMode = value.toBoolean()) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("currency_symbol", "ريال يمني").collect { value ->
                        _uiState.update { it.copy(currencySymbol = value) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("is_auto_sms", "false").collect { value ->
                        _uiState.update { it.copy(isAutoSmsEnabled = value.toBoolean()) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("is_pin_lock", "false").collect { value ->
                        _uiState.update { it.copy(isPinLockEnabled = value.toBoolean()) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("pin_code", "").collect { value ->
                        _uiState.update { it.copy(pinCode = value) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("is_biometric_lock", "false").collect { value ->
                        _uiState.update { it.copy(isBiometricEnabled = value.toBoolean()) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("lock_type", "BIOMETRIC").collect { value ->
                        _uiState.update { it.copy(lockType = value) }
                    }
                }
                launch {
                    settingsRepository.getSettingFlow("backup_schedule", "MANUAL").collect { value ->
                        _uiState.update { it.copy(backupSchedule = value) }
                    }
                }
                
                launch {
                    companyProfileRepository.getProfileFlow().collect { profile ->
                        if (profile != null) {
                            _uiState.update {
                                it.copy(
                                    companyName = profile.companyName,
                                    phoneNumbers = profile.phoneNumbers,
                                    address = profile.address,
                                    services = profile.services,
                                    footerNote = profile.footerNote,
                                    logoUri = profile.logoUri,
                                    sealUri = profile.sealUri,
                                    signatureUri = profile.signatureUri
                                )
                            }
                        }
                    }
                }

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
    
    fun clearActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
    
    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.saveSetting("theme_mode", mode) }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch { settingsRepository.saveSetting("is_dark_mode", isDark.toString()) }
    }
    
    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch { settingsRepository.saveSetting("currency_symbol", symbol) }
    }

    fun setAutoSmsEnabled(isEnabled: Boolean) {
        viewModelScope.launch { settingsRepository.saveSetting("is_auto_sms", isEnabled.toString()) }
    }

    fun setPinLockEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveSetting("is_pin_lock", isEnabled.toString())
            if (!isEnabled) {
                settingsRepository.saveSetting("pin_code", "")
            }
        }
    }

    fun setPinCode(code: String) {
        viewModelScope.launch {
            settingsRepository.saveSetting("pin_code", code)
            if (code.isNotEmpty()) {
                settingsRepository.saveSetting("is_pin_lock", "true")
            }
        }
    }

    fun setBiometricEnabled(isEnabled: Boolean) {
        viewModelScope.launch { settingsRepository.saveSetting("is_biometric_lock", isEnabled.toString()) }
    }
    
    private suspend fun copyUriToInternalStorage(context: Context, uri: Uri?, fileName: String): String? {
        if (uri == null) return null
        if (uri.scheme == "file" && uri.path?.contains(context.filesDir.path) == true) {
            return uri.toString()
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.filesDir, fileName)
                    file.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    Uri.fromFile(file).toString()
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun saveCompanyProfile(
        companyName: String,
        phoneNumbers: String,
        address: String,
        services: String,
        footerNote: String,
        logoUri: Uri?,
        sealUri: Uri?,
        signatureUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val context = getApplication<Application>().applicationContext
                val localLogoUri = logoUri?.let { if (it.toString() == _uiState.value.logoUri) it.toString() else copyUriToInternalStorage(context, it, "logo_image.jpg") } ?: _uiState.value.logoUri
                val localSealUri = sealUri?.let { if (it.toString() == _uiState.value.sealUri) it.toString() else copyUriToInternalStorage(context, it, "seal_image.png") } ?: _uiState.value.sealUri
                val localSignatureUri = signatureUri?.let { if (it.toString() == _uiState.value.signatureUri) it.toString() else copyUriToInternalStorage(context, it, "signature_image.png") } ?: _uiState.value.signatureUri
                
                val profile = CompanyProfileEntity(
                    id = 1,
                    companyName = companyName,
                    phoneNumbers = phoneNumbers,
                    address = address,
                    services = services,
                    footerNote = footerNote,
                    logoUri = localLogoUri,
                    sealUri = localSealUri,
                    signatureUri = localSignatureUri
                )
                companyProfileRepository.saveProfile(profile)
                
                _uiState.update { it.copy(isLoading = false, actionMessage = "تم حفظ إعدادات هوية المؤسسة بنجاح") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "خطأ أثناء الحفظ: ${e.message}") }
            }
        }
    }
}
""")
print("SettingsViewModel overwritten")
