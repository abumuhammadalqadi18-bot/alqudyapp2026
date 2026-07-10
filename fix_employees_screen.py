import re

with open("app/src/main/java/com/example/ui/screens/employee/EmployeesScreen.kt", "r") as f:
    content = f.read()

# I need to fetch netPayable. The screen probably has reportViewModel? Let's check what viewModels are there.
