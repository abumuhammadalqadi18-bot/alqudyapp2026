import re

files = [
    'app/src/test/java/com/example/qadi/WageAndBalanceTest.kt',
    'app/src/test/java/com/example/qadi/NavigationAndRtlUiTest.kt'
]

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    # Remove setup and teardown
    content = re.sub(r'@Before\s*fun setup\(\)\s*\{\s*Dispatchers\.setMain\(UnconfinedTestDispatcher\(\)\)\n.*?(?=\s*@Test)', '', content, flags=re.DOTALL)
    content = re.sub(r'@Before\s*fun setup\(\)\s*\{\s*Dispatchers\.setMain\(UnconfinedTestDispatcher\(\)\)\s*\}', '', content, flags=re.DOTALL)
    content = re.sub(r'@After\s*fun tearDown\(\)\s*\{\s*Dispatchers\.resetMain\(\)\s*\}', '', content, flags=re.DOTALL)
    
    # Replace runTest with runBlocking
    content = content.replace('runTest', 'runBlocking')
    content = content.replace('advanceUntilIdle()', 'org.robolectric.shadows.ShadowLooper.idleMainLooper()')
    
    with open(f, 'w') as file:
        file.write(content)

