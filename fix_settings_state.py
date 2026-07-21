import sys

with open('app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt', 'r') as f:
    content = f.read()

target = """    val errorMessage: String? = null,
    val actionMessage: String? = null,
    
    // Company Profile Settings"""

replacement = """    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val isLocalSaving: Boolean = false,
    val isLocalRestoring: Boolean = false,
    val isCloudSaving: Boolean = false,
    val isCloudRestoring: Boolean = false,
    
    // Company Profile Settings"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt', 'w') as f:
        f.write(content)
    print("Fixed SettingsUiState")
