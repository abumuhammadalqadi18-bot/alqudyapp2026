import sys

with open('app/src/main/java/com/example/ui/components/SmsBanner.kt', 'r') as f:
    content = f.read()

if "CompositionLocalProvider" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.runtime.CompositionLocalProvider\nimport androidx.compose.ui.platform.LocalLayoutDirection\nimport androidx.compose.ui.unit.LayoutDirection")
    
    target_row = """        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight(), // يتمدد مرناً لمنع القص
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // لون كحلي متناسق
        ) {
            Row("""
    
    replacement = """        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight(), // يتمدد مرناً لمنع القص
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // لون كحلي متناسق
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row("""
    
    content = content.replace(target_row, replacement)
    
    # Close the CompositionLocalProvider block
    target_end = """            }
        }
    }
}"""
    replacement_end = """                }
            }
        }
    }
}"""
    content = content.replace(target_end, replacement_end)
    
    with open('app/src/main/java/com/example/ui/components/SmsBanner.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Already forced RTL")
