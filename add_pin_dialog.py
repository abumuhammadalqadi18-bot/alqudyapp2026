import re

with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

# Add a state for showing the dialog
security_section_pattern = r"fun SecuritySettingsSection\(settingsViewModel: SettingsViewModel, uiState: SettingsUiState\) \{[\s\S]*?fun BackupSettingsSection"
security_section_match = re.search(security_section_pattern, content)

if security_section_match:
    old_section = security_section_match.group(0)
    
    new_section = """fun SecuritySettingsSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {
    var showPinDialog by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                tempPin = ""
                pinError = null
            },
            title = { Text("إعداد رمز PIN (4 أرقام)") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { 
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                tempPin = it
                                pinError = null
                            }
                        },
                        label = { Text("رمز PIN") },
                        isError = pinError != null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    if (pinError != null) {
                        Text(text = pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempPin.length == 4) {
                        settingsViewModel.setPinCode(tempPin)
                        settingsViewModel.setPinLockEnabled(true)
                        showPinDialog = false
                        tempPin = ""
                    } else {
                        pinError = "يجب أن يتكون الرمز من 4 أرقام"
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    tempPin = ""
                    pinError = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    SettingsCard(title = "الحماية والخصوصية", icon = Icons.Default.Security) {
        SettingSwitchRow(
            title = "الدخول بالبصمة",
            description = "تأمين التطبيق باستخدام البصمة الحيوية",
            checked = uiState.isBiometricEnabled,
            onCheckedChange = { settingsViewModel.setBiometricEnabled(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingSwitchRow(
            title = "قفل برمز PIN",
            description = "تأمين التطبيق برمز مرور",
            checked = uiState.isPinLockEnabled,
            onCheckedChange = {
                if (it) {
                    showPinDialog = true
                } else {
                    settingsViewModel.setPinLockEnabled(false)
                }
            }
        )
        if (uiState.isPinLockEnabled) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingActionRow(
                title = "تغيير رمز PIN",
                description = "تحديث رمز المرور الخاص بك",
                buttonText = "تغيير",
                onClick = { showPinDialog = true }
            )
        }
    }
}

@Composable
fun BackupSettingsSection"""
    
    content = content.replace(old_section, new_section)
    with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "w") as f:
        f.write(content)
    print("Updated SecuritySettingsSection")
else:
    print("SecuritySettingsSection not found")
