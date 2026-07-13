with open("app/src/main/java/com/example/ui/screens/employee/AddEditEmployeeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("employeeViewModel.deleteEmployee(employee) {", """employeeViewModel.archiveEmployee(employee.id)
                            onNavigateBack()""")
content = content.replace("                                onNavigateBack()\n                            }", "")

with open("app/src/main/java/com/example/ui/screens/employee/AddEditEmployeeScreen.kt", "w") as f:
    f.write(content)
