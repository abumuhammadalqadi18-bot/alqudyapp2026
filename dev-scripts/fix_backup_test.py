import re

with open('app/src/test/java/com/example/qadi/BackupRestoreTest.kt', 'r') as f:
    content = f.read()

content = content.replace('assertTrue("Backup operation should return true", success)', '''
        val dbFile = context.getDatabasePath("alqadi_database.db")
        println("DB Exists: ${dbFile.exists()}")
        println("DB Size: ${dbFile.length()}")
        assertTrue("Backup operation should return true", success)
''')

with open('app/src/test/java/com/example/qadi/BackupRestoreTest.kt', 'w') as f:
    f.write(content)

