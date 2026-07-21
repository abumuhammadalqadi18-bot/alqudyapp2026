import re

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

# Replace backupToLocal
old_backup_to_local = """    fun backupToLocal(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocalSaving = true) }
            try {
                
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم إنشاء النسخة الاحتياطية محلياً بنجاح") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, errorMessage = "فشل النسخ الاحتياطي: ${e.message}") }
            }
        }
    }"""

new_backup_to_local = """    fun backupToLocal(context: Context, uri: Uri) {
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
                    _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم إنشاء النسخة الاحتياطية محلياً بنجاح") }
                } else {
                    _uiState.update { it.copy(isLocalSaving = false, errorMessage = "لم يتم العثور على قاعدة بيانات") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, errorMessage = "فشل النسخ الاحتياطي: ${e.message}") }
            }
        }
    }"""
content = content.replace(old_backup_to_local, new_backup_to_local)

old_cloud = """    fun backupToCloud(context: Context, uri: Uri) {
        _uiState.update { it.copy(actionMessage = "تم الرفع إلى جوجل درايف بنجاح") }
    }
    fun restoreFromLocal(context: Context, uri: Uri) {
        _uiState.update { it.copy(actionMessage = "تم استيراد النسخة الاحتياطية من الجهاز بنجاح") }
    }
    fun restoreFromCloud(context: Context, uri: Uri) {
        _uiState.update { it.copy(actionMessage = "تم استيراد النسخة الاحتياطية من جوجل درايف بنجاح") }
    }
    fun connectToGoogleDrive(context: Context) {
        _uiState.update { it.copy(actionMessage = "تم الربط مع جوجل درايف بنجاح") }
    }"""

new_cloud = """    fun backupToCloud(context: Context, uri: Uri) {
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
                    _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم الرفع إلى جوجل درايف بنجاح") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, errorMessage = "فشل الرفع: ${e.message}") }
            }
        }
    }
    fun restoreFromLocal(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.update { it.copy(isLocalSaving = true) }
            try {
                com.example.data.local.AppDatabase.resetInstance()
                val dbFile = context.getDatabasePath("alqadi_database.db")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. يرجى إعادة تشغيل التطبيق.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, errorMessage = "فشل الاستيراد: ${e.message}") }
            }
        }
    }
    fun restoreFromCloud(context: Context, uri: Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.update { it.copy(isLocalSaving = true) }
            try {
                com.example.data.local.AppDatabase.resetInstance()
                val dbFile = context.getDatabasePath("alqadi_database.db")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. يرجى إعادة تشغيل التطبيق.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocalSaving = false, errorMessage = "فشل الاستيراد: ${e.message}") }
            }
        }
    }
    fun connectToGoogleDrive(context: Context) {
        _uiState.update { it.copy(actionMessage = "تم الربط مع جوجل درايف بنجاح") }
    }"""
content = content.replace(old_cloud, new_cloud)

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)
print("Replaced ViewModel methods")
