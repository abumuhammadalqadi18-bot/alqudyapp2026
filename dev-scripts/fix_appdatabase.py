import re

with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''                if (!isRobolectric) {
                    builder.openHelperFactory(SupportFactory(passphrase))
                    val instance = builder.build()
                    INSTANCE = instance
                    return instance
                } else {
                    // Force in-memory for testing to avoid lock issues and bypass SQLCipher
                    val instance = Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java
                    )
                        .allowMainThreadQueries()
                        .build()
                    INSTANCE = instance
                    return instance
                }''',
'''                if (!isRobolectric) {
                    builder.openHelperFactory(SupportFactory(passphrase))
                } else {
                    builder.allowMainThreadQueries()
                }
                val instance = builder.build()
                INSTANCE = instance
                return instance'''
)

with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'w') as f:
    f.write(content)

