import re

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "r") as f:
    content = f.read()

# Replace val isAlreadySubmitted = uiState.hasAttendanceForSelectedDate
content = content.replace(
    "val isAlreadySubmitted = uiState.hasAttendanceForSelectedDate",
    "val submittedEmployeeIds = uiState.submittedEmployeeIds\n    val allSubmitted = uiState.activeEmployees.isNotEmpty() && uiState.activeEmployees.all { submittedEmployeeIds.contains(it.id) }"
)

# Fix Button in bottom bar
button_code = """
                    Button(
                        onClick = {
                            if (!isSaving && !allSubmitted) {
                                scope.launch {
                                    isSaving = true
                                    val sentEmployees = mutableListOf<EmployeeEntity>()
                                    
                                    uiState.activeEmployees.forEach { employee ->
                                        if (!submittedEmployeeIds.contains(employee.id)) {
                                            val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                                            wageViewModel.recordWage(
                                                employeeId = employee.id,
                                                date = selectedDate,
                                                dayType = selection.dayType,
                                                notes = selection.note.takeIf { it.isNotBlank() }
                                            )
                                            sentEmployees.add(employee)
                                        }
                                    }
                                    kotlinx.coroutines.delay(800)
                                    isSaving = false
                                    if (!settingsState.isAutoSmsEnabled) {
                                        showSmsBanner = true
                                    }
                                    wageViewModel.checkAttendanceForDate(selectedDate)
                                    
                                    if (settingsState.isAutoSmsEnabled) {
                                        sentEmployees.forEach { employee ->
                                            val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                                            if (selection.dayType != DayType.ABSENT) {
                                                val amount = when (selection.dayType) {
                                                    DayType.FULL_DAY, DayType.LATE -> employee.currentDailyWage
                                                    DayType.HALF_DAY -> employee.currentDailyWage / 2.0
                                                    else -> 0.0
                                                }
                                                val dayTypeStr = when (selection.dayType) {
                                                    DayType.FULL_DAY -> "يوم كامل"
                                                    DayType.HALF_DAY -> "نصف يوم"
                                                    DayType.LATE -> "متأخر"
                                                    else -> ""
                                                }
                                                val netPayable = reportViewModel.getNetPayableForEmployee(employee.id)
                                                com.example.ui.utils.SmsHelper.sendWageSms(
                                                    context = context,
                                                    phone = employee.phone,
                                                    employeeName = employee.name,
                                                    dateMillis = selectedDate,
                                                    dayTypeStr = dayTypeStr,
                                                    amount = amount,
                                                    currency = currency,
                                                    netPayable = netPayable
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allSubmitted) Color.Gray else AccentGold,
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        enabled = !allSubmitted
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF0F1B2B),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (allSubmitted) "الكل\nمعتمد" else "اعتماد\nالتحضير",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (allSubmitted) Color.White else Color(0xFF0F1B2B),
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = "اعتماد",
                                    tint = if (allSubmitted) Color.White else Color(0xFF0F1B2B)
                                )
                            }
                        }
                    }
"""

content = re.sub(
    r"Button\(\s*onClick = \{\s*if \(\!isSaving && \!isAlreadySubmitted\) \{.*?\}\s*\}\s*\},.*?\}\s*\)\s*\{\s*if \(isSaving\) \{.*?\}\s*\}\s*\}",
    button_code.strip(),
    content,
    flags=re.DOTALL
)

# Now find the place where AttendanceLuxuryCard is called
# It has enabled = !isAlreadySubmitted
content = content.replace("enabled = !isAlreadySubmitted", "enabled = !submittedEmployeeIds.contains(employee.id)")

# Remove the overall "isAlreadySubmitted" banner
content = re.sub(
    r"if \(isAlreadySubmitted\) \{.*?تم اعتماد التحضير لهذا اليوم.*?\}\s*\}",
    "",
    content,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "w") as f:
    f.write(content)

