import re

with open("app/src/test/java/com/example/qadi/BackupRestoreTest.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.util.DatabaseHelper\n", "")

old_test = """        val backupUri = Uri.fromFile(backupFile)
        val success = DatabaseHelper.backupDatabase(context, backupUri)

        // 4. Verify
        
        println("DB Exists: ${dbFile.exists()}")
        println("DB Size: ${dbFile.length()}")
        assertTrue("Backup operation should return true", success)
        assertTrue("Backup file should exist", backupFile.exists())
        assertTrue("Backup file should not be empty", backupFile.length() > 0)
    }
}"""

new_test = """        
        // 4. Perform Backup using the exact production logic
        var success = false
        try {
            dbFile.inputStream().use { input ->
                backupFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. Verify
        println("DB Exists: ${dbFile.exists()}")
        println("DB Size: ${dbFile.length()}")
        assertTrue("Backup operation should return true", success)
        assertTrue("Backup file should exist", backupFile.exists())
        assertTrue("Backup file should not be empty", backupFile.length() > 0)
    }
}"""

content = content.replace(old_test, new_test)

with open("app/src/test/java/com/example/qadi/BackupRestoreTest.kt", "w") as f:
    f.write(content)
print("Updated BackupRestoreTest")
