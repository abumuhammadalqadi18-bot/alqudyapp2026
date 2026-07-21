import sys

with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'r') as f:
    lines = f.read().split('\n')

# The Box closes at line 349.
# Let's verify line 349 is just "        }"
if lines[348].strip() == "}":
    # Remove it from there
    lines.pop(348)
    
    # Insert it before the final "}" of Scaffold (which was at line 379, now 378)
    # The Scaffold closes at what was 379, so we insert at 377
    
    # Wait, let's just do a safer replacement.
    pass

# Safer replacement:
with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'r') as f:
    content = f.read()

target = """            }
        }
        
        if (showDatePicker) {"""

replacement = """            }
        
        if (showDatePicker) {"""

content = content.replace(target, replacement)

target2 = """            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}"""

replacement2 = """            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
        }
    }
}"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'w') as f:
    f.write(content)

print("Box closing brace moved")
