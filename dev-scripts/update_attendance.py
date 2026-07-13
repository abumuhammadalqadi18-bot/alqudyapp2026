import re

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "r") as f:
    content = f.read()

# Update AttendanceSelection
selection_old = """data class AttendanceSelection(
    val dayType: DayType = DayType.FULL_DAY,
    val note: String = ""
)"""
selection_new = """data class AttendanceSelection(
    val dayType: DayType = DayType.FULL_DAY,
    val note: String = "",
    val hours: String = "",
    val customAmount: String = "",
    val manualAmount: String = ""
)"""
content = content.replace(selection_old, selection_new)

# Initializations of AttendanceSelection
content = content.replace('AttendanceSelection(DayType.FULL_DAY, "")', 'AttendanceSelection(DayType.FULL_DAY, "", "", "", "")')

# Total wage calculation
wage_calc_old = """            when (selection.dayType) {
                DayType.FULL_DAY, DayType.LATE -> emp.currentDailyWage
                DayType.HALF_DAY -> emp.currentDailyWage / 2
                DayType.HOURS -> {
                    val hrs = selection.hours.toDoubleOrNull() ?: 0.0
                    (emp.currentDailyWage / 8) * hrs
                }
                DayType.ABSENT -> 0.0
                else -> 0.0
            }"""
wage_calc_new = """            val manual = selection.manualAmount.toDoubleOrNull()
            if (manual != null) {
                manual
            } else if (selection.dayType == DayType.CUSTOM) {
                selection.customAmount.toDoubleOrNull() ?: 0.0
            } else {
                when (selection.dayType) {
                    DayType.FULL_DAY, DayType.LATE -> emp.currentDailyWage
                    DayType.HALF_DAY -> emp.currentDailyWage / 2
                    DayType.HOURS -> {
                        val hrs = selection.hours.toDoubleOrNull() ?: 0.0
                        (emp.currentDailyWage / 8) * hrs
                    }
                    DayType.ABSENT -> 0.0
                    else -> 0.0
                }
            }"""
if wage_calc_old in content:
    content = content.replace(wage_calc_old, wage_calc_new)
else:
    # Let's search via regex for the sumOf block
    wage_calc_regex = r"when \(selection\.dayType\) \{[\s\S]*?else -> 0\.0\n            \}"
    content = re.sub(wage_calc_regex, wage_calc_new, content)

# recordWage call
record_old = """                                    wageViewModel.recordWage(
                                        employeeId = employee.id,
                                        date = selectedDate,
                                        dayType = sel.dayType,
                                        notes = sel.note.takeIf { it.isNotBlank() }
                                    )"""
record_new = """                                    val manual = sel.manualAmount.toDoubleOrNull()
                                    val finalOverride = if (manual != null) manual else if (sel.dayType == DayType.CUSTOM) sel.customAmount.toDoubleOrNull() else null
                                    wageViewModel.recordWage(
                                        employeeId = employee.id,
                                        date = selectedDate,
                                        dayType = sel.dayType,
                                        hoursWorked = sel.hours.toDoubleOrNull(),
                                        notes = sel.note.takeIf { it.isNotBlank() },
                                        finalAmountOverride = finalOverride
                                    )"""
content = content.replace(record_old, record_new)

# In AttendanceLuxuryCard:
# We need to add the chips for HOURS and CUSTOM.
chips_old = """                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayTypeChip(
                                label = "يوم كامل",
                                type = DayType.FULL_DAY,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "نصف يوم",
                                type = DayType.HALF_DAY,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "متأخر",
                                type = DayType.LATE,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "غائب",
                                type = DayType.ABSENT,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                        }"""
chips_new = """                        // Top Row of chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayTypeChip(
                                label = "يوم كامل",
                                type = DayType.FULL_DAY,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "نصف يوم",
                                type = DayType.HALF_DAY,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "متأخر",
                                type = DayType.LATE,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Bottom Row of chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayTypeChip(
                                label = "ساعات",
                                type = DayType.HOURS,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "أجر مخصص",
                                type = DayType.CUSTOM,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "غائب",
                                type = DayType.ABSENT,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                        }"""
if chips_old in content:
    content = content.replace(chips_old, chips_new)

# In AttendanceLuxuryCard, after the notes field, add input fields for hours/custom/manual
notes_old = """                        OutlinedTextField(
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
notes_new = """                        OutlinedTextField(
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
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )
                        }
                        
                        if (selection.dayType != DayType.ABSENT && selection.dayType != DayType.CUSTOM) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = selection.manualAmount,
                                onValueChange = { onSelectionChange(selection.copy(manualAmount = it)) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("تعديل القيمة (اختياري)", style = MaterialTheme.typography.bodyMedium) },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentGold, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )
                        }"""
content = content.replace(notes_old, notes_new)

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "w") as f:
    f.write(content)
