package site.lxwis.codelab

import android.content.Context
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test

class RunConsoleInputTest {
    @get:Rule val compose = createComposeRule()

    @Test fun inputAndSendRemainUsableInKeyboardSizedPane() = verifyInput(240.dp)

    @Test fun inputIsReservedBeforeChromeInShortSplitPane() = verifyInput(112.dp)

    private fun verifyInput(height: Dp) {
        lateinit var session: RunSession
        compose.runOnUiThread {
            session = RunSession(ApplicationProvider.getApplicationContext<Context>())
            session.run("input.js", "const name = readLine('Name?'); print('Hello, ' + name + '!');")
        }
        try {
            compose.setContent { MaterialTheme {
                RunConsole(session, Modifier.height(height), fileName = "input.js")
            } }
            compose.waitUntil(15_000) { session.waiting }
            compose.onNodeWithText("Program input").performTextInput("Ada")
            compose.onNodeWithText("Send").assertIsDisplayed().performClick()
            compose.waitUntil(15_000) { !session.running }
            compose.onNodeWithText("Hello, Ada!", substring = true).assertExists()
        } finally {
            compose.runOnUiThread { session.close() }
        }
    }
}
