import sys

with open('app/src/main/java/com/example/ui/screens/lock/LockScreen.kt', 'r') as f:
    content = f.read()

target_layout = """            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {"""

replacement_layout = """            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {"""

if target_layout in content:
    content = content.replace(target_layout, replacement_layout)
    with open('app/src/main/java/com/example/ui/screens/lock/LockScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")

