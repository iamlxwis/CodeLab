package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class ConsoleSeverityTest {
    @Test fun keepsOutputAndCombinesTextAndSeverityFilters() {
        val output = "normal\n! broken.js#2\nWarning: careful\nnot an error\n"
        assertEquals(output, filterConsoleOutput(output, "", "All"))
        assertEquals("! broken.js#2", filterConsoleOutput(output, "BROKEN", "Errors"))
        assertEquals("Warning: careful", filterConsoleOutput(output, "", "Warnings"))
        assertEquals("", filterConsoleOutput(output, "broken", "Warnings"))
        assertEquals(ConsoleSeverity.Output, consoleSeverity("not an error"))
    }
}
