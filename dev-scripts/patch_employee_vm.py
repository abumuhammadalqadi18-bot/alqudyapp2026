import re

with open("app/src/main/java/com/example/ui/viewmodels/EmployeeViewModel.kt", "r") as f:
    content = f.read()

content = re.sub(r'    fun deleteEmployee\(employee: EmployeeEntity, onSuccess: \(\) -> Unit\) \{.*?\n    \}\n', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/viewmodels/EmployeeViewModel.kt", "w") as f:
    f.write(content)
