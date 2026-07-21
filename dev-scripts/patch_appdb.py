with open("app/src/main/java/com/example/data/local/AppDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("fun getDatabase(context: Context, passphrase: ByteArray): AppDatabase {", "fun resetInstance() {\n            INSTANCE?.close()\n            INSTANCE = null\n        }\n\n        fun getDatabase(context: Context, passphrase: ByteArray): AppDatabase {")

with open("app/src/main/java/com/example/data/local/AppDatabase.kt", "w") as f:
    f.write(content)
