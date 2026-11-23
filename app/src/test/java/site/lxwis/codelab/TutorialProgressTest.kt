package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialProgressTest {
    @Test fun resumesAtNextTopicAfterTryingRealControl() {
        assertEquals(2, TutorialProgress.afterTry(1, 14))
        assertEquals(5, TutorialProgress.afterTry(4, 14))
    }

    @Test fun neverReadsPastLastTopicOrBeforeFirstTopic() {
        assertEquals(13, TutorialProgress.afterTry(13, 14))
        assertEquals(13, TutorialProgress.restore(10000, 14))
        assertEquals(0, TutorialProgress.restore(-20, 14))
        assertEquals(0, TutorialProgress.restore(999, 0))
    }

    @Test fun returningToAdvancedTopicShowsAllFourteenTopics() {
        assertFalse(TutorialProgress.showExtendedTopics(3))
        assertTrue(TutorialProgress.showExtendedTopics(4))
        assertTrue(TutorialProgress.showExtendedTopics(13))
    }
}
