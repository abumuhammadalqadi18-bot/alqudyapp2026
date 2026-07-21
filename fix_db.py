import sys

with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'r') as f:
    content = f.read()

if "CompanyProfileEntity" not in content:
    content = content.replace("import com.example.data.local.entity.SettingEntity", "import com.example.data.local.entity.SettingEntity\nimport com.example.data.local.entity.CompanyProfileEntity\nimport com.example.data.local.dao.CompanyProfileDao")
    
    content = content.replace("SettingEntity::class", "SettingEntity::class,\n        CompanyProfileEntity::class")
    
    content = content.replace("version = 1,", "version = 2,")
    
    content = content.replace("abstract fun settingDao(): SettingDao", "abstract fun settingDao(): SettingDao\n    abstract fun companyProfileDao(): CompanyProfileDao")

with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/di/AppContainer.kt', 'r') as f:
    content = f.read()

if "CompanyProfileRepository" not in content:
    content = content.replace("import com.example.data.repository.SettingsRepository", "import com.example.data.repository.SettingsRepository\nimport com.example.data.repository.CompanyProfileRepository")
    
    content = content.replace("val settingsRepository: SettingsRepository", "val settingsRepository: SettingsRepository\n    val companyProfileRepository: CompanyProfileRepository")
    
    content = content.replace("override val settingsRepository: SettingsRepository by lazy { SettingsRepository(database.settingDao()) }", "override val settingsRepository: SettingsRepository by lazy { SettingsRepository(database.settingDao()) }\n    override val companyProfileRepository: CompanyProfileRepository by lazy { CompanyProfileRepository(database.companyProfileDao()) }")

with open('app/src/main/java/com/example/di/AppContainer.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'r') as f:
    content = f.read()

if "CompanyProfileRepository" not in content:
    content = content.replace("SettingsViewModel(appContainer.settingsRepository, appContainer.backupRepository)", "SettingsViewModel(application, appContainer.settingsRepository, appContainer.backupRepository, appContainer.companyProfileRepository)")
    
with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'w') as f:
    f.write(content)

print("Updated DB and Container")
