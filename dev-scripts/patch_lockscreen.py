import re

with open("app/src/main/java/com/example/ui/screens/lock/LockScreen.kt", "r") as f:
    content = f.read()

# Add imports
if "CompositionLocalProvider" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.runtime.CompositionLocalProvider\nimport androidx.compose.ui.platform.LocalLayoutDirection\nimport androidx.compose.ui.unit.LayoutDirection\nimport androidx.compose.ui.Modifier")

# Find the keypad generation and wrap it
keypad_code_target = """        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {"""

keypad_code_replacement = """        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {"""

content = content.replace(keypad_code_target, keypad_code_replacement)

# End of keypad loop
end_target = """            Spacer(modifier = Modifier.height(8.dp))
        }"""

end_replacement = """            Spacer(modifier = Modifier.height(8.dp))
            }
        }"""
content = content.replace(end_target, end_replacement)

with open("app/src/main/java/com/example/ui/screens/lock/LockScreen.kt", "w") as f:
    f.write(content)
