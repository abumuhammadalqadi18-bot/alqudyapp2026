import re

filepath = ".github/workflows/android.yml"

with open(filepath, 'r') as f:
    content = f.read()

# Add gradle-version
pattern = r"validate-wrappers: false\n\s*cache-disabled: true"
replacement = "validate-wrappers: false\n        cache-disabled: true\n        gradle-version: '8.6'"
content = re.sub(pattern, replacement, content)

with open(filepath, 'w') as f:
    f.write(content)

