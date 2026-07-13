import re

with open("app/src/main/java/com/example/ui/screens/splash/SplashScreen.kt", "r") as f:
    content = f.read()

content = content.replace('text = "القاضي",', 'text = androidx.compose.ui.res.stringResource(com.example.R.string.app_name),')
content = content.replace('text = "القاضي لإدارة الأجور – العدل في كل حساب",', 'text = androidx.compose.ui.res.stringResource(com.example.R.string.app_tagline),')

with open("app/src/main/java/com/example/ui/screens/splash/SplashScreen.kt", "w") as f:
    f.write(content)
