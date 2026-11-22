package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorLocationTest {
    private val files = listOf("src/main.js", "lua/test.lua", "Main.java")

    @Test fun findsExactAndUniquelyMatchedPaths() {
        val result = parseErrorLocations(
            "ReferenceError: x is not defined (main.js#7)\n" +
            "lua/test.lua:13: attempt to index a nil value",
            "src/main.js", files)
        assertEquals(listOf(SourceLineReference("src/main.js", 7),
            SourceLineReference("lua/test.lua", 13)), result)
    }

    @Test fun genericErrorsJumpOnlyToRealActiveFile() {
        assertEquals(listOf(SourceLineReference("Main.java", 9)),
            parseErrorLocations("Parse error: Line: 9", "Main.java", files))
        assertTrue(parseErrorLocations("Not an error: Line 9", null, files).isEmpty())
        assertTrue(parseErrorLocations("other.js:33: failure", "src/main.js", files).isEmpty())
    }
}
