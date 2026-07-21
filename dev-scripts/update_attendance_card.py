import re

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "r") as f:
    content = f.read()

# Add onUnapprove to the signature of AttendanceLuxuryCard
sig_old = """@Composable
fun AttendanceLuxuryCard(
    employee: EmployeeEntity,
    selection: AttendanceSelection,
    onSelectionChange: (AttendanceSelection) -> Unit,
    enabled: Boolean
)"""

sig_new = """@Composable
fun AttendanceLuxuryCard(
    employee: EmployeeEntity,
    selection: AttendanceSelection,
    onSelectionChange: (AttendanceSelection) -> Unit,
    enabled: Boolean,
    onUnapprove: () -> Unit = {}
)"""

content = content.replace(sig_old, sig_new)
content = content.replace("enabled: Boolean)", "enabled: Boolean,\n    onUnapprove: () -> Unit = {}\n)")

# Replace the Status Badge to show an Unapprove button if not enabled
badge_old = """                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (enabled) indicatorColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selection.dayType) {
                                DayType.FULL_DAY -> "يوم كامل"
                                DayType.HALF_DAY -> "نصف يوم"
                                DayType.LATE -> "متأخر"
                                DayType.ABSENT -> "غائب"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (enabled) indicatorColor else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }"""

badge_new = """                    // Status Badge & Unapprove Button
                    if (!enabled) {
                        androidx.compose.material3.TextButton(
                            onClick = onUnapprove,
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = DangerRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("إلغاء الاعتماد", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(indicatorColor.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (selection.dayType) {
                                    DayType.FULL_DAY -> "يوم كامل"
                                    DayType.HALF_DAY -> "نصف يوم"
                                    DayType.LATE -> "متأخر"
                                    DayType.ABSENT -> "غائب"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = indicatorColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }"""

content = content.replace(badge_old, badge_new)

# Update the call in the list
call_old = """                        AttendanceLuxuryCard(
                            employee = employee,
                            selection = selection,
                            onSelectionChange = { attendanceMap[employee.id] = it },
                            enabled = !submittedEmployeeIds.contains(employee.id)
                        )"""

call_new = """                        AttendanceLuxuryCard(
                            employee = employee,
                            selection = selection,
                            onSelectionChange = { attendanceMap[employee.id] = it },
                            enabled = !submittedEmployeeIds.contains(employee.id),
                            onUnapprove = {
                                wageViewModel.deleteWage(employee.id, selectedDate)
                            }
                        )"""

content = content.replace(call_old, call_new)

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "w") as f:
    f.write(content)
