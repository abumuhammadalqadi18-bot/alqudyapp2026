import sys

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

target = """)
import com.example.data.repository.SettingsRepository
class"""
replacement = """)
class"""

if target in content:
    content = content.replace(target, replacement)
    
if "import com.example.data.repository.SettingsRepository" not in content[:500]:
    content = content.replace("package com.example.ui.viewmodels\n", "package com.example.ui.viewmodels\nimport com.example.data.repository.SettingsRepository\n")

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'w') as f:
    f.write(content)
print("Updated State syntax")
