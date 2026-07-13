import re

with open("app/src/main/java/com/example/data/repository/EmployeeRepository.kt", "r") as f:
    content = f.read()

content = re.sub(r'    suspend fun deleteEmployee\(employee: EmployeeEntity\) \{\n        employeeDao\.delete\(employee\)\n    \}\n', '', content)

with open("app/src/main/java/com/example/data/repository/EmployeeRepository.kt", "w") as f:
    f.write(content)
