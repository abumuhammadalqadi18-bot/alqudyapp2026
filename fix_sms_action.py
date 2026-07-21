import sys

with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'r') as f:
    content = f.read()

target1 = """    var showSmsBanner by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }"""
replacement1 = """    var showSmsBanner by remember { mutableStateOf(false) }
    var pendingSmsAction by remember { mutableStateOf<(() -> Unit)?>(null) }"""
content = content.replace(target1, replacement1)

target2 = """                                    } else if (!settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
                                        showSmsBanner = true
                                    }"""
replacement2 = """                                    } else if (!settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
                                        pendingSmsAction = {
                                            val dayTypeStr = when (sel.dayType) {
                                                DayType.FULL_DAY -> "يوم كامل"
                                                DayType.HALF_DAY -> "نصف يوم"
                                                DayType.LATE -> "متأخر"
                                                else -> ""
                                            }
                                            val netPayable = reportViewModel.getNetPayableForEmployee(employee.id)
                                            SmsHelper.sendWageSms(
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
                                        showSmsBanner = true
                                    }"""
content = content.replace(target2, replacement2)

target3 = """        com.example.ui.components.SmsBanner(
            isVisible = showSmsBanner,
            onSend = {
                showSmsBanner = false
                // Logic for mass SMS has been replaced with per-employee logic as per requirements.
                // It is better to rely on auto SMS for per-employee. If they click this banner, we could send for all approved today.
            },
            onDismiss = { showSmsBanner = false },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )"""
replacement3 = """        com.example.ui.components.SmsBanner(
            isVisible = showSmsBanner,
            onSend = {
                showSmsBanner = false
                pendingSmsAction?.invoke()
                pendingSmsAction = null
            },
            onDismiss = { 
                showSmsBanner = false 
                pendingSmsAction = null
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )"""
content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'w') as f:
    f.write(content)

print("Updated SMS action")
