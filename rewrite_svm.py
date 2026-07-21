import re

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

# First we need to extract everything before backupToLocal
backup_to_local_idx = content.find("fun backupToLocal(context: Context, uri: Uri) {")
clear_action_message_idx = content.find("fun clearActionMessage() {")

if backup_to_local_idx != -1 and clear_action_message_idx != -1:
    before = content[:backup_to_local_idx]
    after = content[clear_action_message_idx:]
    
    methods = """fun backupToLocal(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.update { it.copy(isLocalSaving = true) }
            try {
                com.example.data.local.AppDatabase.resetInstance()
                val dbFile = context.getDatabasePath("alqadi_database.db")
                if (dbFile.exists()) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        dbFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                    backupRepository.recordBackup(
                        BackupHistoryEntity(
                            backupDate = System.currentTimeMillis(),
                            backupLocation = BackupLocation.LOCAL,
                            backupSize = fileSize,
                            status = BackupStatus.SUCCESS
                        )
                    )
                    _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم إنشاء النسخة الاحتياطية محلياً بنجاح") }
                } else {
                    _uiState.update { it.copy(isLocalSaving = false, errorMessage = "لم يتم العثور على قاعدة بيانات") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, errorMessage = "فشل النسخ الاحتياطي: ${e.message}") }
            }
        }
    }

    fun restoreFromLocal(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.update { it.copy(isLocalSaving = true) }
            try {
                com.example.data.local.AppDatabase.resetInstance()
                val dbFile = context.getDatabasePath("alqadi_database.db")
                
                // Delete WAL and SHM if they exist to prevent corruption after restore
                val walFile = java.io.File(dbFile.path + "-wal")
                val shmFile = java.io.File(dbFile.path + "-shm")
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()

                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                (context.applicationContext as com.example.AlQadiApplication).resetContainer()
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. تم تحديث البيانات.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, errorMessage = "فشل الاستيراد: ${e.message}") }
            }
        }
    }

    """
    
    with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
        f.write(before + methods + after)
    print("Fixed SettingsViewModel")
else:
    print("Could not find markers")
