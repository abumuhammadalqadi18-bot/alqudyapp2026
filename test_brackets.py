def find_closing(code, start_line):
    lines = code.split('\n')
    open_count = 0
    for i in range(start_line - 1, len(lines)):
        line = lines[i]
        open_count += line.count('{')
        open_count -= line.count('}')
        if open_count == 0:
            return i + 1
    return -1

with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt') as f:
    code = f.read()

print(f"Box closes at line: {find_closing(code, 207)}")
print(f"Scaffold closes at line: {find_closing(code, 206)}")
