import sys

with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'r') as f:
    content = f.read()

target = """                                    if (settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
                                        val amount = when (sel.dayType) {
                                            DayType.FULL_DAY, DayType.LATE -> employee.currentDailyWage
                                            DayType.HALF_DAY -> employee.currentDailyWage / 2.0
                                            else -> 0.0
                                        }
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
                                    } else if (!settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
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

replacement = """                                    val amount = when (sel.dayType) {
                                        DayType.FULL_DAY, DayType.LATE -> employee.currentDailyWage
                                        DayType.HALF_DAY -> employee.currentDailyWage / 2.0
                                        else -> 0.0
                                    }
                                    val dayTypeStr = when (sel.dayType) {
                                        DayType.FULL_DAY -> "يوم كامل"
                                        DayType.HALF_DAY -> "نصف يوم"
                                        DayType.LATE -> "متأخر"
                                        else -> ""
                                    }
                                    if (settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
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
                                    } else if (!settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
                                        pendingSmsAction = {
                                            scope.launch {
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
                                        }
                                        showSmsBanner = true
                                    }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'w') as f:
        f.write(content)
    print("Fixed SMS action scoping")
else:
    print("Target not found")
