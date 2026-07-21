import re

with open('app/src/main/java/com/example/util/DatabaseHelper.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val outputStream = context.contentResolver.openOutputStream(destinationUri)',
    '''val outputStream = if (destinationUri.scheme == "file") {
                FileOutputStream(destinationUri.path)
            } else {
                context.contentResolver.openOutputStream(destinationUri)
            }'''
)

with open('app/src/main/java/com/example/util/DatabaseHelper.kt', 'w') as f:
    f.write(content)

