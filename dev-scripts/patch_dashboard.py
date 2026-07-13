import re

with open("app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports = """
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
"""
content = content.replace("import androidx.compose.foundation.layout.Box", imports)

# Find Scaffold
scaffold_search = r"Scaffold\(\s*topBar = \{"
scaffold_replace = """    var showFabMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showFabMenu = true },
                    containerColor = AccentGold,
                    contentColor = Color(0xFF0F1B2B), // Dark Navy
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "خيارات الإضافة")
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

content = re.sub(scaffold_search, scaffold_replace, content)

with open("app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt", "w") as f:
    f.write(content)
