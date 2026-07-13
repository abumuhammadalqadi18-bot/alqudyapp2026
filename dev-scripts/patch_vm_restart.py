import re

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

# Replace recreate with proper app restart
old_restart = """                // Trigger live UI reload without killing process
                if (context is Activity) {
                    context.runOnUiThread {
                        context.recreate()
                    }
                }"""

new_restart = """                // Trigger full app restart to ensure all ViewModels and DB connections are fresh
                if (context is Activity) {
                    context.runOnUiThread {
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }
                }"""

content = content.replace(old_restart, new_restart)

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)
