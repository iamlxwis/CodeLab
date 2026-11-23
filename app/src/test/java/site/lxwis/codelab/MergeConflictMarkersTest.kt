package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class MergeConflictMarkersTest {
    @Test fun resolvesTwoConflictsWithoutTouchingSurroundingText() {
        val source = "before\n<<<<<<< HEAD\nours\n=======\ntheirs\n>>>>>>> feature\n" +
            "middle\n<<<<<<< local\none\n=======\ntwo\n>>>>>>> remote\nafter\n"
        val hunks = MergeConflictMarkers.parse(source)
        assertEquals(2, hunks.size)
        assertEquals(2, hunks[0].line)
        assertEquals("HEAD", hunks[0].oursLabel)
        assertEquals("feature", hunks[0].theirsLabel)
        assertEquals("before\nours\nmiddle\ntwo\nafter\n",
            MergeConflictMarkers.resolve(source, listOf(
                MergeConflictMarkers.Choice.OURS, MergeConflictMarkers.Choice.THEIRS)))
    }

    @Test fun supportsBothAndPreservesCrLfAndTrailingNewline() {
        val source = "prefix\r\n<<<<<<< ours\r\nA\r\n=======\r\nB\r\n>>>>>>> theirs\r\nsuffix"
        assertEquals("prefix\r\nA\r\nB\r\nsuffix",
            MergeConflictMarkers.resolve(source, listOf(MergeConflictMarkers.Choice.BOTH)))
    }

    @Test fun refusesMalformedNestedAndThreeWayConflicts() {
        for (source in listOf(
            "<<<<<<< HEAD\nours\n>>>>>>> branch\n",
            "<<<<<<< HEAD\nours\n=======\ntheirs\n",
            "<<<<<<< HEAD\n<<<<<<< nested\n=======\ntheirs\n>>>>>>> branch\n",
            "<<<<<<< HEAD\nours\n||||||| base\n=======\ntheirs\n>>>>>>> branch\n",
            "=======\nstray\n"
        )) {
            assertTrue(source, runCatching { MergeConflictMarkers.parse(source) }.isFailure)
        }
    }

    @Test fun doesNotModifyFilesWithoutMarkersAndRejectsStaleChoices() {
        val source = "fun main() {}\n"
        assertTrue(MergeConflictMarkers.parse(source).isEmpty())
        assertEquals(source, MergeConflictMarkers.resolve(source, emptyList()))
        assertTrue(runCatching {
            MergeConflictMarkers.resolve(source, listOf(MergeConflictMarkers.Choice.OURS))
        }.isFailure)
    }
}
