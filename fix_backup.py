import re

with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

start_marker = "fun BackupSettingsSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {"
end_marker = "fun SettingsCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {"

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)

if start_idx != -1 and end_idx != -1:
    new_section = """fun BackupSettingsSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {
    val context = LocalContext.current
    
    val createLocalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) settingsViewModel.backupToLocal(context, uri)
    }
    val openLocalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) settingsViewModel.restoreFromLocal(context, uri)
    }
    
    val createDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) settingsViewModel.backupToCloud(context, uri)
    }
    val openDriveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) settingsViewModel.restoreFromCloud(context, uri)
    }
    
    val accountPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            settingsViewModel.connectToGoogleDrive(context)
        }
    }

    SettingsCard(title = "النسخ الاحتياطي", icon = Icons.Default.CloudSync) {
        SettingActionRow(
            title = "النسخ الاحتياطي إلى الهاتف",
            description = "حفظ نسخة من البيانات في الجهاز المحلي",
            buttonText = "نسخ",
            onClick = { createLocalLauncher.launch("alqadi_backup.db") }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingActionRow(
            title = "استيراد نسخة احتياطية من الهاتف",
            description = "استعادة البيانات من ملف محلي",
            buttonText = "استيراد",
            onClick = { openLocalLauncher.launch(arrayOf("*/*")) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingActionRow(
            title = "ربط مع جوجل درايف",
            description = "تسجيل الدخول إلى حساب جوجل",
            buttonText = "ربط",
            onClick = {
                try {
                    val intent = com.google.android.gms.common.AccountPicker.newChooseAccountIntent(
                        com.google.android.gms.common.AccountPicker.AccountChooserOptions.Builder()
                            .setAllowableAccountsTypes(listOf("com.google"))
                            .build()
                    )
                    accountPickerLauncher.launch(intent)
                } catch (e: Exception) {
                    settingsViewModel.connectToGoogleDrive(context)
                }
            }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingActionRow(
            title = "رفع نسخة احتياطية إلى جوجل درايف",
            description = "رفع نسخة آمنة إلى جوجل درايف",
            buttonText = "رفع",
            onClick = { createDriveLauncher.launch("alqadi_drive_backup.db") }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingActionRow(
            title = "استيراد نسخة احتياطية من جوجل درايف",
            description = "استعادة البيانات من جوجل درايف",
            buttonText = "استيراد",
            onClick = { openDriveLauncher.launch(arrayOf("*/*")) }
        )
    }
}

@Composable
"""
    new_content = content[:start_idx] + new_section + content[end_idx:]
    with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "w") as f:
        f.write(new_content)
    print("Fixed successfully")
else:
    print("Markers not found")
