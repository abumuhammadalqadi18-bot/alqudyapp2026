with open("app/src/main/java/com/example/ui/screens/lock/LockScreen.kt", "r") as f:
    content = f.read()

content = content.replace('val defaultPin = "1234" // In a real app, this should be stored securely', '')
content = content.replace('if (pinCode == defaultPin) {', 'if (pinCode == uiState.pinCode) {')

# For the launch effect, check if biometric is enabled
content = content.replace('LaunchedEffect(uiState.lockType) {', 'LaunchedEffect(uiState.isBiometricEnabled) {')
content = content.replace('if (uiState.lockType == "BIOMETRIC") {', 'if (uiState.isBiometricEnabled) {')
content = content.replace('if (uiState.lockType == "BIOMETRIC") {', 'if (uiState.isBiometricEnabled) {')

with open("app/src/main/java/com/example/ui/screens/lock/LockScreen.kt", "w") as f:
    f.write(content)
