import re

with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace BackupSettingsSection
start_marker = "fun BackupSettingsSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {"
end_marker = "@Composable\nfun SettingsCard"

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
    
    SettingsCard(title = "النسخ الاحتياطي", icon = Icons.Default.CloudSync) {
        SettingActionRow(
            title = "تصدير نسخة احتياطية",
            description = "حفظ نسخة من البيانات (محلي / سحابي عبر مدير الملفات)",
            buttonText = "تصدير",
            onClick = { createLocalLauncher.launch("alqadi_backup.db") }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingActionRow(
            title = "استيراد نسخة احتياطية",
            description = "استعادة البيانات من ملف النسخة الاحتياطية",
            buttonText = "استيراد",
            onClick = { openLocalLauncher.launch(arrayOf("*/*")) }
        )
    }
}

"""
    new_content = content[:start_idx] + new_section + content[end_idx:]
    with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "w") as f:
        f.write(new_content)
    print("Fixed SettingsScreen")
else:
    print("Markers not found in SettingsScreen")


# Now fix SettingsViewModel.kt
with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    vm_content = f.read()

# We need to remove backupToCloud, restoreFromCloud, connectToGoogleDrive
cloud_methods_pattern = r"fun backupToCloud\(context: Context, uri: Uri\).*?fun connectToGoogleDrive\(context: Context\) \{.*?\}\n"

vm_content = re.sub(cloud_methods_pattern, "", vm_content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(vm_content)
print("Fixed SettingsViewModel")

