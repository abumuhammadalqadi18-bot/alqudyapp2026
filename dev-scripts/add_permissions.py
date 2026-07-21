with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

content = content.replace('<uses-permission android:name="android.permission.READ_CONTACTS" />', 
    '<uses-permission android:name="android.permission.READ_CONTACTS" />\n    <uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />')

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
