import sys

with open('app/src/main/java/com/example/di/AppContainer.kt', 'r') as f:
    content = f.read()

if "companyProfileRepository" not in content:
    content = content.replace("val settingsRepository by lazy { SettingsRepository(settingDao) }", "val settingsRepository by lazy { SettingsRepository(settingDao) }\n    val companyProfileRepository by lazy { CompanyProfileRepository(database.companyProfileDao()) }")
    with open('app/src/main/java/com/example/di/AppContainer.kt', 'w') as f:
        f.write(content)
    print("Fixed AppContainer")
