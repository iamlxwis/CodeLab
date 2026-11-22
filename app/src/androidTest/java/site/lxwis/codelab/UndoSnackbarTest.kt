package site.lxwis.codelab

import androidx.compose.material3.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class UndoSnackbarTest {
    @get:Rule val compose = createComposeRule()

    @Test fun undoIsARealAccessibleAction() {
        var tapped = 0
        compose.setContent {
            MaterialTheme {
                Snackbar(
                    action = { TextButton(onClick = { tapped++ }) { Text("Undo") } }
                ) { Text("File moved to trash") }
            }
        }
        compose.onNodeWithText("Undo").performClick()
        compose.runOnIdle { assertEquals(1, tapped) }
    }
}
