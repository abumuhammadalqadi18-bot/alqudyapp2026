import re

with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

# Make sure we add DangerRed
if "import com.example.ui.theme.DangerRed" not in content:
    content = content.replace("import com.example.ui.theme.RoyalNavy", "import com.example.ui.theme.RoyalNavy\nimport com.example.ui.theme.DangerRed")

backup_ui_code = """
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    
                    Text(
                        text = "جدولة النسخ الاحتياطي التلقائي (في الخلفية أثناء الشحن والاتصال بالـ Wi-Fi)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoyalNavy,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val schedules = listOf("MANUAL" to "يدوي", "DAILY" to "تلقائي يومي", "WEEKLY" to "تلقائي أسبوعي")
                        schedules.forEach { (key, label) ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { settingsViewModel.setBackupSchedule(context, key) }) {
                                RadioButton(
                                    selected = uiState.backupSchedule == key,
                                    onClick = { settingsViewModel.setBackupSchedule(context, key) },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
"""

if "جدولة النسخ الاحتياطي التلقائي" not in content:
    # We will insert it at the end of the Backup SettingsCard, right after cloudRestoreLauncher.launch
    target = """                            cloudRestoreLauncher.launch(arrayOf("*/*"))
                        }
                    )
                }"""
    content = content.replace(target, """                            cloudRestoreLauncher.launch(arrayOf("*/*"))
                        }
                    )""" + backup_ui_code + """
                }""")

with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "w") as f:
    f.write(content)
