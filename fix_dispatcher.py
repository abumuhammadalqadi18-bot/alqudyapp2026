import os

files = [
    'app/src/test/java/com/example/qadi/WageAndBalanceTest.kt',
    'app/src/test/java/com/example/qadi/NavigationAndRtlUiTest.kt'
]

imports = """
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
"""

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    if "import kotlinx.coroutines.Dispatchers" not in content:
        content = content.replace('import org.junit.Before', imports + 'import org.junit.Before')
    
    if "@OptIn(ExperimentalCoroutinesApi::class)" not in content:
        content = content.replace('class ', '@OptIn(ExperimentalCoroutinesApi::class)\nclass ')
        
    setup_code = """
    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
"""
    if "Dispatchers.setMain" not in content:
        content = content.replace('@Before\n    fun setup() {', setup_code)
        
    teardown_code = """
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
"""
    if "fun tearDown()" not in content:
        content = content + teardown_code

    with open(f, 'w') as file:
        file.write(content)

