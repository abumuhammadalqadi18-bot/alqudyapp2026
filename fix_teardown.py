import re

files = [
    'app/src/test/java/com/example/qadi/WageAndBalanceTest.kt',
    'app/src/test/java/com/example/qadi/NavigationAndRtlUiTest.kt'
]

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    # Remove it from the end
    content = content.replace('}\n    @After\n    fun tearDown() {\n        Dispatchers.resetMain()\n    }', '}')
    content = content.replace('    @After\n    fun tearDown() {\n        Dispatchers.resetMain()\n    }\n', '')
    
    # Add it inside the class
    content = content.replace('\n}', '\n    @After\n    fun tearDown() {\n        Dispatchers.resetMain()\n    }\n}')

    with open(f, 'w') as file:
        file.write(content)

