import re

with open("app/src/main/java/com/example/worker/BackupWorker.kt", "r") as f:
    content = f.read()

# Remove simulate cloud upload
content = re.sub(
    r'// Simulate cloud upload to Google Drive\s+delay\(2000\)',
    '// حالياً النسخة الاحتياطية التلقائية هي محلية فقط حتى يتم اتخاذ قرار بشأن الربط مع جوجل درايف (البند 1).',
    content
)

# Add backup recording
# We need to add an import for com.example.data.local.entity.BackupHistoryEntity
if "com.example.data.local.entity.BackupHistoryEntity" not in content:
    content = content.replace(
        "import java.util.Locale",
        "import java.util.Locale\nimport com.example.data.local.entity.BackupHistoryEntity"
    )

old_success = """                // Note: For actual Google Drive background upload without user prompt, 
                // Google Drive REST API & OAuth2 server-side tokens are required.
                // This local backup acts as the automated job.
                
                Result.success()"""

new_success = """                // Note: For actual Google Drive background upload without user prompt, 
                // Google Drive REST API & OAuth2 server-side tokens are required.
                // This local backup acts as the automated job.
                
                app.container.backupRepository.recordBackup(
                    BackupHistoryEntity(
                        backupDate = System.currentTimeMillis(),
                        backupLocation = "LOCAL",
                        backupSize = backupFile.length(),
                        status = "SUCCESS"
                    )
                )
                
                Result.success()"""

content = content.replace(old_success, new_success)

with open("app/src/main/java/com/example/worker/BackupWorker.kt", "w") as f:
    f.write(content)
print("Updated BackupWorker.kt")
