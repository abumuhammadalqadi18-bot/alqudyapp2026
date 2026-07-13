import re

with open("app/src/main/java/com/example/ui/screens/employee/EmployeesScreen.kt", "r") as f:
    content = f.read()

# Pass netPayable down to EmployeeCard
content = content.replace(
    "fun EmployeeCard(\n    employee: EmployeeEntity,\n    onClick: () -> Unit\n)",
    "fun EmployeeCard(\n    employee: EmployeeEntity,\n    netPayable: Double,\n    onClick: () -> Unit\n)"
)

content = content.replace("val netPayable = uiState.netPayables[employee.id] ?: 0.0", "")

# Update the call site in LazyColumn
call_old = """                            EmployeeCard(
                                employee = employee,
                                onClick = {
                                    navController.navigate("${Screen.EmployeeDetails.route}/${employee.id}")
                                }
                            )"""
call_new = """                            EmployeeCard(
                                employee = employee,
                                netPayable = uiState.netPayables[employee.id] ?: 0.0,
                                onClick = {
                                    navController.navigate("${Screen.EmployeeDetails.route}/${employee.id}")
                                }
                            )"""

content = content.replace(call_old, call_new)

with open("app/src/main/java/com/example/ui/screens/employee/EmployeesScreen.kt", "w") as f:
    f.write(content)

