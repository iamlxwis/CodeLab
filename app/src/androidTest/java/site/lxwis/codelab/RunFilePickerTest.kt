package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RunFilePickerTest {
    @get:Rule val compose = createComposeRule()

    @Test fun searchablePickerReturnsTheChosenFile() {
        var selection = ""
        compose.setContent {
            MaterialTheme {
                RunFilePicker(
                    files = listOf("src/main.js", "scratch.lua"),
                    onRun = { selection = it },
                    onDismiss = {}
                )
            }
        }
        compose.onNodeWithText("scratch.lua").performClick()
        compose.runOnIdle { assertEquals("scratch.lua", selection) }
    }
    @Test fun verifiedTypeScriptPackRemainsVisibleWithRunnableFilterEnabled() {
        var selection = ""
        compose.setContent { MaterialTheme {
            RunFilePicker(files = listOf("src/main.ts", "README.md"),
                installedModules = setOf("ts"), onRun = { selection = it }, onDismiss = {})
        } }
        compose.onNodeWithText("src/main.ts").performClick()
        compose.runOnIdle { assertEquals("src/main.ts", selection) }
    }
}
