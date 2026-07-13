#!/bin/bash

# Define the file path
FILE="app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt"

# Use sed to replace the google sign-in launchers with SAF launchers
sed -i 's/val googleSignInBackupLauncher = rememberLauncherForActivityResult(/val cloudBackupLauncher = rememberLauncherForActivityResult(/g' $FILE
sed -i 's/contract = ActivityResultContracts.StartActivityForResult()/contract = ActivityResultContracts.CreateDocument("application\/octet-stream")/g' $FILE
sed -i '/if (result.resultCode == Activity.RESULT_OK) {/,/Toast.makeText(context, "تم إلغاء عملية الربط بحساب Google", Toast.LENGTH_SHORT).show()/d' $FILE

# Wait, sed might be messy. Let's just create a python script to rewrite it cleanly.
