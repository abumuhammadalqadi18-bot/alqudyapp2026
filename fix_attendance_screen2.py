import re

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "r") as f:
    content = f.read()

# I see my previous replace of Button didn't work because of dotall or something.
# Let's just manually replace "isAlreadySubmitted" with "allSubmitted"
content = content.replace("isAlreadySubmitted", "allSubmitted")

# And "enabled = !submittedEmployeeIds.contains(employee.id)" in the bottomBar should be "enabled = !allSubmitted"
content = content.replace("enabled = !submittedEmployeeIds.contains(employee.id)", "enabled = !allSubmitted")

# Note that inside the button click we need to filter by submittedEmployeeIds
button_code = """
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
"""

# I will replace the inside of onClick to match this. 
# actually it's easier to just do:
old_onclick = """                        onClick = {
                            if (!isSaving && !allSubmitted) {
                                scope.launch {
                                    isSaving = true
                                    val sentEmployees = mutableListOf<EmployeeEntity>()
                                    
                                    uiState.activeEmployees.forEach { employee ->
                                        val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                                        wageViewModel.recordWage(
                                            employeeId = employee.id,
                                            date = selectedDate,
                                            dayType = selection.dayType,
                                            notes = selection.note.takeIf { it.isNotBlank() }
                                        )
                                        sentEmployees.add(employee)
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
                        },"""

content = content.replace(old_onclick, button_code)

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "w") as f:
    f.write(content)

