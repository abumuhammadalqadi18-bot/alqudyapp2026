import re

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

# Make sure we import BackupHistoryEntity
if "com.example.data.local.entity.BackupHistoryEntity" not in content:
    content = content.replace(
        "import com.example.data.repository.SettingsRepository",
        "import com.example.data.local.entity.BackupHistoryEntity\nimport com.example.data.repository.SettingsRepository"
    )

# Fix backupToLocal
old_backup_to_local_success = """                    _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم إنشاء النسخة الاحتياطية محلياً بنجاح") }"""
new_backup_to_local_success = """                    val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                    backupRepository.recordBackup(
                        BackupHistoryEntity(
                            backupDate = System.currentTimeMillis(),
                            backupLocation = "LOCAL",
                            backupSize = fileSize,
                            status = "SUCCESS"
                        )
                    )
                    _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم إنشاء النسخة الاحتياطية محلياً بنجاح") }"""
content = content.replace(old_backup_to_local_success, new_backup_to_local_success)

# Fix backupToCloud
old_backup_to_cloud_success = """                    _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم الرفع إلى جوجل درايف بنجاح") }"""
new_backup_to_cloud_success = """                    val fileSize = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                    backupRepository.recordBackup(
                        BackupHistoryEntity(
                            backupDate = System.currentTimeMillis(),
                            backupLocation = "GOOGLE_DRIVE_MOCK",
                            backupSize = fileSize,
                            status = "SUCCESS"
                        )
                    )
                    _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم الرفع بنجاح (مؤقتاً يتم الحفظ محلياً حتى إعداد جوجل درايف)") }"""
content = content.replace(old_backup_to_cloud_success, new_backup_to_cloud_success)

# Fix restoreFromLocal
old_restore_local_success = """                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. يرجى إغلاق وإعادة فتح التطبيق.") }"""
new_restore_local_success = """                (context.applicationContext as com.example.AlQadiApplication).resetContainer()
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. تم تحديث البيانات.") }"""
content = content.replace(old_restore_local_success, new_restore_local_success)

# Fix restoreFromCloud
old_restore_cloud_success = """                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. يرجى إعادة تشغيل التطبيق.") }"""
new_restore_cloud_success = """                
                // Delete WAL and SHM if they exist to prevent corruption after restore
                val walFile = java.io.File(dbFile.path + "-wal")
                val shmFile = java.io.File(dbFile.path + "-shm")
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
                
                (context.applicationContext as com.example.AlQadiApplication).resetContainer()
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. تم تحديث البيانات.") }"""
content = content.replace(old_restore_cloud_success, new_restore_cloud_success)

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)
print("Updated SettingsViewModel")
