import re

with open("app/src/main/java/com/example/ui/screens/employee/EmployeesScreen.kt", "r") as f:
    content = f.read()

call_old = """                        EmployeeCard(
                            employee = employee,
                            onClick = { onNavigateToAddEdit(employee.id) }
                        )"""
call_new = """                        EmployeeCard(
                            employee = employee,
                            netPayable = uiState.netPayables[employee.id] ?: 0.0,
                            onClick = { onNavigateToAddEdit(employee.id) }
                        )"""

content = content.replace(call_old, call_new)

with open("app/src/main/java/com/example/ui/screens/employee/EmployeesScreen.kt", "w") as f:
    f.write(content)
