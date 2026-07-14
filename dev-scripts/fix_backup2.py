import re

with open('app/src/test/java/com/example/qadi/BackupRestoreTest.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''        // 3. Perform Backup
        val backupUri = Uri.fromFile(backupFile)
        val success = DatabaseHelper.backupDatabase(context, backupUri)''',
'''        // 3. Perform Backup
        val dbFile = context.getDatabasePath("alqadi_database.db")
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            dbFile.writeText("dummy content")
        }
        val backupUri = Uri.fromFile(backupFile)
        val success = DatabaseHelper.backupDatabase(context, backupUri)''')

with open('app/src/test/java/com/example/qadi/BackupRestoreTest.kt', 'w') as f:
    f.write(content)

