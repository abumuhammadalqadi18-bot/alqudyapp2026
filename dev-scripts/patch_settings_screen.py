import re

with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

# State for restore dialog
if "var showRestoreConfirmDialog by remember { mutableStateOf(false) }" not in content:
    content = content.replace("var tempPin by remember { mutableStateOf(\"\") }", "var tempPin by remember { mutableStateOf(\"\") }\n    var showRestoreConfirmDialog by remember { mutableStateOf(false) }\n    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }\n    var pendingRestoreType by remember { mutableStateOf(\"\") }")

# Update launchers
old_local_restore = """    val localRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromLocal(context, it)
        }
    }"""
new_local_restore = """    val localRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            pendingRestoreType = "LOCAL"
            showRestoreConfirmDialog = true
        }
    }"""
content = content.replace(old_local_restore, new_local_restore)

old_cloud_restore = """    val cloudRestoreLauncher = rememberLauncherForActivityResult(
        contract = com.example.ui.screens.settings.CloudOpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromCloud(context, it)
        }
    }"""
new_cloud_restore = """    val cloudRestoreLauncher = rememberLauncherForActivityResult(
        contract = com.example.ui.screens.settings.CloudOpenDocument()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            pendingRestoreType = "CLOUD"
            showRestoreConfirmDialog = true
        }
    }"""
content = content.replace(old_cloud_restore, new_cloud_restore)

# Insert Dialog logic
dialog_code = """
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRestoreConfirmDialog = false 
                pendingRestoreUri = null
            },
            title = { Text("تنبيه حرج", color = DangerRed) },
            text = { Text("هل أنت متأكد من استعادة هذه النسخة الاحتياطية؟ سيتم حذف واستبدال كافة البيانات الحالية وسجلات الموظفين والحركات المالية الموجودة في التطبيق بالبيانات المستوردة فوراً، ولا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreUri?.let { uri ->
                            if (pendingRestoreType == "LOCAL") {
                                settingsViewModel.restoreFromLocal(context, uri)
                            } else {
                                settingsViewModel.restoreFromCloud(context, uri)
                            }
                        }
                        pendingRestoreUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("تأكيد الاستبدال", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRestoreConfirmDialog = false 
                    pendingRestoreUri = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }
"""
# insert before if (showPinDialog)
if "if (showRestoreConfirmDialog)" not in content:
    content = content.replace("if (showPinDialog) {", dialog_code + "\n    if (showPinDialog) {")

# Insert UI for Auto-Backup
# Wait, I need DangerRed import? DangerRed is likely in com.example.ui.theme.DangerRed
if "import com.example.ui.theme.DangerRed" not in content:
    content = content.replace("import com.example.ui.theme.RoyalNavy", "import com.example.ui.theme.RoyalNavy\nimport com.example.ui.theme.DangerRed")

backup_ui_code = """
                Spacer(modifier = Modifier.height(24.dp))
                
                // Backup Schedule
                Text(
                    text = "جدولة النسخ الاحتياطي التلقائي (في الخلفية أثناء الشحن والاتصال بالـ Wi-Fi)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoyalNavy,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val schedules = listOf("MANUAL" to "يدوي", "DAILY" to "تلقائي يومي", "WEEKLY" to "تلقائي أسبوعي")
                    schedules.forEach { (key, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { settingsViewModel.setBackupSchedule(context, key) }) {
                            RadioButton(
                                selected = uiState.backupSchedule == key,
                                onClick = { settingsViewModel.setBackupSchedule(context, key) }
                            )
                            Text(text = label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
"""

if "جدولة النسخ الاحتياطي التلقائي" not in content:
    content = content.replace("BackupSection(", backup_ui_code + "\n                Spacer(modifier = Modifier.height(24.dp))\n                BackupSection(")


with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "w") as f:
    f.write(content)
