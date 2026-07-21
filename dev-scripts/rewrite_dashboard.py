import re

with open("app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt", "r") as f:
    content = f.read()

# Replace Scaffold topBar = { with FAB addition

new_scaffold = """    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    containerColor = AccentGold,
                    contentColor = Color(0xFF0F1B2B), // Dark Navy
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = "خيارات الإضافة")
                }
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("تسجيل أجر", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showFabMenu = false
                            onNavigateToAttendance()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("تسجيل سحب", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showFabMenu = false
                            onNavigateToAdvances()
                        }
                    )
                }
            }
        },
        topBar = {"""

content = content.replace("    Scaffold(\n        containerColor = MaterialTheme.colorScheme.background,\n        topBar = {", new_scaffold)

# Need to import Add
if "import androidx.compose.material.icons.filled.Add" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Assessment", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.Assessment")

with open("app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt", "w") as f:
    f.write(content)
