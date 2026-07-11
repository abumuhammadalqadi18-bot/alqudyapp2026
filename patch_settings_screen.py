with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace the launcher creations
old_launchers = """    val localBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            settingsViewModel.backupToLocal(context, it)
        }
    }

    val localRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromLocal(context, it)
        }
    }

    val cloudBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            settingsViewModel.backupToCloud(context, it)
        }
    }

    val cloudRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromCloud(context, it)
        }
    }"""

new_launchers = """    val localBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            settingsViewModel.backupToLocal(context, it)
        }
    }

    val localRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromLocal(context, it)
        }
    }

    val cloudBackupLauncher = rememberLauncherForActivityResult(
        contract = com.example.ui.screens.settings.CloudCreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            settingsViewModel.backupToCloud(context, it)
        }
    }

    val cloudRestoreLauncher = rememberLauncherForActivityResult(
        contract = com.example.ui.screens.settings.CloudOpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromCloud(context, it)
        }
    }"""

content = content.replace(old_launchers, new_launchers)

# Remove the text saying it will disable biometric if pin enabled, etc.
content = content.replace('settingsViewModel.setPinLockEnabled(false)', 'settingsViewModel.setPinLockEnabled(false)\n                                    settingsViewModel.setPinCode("")')

# Wait, let's just make sure we don't have mutual exclusions in UI either
with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "w") as f:
    f.write(content)
