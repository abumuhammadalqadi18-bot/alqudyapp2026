import re

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

restore_code = """                val dbFile = context.getDatabasePath("alqadi_database.db")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. يرجى إعادة تشغيل التطبيق.") }"""

safe_restore_code = """                val dbFile = context.getDatabasePath("alqadi_database.db")
                
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
                _uiState.update { it.copy(isLocalSaving = false, actionMessage = "تم استيراد النسخة الاحتياطية بنجاح. يرجى إغلاق وإعادة فتح التطبيق.") }"""

content = content.replace(restore_code, safe_restore_code)

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)
print("Updated restore methods to handle WAL/SHM")
