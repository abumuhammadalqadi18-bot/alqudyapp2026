import re

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "r") as f:
    content = f.read()

# Make sure we add imports for KeyboardOptions and KeyboardType
if "import androidx.compose.foundation.text.KeyboardOptions" not in content:
    content = content.replace("import androidx.compose.foundation.text.BasicTextField", "import androidx.compose.foundation.text.BasicTextField\nimport androidx.compose.foundation.text.KeyboardOptions\nimport androidx.compose.ui.text.input.KeyboardType")

old_text_field = """                        OutlinedTextField(
                            value = selection.note,
                            onValueChange = { onSelectionChange(selection.copy(note = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { 
                                 Text(
                                    "إضافة ملاحظة (اختياري)...", 
                                     style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                             },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )"""

new_text_field = """                        OutlinedTextField(
                            value = selection.note,
                            onValueChange = { onSelectionChange(selection.copy(note = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { 
                                 Text(
                                    "إضافة ملاحظة (اختياري)...", 
                                     style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                             },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                        
                        if (selection.dayType == DayType.HOURS) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = selection.hours,
                                onValueChange = { onSelectionChange(selection.copy(hours = it)) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("عدد الساعات", style = MaterialTheme.typography.bodyMedium) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGold, 
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        } else if (selection.dayType == DayType.CUSTOM) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = selection.customAmount,
                                onValueChange = { onSelectionChange(selection.copy(customAmount = it)) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("المبلغ المخصص", style = MaterialTheme.typography.bodyMedium) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGold, 
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }
                        
                        if (selection.dayType != DayType.ABSENT && selection.dayType != DayType.CUSTOM) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val currentCalc = when (selection.dayType) {
                                DayType.FULL_DAY, DayType.LATE -> employee.currentDailyWage
                                DayType.HALF_DAY -> employee.currentDailyWage / 2
                                DayType.HOURS -> {
                                    val hrs = selection.hours.toDoubleOrNull() ?: 0.0
                                    (employee.currentDailyWage / 8) * hrs
                                }
                                else -> 0.0
                            }
                            OutlinedTextField(
                                value = selection.manualAmount,
                                onValueChange = { onSelectionChange(selection.copy(manualAmount = it)) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("تعديل يدوي (اختياري) - المحسوب: ${currentCalc}", style = MaterialTheme.typography.bodyMedium) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentGold, 
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                        }"""

content = content.replace(old_text_field, new_text_field)

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "w") as f:
    f.write(content)
