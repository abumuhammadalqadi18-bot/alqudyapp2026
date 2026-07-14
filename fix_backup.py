with open('app/src/test/java/com/example/qadi/BackupRestoreTest.kt', 'r') as f:
    content = f.read()

# Remove the comments
content = content.replace('''        // Wait for DB to be written (Room might do it asynchronously)
        // Wait, insert is a suspend function, so it's written immediately.
        
        // Ensure DB is closed to flush to disk?
        // Room auto-flushes on close, but we can just use the db path.
        // Ensure DB is closed to flush to disk?
        // db.close()''', '')

# Replace the dummy content creation
content = content.replace('''        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            dbFile.writeText("dummy content")
        }''', '''        assertTrue("قاعدة البيانات الحقيقية يجب أن تكون موجودة على القرص بعد عملية الإدراج", dbFile.exists())
        assertTrue("قاعدة البيانات يجب ألا تكون فارغة", dbFile.length() > 0)''')

with open('app/src/test/java/com/example/qadi/BackupRestoreTest.kt', 'w') as f:
    f.write(content)
