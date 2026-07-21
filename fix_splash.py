import re

with open("app/src/main/java/com/example/ui/screens/splash/SplashScreen.kt", "r") as f:
    content = f.read()

content = content.replace("uiState.isAppLockEnabled", "uiState.isPinLockEnabled || uiState.isBiometricEnabled")

with open("app/src/main/java/com/example/ui/screens/splash/SplashScreen.kt", "w") as f:
    f.write(content)
print("Updated SplashScreen.kt")
