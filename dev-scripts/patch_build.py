import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Remove plugins
content = re.sub(r'  alias\(libs\.plugins\.roborazzi\)\n', '', content)
content = re.sub(r'  alias\(libs\.plugins\.google\.services\)\n', '', content)
content = re.sub(r'import com\.google\.gms\.googleservices\.GoogleServicesPlugin\.MissingGoogleServicesStrategy\n', '', content)

# Remove googleServices block
content = re.sub(r'googleServices \{\n  missingGoogleServicesStrategy = MissingGoogleServicesStrategy\.WARN\n\}\n', '', content)

# Remove dependencies
content = re.sub(r'  implementation\(platform\(libs\.firebase\.bom\)\)\n', '', content)
content = re.sub(r'  implementation\(libs\.firebase\.ai\)\n', '', content)
content = re.sub(r'  implementation\(libs\.firebase\.appcheck\.recaptcha\)\n', '', content)
content = re.sub(r'  implementation\(libs\.retrofit\)\n', '', content)
content = re.sub(r'  implementation\(libs\.converter\.moshi\)\n', '', content)
content = re.sub(r'  implementation\(libs\.logging\.interceptor\)\n', '', content)
content = re.sub(r'  implementation\(libs\.okhttp\)\n', '', content)
content = re.sub(r'  implementation\(libs\.moshi\.kotlin\)\n', '', content)
content = re.sub(r'  "ksp"\(libs\.moshi\.kotlin\.codegen\)\n', '', content)
content = re.sub(r'  testImplementation\(libs\.roborazzi\)\n', '', content)
content = re.sub(r'  testImplementation\(libs\.roborazzi\.compose\)\n', '', content)
content = re.sub(r'  testImplementation\(libs\.roborazzi\.junit\.rule\)\n', '', content)

# Change application ID
content = re.sub(r'applicationId = "com.aistudio.androidapp.mxaqpw"', 'applicationId = "com.qadi.app"', content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
