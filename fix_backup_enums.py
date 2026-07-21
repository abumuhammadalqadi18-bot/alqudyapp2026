import re

# Fix SettingsViewModel
with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

if "com.example.domain.model.BackupLocation" not in content:
    content = content.replace(
        "import com.example.data.local.entity.BackupHistoryEntity",
        "import com.example.data.local.entity.BackupHistoryEntity\nimport com.example.domain.model.BackupLocation\nimport com.example.domain.model.BackupStatus"
    )

content = content.replace('backupLocation = "LOCAL"', 'backupLocation = BackupLocation.LOCAL')
content = content.replace('backupLocation = "GOOGLE_DRIVE_MOCK"', 'backupLocation = BackupLocation.DRIVE')
content = content.replace('status = "SUCCESS"', 'status = BackupStatus.SUCCESS')

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)


# Fix BackupWorker
with open("app/src/main/java/com/example/worker/BackupWorker.kt", "r") as f:
    content = f.read()

if "com.example.domain.model.BackupLocation" not in content:
    content = content.replace(
        "import com.example.data.local.entity.BackupHistoryEntity",
        "import com.example.data.local.entity.BackupHistoryEntity\nimport com.example.domain.model.BackupLocation\nimport com.example.domain.model.BackupStatus"
    )

content = content.replace('backupLocation = "LOCAL"', 'backupLocation = BackupLocation.LOCAL')
content = content.replace('status = "SUCCESS"', 'status = BackupStatus.SUCCESS')

with open("app/src/main/java/com/example/worker/BackupWorker.kt", "w") as f:
    f.write(content)

print("Updated Enums")
