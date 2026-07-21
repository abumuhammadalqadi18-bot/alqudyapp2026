import os

filepath = "app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt"

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# exact strings to replace
orig_backup_launcher = """    val googleSignInBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            settingsViewModel.backupToCloud(context)
        } else {
            Toast.makeText(context, "تم إلغاء عملية الربط بحساب Google", Toast.LENGTH_SHORT).show()
        }
    }"""

new_backup_launcher = """    val cloudBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            settingsViewModel.backupToCloud(context, it)
        }
    }"""

orig_restore_launcher = """    val googleSignInRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            settingsViewModel.restoreFromCloud(context)
        } else {
            Toast.makeText(context, "تم إلغاء عملية الربط بحساب Google", Toast.LENGTH_SHORT).show()
        }
    }"""

new_restore_launcher = """    val cloudRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromCloud(context, it)
        }
    }"""

orig_chooser = """    fun launchGoogleAccountChooser(launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>) {
        val intent = android.accounts.AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"), null, null, null, null
        )
        launcher.launch(intent)
    }"""

new_chooser = ""

orig_onClick_backup = "launchGoogleAccountChooser(googleSignInBackupLauncher)"
new_onClick_backup = "cloudBackupLauncher.launch(\"AlQadhi_Cloud_Backup.db\")"

orig_onClick_restore = "launchGoogleAccountChooser(googleSignInRestoreLauncher)"
new_onClick_restore = "cloudRestoreLauncher.launch(arrayOf(\"*/*\"))"

content = content.replace(orig_backup_launcher, new_backup_launcher)
content = content.replace(orig_restore_launcher, new_restore_launcher)
content = content.replace(orig_chooser, new_chooser)
content = content.replace(orig_onClick_backup, new_onClick_backup)
content = content.replace(orig_onClick_restore, new_onClick_restore)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Replacement done.")
