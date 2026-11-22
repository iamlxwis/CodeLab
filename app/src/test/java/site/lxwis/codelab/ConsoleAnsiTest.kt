package site.lxwis.codelab

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class ConsoleAnsiTest {
    @Test fun removesOnlyColorControls() {
        val result = ConsoleAnsi.format("\u001B[31merror\u001B[0m ok", Color.White)
        assertEquals("error ok", result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test fun preservesOrdinaryOutput() {
        val original = "first\nsecond\n"
        assertEquals(original, ConsoleAnsi.format(original, Color.White).text)
    }
}
