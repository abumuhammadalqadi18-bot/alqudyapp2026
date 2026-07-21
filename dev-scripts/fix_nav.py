import re

with open('app/src/test/java/com/example/qadi/NavigationAndRtlUiTest.kt', 'r') as f:
    content = f.read()

content = content.replace('    @After\n    fun tearDown() {\n        Dispatchers.resetMain()\n    }\n', '')

content = content.replace('import org.junit.Rule', '''
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule''')

content = content.replace('    @get:Rule', '''
    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @get:Rule''')

with open('app/src/test/java/com/example/qadi/NavigationAndRtlUiTest.kt', 'w') as f:
    f.write(content)

