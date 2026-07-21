import os
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find usages of String.format("%,.2f", something) or String.format("%.2f", something)
    # Be careful not to replace String.format(Locale.US, ...) here if it's already specific,
    # but actually we want to use the extension function anyway.
    
    # regex for String.format("%.2f", value) -> value.toCurrencyFormat()
    # also for "%,.2f"
    # also for String.format("%s%.2f", if (netPayable >= 0) "+" else "", netPayable) -> we might just manually fix this.
    
    pattern1 = re.compile(r'String\.format\("%,\.2f",\s*([^)]+)\)')
    content = pattern1.sub(r'\1.toCurrencyFormat()', content)

    pattern2 = re.compile(r'String\.format\("%.2f",\s*([^)]+)\)')
    content = pattern2.sub(r'\1.toCurrencyFormat()', content)
    
    # Handle SmsHelper
    pattern3 = re.compile(r'String\.format\(Locale\.US,\s*"%,\.2f",\s*([^)]+)\)')
    content = pattern3.sub(r'\1.toCurrencyFormat()', content)

    # Need to add import if .toCurrencyFormat() is used
    if '.toCurrencyFormat()' in content and 'import com.example.util.toCurrencyFormat' not in content:
        # insert after package
        content = re.sub(
            r'^(package\s+[^\n]+)',
            r'\1\n\nimport com.example.util.toCurrencyFormat',
            content,
            count=1
        )
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

files_to_check = [
    "app/src/main/java/com/example/ui/screens/reports/ReportScreen.kt",
    "app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt",
    "app/src/main/java/com/example/ui/screens/employee/EmployeesScreen.kt",
    "app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt",
    "app/src/main/java/com/example/ui/utils/SmsHelper.kt"
]

for file in files_to_check:
    if os.path.exists(file):
        process_file(file)

