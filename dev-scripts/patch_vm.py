import os

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

# Add imports for work manager
if "androidx.work.WorkManager" not in content:
    content = content.replace("import android.net.Uri", "import android.net.Uri\nimport androidx.work.WorkManager\nimport androidx.work.PeriodicWorkRequestBuilder\nimport androidx.work.ExistingPeriodicWorkPolicy\nimport androidx.work.Constraints\nimport androidx.work.NetworkType\nimport com.example.worker.BackupWorker\nimport java.util.concurrent.TimeUnit")

# Add backup schedule state
if "val backupSchedule: String = \"MANUAL\"," not in content:
    content = content.replace("val lockType: String = \"BIOMETRIC\",", "val lockType: String = \"BIOMETRIC\",\n    val backupSchedule: String = \"MANUAL\",")

# Load backup schedule in loadSettings
if 'settingsRepository.getSettingFlow("backup_schedule"' not in content:
    load_part = """        viewModelScope.launch {
            settingsRepository.getSettingFlow("backup_schedule", "MANUAL").collect { value ->
                _uiState.update { it.copy(backupSchedule = value) }
            }
        }
        _uiState.update { it.copy(isLoading = false) }"""
    content = content.replace("_uiState.update { it.copy(isLoading = false) }", load_part)

# Set backup schedule function
set_backup_func = """    fun setBackupSchedule(context: Context, schedule: String) {
        viewModelScope.launch {
            settingsRepository.saveSetting("backup_schedule", schedule)
            
            val workManager = WorkManager.getInstance(context)
            if (schedule == "MANUAL") {
                workManager.cancelUniqueWork("AutoBackup")
            } else {
                val repeatInterval = if (schedule == "DAILY") 1L else 7L
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresCharging(true)
                    .build()
                    
                val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(repeatInterval, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build()
                    
                workManager.enqueueUniquePeriodicWork(
                    "AutoBackup",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    backupRequest
                )
            }
        }
    }
"""
if "fun setBackupSchedule" not in content:
    content = content.replace("fun clearActionMessage() {", set_backup_func + "\n    fun clearActionMessage() {")


# Restore protocol modification
restore_local_old = """                    val app = context.applicationContext as AlQadiApplication
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
                    app.container = AppContainer(app)"""

restore_local_new = """                    val state = _uiState.value
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
                    
                    // Restore settings
                    val newSettingsRepo = SettingsRepository(app.container.database.settingDao())
                    newSettingsRepo.saveSetting("theme_mode", state.themeMode)
                    newSettingsRepo.saveSetting("currency_symbol", state.currencySymbol)
                    newSettingsRepo.saveBooleanSetting("auto_sms_enabled", state.isAutoSmsEnabled)
                    newSettingsRepo.saveBooleanSetting("pin_lock_enabled", state.isPinLockEnabled)
                    newSettingsRepo.saveSetting("pin_code", state.pinCode)
                    newSettingsRepo.saveBooleanSetting("biometric_enabled", state.isBiometricEnabled)
                    newSettingsRepo.saveSetting("backup_schedule", state.backupSchedule)"""

content = content.replace(restore_local_old, restore_local_new)

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)
