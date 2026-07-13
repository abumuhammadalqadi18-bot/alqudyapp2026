with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Modifier.padding(horizontal = 8.dp, bottom = 8.dp)", "Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)")

with open("app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt", "w") as f:
    f.write(content)
