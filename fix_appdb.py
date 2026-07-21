import re

with open("app/src/main/java/com/example/data/local/AppDatabase.kt", "r") as f:
    content = f.read()

old_fallback = ").fallbackToDestructiveMigration(dropAllTables = true)"
new_fallback = """)
                    // TODO: قبل النشر الرسمي للمستخدمين (الإنتاج)، يجب إزالة fallbackToDestructiveMigration 
                    // وكتابة كائنات Migration حقيقية للحفاظ على بيانات المستخدمين عند تحديث إصدار قاعدة البيانات.
                    .fallbackToDestructiveMigration(dropAllTables = true)"""

content = content.replace(old_fallback, new_fallback)

with open("app/src/main/java/com/example/data/local/AppDatabase.kt", "w") as f:
    f.write(content)
print("Updated AppDatabase.kt")
