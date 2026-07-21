import re

with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

# Remove versions
content = re.sub(r'cameraCamera2 = ".*"\n', '', content)
content = re.sub(r'cameraLifecycle = ".*"\n', '', content)
content = re.sub(r'cameraView = ".*"\n', '', content)
content = re.sub(r'cameraCore = ".*"\n', '', content)
content = re.sub(r'loggingInterceptor = ".*"\n', '', content)
content = re.sub(r'okhttp = ".*"\n', '', content)
content = re.sub(r'moshiKotlin = ".*"\n', '', content)
content = re.sub(r'moshiKotlinCodegen = ".*"\n', '', content)
content = re.sub(r'roborazzi = ".*"\n', '', content)
content = re.sub(r'firebaseBom = ".*"\n', '', content)
content = re.sub(r'googleServices = ".*"\n', '', content)
content = re.sub(r'retrofit = ".*"\n', '', content)
content = re.sub(r'converterMoshi = ".*"\n', '', content)


# Remove libraries
content = re.sub(r'retrofit = \{.*?\}\n', '', content)
content = re.sub(r'converter-moshi = \{.*?\}\n', '', content)
content = re.sub(r'androidx-camera-camera2 = \{.*?\}\n', '', content)
content = re.sub(r'androidx-camera-lifecycle = \{.*?\}\n', '', content)
content = re.sub(r'androidx-camera-view = \{.*?\}\n', '', content)
content = re.sub(r'androidx-camera-core = \{.*?\}\n', '', content)
content = re.sub(r'logging-interceptor = \{.*?\}\n', '', content)
content = re.sub(r'okhttp = \{.*?\}\n', '', content)
content = re.sub(r'moshi-kotlin = \{.*?\}\n', '', content)
content = re.sub(r'moshi-kotlin-codegen = \{.*?\}\n', '', content)
content = re.sub(r'roborazzi = \{.*?\}\n', '', content)
content = re.sub(r'roborazzi-compose = \{.*?\}\n', '', content)
content = re.sub(r'roborazzi-junit-rule = \{.*?\}\n', '', content)
content = re.sub(r'firebase-bom = \{.*?\}\n', '', content)
content = re.sub(r'firebase-ai = \{.*?\}\n', '', content)
content = re.sub(r'firebase-appcheck-recaptcha = \{.*?\}\n', '', content)

# Remove plugins
content = re.sub(r'roborazzi = \{.*?\}\n', '', content)
content = re.sub(r'google-services = \{.*?\}\n', '', content)

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
