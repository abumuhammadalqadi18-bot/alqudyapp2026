import sys

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

print("Flow combine index:")
idx = content.find('combine(')
print(content[idx:idx+500])
