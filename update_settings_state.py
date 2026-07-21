import sys

with open('app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt', 'r') as f:
    content = f.read()

target = """    val actionMessage: String? = null
)"""

replacement = """    val actionMessage: String? = null,
    
    // Company Profile Settings
    val companyName: String = "",
    val phoneNumbers: String = "",
    val address: String = "",
    val services: String = "",
    val footerNote: String = "",
    val logoUri: String? = null,
    val sealUri: String? = null,
    val signatureUri: String? = null
)"""

if target in content:
    content = content.replace(target, replacement)
    
target2 = """        viewModelScope.launch {
            settingsRepository.getSettingFlow("backup_schedule", "MANUAL").collect { value ->
                _uiState.update { it.copy(backupSchedule = value) }
            }
        }
        _uiState.update { it.copy(isLoading = false) }
    }"""

replacement2 = """        viewModelScope.launch {
            settingsRepository.getSettingFlow("backup_schedule", "MANUAL").collect { value ->
                _uiState.update { it.copy(backupSchedule = value) }
            }
        }
        
        viewModelScope.launch { settingsRepository.getSettingFlow("company_name", "").collect { value -> _uiState.update { it.copy(companyName = value) } } }
        viewModelScope.launch { settingsRepository.getSettingFlow("company_phone", "").collect { value -> _uiState.update { it.copy(phoneNumbers = value) } } }
        viewModelScope.launch { settingsRepository.getSettingFlow("company_address", "").collect { value -> _uiState.update { it.copy(address = value) } } }
        viewModelScope.launch { settingsRepository.getSettingFlow("company_services", "").collect { value -> _uiState.update { it.copy(services = value) } } }
        viewModelScope.launch { settingsRepository.getSettingFlow("company_footer", "").collect { value -> _uiState.update { it.copy(footerNote = value) } } }
        viewModelScope.launch { settingsRepository.getSettingFlow("company_logo", "").collect { value -> _uiState.update { it.copy(logoUri = value.takeIf { it.isNotBlank() }) } } }
        viewModelScope.launch { settingsRepository.getSettingFlow("company_seal", "").collect { value -> _uiState.update { it.copy(sealUri = value.takeIf { it.isNotBlank() }) } } }
        viewModelScope.launch { settingsRepository.getSettingFlow("company_signature", "").collect { value -> _uiState.update { it.copy(signatureUri = value.takeIf { it.isNotBlank() }) } } }

        _uiState.update { it.copy(isLoading = false) }
    }"""

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("Target 2 not found!")

target3 = """    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}"""

replacement3 = """    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    // Company Profile Updating
    fun updateCompanyProfile(
        name: String, phone: String, address: String, services: String, footer: String,
        logoUri: String?, sealUri: String?, signatureUri: String?
    ) {
        viewModelScope.launch {
            settingsRepository.saveSetting("company_name", name)
            settingsRepository.saveSetting("company_phone", phone)
            settingsRepository.saveSetting("company_address", address)
            settingsRepository.saveSetting("company_services", services)
            settingsRepository.saveSetting("company_footer", footer)
            settingsRepository.saveSetting("company_logo", logoUri ?: "")
            settingsRepository.saveSetting("company_seal", sealUri ?: "")
            settingsRepository.saveSetting("company_signature", signatureUri ?: "")
            
            _uiState.update { it.copy(actionMessage = "تم حفظ بيانات المؤسسة بنجاح") }
        }
    }
}"""

if target3 in content:
    content = content.replace(target3, replacement3)
else:
    print("Target 3 not found!")

with open('app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt', 'w') as f:
    f.write(content)

print("SettingsViewModel updated!")
