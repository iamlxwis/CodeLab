package site.lxwis.codelab

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class InteractiveCodeLessonTest {
    @get:Rule val compose = createComposeRule()

    @Test fun practiceReturnsToSameGuideTopicWithoutCompletingOnboarding() {
        var finished = false
        compose.setContent { MaterialTheme {
            FirstRunTutorial(onFinish = { finished = true }, resumeFrom = 6,
                motion = CodeLabMotion(0f))
        } }
        compose.onNodeWithText("Run code offline").assertExists()
        compose.onNodeWithText("Practice editing and running").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Run lesson offline").assertExists()
        compose.onNodeWithContentDescription("Return to guide").performClick()
        compose.onNodeWithText("Run code offline").assertExists()
        compose.runOnIdle { org.junit.Assert.assertFalse(finished) }
    }
}
